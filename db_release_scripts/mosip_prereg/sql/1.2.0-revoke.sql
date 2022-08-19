-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_prereg
-- Release Version 	: 1.2.0
-- Purpose    		: Revoking Database Alter deployment done for release in Pre registration DB.
-- Create By   		: Kamesh Shekhar Prasad
-- Created Date		: Aug-2022
--
-- Modified Date        Modified By         Comments / Remarks
-- -------------------------------------------------------------------------------------------------

\c mosip_prereg sysadmin

DROP TABLE IF EXISTS prereg-applications;
DROP TABLE IF EXISTS prereg-anonymous_profile;

ALTER TABLE prereg.reg_appointment ADD CONSTRAINT fk_rappmnt_id FOREIGN KEY (prereg_id)
REFERENCES prereg.applicant_demographic(prereg_id) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;