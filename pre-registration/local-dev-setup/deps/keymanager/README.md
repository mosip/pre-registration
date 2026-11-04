# Keymanager local assets (DDL + bootstrap)

| Path | Role |
|------|------|
| [`ddl/`](ddl/) | `mosip_keymgr` table DDL applied by `deps/init.sql` via `\i` |
| [`../init.sql`](../init.sql) | Single init: `mosip_prereg`, `mosip_keymgr`, and the static key seed |
| [`bootstrap.sh`](bootstrap.sh) | Container entrypoint: start KM, verify the PKCS12, create `PRE_REGISTRATION` / `INDIVIDUAL` |
| `keys-generator.jar` | Not used with default `KM_STATIC_SEED=true`. Gitignored optional fallback only |

`mosip_keymgr` is its own database. It is not merged into `mosip_prereg` or into `mosip_kernel` (PRID only).

## Default path

1. Postgres runs `deps/init.sql` (DDL + seed). The seed matches `keys/mosip-prereg-ks.p12` (password **`1234`**).
2. `bootstrap.sh` with `KM_STATIC_SEED=true` skips `keys-generator.jar`.
3. Verifies `getCertificate?applicationId=PRE_REGISTRATION` (empty reference id).
4. Calls `getCertificate` for `PRE_REGISTRATION` / `INDIVIDUAL`, which creates that keypair when the seed has only the base key. `generateMasterKey` returns `KER-KMS-010` for this pair. Then checks `KERNEL` / `SIGN`.

ZK `data_encrypt_keystore` rows from the id-repository dump are not loaded. Pre-Registration does not use them.

## Fallback (`KM_STATIC_SEED=false`)

Place `keys-generator.jar` in this folder (from `mosipid/keys-generator:1.3.0`). Bootstrap then runs the jar and `generateMasterKey` for `ROOT`, `KERNEL:SIGN`, and `PRE_REGISTRATION:INDIVIDUAL`.
