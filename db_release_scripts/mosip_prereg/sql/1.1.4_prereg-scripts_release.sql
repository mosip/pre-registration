-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_prereg
-- Release Version 	: 1.1.4
-- Purpose    		: Database Alter scripts for the release for Pre Registration DB.       
-- Create By   		: Sadanandegowda DM
-- Created Date		: Dec-2020
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -------------------------------------------------------------------------------------------------

\c mosip_prereg sysadmin

ALTER TABLE prereg.applicant_document ADD COLUMN IF NOT EXISTS doc_ref_id character varying;




ALTER TABLE prereg.applicant_document_consumed ADD COLUMN IF NOT EXISTS doc_ref_id character varying;

\ir ../ddl/prereg-otp_transaction.sql

