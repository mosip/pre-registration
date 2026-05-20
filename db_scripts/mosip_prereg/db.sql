CREATE DATABASE :mosipdbname
	ENCODING = 'UTF8'
	LC_COLLATE = 'en_US.UTF-8'
	LC_CTYPE = 'en_US.UTF-8'
	TABLESPACE = pg_default
	OWNER = postgres
	TEMPLATE  = template0;
COMMENT ON DATABASE :mosipdbname IS 'Pre-registration database to store the data that is captured as part of pre-registration process';

\c :mosipdbname

DROP SCHEMA IF EXISTS prereg CASCADE;
CREATE SCHEMA prereg;
ALTER SCHEMA prereg OWNER TO postgres;
ALTER DATABASE :mosipdbname SET search_path TO prereg,pg_catalog,public;
