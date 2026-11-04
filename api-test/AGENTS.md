# api-test

REST Assured/TestNG for frozen `/preregistration/v1`. How-to: README.md.

```text
run-local-smoke.bat|sh
config/prereg-local.properties    (never edit prereg.properties)
preReg/<Api>/<Api>.yml+.hbs
```

- Contract change → YAML + HBS. Local IAM = WireMock `:8082`. `skipPartnerSetup` = no Keycloak.
- OTP: `ws://localhost:8081` + notifier `:8083`.
- No secrets in git. Copy `run-local.env.example` → `.env.local` only to override.
