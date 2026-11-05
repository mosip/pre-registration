# Local audit DB

Laptop copy of [audit-manager issue-1633 `db_scripts`](https://github.com/mosip/audit-manager/tree/issue-1633/db_scripts). Creates `mosip_audit` / schema `audit` / table `app_audit_log` so api-test `db:mosip_audit` can connect. Audit HTTP is the WireMock stub `POST /v1/auditmanager/audits` in `deps/wiremock/mappings/prereg.json`.

| Path | Role |
|------|------|
| [`ddl/audit-app_audit_log.sql`](ddl/audit-app_audit_log.sql) | Upstream `app_audit_log` DDL |
| [`../init.sql`](../init.sql) | Fresh volume: role `audituser`, database `mosip_audit`, `\i` this DDL |
| [`apply-existing.sql`](apply-existing.sql) | Existing volume that already ran an older init (no wipe) |

`mosip_audit` is its own database. It is not merged into `mosip_prereg` or `mosip_kernel`. Host `audit.url` is `http://localhost:8082/v1/auditmanager/audits`. After changing `init.sql` on a volume that already initialized, either `wipe` or run `apply-existing.sql` inside the postgres container.
