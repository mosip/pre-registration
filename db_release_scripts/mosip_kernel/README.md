# mosip_kernel release scripts (PRID only)

Mirrors [commons `db_release_scripts/mosip_kernel`](https://github.com/mosip/commons/tree/develop/db_release_scripts/mosip_kernel). Commons 1.1.0 / 1.1.4 / 1.2.0 alter UIN-assigned, VID-assigned, and key/sync tables — none of those belong to `pridgenerator-service`. The SQL files here are empty no-ops so release/revoke still have a file per version for `kernel.prid`.

```text
bash deploy.sh deploy.properties 1.2.0
bash revoke.sh deploy.properties 1.2.0
```
