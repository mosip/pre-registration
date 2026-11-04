# PRID generator service

Issues Pre-Registration IDs from the `kernel.prid` pool. Vert.x HTTP on port **8100**. Public prefix **`/v1/pridgenerator`** (not `/preregistration/v1`). Separate JVM from `pre-registration-service`.

- Fetch: `GET /v1/pridgenerator/prid`
- Health: `/v1/pridgenerator/actuator/health`

`pre-registration-service` still HTTP-calls `mosip.io.prid.url`. Database: `mosip_kernel.kernel.prid` (see `db_scripts/mosip_kernel`). Helm: `helm/pridgenerator`.

Local:

```text
./run-local.sh init | start | smoke | stop
# Windows: run-local.bat …
```

`CONTEXT=/v1/pridgenerator`.
