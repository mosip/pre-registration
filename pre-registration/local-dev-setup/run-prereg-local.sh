#!/usr/bin/env bash
# Host JVM + local Maven/Docker helpers for consolidated Pre-Registration.
#
#   ./run-prereg-local.sh up | prep | run | start | stop | smoke | wipe-run | test | help
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MAVEN_PARENT="$(cd "$SCRIPT_DIR/.." && pwd)"
SERVICE_DIR="$MAVEN_PARENT/pre-registration-service"
STACK="$SCRIPT_DIR/run-local-stack.sh"
LOADER_PATH="${loader_path_env:-$SCRIPT_DIR/deps/additional_jars}"
PORT="${PORT:-9090}"
CONTEXT="/preregistration/v1"
BASE="http://127.0.0.1:${PORT}${CONTEXT}"
PROFILE="${SPRING_PROFILES_ACTIVE:-default,local}"
CONFIG_URI="${SPRING_CLOUD_CONFIG_URI:-http://localhost:51100/config}"
LOCAL_DIR="${LOCAL_DIR:-$SCRIPT_DIR/.local}"

die() { echo "ERROR: $*" >&2; exit 1; }
info() { echo "$*"; }

print_help() {
  cat <<EOF
$0 [command]

Docker deps:
  up               Start Postgres, config-server, WireMock
  wipe             compose down -v then up

App (host JVM on ${BASE}):
  prep             Fetch loader.path jars (idobjectvalidator + virusscanner)
  run              Rebuild jar, then start with profiles default,local
  start            Start existing jar, no Maven
  stop             Stop the host JVM
  smoke            GET health + swagger
  wipe-run         wipe, then rebuild + start
  test             mvn test on pre-registration-service
  help
EOF
}

stop_app() {
  info "Stopping host pre-registration-service if it is running..."
  if [[ -x "$SERVICE_DIR/run-local.sh" ]]; then
    (cd "$SERVICE_DIR" && LOCAL_DIR="$LOCAL_DIR" ./run-local.sh stop) || true
  fi
}

rebuild() {
  info "Packaging pre-registration-service"
  (
    cd "$MAVEN_PARENT"
    unset SPRING_PROFILES_ACTIVE || true
    mvn -pl pre-registration-service -am clean package -DskipTests -Dgpg.skip=true -Dmaven.javadoc.skip=true
  )
}

start_app() {
  if [[ ! -f "$LOADER_PATH/kernel-ref-idobjectvalidator.jar" || ! -f "$LOADER_PATH/kernel-virusscanner-clamav.jar" ]]; then
    die "missing loader.path jars under $LOADER_PATH — run: $STACK prep"
  fi
  export SPRING_PROFILES_ACTIVE="${PROFILE}"
  export SPRING_CLOUD_CONFIG_URI="${CONFIG_URI}"
  export loader_path_env="$LOADER_PATH"
  export LOCAL_DIR
  export PORT
  (cd "$SERVICE_DIR" && ./run-local.sh start)
}

smoke() {
  (cd "$SERVICE_DIR" && PORT="$PORT" LOCAL_DIR="$LOCAL_DIR" ./run-local.sh smoke)
}

cmd="${1:-help}"
shift || true
case "$cmd" in
  -h|--help|help) print_help ;;
  up) "$STACK" up ;;
  prep) "$STACK" prep ;;
  down) "$STACK" down ;;
  wipe) stop_app; "$STACK" wipe ;;
  run) "$STACK" up; rebuild; start_app ;;
  start) start_app ;;
  stop) stop_app ;;
  smoke) smoke ;;
  wipe-run) stop_app; "$STACK" wipe; rebuild; start_app ;;
  test)
    (cd "$MAVEN_PARENT" && unset SPRING_PROFILES_ACTIVE || true
     mvn -pl pre-registration-service -am test -Dgpg.skip=true -Dmaven.javadoc.skip=true)
    ;;
  rebuild) rebuild ;;
  *) echo "unknown command '$cmd'" >&2; print_help; exit 1 ;;
esac
