# local-dev-setup

Docker deps; host app :9090. How-to: README.md. Detail: LOCAL-DEV-SETUP.md.

```text
5455 postgres   51100 config   8082 WireMock   8088 keymanager
8080 mock-smtp UI   8025 SMTP   8081 WS   8083 notifier   9000 MinIO
9090 host HTTP (not in Compose)
```

`run-prereg-local.bat|sh up` then `start`/`run`. Local OTP is always `111111`. Mailbox `:8080` is live-only and often shows `Invalid Date`.

- Edit this tree + `application-local.properties` only. Mapping edit → recreate `mock-service`.
- Never: HTTP app in Compose; `mosip.auth.filter_disable=true` as default; kernel-auth-adapter in `additional_jars`.
- `:9090` down → host app not started. OTP missing → mailbox closed or notifier `:8083` down.
