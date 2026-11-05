# ui

Angular 7 SPA. Backend :9090 `/preregistration/v1`. How-to: README.md.

```text
run-local.bat|sh              HTTP ng serve :4200 + /proxyapi → :9090
src/assets/config.json        BASE_URL=http://127.0.0.1:4200/proxyapi/  (do not edit for laptop)
.local/node_modules           junctioned as node_modules
src/app/app.constants.ts      paths, config keys, error codes
```

- Open http://127.0.0.1:4200. Node 17+ needs `--openssl-legacy-provider` (`run-local` sets it). Ignore `npm audit`.
- New API path → `app.constants.ts`. Do not hardcode URLs.
- OTP is always `111111` locally (WireMock). Mailbox `:8080` often shows `Invalid Date` — ignore it.
- Skip `node_modules/` `dist/` `.local/`.
