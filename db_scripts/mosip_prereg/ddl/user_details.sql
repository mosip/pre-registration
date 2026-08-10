-- ================================================================================================
-- MOSIP Pre-Registration PII Security: Canonical User Registry Table
-- Purpose: Centralized surrogate user ID mapping for cr_by/upd_by/cr_appuser_id
-- ================================================================================================
-- This table eliminates plaintext PII replication across the tables
-- Stores: hash(authUserId) -> UUID surrogate mapping + encrypted original for notifications/audit
-- Usage:
--   1. Resolve: hash(authUserId) -> user_id (fast lookup)
--   2. Store: user_id in cr_by/upd_by/cr_appuser_id/contact_info fields(instead of plaintext)
--   3. Recover: decrypt from user_details for notifications/audit

-- ========== CREATE TABLE: Canonical User Registry ==========

CREATE TABLE IF NOT EXISTS prereg.user_details (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    identifier_hash VARCHAR(128) NOT NULL UNIQUE,
    identifier_encrypted TEXT NOT NULL,
    cr_dtimes TIMESTAMP NOT NULL,
    encrypted_dtimes TIMESTAMP NOT NULL
);


-- ========== CREATE INDEXES ==========

CREATE INDEX IF NOT EXISTS idx_user_details_cr_dtimes
  ON prereg.user_details (cr_dtimes);

CREATE INDEX IF NOT EXISTS idx_user_details_encrypted_dtimes
  ON prereg.user_details (encrypted_dtimes);

-- ================================================================================================