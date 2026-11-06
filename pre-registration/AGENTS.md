# pre-registration (Maven parent)

Boot 4.1.1, Java 21, no kernel-bom. How-to: README.md.

```text
core/                    library (no repackage)
service/                 HTTP :9090  application|booking|datasync|captcha
batchjob/                Job :9097
pridgenerator-service/   Vert.x :8100
```

- New REST = thin controller + `@PreAuthorize`. Shared types in core.
- Scan via `HttpModeScanConfiguration`. Do not scan `io.mosip.*` wholesale.
- Do not reintroduce kernel-bom. Pin MOSIP artifacts in parent `dependencyManagement`.
- Captcha prefix `/captcha` via `PreregApiPathConfig`. Frozen URLs stay.
- Datasync/booking/notification in-process. No HTTP-loopback to this JVM.
- Sonar: no DTO getter tests. Raise service/controller coverage.
