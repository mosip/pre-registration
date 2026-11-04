#!/usr/bin/env bash
# Docker deps for laptop Pre-Registration. The HTTP JVM stays on the host.
#
#   ./run-local-stack.sh up | prep | down | wipe | status | logs [svc] | help
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_DIR="$SCRIPT_DIR/deps"
ADDITIONAL_JARS_DIR="$COMPOSE_DIR/additional_jars"
IDOBJECTVALIDATOR_JAR="$ADDITIONAL_JARS_DIR/kernel-ref-idobjectvalidator.jar"
VIRUSSCANNER_JAR="$ADDITIONAL_JARS_DIR/kernel-virusscanner-clamav.jar"
REF_IMPL_VERSION="${REF_IMPL_VERSION:-1.4.1-SNAPSHOT}"
REF_IMPL_GIT_URL="${REF_IMPL_GIT_URL:-https://github.com/mosip/mosip-ref-impl.git}"
REF_IMPL_GIT_REF="${REF_IMPL_GIT_REF:-issue-1633}"

log() { printf '%s\n' "$*"; }
info() { printf '[INFO] %s\n' "$*"; }
warn() { printf '[WARN] %s\n' "$*" >&2; }
die() { echo "ERROR: $*" >&2; exit 1; }

need_cmd() {
  command -v "$1" >/dev/null 2>&1 || die "Required command not found: $1"
}

m2_repo() {
  if [[ -n "${M2_REPO:-}" ]]; then
    echo "$M2_REPO"
    return
  fi
  if [[ -n "${USERPROFILE:-}" ]]; then
    local win_m2
    win_m2="$(cygpath -u "$USERPROFILE/.m2/repository" 2>/dev/null || echo "")"
    if [[ -n "$win_m2" && -d "$win_m2" ]]; then
      echo "$win_m2"
      return
    fi
  fi
  echo "${HOME}/.m2/repository"
}

copy_ref_jar() {
  local artifact="$1" dest="$2" work="$3"
  local built="$work/kernel/${artifact}/target/${artifact}-${REF_IMPL_VERSION}.jar"
  if [[ -f "$built" ]]; then
    cp "$built" "$dest"
    info "Wrote $dest"
    return 0
  fi
  local src
  src="$(m2_repo)/io/mosip/kernel/${artifact}/${REF_IMPL_VERSION}/${artifact}-${REF_IMPL_VERSION}.jar"
  if [[ -f "$src" ]]; then
    warn "Git build did not produce $built — using Maven cache $src"
    cp "$src" "$dest"
    info "Copied ${artifact}.jar → $dest"
    return 0
  fi
  return 1
}

ensure_ref_impl_jars() {
  mkdir -p "$ADDITIONAL_JARS_DIR"
  if [[ -f "$IDOBJECTVALIDATOR_JAR" && -f "$VIRUSSCANNER_JAR" ]]; then
    info "Ref-impl loader jars already present under $ADDITIONAL_JARS_DIR"
    return 0
  fi
  need_cmd git
  need_cmd mvn
  local work
  work="${TMPDIR:-/tmp}/mosip-ref-impl-prereg-jars"
  info "Building kernel-ref-idobjectvalidator and kernel-virusscanner-clamav ${REF_IMPL_VERSION} from ${REF_IMPL_GIT_URL} (${REF_IMPL_GIT_REF}) ..."
  rm -rf "$work"
  git clone --depth 1 --branch "$REF_IMPL_GIT_REF" --filter=blob:none --sparse \
    "$REF_IMPL_GIT_URL" "$work"
  git -C "$work" sparse-checkout set kernel
  (cd "$work/kernel" && mvn -pl kernel-ref-idobjectvalidator,kernel-virusscanner-clamav -am package \
    -DskipTests -Dgpg.skip=true -Dmaven.javadoc.skip=true)
  copy_ref_jar kernel-ref-idobjectvalidator "$IDOBJECTVALIDATOR_JAR" "$work" \
    || die "Could not obtain kernel-ref-idobjectvalidator. See deps/additional_jars/README.md"
  copy_ref_jar kernel-virusscanner-clamav "$VIRUSSCANNER_JAR" "$work" \
    || die "Could not obtain kernel-virusscanner-clamav. See deps/additional_jars/README.md"
}

cmd_prep() {
  log "========================================"
  log " Local stack prep"
  log "========================================"
  ensure_ref_impl_jars
  info "Prep complete. loader.path jars: $ADDITIONAL_JARS_DIR"
}

need_docker() {
  command -v docker >/dev/null 2>&1 || die "docker is required on PATH"
  docker compose version >/dev/null 2>&1 || die "docker compose v2 is required"
}

compose() {
  (cd "$COMPOSE_DIR" && docker compose "$@")
}

usage() {
  cat <<EOF
$0 [command]

  up       Start Postgres :5455, config-server :51100, WireMock :8082, keymanager :8088, mock-smtp :8080/:8025/:8081, notifier :8083
  prep     Fetch loader jars (idobjectvalidator + virusscanner)
  down     Stop containers, keep volumes
  wipe     down -v then up (re-runs init SQL)
  status   docker compose ps
  logs     Follow logs (optional service name)
  help     This text

Host app: ../pre-registration-service/run-local.sh  (or run-prereg-local.sh)
EOF
}

cmd_up() {
  need_docker
  cmd_prep
  log "==> starting prereg local deps"
  compose up -d --build
  compose ps
  log "Postgres     localhost:5455  mosip_prereg / prereguser / mosip123"
  log "config-server http://localhost:51100/config"
  log "WireMock      http://localhost:8082"
  log "keymanager    http://localhost:8088/v1/keymanager"
  log "mock-smtp     http://localhost:8080  SMTP :8025  WS :8081"
  log "notifier      http://localhost:8083/v1/notifier"
  log "loader.path   $ADDITIONAL_JARS_DIR"
}

cmd="${1:-up}"
shift || true
case "$cmd" in
  -h|--help|help) usage ;;
  up) cmd_up ;;
  prep|prepare) cmd_prep ;;
  down) need_docker; compose down ;;
  wipe) need_docker; compose down -v; cmd_up ;;
  status) need_docker; compose ps ;;
  logs) need_docker; compose logs -f "${1:-}" ;;
  *) echo "unknown command '$cmd'" >&2; usage; exit 1 ;;
esac
