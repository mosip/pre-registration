# Spring Boot 4 changes in Pre-Registration

This is the how-to for moving MOSIP Pre-Registration from Spring Boot 2.x / Java 11 onto **Spring Boot 4.1.2** and **Java 21**, then the Boot-related follow-ups on this branch (scan, Jackson 2, tests, OpenAPI, JWT). Layout and frozen URLs are in [service-consolidation.md](service-consolidation.md). How to run locally is in the root [README](../../README.md).

Parent POM: `pre-registration/pom.xml`. Do not parent `kernel-bom`. Maven from `pre-registration/` only.

---

## 1. What changed at the platform layer

| Item | Before (`develop`) | This branch |
|---|---|---|
| JDK | 11 | 21 |
| Spring Boot | 2.x via MOSIP parent / kernel-bom | `org.springframework.boot:spring-boot-starter-parent` 4.1.2 |
| Spring Cloud | Hoxton / 2021 line | 2025.1.4 |
| Servlet / JPA / inject | `javax.*` | `jakarta.*` |
| JSON | Jackson 2 on the Boot 2 classpath | Boot 4 defaults to Jackson 3; this repo **keeps Jackson 2** with `spring-boot-jackson2` |
| Tests | `@MockBean` | `@MockitoBean` (`org.springframework.test.context.bean.override.mockito`) |
| SpringDoc | 1.x / 2.x grouped UI | 3.1.1, **one** `/v3/api-docs` document |

Public URLs stay `/preregistration/v1` on **9090**. The batch job stays on **9097**. PRID stays Vert.x **8100**.

---

## 2. Parent POM

1. Set the Maven parent to Spring Boot (not a MOSIP BOM):

```xml
<parent>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-parent</artifactId>
  <version>4.1.2</version>
  <relativePath />
</parent>
```

2. Pin Java 21: `java.version`, `maven.compiler.source`, `maven.compiler.target`.
3. Import Spring Cloud BOM in `dependencyManagement` (`spring-cloud.version`). Use **2025.1.4** with Boot 4.1: `2025.1.3` still calls the Boot 4.0 `ApplicationEnvironmentPreparedEvent.getBootstrapContext()` signature and the JVM dies in `BootstrapApplicationListener` before config-server load.
4. Pin MOSIP artifacts (`kernel-core`, auth adapter, …) **explicitly**. Do not reintroduce `kernel-bom`.
5. Keep module packaging: `pre-registration-core` is a library (`spring-boot:repackage` off). The three deployables use `spring-boot-maven-plugin`.

---

## 3. Jakarta namespace

Boot 4 does not load `javax.servlet` / `javax.persistence` APIs.

1. In production and test Java, replace:
   - `javax.servlet` → `jakarta.servlet`
   - `javax.persistence` → `jakarta.persistence`
   - `javax.annotation.PostConstruct` / `Resource` → `jakarta.annotation.*`
   - `javax.transaction.Transactional` on repositories → `jakarta.transaction.Transactional` (or Spring’s `org.springframework.transaction.annotation.Transactional`)
2. Do not mix `javax.*` and `jakarta.*` on the same type.
3. Hibernate still reads MOSIP’s `javax_persistence.jdbc.*` property names through `HibernateDaoConfig`. Those **keys** stay; only Java packages moved.

---

## 4. Jackson 2 on Boot 4

Boot 4’s web starter pulls Jackson 3. MOSIP `kernel-core` and this code still use Jackson 2 (`ObjectMapper`, `JavaTimeModule`, afterburner).

1. Add `org.springframework.boot:spring-boot-jackson2` to every deployable that serializes JSON (`pre-registration-service`, `pre-registration-batchjob`, `pridgenerator-service`, and `pre-registration-core` if it builds mappers).
2. Keep explicit Jackson 2 artifacts where needed (`jackson-databind`, `jackson-annotations`, `jackson-module-afterburner`).
3. Call `io.mosip.kernel.core.util.DateUtils2` (not the old `DateUtils`) so date helpers match Jackson 2 + `kernel-core`.

If Jackson 3 types leak onto the compile classpath, `ObjectMapper` construction fails at runtime with method-not-found errors. Prefer `spring-boot-jackson2` over fighting the default.

---

## 5. Component scan (Boot 4 auto-config)

`kernel-core` ships `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` with `@Component` ID-generator/validator types this JVM does not provide repositories for. Boot 4 instantiates those unless they are excluded.

1. Entry class: `PreRegistrationBootApplication`.
2. `scanBasePackageClasses = HttpModeScanConfiguration.class` so the default scan is not `io.mosip.*`.
3. `excludeName` lists unused kernel `@Component` types (VID/RID/UIN generators, PDF, websub, …).
4. `HttpModeScanConfiguration` scans only:
   - `io.mosip.preregistration`
   - `io.mosip.analytics`
   - `io.mosip.kernel.idobjectvalidator`
   - `io.mosip.kernel.virusscanner`
   - `io.mosip.commons.khazana`
   - `${mosip.auth.adapter.impl.basepackage}`
   - `${mosip.lang.traslate.adapter.impl.basepackage}`
5. Import `HibernateDaoConfig` for the DataSource. Do not scan `io.mosip.kernel` as a whole.
6. Batch job packages stay out of the HTTP scan.

Virus-scanner / ID-object-validator jars on `-Dloader.path` are not Boot 4 auto-configs (`spring.factories` is ignored). They are picked up because those packages are in the scan list.

---

## 6. Tests

1. Replace `@MockBean` with `@MockitoBean` (`org.springframework.test.context.bean.override.mockito.MockitoBean`). Named beans keep `@MockitoBean(name = "selfTokenRestTemplate")`.
2. Controller tests that use `MockMvc` need `spring-boot-starter-webmvc-test` (Boot 4 split the web test starter).
3. `@SpringBootTest(classes = SomeUtil.class)` slices must mock every `@Autowired` collaborator, including new ones (for example `ApplicationRepostiory` after booking ownership).
4. Test `application.properties` may set `prereg.auth.jwt.secret=test-secret` directly. The main jar does not.

Wildcard `List<SimpleGrantedAuthority>` stubs against `getAuthorities()` need `Mockito.doReturn(...).when(user).getAuthorities()` (capture of `? extends GrantedAuthority`).

---

## 7. OpenAPI / Swagger UI

Boot 4 uses SpringDoc 3. Multiple `GroupedOpenApi` beans produced a **Select a definition** dropdown. MOSIP ID-Repository does not.

1. One `OpenAPI` `@Bean` in `PreregOpenApiConfig` (title, license, `Authorization` API-key header).
2. No `GroupedOpenApi` beans.
3. Do not set `openapi.service.servers[]`. SpringDoc fills the server from the request (**Generated server url**), so Try it out hits `127.0.0.1:9090` locally and the gateway host in cluster.
4. `bootstrap.properties`:
   - `springdoc.api-docs.path=/v3/api-docs`
   - `springdoc.swagger-ui.disable-swagger-default-url=true`
   - `springdoc.swagger-ui.path=/swagger-ui.html`
5. UI: `http://127.0.0.1:9090/preregistration/v1/swagger-ui/index.html`
6. Spec: `http://127.0.0.1:9090/preregistration/v1/v3/api-docs`

Restart the HTTP JVM after this change. `/v3/api-docs/swagger-config` must **not** list `urls` groups.

---

## 8. Login JWT (Boot app, not a Boot API change)

OTP tokens are signed in `LoginService` (JJWT HS256).

1. Main `application.properties` has **no** secret constant:

```properties
prereg.auth.jwt.secret=${prereg.auth.jwt.secret.override}
```

1. Local config-server (`pre-registration-default.properties`) sets `prereg.auth.jwt.secret.override=…`.
2. Cluster must set `prereg.auth.jwt.secret.override` (or replace `prereg.auth.jwt.secret`). Missing override → the process does not start.
3. `LoginService.rejectPublishedJwtSecret()` (`@PostConstruct`) rejects a blank secret and the published MOSIP sample `Yn2kjibddFAWtnPJ2AFlL8WXmohJMCvigQggaEypa5E=`.
4. OTP roles: `prereg.auth.jwt.token.roles=INDIVIDUAL` only. Staff roles come from Keycloak, not from OTP.

---

## 9. Security filters that interact with Boot Security

- HTTP service: `mosip.auth.filter_disable=false`.
- Native booking (`BookingController` `/appointment/**`) calls `BookingServiceUtil.assertAppointmentOwner` so `ROLE_INDIVIDUAL` cannot book/cancel another user’s PRID.
- PRID Vert.x (`GET /v1/pridgenerator/prid`): when `mosip.kernel.prid.fetch.secret` is set, require header `X-PRID-Fetch-Secret`. Local profile may bind `server.address=127.0.0.1`. `DemographicServiceUtil.generateId()` sends that header.

These are application beans, not Spring Boot version migrations, but they run on the Boot 4 security filter chain (`@PreAuthorize` + kernel-auth-adapter).

---

## 10. Verify

From `pre-registration/`:

```text
mvn clean test -Dmaven.javadoc.skip=true -Dgpg.skip=true
```

Then start the HTTP JVM (`./run-local.sh start` or `run-prereg-local.bat run`) and:

1. Log shows `Started PreRegistrationBootApplication` on **9090**.
2. `GET /preregistration/v1/actuator/health` is UP.
3. Swagger has **no** “Select a definition” dropdown.
4. Login OTP JWT `roles` claim is only `INDIVIDUAL`.
5. Process fails fast if `prereg.auth.jwt.secret.override` is unset and the published sample is used.

PowerShell: pass Maven `-D` flags after `--%`.

---

## 11. Do not

- Parent `kernel-bom` or a MOSIP Boot 2 parent.
- Scan `io.mosip.kernel` as a whole.
- Fold the batch job or PRID into the HTTP JVM.
- Put a JWT secret constant in `src/main/resources/application.properties`.
- Re-add `GroupedOpenApi` (brings the definition dropdown back).
- Switch this codebase to Jackson 3 until `kernel-core` does.
