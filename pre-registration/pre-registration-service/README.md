# pre-registration-service

Single HTTP deployable for MOSIP Pre-Registration. Hosts application, datasync, captcha, and booking APIs in one JVM.

- Port **9090**, context `/preregistration/v1`
- Booking-native: `/preregistration/v1/appointment/**` (imported from [mosip-ref-impl issue-1633](https://github.com/mosip/mosip-ref-impl/tree/issue-1633/pre-registration-booking-service) with git history)
- Captcha: `POST /preregistration/v1/captcha/validatecaptcha`
- Swagger (legacy catalog, Authorize header `Authorization`):
  - http://localhost:9090/preregistration/v1/application-service/swagger-ui.html
  - http://localhost:9090/preregistration/v1/sync/datasync-service/swagger-ui.html
  - http://localhost:9090/preregistration/v1/captcha-service/swagger-ui.html
  - http://localhost:9090/preregistration/v1/appointment/booking-service/swagger-ui.html

```text
./run-local.sh init | start | smoke | stop | test | all | docker
run-local.bat …
```

See [docs/design/service-consolidation.md](../../docs/design/service-consolidation.md) and [AGENTS.md](AGENTS.md).
