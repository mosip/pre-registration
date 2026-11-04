# mosip_kernel (PRID pool only)

`pridgenerator-service` stores pre-generated PRIDs in `kernel.prid` in database `mosip_kernel`. This folder ships **that table only**, matching [commons `db_scripts/mosip_kernel/ddl/kernel-prid.sql`](https://github.com/mosip/commons/blob/develop/db_scripts/mosip_kernel/ddl/kernel-prid.sql). UIN, VID, OTP, `uin_assigned`, `vid_assigned`, and other kernel tables stay in [mosip/commons](https://github.com/mosip/commons/tree/develop/db_scripts).

Do not merge this schema into `mosip_prereg`.

If `mosip_kernel` already exists in the cluster (commons install), run only `ddl/kernel-prid.sql` against it. Fresh install: `deploy.sh deploy.properties` after setting `DB_SERVERIP` and passwords.
