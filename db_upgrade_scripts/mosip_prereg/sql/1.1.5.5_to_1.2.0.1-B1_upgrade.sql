\c mosip_prereg

REASSIGN OWNED BY sysadmin TO postgres;

REVOKE ALL PRIVILEGES ON ALL TABLES IN SCHEMA prereg FROM prereguser;

REVOKE ALL PRIVILEGES ON ALL TABLES IN SCHEMA prereg FROM sysadmin;

GRANT SELECT, INSERT, TRUNCATE, REFERENCES, UPDATE, DELETE ON ALL TABLES IN SCHEMA prereg TO prereguser;

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA prereg TO postgres;

\ir ../ddl/prereg-applications.sql

\ir  ../ddl/prereg-anonymous_profile.sql

ALTER TABLE prereg.reg_appointment DROP CONSTRAINT IF EXISTS fk_rappmnt_id CASCADE;

CREATE INDEX IF NOT EXISTS idx_app_demo_cr_by ON prereg.applicant_demographic USING btree (cr_by COLLATE pg_catalog."default" ASC NULLS LAST) TABLESPACE pg_default;
CREATE INDEX IF NOT EXISTS idx_app_demo_prid ON prereg.applicant_demographic USING btree (prereg_id COLLATE pg_catalog."default" ASC NULLS LAST) TABLESPACE pg_default;

ALTER TABLE prereg.prid_seq RENAME TO prid_seq_to_be_deleted;
ALTER TABLE prereg.transaction_type RENAME TO transaction_type_to_be_deleted;
ALTER TABLE prereg.language_transliteration RENAME TO language_transliteration_to_be_deleted;
ALTER TABLE prereg.prid_seed RENAME TO prid_seed_to_be_deleted;
ALTER TABLE prereg.pre_registration_transaction RENAME TO pre_registration_transaction_to_be_deleted;

INSERT INTO prereg.applications(application_id, booking_type, booking_status_code, regcntr_id, appointment_date, booking_date,
   slot_from_time, slot_to_time, contact_info, cr_by, cr_dtimes, upd_by, upd_dtimes, application_status_code)
   Select t1.prereg_id, 'NEW_PREREGISTRATION', t1.status_code, t2.regcntr_id, t2.appointment_date,
   t2.booking_dtimes, t2.slot_from_time, t2.slot_to_time, t1.cr_appuser_id, t1.cr_by, t1.cr_dtimes, t1.upd_by, t1.upd_dtimes,
   Case When t1.status_code='Application_Incomplete' THEN 'DRAFT' Else 'SUBMITTED' End
   From prereg.applicant_demographic t1
   LEFT Join prereg.reg_appointment t2 On t1.prereg_id=t2.prereg_id;

