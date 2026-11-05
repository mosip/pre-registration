# Postman — Pre-Registration local APIs

Import into Postman:

1. [`Pre-Registration-Local.postman_collection.json`](Pre-Registration-Local.postman_collection.json)
2. [`Pre-Registration-Local.postman_environment.json`](Pre-Registration-Local.postman_environment.json)

Select environment **Pre-Registration-Local**.

## Prerequisites

```bash
cd pre-registration/local-dev-setup
./run-prereg-local.sh up      # or run-prereg-local.bat up
./run-prereg-local.sh start
```

Optional: PRID generator on `:8100` for folder **11 - PRID generator**.

Swagger (same APIs): http://localhost:9090/preregistration/v1/swagger-ui/index.html — Authorize uses header `Authorization`.

## Suggested order

1. **00 - Auth** → `Health (prereg)` then `Client token (prereg)` (saves `serviceToken` for datasync)
2. **01 - Login** → `Send OTP (langcode)` → set env `otp` to `111111` → `Validate OTP` (saves `accessToken`)
3. **03 - Demographic** → `Create Pre-Registration` (saves `preRegistrationId`)
4. **04 - Documents** → `Upload document` (attach a PDF)
5. **03** → `Update status` (`Pending_Appointment`)
6. **05 - Appointment** → `Get slot availability` then `Book appointment`
7. Other folders as needed. **Copy document** needs a second create (`Create Pre-Registration (copy destination)`).

Do not run `Delete Pre-Registration` / `Invalidate token` until you are finished with that PRID / session.

## Variables

| Variable | Default | Role |
|----------|---------|------|
| `preregBaseUrl` | `http://localhost:9090` | Host pre-registration-service |
| `bookingBaseUrl` | `http://localhost:9090` | Frozen `/preregistration/v1/appointment/**` |
| `wiremockBaseUrl` | `http://localhost:8082` | IAM |
| `pridBaseUrl` | `http://localhost:8100` | PRID generator (separate JVM) |
| `accessToken` | _(empty)_ | Applicant OTP JWT (cookie) |
| `serviceToken` | _(empty)_ | `mosip-prereg-client` (no INDIVIDUAL) |
| `loginUser` / `otp` | email / blank | Login |
| `preRegistrationId` / `documentId` | _(empty)_ | Filled by tests |
| `registrationCenterId` | `10003` | Local slot seed |

Applicant APIs send both `Authorization` and `Cookie: Authorization=…` (login cookie). Datasync uses `serviceToken`.

## Sources

- Controllers under `pre-registration-service` (`/preregistration/v1`)
- Frozen booking prefixes `/preregistration/v1/appointment/**` on `:9090`
- `api-test` YAML happy-path bodies
