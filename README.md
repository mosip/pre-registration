[![Maven Package upon a push](https://github.com/mosip/pre-registration/actions/workflows/push-trigger.yml/badge.svg?branch=develop)](https://github.com/mosip/pre-registration/actions/workflows/push-trigger.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?branch=develop&project=mosip_pre-registration&metric=alert_status)](https://sonarcloud.io/dashboard?branch=develop&id=mosip_pre-registration)

## Overview

The **Pre-Registration** module enables a resident to:
1. Log in with email id / phone number
2. Create one or more Pre-Registration applications
3. Capture demographic data in multiple languages
4. Upload proof documents
5. Search a registration centre and book an appointment slot
6. Receive acknowledgement and notification, and cancel or reschedule

The reference front-end lives in [`ui/`](ui/) (imported from [mosip/pre-registration-ui](https://github.com/mosip/pre-registration-ui/) `develop`). Helm: `helm/prereg-ui`. Cluster scripts: `deploy/prereg-ui`. Functional overview: [docs.mosip.io](https://docs.mosip.io/1.2.0/modules/pre-registration). Design of this layout: [docs/design/service-consolidation.md](docs/design/service-consolidation.md).

## Artifacts

| Artifact | Role | Port / packaging |
|---|---|---|
| `pre-registration-core` | Published library of shared DTOs, entities, utilities | jar (not a deployable) |
| `pre-registration-service` | One HTTP JVM: application + datasync + captcha + booking | **9090**, `/preregistration/v1` |
| `pre-registration-batchjob` | Scheduled Job: slots, expiry, archival | **9097** |
| `pridgenerator-service` | PRID pool (separate Vert.x JVM) | **8100**, `/v1/pridgenerator` |
| `ui/` (`pre-registration-ui` image) | Angular SPA | **8080**, `/pre-registration-ui/` |

Captcha and datasync are **not** separate deployables. Their code runs in-process in `pre-registration-service` so public URLs stay:

- `/preregistration/v1/applications`, `/documents`, `/login`, `/notification`, `/proxy`, …
- `/preregistration/v1/appointment/**` (booking-native) and `/preregistration/v1/applications/appointment/**` (application proxy)
- `/preregistration/v1/sync`, `/syncV2`, `/sync/{id}`, `/sync/consumedPreRegIds`
- `POST /preregistration/v1/captcha/validatecaptcha`
- health: `/preregistration/v1/actuator/health`

PRID generation stays a **separate** deployable (`pridgenerator-service`). Callers keep `GET /v1/pridgenerator/prid`. The HTTP service still calls `mosip.io.prid.url`.

Booking (`io.mosip.preregistration.booking`) was imported from [mosip-ref-impl issue-1633](https://github.com/mosip/mosip-ref-impl/tree/issue-1633/pre-registration-booking-service) into `pre-registration-service`. Git history of that subdirectory is the `booking-hist` remote (`issue-1633`, git-filter-repo). Callers keep `/preregistration/v1/appointment/**`.

Version **1.4.1-SNAPSHOT**. Parent: `org.springframework.boot:spring-boot-starter-parent:4.1.1`. Java 21. No `kernel-bom`. Detailed Boot 4 steps: [docs/design/spring-boot-4.md](docs/design/spring-boot-4.md).

## Database

PostgreSQL 10.2+. Application tables: [db_scripts/mosip_prereg](db_scripts/mosip_prereg). PRID pool (`kernel.prid` in `mosip_kernel`): [db_scripts/mosip_kernel](db_scripts/mosip_kernel) — that folder is PRID-only; other kernel tables stay in [mosip/commons](https://github.com/mosip/commons). Upgrades: [db_upgrade_scripts](db_upgrade_scripts).

## Local setup

### Laptop stack (recommended)

From `pre-registration/local-dev-setup/`:

```text
./run-local-stack.sh up
./run-prereg-local.sh run
```

Windows cmd: `run-prereg-local.bat run`. Details: [LOCAL-DEV-SETUP.md](pre-registration/local-dev-setup/LOCAL-DEV-SETUP.md).

### Module runners

```text
cd pre-registration/pre-registration-service
./run-local.sh init | start | smoke | stop | test | all | docker
# Windows: run-local.bat …

cd ../pre-registration-batchjob
./run-local.sh init | start | smoke | stop

cd ../pridgenerator-service
./run-local.sh init | start | smoke | stop
# CONTEXT=/v1/pridgenerator  port 8100

cd ../../ui
./run-local.sh              # ng serve (HTTP) + proxy to host :9090
# Windows: run-local.bat
```

PID and logs live under `.local/` (gitignored). Service wait string: `Started PreRegistrationBootApplication`.

### Maven

From `pre-registration/` (the Maven parent), not the repo root:

```text
mvn clean install -Dmaven.javadoc.skip=true -Dgpg.skip=true
java -jar pre-registration-service/target/pre-registration-service-1.4.1-SNAPSHOT.jar
```

Swagger: http://localhost:9090/preregistration/v1/application-service/swagger-ui.html — four catalog UIs (`/application-service`, `/sync/datasync-service`, `/captcha-service`, `/appointment/booking-service`). Authorize header `Authorization`.

### Docker Hub

```text
docker pull mosipid/pre-registration-service:1.4.1-SNAPSHOT
docker pull mosipid/pre-registration-batchjob:1.4.1-SNAPSHOT
docker pull mosipid/pridgenerator-service:1.4.1-SNAPSHOT
docker pull mosipid/pre-registration-ui:1.4.1-SNAPSHOT
```

Booking is part of `pre-registration-service`; there is no separate booking image.

## Configuration

Spring Cloud Config. Documents: `application`, `pre-registration` (`spring.cloud.config.name=application,pre-registration`). Files live in [mosip/mosip-config](https://github.com/mosip/mosip-config).

Required:

- `mosip.prereg.database.hostname` / `mosip.prereg.database.port`
- `db.dbuser.password` (env)
- `keycloak.internal.url` / `keycloak.external.url` (env)
- `mosip.prereg.client.secret` (env)
- `mosip.kernel.authmanager.url` / `mosip.kernel.prereg-application.url`

A key that used to differ per JVM now has **one** value in this process.

## Helm / deploy

Charts: `helm/prereg-application` (HTTP, Istio prefixes include `/appointment`, `/sync`, and `/captcha`), `helm/prereg-batchjob`, `helm/pridgenerator` (Istio prefix `/v1/pridgenerator`, Service `kernel-pridgenerator-service`), and `helm/prereg-ui`. `deploy/prereg/install.sh` installs application + batchjob + pridgenerator + UI.

## Testing

Unit tests: `mvn test` from the parent. API automation: `api-test/` (endpoint paths unchanged). Local laptop run: `api-test/run-local-smoke.bat` / `./run-local-smoke.sh` against host `:9090` and WireMock `:8082`.

## License

[Mozilla Public License 2.0](LICENSE). Third-party notices: [NOTICE](NOTICE), [THIRD-PARTY-NOTICES.txt](THIRD-PARTY-NOTICES.txt), [licenses/](licenses/).
