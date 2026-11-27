#!/usr/bin/env bash
# Export grouped OpenAPI JSON from a running local pre-registration-service.
set -euo pipefail
BASE="${BASE:-http://127.0.0.1:9090/preregistration/v1}"
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
for group in application datasync captcha booking; do
  out="${DIR}/${group}.json"
  curl -fsS "${BASE}/v3/api-docs/${group}" -o "${out}"
  echo "wrote ${out}"
done
