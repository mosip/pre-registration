# Local Pre-Registration

This folder runs the **consolidated** Pre-Registration HTTP service on a laptop. One host JVM on `:9090` serves the frozen `/preregistration/v1` APIs (applications, documents, login, notification, proxy, captcha, datasync, booking). Docker only runs dependencies. There is no HTTP-app container.

## Prerequisites

| Tool | Notes |
|------|--------|
| Docker Desktop **running** | Compose v2. ~5 GB images + Maven cache |
| JDK **21** | `java -version` |
| Maven **≥ 3.9** | MOSIP `settings.xml` in `~/.m2` for `prep` / api-test |
| Git | Windows: Git Bash **or** `run-prereg-local.bat` from **cmd** (not Git Bash for the JVM) |
| RAM | 8 GB+ recommended |

Working directory for every command below:

```text
cd pre-registration/local-dev-setup
```

## 1. Start Docker deps

**Windows cmd:**

```bat
run-prereg-local.bat up
```

**Linux / macOS / Git Bash:**

```sh
./run-local-stack.sh up
```

`up` also runs `prep` (clones mosip-ref-impl and builds the two `loader.path` jars — needs network the first time). It does **not** pass `--build`. Rebuild images only when you change a Dockerfile:

```bat
run-prereg-local.bat build
```

Object store images are `pgsty/minio` and `pgsty/mc` (Docker Hub `minio/minio` / `minio/mc` were withdrawn). Postgres is `postgres:16-bookworm`.

### Verify deps

| Service | Check |
|---------|--------|
| Postgres | `localhost:5455` user `postgres` / `mosip123` |
| Config server | http://localhost:51100/config |
| WireMock | http://localhost:8082/__admin/health |
| MinIO | http://localhost:9000  console :9001  bucket `prereg` |
| Keymanager | http://localhost:8088/v1/keymanager/actuator/health |
| mock-smtp | http://localhost:8080 |
| notifier | http://localhost:8083 |

## 2. Build and start the host app

```bat
run-prereg-local.bat run
```

```sh
./run-prereg-local.sh run
```

`run` = Maven package + start. It does **not** start Docker. Use `start` only after a finished `rebuild` / `run`.

Optional: `SPRING_CLOUD_CONFIG_URI` (default `http://localhost:51100/config`), `PORT` (default `9090`).

## 3. Verify the app

```bat
run-prereg-local.bat smoke
```

Expect HTTP 200 from:

- http://localhost:9090/preregistration/v1/actuator/health
- http://localhost:9090/preregistration/v1/swagger-ui/index.html

Log line: `Started PreRegistrationBootApplication`. Logs: `.local/logs/prereg-service.log`.

Local OTP is always `111111`.

## Data seeding

`deps/init.sql` runs once on an empty Postgres volume: schemas, slot rows for centre `10003` (from init date + 2), keymanager seed. Wipe to re-seed:

```bat
run-prereg-local.bat wipe
```

## Swagger and Postman

Grouped OpenAPI (Authorize header `Authorization`):

- http://localhost:9090/preregistration/v1/application-service/swagger-ui.html
- `/v3/api-docs/application` `/v3/api-docs/datasync` `/v3/api-docs/captcha` `/v3/api-docs/booking`

Postman: import [`postman/Pre-Registration-Local.postman_collection.json`](postman/Pre-Registration-Local.postman_collection.json) and [`postman/Pre-Registration-Local.postman_environment.json`](postman/Pre-Registration-Local.postman_environment.json). How-to: [`postman/README.md`](postman/README.md). Happy path: Auth → Login (OTP `111111`) → Create → Upload `postman/sample/IDProof.pdf` → Update status → Availability → Book.

## api-test

See [`api-test/README.md`](../../api-test/README.md). Need MOSIP `settings.xml`. From repo root:

```bat
cd api-test
run-local-smoke.bat
```

## Optional processes

| Process | Port | How |
|---------|------|-----|
| Batchjob | 9097 | `../pre-registration-batchjob/run-local.bat start` |
| PRID generator | 8100 | `../pridgenerator-service/run-local.bat start` (Postman folder 11) |

## Stop / wipe

```bat
run-prereg-local.bat stop
run-prereg-local.bat wipe
```

`stop` kills the PID file **and** whatever is listening on `:9090`.

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| `minio/minio` pull denied | This compose uses `pgsty/minio` / `pgsty/mc`. `git pull` this branch. |
| keymanager `set: pipefail: invalid option name` | CRLF on `bootstrap.sh`. This repo has `.gitattributes` (`*.sh eol=lf`); compose also strips CR. Re-clone or `git add --renormalize .` |
| `Port 9090 was already in use` | `run-prereg-local.bat stop` then `start` |
| Git Bash `Selector.open()` / UnixDomainSockets | Start the JVM from **cmd** or PowerShell, or keep `-Djdk.net.unixdomain.tmpdir` (set by `run-local.sh` / `run-local.bat`) |
| Document upload hangs ~2.5 min | MinIO is down. Check `:9000`. Local profile sets `object.store.s3.max.retry=0` |
| `PRG_PAM_DOC_010` virus scan | Local config has `mosip.preregistration.document.scan=false` (no ClamAV container) |
| `up` fails offline at `--build` | `up` no longer passes `--build`. Use cached images. |
| `.bat` ignores `docker-compose.override.yml` | `run-prereg-local.bat` now `cd`s into `deps` so Compose auto-loads the override |

Detail: [`LOCAL-DEV-SETUP.md`](LOCAL-DEV-SETUP.md).
