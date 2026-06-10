\c mosip_prereg
-- Upgrade script from 1.3.0 to 1.3.1
-- ------------------------------------------------------------------------------------------
-- Introduces canonical user registry table (prereg.user_details) for PII security.
-- Eliminates plaintext PII replication across cr_by/upd_by/cr_appuser_id fields by
-- storing a hash->UUID surrogate mapping with encrypted original for audit/notifications.
-- ------------------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS prereg.user_details (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    identifier_hash VARCHAR(128) NOT NULL UNIQUE,
    identifier_encrypted TEXT NOT NULL,
    cr_dtimes TIMESTAMP NOT NULL,
    encrypted_dtimes TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_user_details_cr_dtimes
  ON prereg.user_details (cr_dtimes);

CREATE INDEX IF NOT EXISTS idx_user_details_encrypted_dtimes
  ON prereg.user_details (encrypted_dtimes);

GRANT SELECT, INSERT, UPDATE, DELETE, REFERENCES ON prereg.user_details TO prereguser;
