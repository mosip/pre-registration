# Local development setup

## What runs where

| Process | Where | Port |
|---|---|---|
| Postgres `mosip_prereg`, `mosip_keymgr`, `mosip_audit` | Docker | 5455 |
| Config server (native, bundled files) | Docker | 51100 |
| WireMock (IAM, masterdata, audit) | Docker | 8082 |
| Keymanager (`PRE_REGISTRATION` / `INDIVIDUAL`) | Docker | 8088 |
| [mock-smtp-sms](https://github.com/mosip/mock-smtp-sms/tree/develop) | Docker | 8080 UI/SMS, 8025 SMTP, 8081 WS |
| notifier (forwards `/v1/notifier` to mock-smtp) | Docker | 8083 |
| MinIO (document object store) | Docker | 9000 / console 9001 |
| `pre-registration-service` (application + datasync + captcha + booking) | **Host JVM** | 9090 |
| `pre-registration-batchjob` | Host JVM (optional) | 9097 |

Profiles: `default,local`. Config documents: `application,pre-registration`.

## Commands

From `pre-registration/local-dev-setup/`:

```text
./run-prereg-local.sh up | prep | run | start | stop | smoke | wipe-run | help
run-prereg-local.bat   (same verbs on Windows cmd)
./run-local-stack.sh up | prep | down | wipe | status | logs
```

Host JVM logs and PIDs: `local-dev-setup/.local/` (gitignored), same layout as id-repository. `run-prereg-local.*` sets `LOCAL_DIR` and `-Dlogging.file.path=.local/logs`. Console stays in `prereg-service.log`; Spring rolling file is `spring.log`.

Module runners (override `LOCAL_DIR` or default to the module `.local/`):

```text
../pre-registration-service/run-local.sh  init | start | smoke | stop | test | all | docker
../pre-registration-batchjob/run-local.sh init | start | smoke | stop | test | all | docker
```

`start` waits for `Started PreRegistrationBootApplication` (service) or `Started PreRegistrationBatchJob` (job).

`prep` clones [mosip-ref-impl `issue-1633`](https://github.com/mosip/mosip-ref-impl/tree/issue-1633) and copies `kernel-ref-idobjectvalidator.jar` plus `kernel-virusscanner-clamav.jar` into `deps/additional_jars/` (`-Dloader.path`). Booking is in the host HTTP JVM (`io.mosip.preregistration.booking`). `kernel-auth-adapter` is a Maven dependency. Transliteration lives in `kernel-core`. Cluster images wget the two SPIs in `configure_start.sh`.

The virus-scanner jar is required so Spring can create `VirusScannerImpl`. It does not scan files by itself; it calls a ClamAV daemon. Local compose has no ClamAV, so `deps/mosip-config/pre-registration-default.properties` sets `mosip.preregistration.document.scan=false`. With scan true, upload fails `PRG_PAM_DOC_010` (`Document virus scan failed`) because `clamav.clamav:3310` is unreachable.

## Database

Init SQL is one file, `deps/init.sql`, same layout as id-repository: create `mosip_prereg`, `mosip_audit`, and `mosip_keymgr`, `\i` the official [db_scripts](https://github.com/mosip/pre-registration/tree/master/db_scripts) DDL (`reg_appointment`, `reg_available_slot`, …) plus `deps/keymanager/ddl` and [audit-manager issue-1633](https://github.com/mosip/audit-manager/tree/issue-1633/db_scripts) `app_audit_log`, then inline the local slot rows (centre `10003`) and the key seed. Users `prereguser`, `audituser`, and `keymgruser`, password `mosip123`. Wipe volumes to re-apply it. An already-initialized volume can run `deps/audit/apply-existing.sql` instead of wiping.

Audit HTTP is WireMock on `:8082` (`POST /v1/auditmanager/audits` in `deps/wiremock/mappings/prereg.json`). Host `audit.url` is `http://localhost:8082/v1/auditmanager/audits`. WireMock does not write Postgres; the host `local` profile inserts into `mosip_audit.audit.app_audit_log` from `application-local.properties` (`preregistration.audit.jdbc.url`). api-test `db:mosip_audit` uses `postgres` / `mosip123` against `localhost:5455/mosip_audit`. Recreate `mock-service` after editing that stub.

## Booking

Booking runs in the host HTTP JVM. Source was imported from [mosip-ref-impl issue-1633](https://github.com/mosip/mosip-ref-impl/tree/issue-1633/pre-registration-booking-service) into `pre-registration-service` (`io.mosip.preregistration.booking`) with git history. Frozen prefixes stay `/preregistration/v1/appointment/**` on `:9090`. Batchjob still HTTP-calls that URL. WireMock does not stub `/appointment/**`. After changing `init.sql`, `wipe` so it re-runs.

`deps/wiremock/mappings/prereg.json` also stubs api-test health (`/v1/*/actuator/health|info`), signed IAM tokens (`mosip-admin-client`, `mosip-prereg-client`, `mosip-regproc-client`, `mosip-testrig-client`, `mosip-resident-client`), JWKS, `validateToken` (includes `INDIVIDUAL` / `REGISTRATION_*` / `PRE_REGISTRATION_ADMIN`), PRID, OTP generate, and `POST /v1/auditmanager/audits`. Recreate `mock-service` after editing that file.

## MinIO and keymanager

| Dependency | Boot | Create application / documents | This compose |
|---|---|---|---|
| MinIO (`localhost:9000`, bucket `prereg`) | No | Documents (`S3Adapter`) | Yes |
| Keymanager (`localhost:8088`) | No | Encrypt/decrypt demographics and files; datasync `jwtSign` | Yes |

`CryptoUtil` calls `PRE_REGISTRATION` / `INDIVIDUAL`. The SQL seed has the `PRE_REGISTRATION` base key; `deps/keymanager/bootstrap.sh` calls `getCertificate` for `INDIVIDUAL`, which creates that keypair. The PKCS12 is `keys/mosip-prereg-ks.p12` (password `1234`), a copy of id-repository `keys/mosip-idrepo-ks.p12`, and must stay paired with the key seed inlined in `deps/init.sql`. Do not add a dummy `/v1/keymanager/encrypt` stub. Host config sets `mosip.kernel.keymanager.url=http://localhost:8088`.

Mailbox UI: http://localhost:8080/. `GET /sendsms` is on the same port. Compose `notifier` (`:8083`) POSTs `/v1/notifier/email/send` into mock-smtp SMTP. api-test `mockSmtpWebSocketUrl=ws://localhost:8081`.

Stop an id-repository local stack before `up` if it already holds 5455, 51100, 8080, 8081, 8082, 8088, 8025, or 9000.
