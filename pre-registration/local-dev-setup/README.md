# Local Pre-Registration

Docker runs Postgres, config-server, WireMock, MinIO, keymanager (`:8088`), [mock-smtp-sms](https://github.com/mosip/mock-smtp-sms/tree/develop) (`:8080` UI/SMS, `:8025` SMTP, `:8081` websocket), and notifier (`:8083`). The HTTP service (application + datasync + captcha + booking) runs on the **host** JVM (port 9090). There is no HTTP-app container. Audit HTTP is the WireMock stub on `:8082`.

```bash
./run-local-stack.sh up       # also runs prep (ref-impl loader jars)
./run-prereg-local.sh run     # or run-prereg-local.bat on Windows cmd
```

Swagger: http://localhost:9090/preregistration/v1/swagger-ui/index.html — Authorize, header `Authorization`.

Postman (all local APIs): import [`postman/Pre-Registration-Local.postman_collection.json`](postman/Pre-Registration-Local.postman_collection.json) and [`postman/Pre-Registration-Local.postman_environment.json`](postman/Pre-Registration-Local.postman_environment.json). How-to: [`postman/README.md`](postman/README.md).

Logs: `.local/logs/` (`prereg-service.log` stdout, `spring.log` from `-Dlogging.file.path`). Use after pom or Java changes. Do **not** use `start` until that rebuild has finished.

`kernel-virusscanner-clamav.jar` on `-Dloader.path` is the ClamAV **client** (`VirusScannerImpl`). It talks to a ClamAV daemon at `mosip.kernel.virus-scanner.host` / `port` (`clamav.clamav:3310` in cluster config). This compose has no ClamAV container, so local mosip-config sets `mosip.preregistration.document.scan=false`. Leaving scan on returns `PRG_PAM_DOC_010` on every document upload. Cluster keeps scan enabled.

See `LOCAL-DEV-SETUP.md` and `AGENTS.md`.
