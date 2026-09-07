\c mosip_prereg
-- Rollback script from 1.3.1 to 1.3.0
-- ------------------------------------------------------------------------------------------
-- Removes the canonical user registry table (prereg.user_details) introduced in 1.3.1.
--
-- !! ONE-WAY MIGRATION — READ BEFORE RUNNING !!
-- The 1.3.1 identity migration rewrites cr_by / upd_by / cr_appuser_id / contact_info across
-- applications, applicant_demographic, applicant_document and reg_appointment from the raw
-- identifier to a surrogate UUID. The ONLY reverse mapping (UUID -> encrypted original identifier)
-- lives in prereg.user_details. Dropping this table therefore ORPHANS every migrated UUID: the
-- raw identifier can no longer be recovered, and an older (pre-1.3.1) application build — which
-- compares the raw auth userId directly against these columns — will fail all ownership checks,
-- silently hiding applicants' own records from them.
--
-- Safe to run ONLY if the identity migration has not yet populated any ownership column with a
-- UUID (i.e. immediately after upgrade, before any login/create/book/update traffic). If migrated
-- data exists, do NOT roll back with this script alone — the UUID columns must first be reverted
-- to their raw values (requires decrypting identifier_encrypted from user_details BEFORE this drop).
-- WARNING: This will permanently delete all surrogate UUID mappings stored in this table.
-- ------------------------------------------------------------------------------------------

DROP INDEX IF EXISTS prereg.idx_user_details_encrypted_dtimes;
DROP INDEX IF EXISTS prereg.idx_user_details_cr_dtimes;

DROP TABLE IF EXISTS prereg.user_details;
