CREATE DATABASE mosip_prereg
	ENCODING = 'UTF8'
	LC_COLLATE = 'en_US.UTF-8'
	LC_CTYPE = 'en_US.UTF-8'
	TABLESPACE = pg_default
	OWNER = postgres
	TEMPLATE  = template0;
COMMENT ON DATABASE mosip_prereg IS 'Pre-registration database to store the data that is captured as part of pre-registration process';

\c mosip_prereg 

DROP SCHEMA IF EXISTS prereg CASCADE;
CREATE SCHEMA prereg;
ALTER SCHEMA prereg OWNER TO postgres;
ALTER DATABASE mosip_prereg SET search_path TO prereg,pg_catalog,public;
