# Local PKCS12 keystore (Pre-Registration)

Key Manager uses **PKCS12** under `keys/` (not SoftHSM). `mosip-prereg-ks.p12` stays paired with the static key seed inlined in `deps/init.sql`.

## Required for keymanager-service

| File | Password | Purpose |
|------|----------|---------|
| `mosip-prereg-ks.p12` | `1234` | HSM substitute for local keymanager |

**Keep this PKCS12 across a database wipe.** Regenerating it breaks decrypt of seeded `key_store` rows.

`deps/init.sql` already has `PRE_REGISTRATION` / `INDIVIDUAL` (the reference `CryptoUtil` uses) and `KERNEL` / `SIGN` (datasync `jwtSign`). Bootstrap only verifies those certificates.

Config: `mosip.kernel.keymanager.hsm.keystore-pass=1234` in `deps/mosip-config/kernel-default.properties`.

`.km-local-ready` is written by the container at startup and is gitignored.
