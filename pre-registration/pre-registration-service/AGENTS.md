# pre-registration-service

HTTP :9090 context `/preregistration/v1`. Entry `PreRegistrationBootApplication`. How-to: README.md.

```text
application/   /applications /documents /login /notification /proxy  + /applications/appointment/**
booking/       /appointment/**   (issue-1633)
datasync/      /sync /syncV2
captcha/       /captcha/validatecaptcha
pipeline/      InProcessBookingClient / InProcessApplicationClient  (@Lazy, no HTTP)
```

- Thin controller + `@PreAuthorize`. Datasync IAM on `/sync/**`; resident roles on application paths. Do not collapse Keycloak clients.
- `spring.application.name=pre-registration-service`. Config `name=application,pre-registration`.
- Local: `./run-local.sh init|start|smoke|stop`. `-Dloader.path=local-dev-setup/deps/additional_jars`.
- HTTP out: IAM, masterdata, notification, keymanager. Not booking.
