-- Idempotent apply for a Postgres volume that already ran an older init.sql.
-- Fresh volumes use deps/init.sql instead. Mounted at /audit in the postgres container.

DO $$
BEGIN
	IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'audituser') THEN
		CREATE ROLE audituser WITH LOGIN PASSWORD 'mosip123';
	END IF;
END
$$;

SELECT 'CREATE DATABASE mosip_audit ENCODING = ''UTF8'' TABLESPACE = pg_default OWNER = postgres TEMPLATE = template0'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'mosip_audit')\gexec

\c mosip_audit

CREATE SCHEMA IF NOT EXISTS audit;
ALTER SCHEMA audit OWNER TO postgres;
ALTER DATABASE mosip_audit SET search_path TO audit,pg_catalog,public;
SET search_path TO audit,pg_catalog,public;
GRANT CONNECT ON DATABASE mosip_audit TO audituser;
GRANT USAGE ON SCHEMA audit TO audituser;
ALTER DEFAULT PRIVILEGES IN SCHEMA audit GRANT SELECT,INSERT,UPDATE,DELETE,REFERENCES ON TABLES TO audituser;

\i /audit/ddl/audit-app_audit_log.sql

GRANT SELECT,INSERT,UPDATE,DELETE,TRUNCATE,REFERENCES ON ALL TABLES IN SCHEMA audit TO audituser;
COMMENT ON DATABASE mosip_audit IS 'Audit related logs and the data is stored in this database';
