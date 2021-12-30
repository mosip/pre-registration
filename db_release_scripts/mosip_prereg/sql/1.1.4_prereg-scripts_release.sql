

\c mosip_prereg sysadmin

ALTER TABLE prereg.applicant_document ADD COLUMN IF NOT EXISTS doc_ref_id character varying;




ALTER TABLE prereg.applicant_document_consumed ADD COLUMN IF NOT EXISTS doc_ref_id character varying;

\ir ../ddl/prereg-otp_transaction.sql

