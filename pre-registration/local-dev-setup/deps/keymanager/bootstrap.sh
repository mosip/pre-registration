#!/usr/bin/env bash
# Local keymanager bootstrap.
# Crypto rows come from deps/init.sql (all aliases, key_store, data_encrypt_keystore,
# PRE_REGISTRATION/INDIVIDUAL). This script only starts KM and verifies certificates.
# keys-generator.jar / generateMasterKey are not used.
set -euo pipefail

CONFIG_DIR="${KM_CONFIG_DIR:-/home/mosip/config}"
READY_MARKER="${CONFIG_DIR}/.km-local-ready"
KM_URL="${KM_URL:-http://127.0.0.1:8088}"
LABEL="${SPRING_CLOUD_CONFIG_LABEL:-develop}"

log() { echo "keymanager-bootstrap: $*"; }

start_keymanager() {
  local jar
  jar="$(ls ./kernel-keymanager-service*.jar 2>/dev/null | head -n 1 || true)"
  if [[ -z "$jar" ]]; then
    log "ERROR: kernel-keymanager-service jar not found in image cwd"
    exit 1
  fi
  log "Starting keymanager: $jar"
  java -XX:+UseZGC -Dfile.encoding=UTF-8 \
    -Dspring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration \
    -Dspring.cloud.config.uri=http://config-server:51000/config \
    -Dspring.cloud.config.label="${LABEL}" \
    -Dspring.profiles.active=default \
    -Dspring.cloud.config.name=keymanager,kernel \
    -Dmosip.kernel.database.hostname=postgres \
    -Dmosip.kernel.database.port=5432 \
    -Ddb.dbuser.password=mosip123 \
    -Dkeymanager_database_url=jdbc:postgresql://postgres:5432/mosip_keymgr \
    -Dkeymanager_database_username=keymgruser \
    -Dkeymanager_database_password=mosip123 \
    -Dmosip.kernel.keymanager.hsm.keystore-type=PKCS12 \
    -Dmosip.kernel.keymanager.hsm.config-path="${CONFIG_DIR}/mosip-prereg-ks.p12" \
    -Dmosip.kernel.keymanager.hsm.keystore-pass=1234 \
    -Dmosip.kernel.authmanager.url=http://mock-service:8082 \
    -Dauth.server.validate.url=http://mock-service:8082/v1/authmanager/authorize/admin/validateToken \
    -Dauth.server.admin.validate.url=http://mock-service:8082/v1/authmanager/authorize/admin/validateToken \
    -Dauth.server.admin.offline.comp.token.validate=false \
    -Dmosip.auth.filter_disable=true \
    -Dmosip.admin.client.secret=local-dev-secret \
    -Dmosip.iam.adapter.appid=admin \
    -Dmosip.iam.adapter.clientid=mosip-admin-client \
    -Dmosip.iam.adapter.clientsecret=local-dev-secret \
    -Dmosip.authmanager.client-token-endpoint=http://mock-service:8082/v1/authmanager/authenticate/clientidsecretkey \
    -Dmosip.iam.adapter.self-token-renewal-enable=false \
    -Dmosip.security.csrf-enable=false \
    -Dspring.security.csrf.enabled=false \
    -Dkeycloak.internal.url=http://mock-service:8082 \
    -Dkeycloak.external.url=http://mock-service:8082 \
    -Dauth.server.admin.issuer.internal.uri=http://mock-service:8082/auth/realms/ \
    -Dauth.server.admin.issuer.uri=http://mock-service:8082/auth/realms/ \
    -Dauth.server.admin.audience.claim.validate=false \
    -Dauth.server.admin.issuer.domain.validate=false \
    -Dspring.cloud.loadbalancer.enabled=false \
    -jar "$jar" &
  KM_PID=$!
}

wait_health() {
  log "Waiting for keymanager health..."
  local i
  for i in $(seq 1 90); do
    if curl -sf "${KM_URL}/v1/keymanager/actuator/health" >/dev/null 2>&1; then
      log "keymanager healthy"
      return 0
    fi
    if ! kill -0 "$KM_PID" 2>/dev/null; then
      log "ERROR: keymanager process exited before healthy"
      wait "$KM_PID" || true
      exit 1
    fi
    sleep 2
  done
  log "ERROR: keymanager health timeout"
  exit 1
}

cert_ok() {
  local app_id="$1"
  local ref_id="$2"
  local code
  code="$(curl -sS -o /tmp/km-cert.out -w "%{http_code}" \
    "${KM_URL}/v1/keymanager/getCertificate?applicationId=${app_id}&referenceId=${ref_id}" || echo 000)"
  log "getCertificate ${app_id}/${ref_id:-empty} HTTP ${code}"
  head -c 400 /tmp/km-cert.out; echo
  if [[ "$code" != "200" ]] || grep -q '"errorCode"' /tmp/km-cert.out; then
    return 1
  fi
}

verify_static_seed() {
  log "Verifying SQL seed matches PKCS12..."
  if ! cert_ok PRE_REGISTRATION ""; then
    log "ERROR: static seed / PKCS12 mismatch — keep keys/mosip-prereg-ks.p12 (password 1234) that matches the keymgr seed in deps/init.sql"
    exit 1
  fi
  cert_ok PRE_REGISTRATION "INDIVIDUAL" || {
    log "ERROR: PRE_REGISTRATION/INDIVIDUAL missing from init.sql seed"
    exit 1
  }
  cert_ok KERNEL "SIGN" || {
    log "ERROR: KERNEL/SIGN certificate missing (datasync jwtSign)"
    exit 1
  }
}

rm -f "$READY_MARKER"
if [[ ! -f "${CONFIG_DIR}/mosip-prereg-ks.p12" ]]; then
  log "ERROR: missing ${CONFIG_DIR}/mosip-prereg-ks.p12 — SQL seed requires the matching PKCS12"
  exit 1
fi
start_keymanager
wait_health
verify_static_seed
touch "$READY_MARKER"
log "ready marker written: $READY_MARKER"
wait "$KM_PID"
