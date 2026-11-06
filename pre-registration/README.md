# pre-registration (Maven parent)

Parent reactor for MOSIP Pre-Registration. Spring Boot **4.1.2**, Java **21**, no `kernel-bom`. How-to for the whole git repo: [root README](../README.md). Layout: [docs/design/service-consolidation.md](../docs/design/service-consolidation.md).

```text
pre-registration/
├─ pre-registration-core/         published library (DTOs, entities, utils)
├─ pre-registration-service/      HTTP :9090 — application + datasync + captcha + booking
├─ pre-registration-batchjob/     scheduled Job :9097
├─ pridgenerator-service/         PRID pool :8100, /v1/pridgenerator
└─ local-dev-setup/               Docker deps + host-run scripts
```

The Angular SPA is [`ui/`](../ui/) at the git root (not a Maven module).

## Frozen public URLs

Servlet context `/preregistration/v1` on port **9090**:

- application: `/preregistration/v1/applications`, `/documents`, `/login`, `/notification`, `/proxy`, …
- booking: `/preregistration/v1/appointment/**` (native) and `/preregistration/v1/applications/appointment/**` (application proxy)
- datasync: `/preregistration/v1/sync`, `/syncV2`, `/sync/{id}`, `/sync/consumedPreRegIds`
- captcha: `POST /preregistration/v1/captcha/validatecaptcha`
- health: `/preregistration/v1/actuator/health`

PRID stays a **separate** Vert.x JVM (port **8100**): `GET /v1/pridgenerator/prid`.

## Build

From this directory (`pre-registration/`), not the git root:

```text
mvn clean install -Dmaven.javadoc.skip=true -Dgpg.skip=true
mvn clean install -pl pre-registration-service -am -Dmaven.javadoc.skip=true -Dgpg.skip=true
mvn clean install -pl pridgenerator-service -am -Dmaven.javadoc.skip=true -Dgpg.skip=true
```

Run the HTTP jar (after local-dev deps are up):

```text
java -jar pre-registration-service/target/pre-registration-service-*.jar
```

Or from `local-dev-setup/`: `./run-prereg-local.sh run` (Windows: `run-prereg-local.bat run`).

Swagger: http://localhost:9090/preregistration/v1/swagger-ui/index.html — Authorize uses header `Authorization`.

## Configuration

Spring Cloud Config documents `application` and `pre-registration` (`spring.cloud.config.name=application,pre-registration`). Cluster files: [mosip-config](https://github.com/mosip/mosip-config). Local overlay: `local-dev-setup/deps/mosip-config/` plus `pre-registration-service/src/main/resources/application-local.properties`.
