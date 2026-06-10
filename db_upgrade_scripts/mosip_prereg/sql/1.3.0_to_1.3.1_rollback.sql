\c mosip_prereg
-- Rollback script from 1.3.1 to 1.3.0
-- ------------------------------------------------------------------------------------------
-- Removes the canonical user registry table (prereg.user_details) introduced in 1.3.1.
-- WARNING: This will permanently delete all surrogate UUID mappings stored in this table.
-- ------------------------------------------------------------------------------------------

DROP INDEX IF EXISTS prereg.idx_user_details_encrypted_dtimes;
DROP INDEX IF EXISTS prereg.idx_user_details_cr_dtimes;

DROP TABLE IF EXISTS prereg.user_details;
