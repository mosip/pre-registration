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

All Maven commands should be run from the `pre-registration/` directory (the Maven parent POM), not the repo root:

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

Run the API test JAR:
```bash
java -Dmodules=prereg -Denv.user=api-internal.<env> \
     -Denv.endpoint=<base_url> -Denv.testLevel=smokeAndRegression \
     -jar target/apitest-prereg-1.4.0-SNAPSHOT-jar-with-dependencies.jar
```
Test level options: `smoke` (positive only) or `smokeAndRegression` (positive + negative). Reports land in `api-test/testng-report/`.

Run the services after building:
```bash
java -jar pre-registration/<service>/target/<service>-<version>.jar
# With remote config server:
java -Dspring.profiles.active=<profile> \
     -Dspring.cloud.config.uri=<config-url> \
     -Dspring.cloud.config.label=<label> \
     -jar <jar>.jar
```

Swagger UI (once running): `http://localhost:8080/preregistration/v1/application-service/swagger-ui/index.html`

## Module Structure

The Maven parent is `pre-registration/pom.xml` (`groupId: io.mosip.preregistration`, current version: `1.4.0-SNAPSHOT`). It contains five modules:

| Module | Role |
|---|---|
| `pre-registration-core` | Shared DTOs, entities, error codes, constants, and utilities used across all services |
| `pre-registration-application-service` | Main resident-facing REST API: create/update/delete applications, document upload, demographic data, delegates booking to external Booking Service |
| `pre-registration-datasync-service` | Syncs application data to registration centers (forward) and marks applications consumed after Registration Processor completes workflow (reverse) |
| `pre-registration-batchjob` | Scheduled cron jobs: creates appointment slots for centers, archives completed pre-registration applications |
| `pre-registration-captcha-service` | **Deprecated** — captcha now handled by [mosip/captcha](https://github.com/mosip/captcha) |

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

The `pre-registration-core` module's packages (`io.mosip.preregistration.core.*`) are imported by all other modules for shared DTOs, exceptions, and utility classes — always check core before duplicating anything.

**Key MOSIP kernel dependencies** (resolved via `kernel.bom.version`):
- `kernel-auth-adapter` — authentication filter (must be on classpath at runtime)
- `kernel-transliteration-icu4j` — multi-language transliteration
- `kernel-ref-idobjectvalidator` — ID object schema validation
- `kernel-virusscanner-clamav` — document virus scanning

Sonar coverage is explicitly excluded for: `dto/`, `entity/`, `errorcodes/`, `exception/`, `repository/`, `config/`, `util/`, `batchjob/`, and all `*Application.java` files. Unit tests are expected only for `service/` layer code.

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

Upgrade scripts across versions live in `db_upgrade_scripts/mosip_prereg/sql/` (naming convention: `<from-version>_to_<to-version>_upgrade.sql` + matching `_rollback.sql`).

## CI/CD

`.github/workflows/push-trigger.yml` triggers on push to `release-*`, `master`, and `develop` branches, on PRs, and on manual dispatch. Key jobs:
- `build-maven-pre-registration` — `mvn clean install` for all service modules
- `build-dockers` — builds Docker images per service using shared workflows from `mosip/kattu`
- `sonar_analysis` — SonarCloud quality gate
- `build-maven-apitest-prereg` — builds the `api-test` module
- `build-dockers_apitest_prereg` — packages the test rig Docker image

Maven Central publishing uses `central-publishing-maven-plugin` (replaced the older `nexus-staging-maven-plugin`).

## External References

- Official docs: https://docs.mosip.io/1.2.0/modules/pre-registration
- ID Lifecycle Management: https://docs.mosip.io/1.2.0/id-lifecycle-management
- Pre-registration UI (separate repo): https://github.com/mosip/pre-registration-ui
- Booking Service (separate repo): https://github.com/mosip/mosip-ref-impl
- Config properties: https://github.com/mosip/mosip-config
