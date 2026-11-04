# Pre-registration Database

## Overview
This folder contains SQL scripts to create databases and tables in Postgres. Application tables live under `mosip_prereg/ddl/`. The PRID pool lives under `mosip_kernel/ddl/` (`kernel.prid` only; other kernel tables stay in mosip/commons). Default data that's populated in the tables is present under `<db name>/dml` folder.

These scripts are run as part of DB initialisation in [MOSIP deployment](https://docs.mosip.io/1.2.0/deploymentnew/v3-installation). Developers may run the SQLs using `<db name>/deploy.sh`.

Do not merge `mosip_prereg` with `mosip_kernel`.
