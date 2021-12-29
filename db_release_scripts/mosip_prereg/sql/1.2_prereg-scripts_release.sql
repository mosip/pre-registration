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


CREATE INDEX IF NOT EXISTS idx_app_demo_cr_by ON prereg.applicant_demographic USING btree (cr_by);
CREATE INDEX IF NOT EXISTS idx_app_demo_prid ON prereg.applicant_demographic USING btree (prereg_id);

------------------------------------------------------------------------------------------------------
