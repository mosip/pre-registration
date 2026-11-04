# mosip_kernel upgrades (PRID only)

Hop names match [commons `db_upgrade_scripts/mosip_kernel/sql`](https://github.com/mosip/commons/tree/develop/db_upgrade_scripts/mosip_kernel/sql).

`kernel.prid` (used by `pridgenerator-service`) only changes on **1.1.5.5 → 1.2.0.1-B1** (`idx_prid_status`). Every other hop is an empty no-op so `upgrade.sh` still finds `sql/${CURRENT_VERSION}_to_${UPGRADE_VERSION}_*.sql`. UIN/VID/OTP upgrades stay in commons. Fresh DDL already includes the index.

```text
./upgrade.sh upgrade.properties
```
