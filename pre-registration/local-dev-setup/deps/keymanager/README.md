# Keymanager local assets (DDL + bootstrap)

| Path | Role |
|------|------|
| [`ddl/`](ddl/) | `mosip_keymgr` table DDL applied by `deps/init.sql` via `\i` |
| [`../init.sql`](../init.sql) | Single init: `mosip_prereg`, `mosip_keymgr`, aliases, key_store, `data_encrypt_keystore` |
| [`bootstrap.sh`](bootstrap.sh) | Container entrypoint: start KM and verify SQL seed certificates |

`mosip_keymgr` is its own database. It is not merged into `mosip_prereg` or into `mosip_kernel` (PRID only).

Crypto rows are inlined in `deps/init.sql` (aliases, key_store, `PRE_REGISTRATION` / `INDIVIDUAL`). `keys-generator.jar` is not used. Keep `keys/mosip-prereg-ks.p12` (password **`1234`**) matched to that seed.
