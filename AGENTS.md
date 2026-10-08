# AGENTS.md

This file provides guidance to AI agents when working with code in this repository.

## Domain Context

Pre-registration is the resident-facing entry point of the MOSIP identity lifecycle. Before visiting a registration center, a resident:
1. Logs in with email/phone
2. Creates one or more pre-registration applications with demographic data (name, DOB, gender, etc.) in multiple languages
3. Uploads proof documents per application
4. Searches for a registration center and books an appointment slot

After booking, the **datasync-service** pushes application data to the registration center. Once the registration officer processes the application and the workflow completes in the Registration Processor, the datasync-service performs a reverse sync marking the application as consumed. Biometric data (face, iris, fingerprint) is captured at the center — not in pre-registration.

The **Booking Service** (appointment slot creation, booking, rescheduling) lives in a separate repository: [mosip-ref-impl](https://github.com/mosip/mosip-ref-impl). The application-service calls it as a remote dependency.

## Build Commands

Requires **JDK 21** (`java.version` in the parent POM) and Maven 3.x. All Maven commands should be run from the `pre-registration/` directory (the Maven parent POM), not the repo root:

```bash
# Full build (skip Javadoc and GPG signing for local dev)
cd pre-registration
mvn clean install -Dmaven.javadoc.skip=true -Dgpg.skip=true

# Build a single service module
mvn clean install -pl pre-registration-application-service -am -Dmaven.javadoc.skip=true -Dgpg.skip=true

# Run tests for a specific class
mvn test -pl pre-registration-application-service -Dtest=ApplicationServiceTest

# Skip tests entirely
mvn clean install -DskipTests -Dmaven.javadoc.skip=true -Dgpg.skip=true
```

For the **API test module** (lives outside the Maven parent, under `api-test/`):

```bash
cd api-test
mvn clean package -s settings.xml -Dgpg.skip=true -Dmaven.gitcommitid.skip=true
```
`settings.xml` is not checked in; download it from [mosip-functional-tests](https://github.com/mosip/mosip-functional-tests/blob/master/settings.xml) (see `api-test/README.md`).

Run the API test JAR:
```bash
java -Dmodules=prereg -Denv.user=api-internal.<env> \
     -Denv.endpoint=<base_url> -Denv.testLevel=smokeAndRegression \
     -jar target/apitest-prereg-<version>-jar-with-dependencies.jar   # currently 1.3.0
```
Test level options: `smoke` (positive only) or `smokeAndRegression` (positive + negative). Reports land in `api-test/testng-report/`.

Run the services after building:
```bash
java -jar pre-registration/<service>/target/<service>-<version>.jar
# With remote config server (add -Dloader.path=<dir> for runtime-only JARs, see below):
java -Dspring.profiles.active=<profile> \
     -Dspring.cloud.config.uri=<config-url> \
     -Dspring.cloud.config.label=<label> \
     -jar <jar>.jar
```

Swagger UI (once running): `http://localhost:8080/preregistration/v1/swagger-ui/index.html` — the context path is `/preregistration/v1` for application, datasync and batchjob services, and `/preregistration/v1/captcha` for the captcha service.

## Module Structure

The Maven parent is `pre-registration/pom.xml` (`groupId: io.mosip.preregistration`, current version: `1.3.1-SNAPSHOT`, `kernel.bom.version` 1.3.0). It contains five modules:

| Module | Role |
|---|---|
| `pre-registration-core` | Shared DTOs, entities, error codes, constants, SPIs (`core.spi.*`), and utilities used across all services; also hosts anonymous analytics events (`io.mosip.analytics.event.anonymous`) |
| `pre-registration-application-service` | Main resident-facing REST API. Controllers: Login (OTP), Application, Demographic, Document, Appointment (delegates to external Booking Service), Notification, Transliteration, UISpecification, ProxyMasterdata, GenerateQRcode, LostUIN, UpdateRegistration |
| `pre-registration-datasync-service` | Syncs application data to registration centers (forward) and marks applications consumed after Registration Processor completes workflow (reverse) |
| `pre-registration-batchjob` | Spring Batch jobs scheduled via cron (`PreregistrationBatchJobScheduler`): slot availability sync for centers, consumed status, expired status, identity reconciliation, booking-status check, purge expired center slots (see `tasklets/`) |
| `pre-registration-captcha-service` | **Deprecated** — captcha now handled by [mosip/captcha](https://github.com/mosip/captcha). Still built and Docker-imaged by CI |

The `api-test/` directory is a standalone Maven project (not a child of the above parent) used for API automation testing with TestNG + REST Assured.

## Code Architecture

All service modules follow a strict layered pattern under `io.mosip.preregistration.<service>`:

```
controller/   → REST endpoints, input validation, response wrapping
service/      → Business logic; service/ and service/util/ split for readability
repository/   → Spring Data JPA interfaces
entity/       → JPA-mapped DB entities
dto/          → Request/response objects (excluded from Sonar coverage)
errorcodes/   → Enum-based error code + message constants
exception/    → Custom exception classes
config/       → Spring @Configuration beans
```

`pre-registration-batchjob` does not follow this pattern; it is organized as `job/` (batch job config), `schedule/` (cron scheduler), `tasklets/`, `impl/`, `helper/`, `model/`, `repository/`, `entity/`. The deprecated captcha service uses `service/` + `serviceimpl/`.

The `pre-registration-core` module's packages (`io.mosip.preregistration.core.*`) are imported by all other modules for shared DTOs, exceptions, and utility classes — always check core before duplicating anything.

**Key MOSIP kernel dependencies** (compile-time, versions managed by `kernel-bom`):
- `kernel-core`, `kernel-dataaccess-hibernate`, `kernel-logger-logback` — common kernel libraries (core)
- `kernel-idobjectvalidator` — ID object validation (application-service)
- `kernel-templatemanager-velocity` — notification/acknowledgement templates
- `kernel-qrcodegenerator-zxing` — QR code generation (application-service)
- `kernel-keymanager-service` — datasync-service

**Runtime-only JARs** — NOT normal dependencies. They are declared only in the `openapi-doc-generate-profile` Maven profile. In Docker, each service's `Dockerfile` downloads them at startup into `additional_jars/` and launches with `-Dloader.path`. To run locally, put them on the loader path yourself:
- `kernel-auth-adapter` — authentication filter (application, datasync)
- `kernel-ref-idobjectvalidator` — ID object schema validation
- `kernel-transliteration-icu4j` + `icu4j` — multi-language transliteration
- `kernel-virusscanner-clamav` — implementation of the kernel `VirusScanner` interface used by `DocumentServiceUtil` (application-service)

Sonar coverage (`sonar.coverage.exclusions` in the parent POM) is explicitly excluded for: `code/`, `config/`, `dao/`, `dto/`, `entity/`, `errorcodes/`, `exception/` (incl. `system/`, `util/`), `repository/`, `util/`, `stateUtil/`, `batchjob/`, and all `*Config.java` / `*Application.java` files. Unit tests are expected only for `service/` layer code.

## Configuration

Pre-registration uses Spring Cloud Config Server. Configuration files live in a separate repo: [mosip/mosip-config](https://github.com/mosip/mosip-config).

- `pre-registration-default.properties` — module-specific config
- `application-default.properties` — shared Spring config

The config server must be running before starting any service locally. Sensitive values (DB password, Keycloak secrets) are passed as environment variable overrides through the config server — never hardcoded in property files.

Key required properties:
- `mosip.prereg.database.hostname` / `mosip.prereg.database.port`
- `db.dbuser.password` (env var)
- `keycloak.internal.url` / `keycloak.external.url` (env vars)
- `mosip.prereg.client.secret` (env var)
- `mosip.kernel.authmanager.url` / `mosip.kernel.prereg-application.url` / `mosip.kernel.prereg-datasync.url`

## Database

PostgreSQL 10.2+. Scripts are in `db_scripts/mosip_prereg/`:
- `db.sql` — creates the database
- `ddl/` — table DDL scripts
- `ddl.sql` — compiled DDL
- `role_dbuser.sql` / `grants.sql` — roles and permissions
- `deploy.sh` + `deploy.properties` — automated initialization
- `drop_db.sql` / `drop_role.sql` — teardown

Release-specific DB scripts live in `db_release_scripts/mosip_prereg/`.

Upgrade scripts across versions live in `db_upgrade_scripts/mosip_prereg/sql/` (naming convention: `<from-version>_to_<to-version>_upgrade.sql` + matching `_rollback.sql`).

## CI/CD

`.github/workflows/push-trigger.yml` triggers on push to `release*`, `master`, `1.*`, `develop*`, and `MOSIP*` branches, on published releases, on PRs, and on manual dispatch. All jobs use shared `mosip/kattu` workflows on the `master-java21` ref. Key jobs:
- `build-maven-pre-registration` — `mvn clean install` for all service modules
- `publish_to_nexus` — publishes snapshots (skipped on `master`, PRs, and releases)
- `build-dockers` — builds Docker images for application, batchjob, datasync, and captcha services
- `sonar_analysis` — SonarCloud quality gate
- `build-maven-apitest-prereg` / `build-apitest-prereg-local` — build the `api-test` module
- `build-dockers_apitest_prereg` — packages the test rig Docker image

Other workflows: `chart-lint-publish.yml` (Helm charts), `db-test.yml` (DB scripts), `use-pr-linker.yml`.

Maven Central publishing uses `central-publishing-maven-plugin` (replaced the older `nexus-staging-maven-plugin`). The root `deploy.sh` is a helper that runs `mvn deploy` for every `pom.xml` matching a given version (`./deploy.sh <dir> <settings.xml> <version>`).

## Deployment

- `helm/` — Helm charts: `prereg-application`, `prereg-batchjob`, `prereg-captcha`, `prereg-datasync`
- `deploy/` — Kubernetes install/delete/restart scripts for `prereg` and `prereg-apitestrig`
- `docs/configuration.md` — configuration notes

## External References

- Official docs: https://docs.mosip.io/1.2.0/modules/pre-registration
- ID Lifecycle Management: https://docs.mosip.io/1.2.0/id-lifecycle-management
- Pre-registration UI (separate repo): https://github.com/mosip/pre-registration-ui
- Booking Service (separate repo): https://github.com/mosip/mosip-ref-impl
- Config properties: https://github.com/mosip/mosip-config
