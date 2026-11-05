# MOSIP Pre-Registration

JDK 21. One HTTP JVM :9090 (application + datasync + captcha + booking). Batchjob :9097. PRID Vert.x :8100.
Load only the `AGENTS.md` of the folder you edit. How-to = README — do not paste it.
Grep/Glob before Read. Read the smallest slice that answers.
Skip: `target/` `.local/` `testng-report/` `*.p12` `ui/node_modules/` `ui/dist/`.
No recap, no tree dumps, no artifact versions in AGENTS.md. No `.cursor/` rules.

```text
.
├─ pre-registration/                 Maven parent
│  ├─ pre-registration-core/         library
│  ├─ pre-registration-service/      HTTP :9090
│  ├─ pre-registration-batchjob/     Job :9097
│  ├─ pridgenerator-service/         PRID :8100 /v1/pridgenerator
│  └─ local-dev-setup/               Docker deps; app on host
├─ ui/                  Angular 7 SPA :4200
├─ db_scripts/          mosip_prereg + mosip_kernel.prid (do not merge)
├─ helm/ deploy/ api-test/
```

Frozen `/preregistration/v1` on **9090**: `/applications` `/documents` `/login` `/notification` `/proxy` `/appointment/**` `/applications/appointment/**` `/sync` `/syncV2` `/captcha/validatecaptcha`.
PRID stays separate: `GET /v1/pridgenerator/prid`. Booking = `io.mosip.preregistration.booking`.
Maven from `pre-registration/` only.
