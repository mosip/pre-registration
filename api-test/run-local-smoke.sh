#!/usr/bin/env bash
# Run api-test against local pre-registration-service without touching prereg.properties.
# Usage: ./run-local-smoke.sh [smokeAndRegression|smoke|help] [testCaseIds]
# Output: console + .local/logs/run-local-<testLevel>-<timestamp>.log
# Prereqs:
#   - Docker deps up (local-dev-setup: ./run-local-stack.sh up)
#   - Host pre-registration-service on :9090 (run-prereg-local.sh / .bat)
#   - WireMock local IAM on :8082
# Secrets: optional .env.local; otherwise local-dev-setup defaults are used.
# Skip health preflight: SKIP_PREFLIGHT=1 ./run-local-smoke.sh
set -euo pipefail

API_TEST_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$API_TEST_DIR"

TEST_LEVEL="${1:-smokeAndRegression}"
TEST_CASES="${2:-}"

usage() {
  cat <<'EOF'

./run-local-smoke.sh [runType] [testCaseIds]

Run types:
  smokeAndRegression   all smoke + regression cases (default)
  smoke                smoke cases only
  help | -h | --help   show this list and exit

testCaseIds (optional): comma-separated uniqueIdentifiers

Environment:
  SKIP_PREFLIGHT=1     skip the WireMock / pre-registration health checks
  .env.local           optional client secrets (see run-local.env.example)

EOF
}

usage
case "${TEST_LEVEL,,}" in
  help|-h|--help) exit 0 ;;
  smokeandregression) TEST_LEVEL=smokeAndRegression ;;
  smoke) TEST_LEVEL=smoke ;;
  *)
    echo "ERROR: unknown run type \"$TEST_LEVEL\". Use one of the run types above." >&2
    exit 1
    ;;
esac
echo "Selected: $TEST_LEVEL"
echo "Test cases: ${TEST_CASES:-all}"
echo

REQUIRED_ENV_KEYS=(
  postgres-password
  keycloak_Password
  mosip_admin_client_secret
  mosip_testrig_client_secret
  mosip_partner_client_secret
  mosip_pms_client_secret
  mosip_resident_client_secret
  mosip_idrepo_client_secret
  mosip_reg_client_secret
  mosip_hotlist_client_secret
  mosip_regproc_client_secret
  mpartner_default_mobile_secret
  AuthClientSecret
  mosip_crvs1_client_secret
)

local_default_for() {
  case "$1" in
    postgres-password) printf '%s' 'mosip123' ;;
    keycloak_Password) printf '%s' 'admin' ;;
    mosip_testrig_client_secret) printf '%s' 'local-dev-testrig-secret' ;;
    mosip_admin_client_secret|mosip_partner_client_secret|mosip_pms_client_secret|mosip_resident_client_secret|mosip_idrepo_client_secret|mosip_reg_client_secret|mosip_hotlist_client_secret|mosip_regproc_client_secret|mpartner_default_mobile_secret|AuthClientSecret|mosip_crvs1_client_secret) printf '%s' 'local-dev-secret' ;;
    *) return 1 ;;
  esac
}

DOTENV_KEYS=()
DOTENV_VALS=()

load_dotenv_file() {
  local file="$1"
  [[ -f "$file" ]] || return 0
  while IFS= read -r line || [[ -n "$line" ]]; do
    line="${line%$'\r'}"
    [[ -z "$line" || "$line" =~ ^[[:space:]]*# ]] && continue
    local key="${line%%=*}"
    local val="${line#*=}"
    key="${key#"${key%%[![:space:]]*}"}"
    key="${key%"${key##*[![:space:]]}"}"
    [[ -z "$key" ]] && continue
    DOTENV_KEYS+=("$key")
    DOTENV_VALS+=("$val")
  done < "$file"
}

dotenv_get() {
  local want="$1"
  local i
  for i in "${!DOTENV_KEYS[@]}"; do
    if [[ "${DOTENV_KEYS[$i]}" == "$want" ]]; then
      printf '%s' "${DOTENV_VALS[$i]}"
      return 0
    fi
  done
  return 1
}

resolve_env_value() {
  local key="$1"
  local val
  val="$(printenv "$key" 2>/dev/null || true)"
  if [[ -n "$val" ]]; then
    printf '%s' "$val"
    return 0
  fi
  if val="$(dotenv_get "$key")"; then
    printf '%s' "$val"
    return 0
  fi
  if val="$(local_default_for "$key")"; then
    printf '%s' "$val"
    return 0
  fi
  return 1
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

load_dotenv_file "$API_TEST_DIR/.env.local"

missing=()
ENV_ARGS=()
for key in "${REQUIRED_ENV_KEYS[@]}"; do
  if val="$(resolve_env_value "$key")" && [[ -n "$val" ]]; then
    ENV_ARGS+=("${key}=${val}")
  else
    missing+=("$key")
  fi
done

if [[ -n "$TEST_CASES" ]]; then
  ENV_ARGS+=("testCasesToExecute=${TEST_CASES}")
fi

if [[ ${#missing[@]} -gt 0 ]]; then
  echo "ERROR: missing required local-run secrets:" >&2
  printf '  - %s\n' "${missing[@]}" >&2
  echo "Export them, or copy run-local.env.example to .env.local." >&2
  exit 1
fi

if [[ "${SKIP_PREFLIGHT:-0}" != "1" ]]; then
  echo "Checking local stack (pre-registration-service + WireMock)..."
  preflight_ok=0
  check_http "http://localhost:8082/__admin/health" "WireMock local IAM" \
    "Start Docker deps: pre-registration/local-dev-setup/run-prereg-local.sh up" || preflight_ok=1
  check_http "http://localhost:9090/preregistration/v1/actuator/health" "pre-registration-service" \
    "Start the host app: pre-registration/local-dev-setup/run-prereg-local.sh start" || preflight_ok=1
  if [[ "$preflight_ok" -ne 0 ]]; then
    echo "Set SKIP_PREFLIGHT=1 to bypass (tests will still fail if the service is down)." >&2
    exit 1
  fi
fi

PROPS_FILE="$API_TEST_DIR/src/main/resources/config/prereg-local.properties"
LOG4J_FILE="$API_TEST_DIR/src/main/resources/log4j.properties"
POM_FILE="$API_TEST_DIR/pom.xml"
shopt -s nullglob
jars=(target/apitest-prereg-*-jar-with-dependencies.jar)
need_build=0
if [[ ${#jars[@]} -eq 0 ]]; then
  need_build=1
elif [[ -f "$PROPS_FILE" && "$PROPS_FILE" -nt "${jars[0]}" ]]; then
  echo "prereg-local.properties is newer than the jar; rebuilding so tests hit :9090..."
  need_build=1
elif [[ -f "$LOG4J_FILE" && "$LOG4J_FILE" -nt "${jars[0]}" ]]; then
  echo "log4j.properties is newer than the jar; rebuilding so logs go to api-test/.local/logs..."
  need_build=1
elif [[ -f "$POM_FILE" && "$POM_FILE" -nt "${jars[0]}" ]]; then
  echo "pom.xml is newer than the jar; rebuilding..."
  need_build=1
elif find "$API_TEST_DIR/src/main/resources" -type f -newer "${jars[0]}" | grep -q .; then
  echo "Test resources are newer than the jar; rebuilding so YAML/cases are current..."
  need_build=1
elif find "$API_TEST_DIR/src/main/java" -type f -name '*.java' -newer "${jars[0]}" | grep -q .; then
  echo "Java sources are newer than the jar; rebuilding..."
  need_build=1
fi
if [[ "$need_build" -eq 1 ]]; then
  echo "Building api-test jar..."
  mvn clean install -Dgpg.skip=true -Dmaven.gitcommitid.skip=true -Dmaven.javadoc.skip=true
  jars=(target/apitest-prereg-*-jar-with-dependencies.jar)
fi
if [[ ${#jars[@]} -eq 0 ]]; then
  echo "ERROR: apitest-prereg jar-with-dependencies not found under target/" >&2
  exit 1
fi
JAR="${jars[0]}"

LOG_DIR="$API_TEST_DIR/.local/logs"
mkdir -p "$LOG_DIR"
LOG_FILE="$LOG_DIR/run-local-${TEST_LEVEL}-$(date +%Y%m%d_%H%M%S).log"

AUTH_CERTS_DIR="$API_TEST_DIR/target/local-authcerts"
REPORT_DIR="$API_TEST_DIR/target/local-reports"
mkdir -p "$AUTH_CERTS_DIR" "$REPORT_DIR"

{
  echo "===== run-local-smoke start $(date -Iseconds) ====="
  echo "Using JAR: $JAR"
  echo "Pre-registration service: http://localhost:9090/preregistration/v1"
  echo "WireMock IAM / stubs: http://localhost:8082"
  echo "Properties: prereg-local.properties"
  echo "testLevel: $TEST_LEVEL"
  echo "authCertsPath: $AUTH_CERTS_DIR"
  echo "Log file: $LOG_FILE"
  echo "================================================"
} | tee "$LOG_FILE"

set +e
env \
  "${ENV_ARGS[@]}" \
  "authCertsPath=$AUTH_CERTS_DIR" \
  java \
    -Dapi.test.log.dir="$LOG_DIR" \
    -Dprereg.propertiesFile=prereg-local.properties \
    -Dprereg.skipPartnerSetup=true \
    -Dmodules=prereg \
    -Denv.user=api-internal.local \
    -Denv.endpoint=http://localhost:8082 \
    -Denv.keycloak=http://localhost:8082 \
    -Denv.testLevel="$TEST_LEVEL" \
    -jar "$JAR" 2>&1 | tee -a "$LOG_FILE"
rc=${PIPESTATUS[0]}
set -e

{
  echo "===== run-local-smoke end exit=$rc ====="
  echo "Log written: $LOG_FILE"
} | tee -a "$LOG_FILE"

exit "$rc"
