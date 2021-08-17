-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_prereg
-- Release Version 	: 1.2
-- Purpose    		: Database Alter scripts for the release for Pre Registration DB.       
-- Create By   		: Ram Bhatt
-- Created Date		: Aug-2021
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -------------------------------------------------------------------------------------------------

\c mosip_prereg sysadmin


\ir ../ddl/prereg-applications.sql

ALTER TABLE prereg.reg_appointment DROP CONSTRAINT IF EXISTS fk_rappmnt_id CASCADE;

-----------------------------------------------------------------------------------------------------
