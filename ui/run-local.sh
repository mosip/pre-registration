#!/usr/bin/env bash
# Run the Angular SPA against local pre-registration-service without editing config.json.
# Usage: ./run-local.sh [init|start|smoke|stop|help]
# Default: start
# Prereqs:
#   - Node 14.x / npm 6.x preferred (Angular 7 / webpack 4). Node 17+ needs
#     NODE_OPTIONS=--openssl-legacy-provider (set automatically).
#   - Docker deps up (local-dev-setup: ./run-prereg-local.sh up)
#   - Host pre-registration-service on :9090
# Skip health preflight: SKIP_PREFLIGHT=1 ./run-local.sh
# Backend URL: PREREG_URL (default http://localhost:9090)
set -euo pipefail

UI_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$UI_DIR"

PORT="${PORT:-4200}"
PREREG_URL="${PREREG_URL:-http://localhost:9090}"
LOG_DIR="${UI_DIR}/.local/logs"
PID_DIR="${UI_DIR}/.local/pids"
PID_FILE="${PID_DIR}/prereg-ui.pid"
LOG_FILE="${LOG_DIR}/prereg-ui.log"
PROXY_FILE="${UI_DIR}/proxy.conf.json"
UI_URL="http://127.0.0.1:${PORT}"
HEALTH_URL="${PREREG_URL%/}/preregistration/v1/actuator/health"

usage() {
  cat <<EOF

./run-local.sh [init|start|smoke|stop|help]

  init     npm install
  start    background ng serve + /proxyapi → ${PREREG_URL}  (default)
  smoke    GET ${UI_URL}
  stop     stop the process listening on :${PORT}
  help     show this list and exit

Environment:
  SKIP_PREFLIGHT=1     skip the WireMock / pre-registration health checks
  PREREG_URL           backend origin (default http://localhost:9090)
  PORT                 Angular port (default 4200; must match src/assets/config.json)
  NODE_OPTIONS         Node 17+ gets --openssl-legacy-provider automatically

Open ${UI_URL}
config.json is already BASE_URL=${UI_URL}/proxyapi/ — this script does not edit it.
proxy.conf.json is gitignored (written from proxy.conf.example.json).
npm install writes to .local/node_modules; node_modules is a symlink/junction to that folder.
start returns after compile; logs: .local/logs/prereg-ui.log

EOF
}

need_cmd() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "error: '$1' is required on PATH" >&2
    exit 1
  }
}

ensure_dirs() {
  mkdir -p "$LOG_DIR" "$PID_DIR"
}

is_windows() {
  case "$(uname -s 2>/dev/null || true)" in
    MINGW*|MSYS*|CYGWIN*) return 0 ;;
  esac
  return 1
}

link_node_modules() {
  local dest="${UI_DIR}/.local/node_modules"
  if is_windows; then
    cmd.exe //c mklink /J "node_modules" ".local\\node_modules" >/dev/null
  else
    ln -sfn .local/node_modules node_modules
  fi
}

ensure_node_modules() {
  # Physical install lives in .local/node_modules (gitignored with .local/).
  # Angular CLI still resolves ui/node_modules via a junction (Windows) or symlink.
  mkdir -p "${UI_DIR}/.local"
  local nm="${UI_DIR}/node_modules"
  local dest="${UI_DIR}/.local/node_modules"
  if [[ -L "$nm" ]]; then
    echo "node_modules -> .local/node_modules"
    return 0
  fi
  if [[ -d "$nm" ]]; then
    if is_windows && fsutil reparsepoint query "$nm" >/dev/null 2>&1; then
      echo "node_modules -> .local/node_modules"
      return 0
    fi
    if [[ -e "$dest" ]]; then
      echo "error: both ui/node_modules and ui/.local/node_modules exist as real folders." >&2
      echo "       Stop ng serve, keep one of them, then re-run." >&2
      exit 1
    fi
    echo "==> moving node_modules -> .local/node_modules"
    mv "$nm" "$dest" || {
      echo "error: could not move node_modules. Stop ng serve and retry." >&2
      exit 1
    }
  fi
  mkdir -p "$dest"
  if [[ ! -e "$nm" ]]; then
    link_node_modules || {
      echo "error: could not link ui/node_modules -> ui/.local/node_modules" >&2
      exit 1
    }
  fi
  echo "node_modules -> .local/node_modules"
}

openssl_legacy() {
  # webpack 4 hashes with MD4, removed in OpenSSL 3 (Node 17+).
  local ver="$1"
  local major="${ver#v}"
  major="${major%%.*}"
  [[ "$major" =~ ^[0-9]+$ ]] || return 0
  (( major >= 17 )) || return 0
  case " ${NODE_OPTIONS:-} " in
    *" --openssl-legacy-provider "*|*" --openssl-legacy-provider") ;;
    *)
      export NODE_OPTIONS="${NODE_OPTIONS:+$NODE_OPTIONS }--openssl-legacy-provider"
      echo "Node ${ver} uses OpenSSL 3; setting NODE_OPTIONS=--openssl-legacy-provider for webpack 4"
      ;;
  esac
}

check_node() {
  need_cmd node
  need_cmd npm
  local ver
  ver="$(node -v 2>/dev/null || true)"
  echo "node: ${ver:-unknown}"
  if [[ ! "$ver" =~ ^v14\. ]]; then
    echo "warn: Node 14.x is preferred (Angular 7 / webpack 4). Found: ${ver:-none}." >&2
  fi
  openssl_legacy "$ver"
}

write_proxy() {
  cat > "$PROXY_FILE" <<EOF
{
  "/proxyapi": {
    "target": "${PREREG_URL}",
    "secure": false,
    "changeOrigin": true,
    "logLevel": "info",
    "pathRewrite": {
      "^/proxyapi": ""
    }
  }
}
EOF
}

check_http() {
  local url="$1"
  local name="$2"
  local hint="$3"
  if curl -sf --max-time 5 "$url" >/dev/null; then
    echo "OK  $name  $url"
    return 0
  fi
  echo "ERROR: $name is not reachable: $url" >&2
  echo "       $hint" >&2
  return 1
}

preflight() {
  if [[ "${SKIP_PREFLIGHT:-0}" == "1" ]]; then
    echo "Skipping health preflight (SKIP_PREFLIGHT=1)"
    return 0
  fi
  echo "Checking local stack (pre-registration-service + WireMock)..."
  local preflight_ok=0
  check_http "http://localhost:8082/__admin/health" "WireMock local IAM" \
    "Start Docker deps: pre-registration/local-dev-setup/run-prereg-local.sh up" || preflight_ok=1
  check_http "$HEALTH_URL" "pre-registration-service" \
    "Start the host app: pre-registration/local-dev-setup/run-prereg-local.sh start" || preflight_ok=1
  if [[ "$preflight_ok" -ne 0 ]]; then
    echo "Set SKIP_PREFLIGHT=1 to bypass (the UI will still fail API calls if the service is down)." >&2
    exit 1
  fi
}

ensure_npm() {
  if [[ ! -x "${UI_DIR}/node_modules/.bin/ng" && ! -f "${UI_DIR}/node_modules/@angular/cli/bin/ng" ]]; then
    echo "node_modules missing; running npm install..."
    cmd_init
  fi
}

cmd_init() {
  check_node
  ensure_node_modules
  echo "==> npm install (ui/ -> .local/node_modules)"
  npm install
  echo "init complete"
}

is_running() {
  [[ -f "$PID_FILE" ]] || return 1
  kill -0 "$(cat "$PID_FILE")" 2>/dev/null
}

port_in_use() {
  if command -v lsof >/dev/null 2>&1; then
    lsof -iTCP:"$PORT" -sTCP:LISTEN >/dev/null 2>&1
    return $?
  fi
  netstat -ano 2>/dev/null | grep -E ":${PORT} .*LISTEN" >/dev/null 2>&1
}

wait_ready() {
  local max="${1:-120}"
  local i=0
  echo "==> waiting for compile on port ${PORT} (up to ${max}s) ..."
  while (( i < max )); do
    if grep -q -E 'Compiled (successfully|with warnings)' "$LOG_FILE" 2>/dev/null; then
      return 0
    fi
    if grep -q -E 'ERR_OSSL|error:0308010C' "$LOG_FILE" 2>/dev/null; then
      echo "error: webpack OpenSSL failure. See ${LOG_FILE}" >&2
      return 1
    fi
    if port_in_use; then
      local code
      code="$(http_ok "$UI_URL")"
      if [[ "$code" == "200" ]]; then
        return 0
      fi
    fi
    sleep 2
    i=$((i + 2))
    echo "    ... compiling (${i}s / ${max}s)"
  done
  echo "error: ng serve did not become ready in ${max}s. See ${LOG_FILE}" >&2
  return 1
}

cmd_stop() {
  ensure_dirs
  local pid=""
  if [[ -f "$PID_FILE" ]]; then
    pid="$(cat "$PID_FILE" || true)"
  fi
  if [[ -n "$pid" ]] && kill -0 "$pid" 2>/dev/null; then
    echo "==> stopping pre-registration-ui (${pid}) on port ${PORT}"
    kill "$pid" 2>/dev/null || true
    local i
    for i in 1 2 3 4 5; do
      kill -0 "$pid" 2>/dev/null || break
      sleep 1
    done
    kill -9 "$pid" 2>/dev/null || true
    rm -f "$PID_FILE"
    echo "stopped."
    return 0
  fi
  local pids=""
  if command -v lsof >/dev/null 2>&1; then
    pids="$(lsof -ti tcp:"$PORT" 2>/dev/null || true)"
  elif command -v netstat >/dev/null 2>&1; then
    pids="$(netstat -ano 2>/dev/null | awk -v p=":${PORT}" '$0 ~ p && /LISTENING|LISTEN/ { print $NF }' | sort -u | tr '\r' ' ' || true)"
  fi
  if [[ -n "${pids// /}" ]]; then
    echo "==> stopping listeners on :${PORT}"
    # shellcheck disable=SC2086
    kill $pids 2>/dev/null || true
    rm -f "$PID_FILE"
    echo "stopped."
    return 0
  fi
  echo "pre-registration-ui is not running"
  rm -f "$PID_FILE"
}

cmd_start() {
  ensure_dirs
  check_node
  ensure_node_modules
  ensure_npm
  write_proxy
  preflight
  if is_running || port_in_use; then
    echo "pre-registration-ui already running on port ${PORT}"
    open_browser
    return 0
  fi
  local ng_js="${UI_DIR}/node_modules/@angular/cli/bin/ng"
  if [[ ! -f "$ng_js" ]]; then
    echo "error: Angular CLI not found at ${ng_js}. Run: $0 init" >&2
    exit 1
  fi
  echo
  echo "==> ng serve (HTTP, background)  port=${PORT}"
  echo "    proxy ${UI_URL}/proxyapi  →  ${PREREG_URL}"
  echo "    config.json BASE_URL is already ${UI_URL}/proxyapi/"
  echo "    logs ${LOG_FILE}"
  echo
  : > "$LOG_FILE"
  nohup node "$ng_js" serve --proxy-config proxy.conf.json --port "$PORT" --host 0.0.0.0 --disable-host-check >>"$LOG_FILE" 2>&1 &
  echo $! > "$PID_FILE"
  wait_ready 120
  echo "ready  pid=$(cat "$PID_FILE")  ${UI_URL}"
  echo "    pdfjs \"Can't resolve zlib\" is expected and does not block the UI."
  echo "    Stop: $0 stop"
  open_browser
}

open_browser() {
  echo
  echo "Open ${UI_URL}"
  if command -v cmd.exe >/dev/null 2>&1; then
    cmd.exe //c start "" "${UI_URL}" >/dev/null 2>&1 || true
  elif command -v xdg-open >/dev/null 2>&1; then
    xdg-open "${UI_URL}" >/dev/null 2>&1 || true
  elif command -v open >/dev/null 2>&1; then
    open "${UI_URL}" >/dev/null 2>&1 || true
  fi
}

http_ok() {
  local url="$1"
  local code
  if command -v curl >/dev/null 2>&1; then
    code="$(curl -sS -o /dev/null -w "%{http_code}" --connect-timeout 2 --max-time 3 "$url" 2>/dev/null || true)"
    [[ -n "$code" ]] || code="000"
    printf '%s' "$code"
    return 0
  fi
  printf '%s' "000"
}

cmd_smoke() {
  echo "==> GET ${UI_URL}"
  local code
  code="$(http_ok "$UI_URL")"
  if [[ "$code" == "200" ]]; then
    echo "healthy  ${UI_URL}  HTTP ${code}"
    echo "smoke ok"
    return 0
  fi
  echo "error: UI not reachable ${UI_URL}  HTTP ${code}" >&2
  echo "       Start it: $0 start" >&2
  return 1
}

CMD="${1:-start}"
case "${CMD}" in
  help|-h|--help)
    usage
    exit 0
    ;;
  init) cmd_init ;;
  start) cmd_start ;;
  stop) cmd_stop ;;
  smoke) cmd_smoke ;;
  *)
    echo "ERROR: unknown command \"$CMD\". Use one of the commands above." >&2
    usage
    exit 1
    ;;
esac
