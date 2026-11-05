# Local PKCS12 keystore (Pre-Registration)

Key Manager uses **PKCS12** under `keys/` (not SoftHSM). `mosip-prereg-ks.p12` is a copy of id-repository `keys/mosip-idrepo-ks.p12` so it stays paired with the static key seed inlined in `deps/init.sql`.

## Required for keymanager-service

| File | Password | Purpose |
|------|----------|---------|
| `mosip-prereg-ks.p12` | `1234` | HSM substitute. Same bytes as id-repository `mosip-idrepo-ks.p12` |

**Keep this PKCS12 across a database wipe.** Regenerating it breaks decrypt of seeded `key_store` rows.

Bootstrap calls `getCertificate` for `PRE_REGISTRATION` / `INDIVIDUAL` (the reference `CryptoUtil` uses). That creates the reference keypair on top of the base `PRE_REGISTRATION` key. `KERNEL` / `SIGN` is already in the seed (datasync `jwtSign`).

Config: `mosip.kernel.keymanager.hsm.keystore-pass=1234` in `deps/mosip-config/kernel-default.properties`.

`.km-local-ready` is written by the container at startup and is gitignored.
