-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_prereg
-- Release Version 	: 1.1.5
-- Purpose    		: Database Alter scripts for the release for Pre Registration DB.       
-- Create By   		: Ram Bhatt
-- Created Date		: Jan-2021
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -------------------------------------------------------------------------------------------------

\c mosip_prereg sysadmin

-----------------------------------------------------------------------------------------------------


ALTER TABLE prereg.intf_processed_prereg_list ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE prereg.processed_prereg_list ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE prereg.reg_available_slot ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE prereg.transaction_type ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE prereg.pre_registration_transaction ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE prereg.otp_transaction ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE prereg.prid_seed ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE prereg.prid_seq ALTER COLUMN is_deleted SET NOT NULL;

ALTER TABLE prereg.intf_processed_prereg_list ALTER COLUMN is_deleted SET DEFAULT FALSE;
ALTER TABLE prereg.processed_prereg_list ALTER COLUMN is_deleted SET DEFAULT FALSE;
ALTER TABLE prereg.reg_available_slot ALTER COLUMN is_deleted SET DEFAULT FALSE;
ALTER TABLE prereg.transaction_type ALTER COLUMN is_deleted SET DEFAULT FALSE;
ALTER TABLE prereg.pre_registration_transaction ALTER COLUMN is_deleted SET DEFAULT FALSE;
ALTER TABLE prereg.otp_transaction ALTER COLUMN is_deleted SET DEFAULT FALSE;
ALTER TABLE prereg.prid_seed ALTER COLUMN is_deleted SET DEFAULT FALSE;
ALTER TABLE prereg.prid_seq ALTER COLUMN is_deleted SET DEFAULT FALSE;


-----------------------------------------------------------------------------------------------------


