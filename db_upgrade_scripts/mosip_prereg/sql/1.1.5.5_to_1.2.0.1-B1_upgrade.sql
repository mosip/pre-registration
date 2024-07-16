\c mosip_prereg

REASSIGN OWNED BY sysadmin TO postgres;

REVOKE ALL PRIVILEGES ON ALL TABLES IN SCHEMA prereg FROM prereguser;

REVOKE ALL PRIVILEGES ON ALL TABLES IN SCHEMA prereg FROM sysadmin;

GRANT SELECT, INSERT, TRUNCATE, REFERENCES, UPDATE, DELETE ON ALL TABLES IN SCHEMA prereg TO prereguser;

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA prereg TO postgres;

CREATE TABLE IF NOT EXISTS prereg.applications(
	application_id character varying(36) NOT NULL,
	booking_type character varying(256) NOT NULL,
	booking_status_code character varying(256),
	application_status_code character varying(256),
	regcntr_id character varying(10),
	appointment_date date,
	booking_date date,
	slot_from_time time without time zone,
	slot_to_time time without time zone,
	contact_info character varying(256),
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp without time zone NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp without time zone,
	CONSTRAINT appid_pk PRIMARY KEY (application_id)
);

GRANT SELECT,INSERT,UPDATE,DELETE,REFERENCES ON prereg.applications TO prereguser;

CREATE TABLE IF NOT EXISTS prereg.anonymous_profile
(
    id character varying(36) NOT NULL,
    profile character varying NOT NULL,
    cr_by character varying(256) NOT NULL,
    cr_dtimes timestamp without time zone NOT NULL,
    upd_by character varying(256),
    upd_dtimes timestamp without time zone,
    is_deleted boolean,
    del_dtimes timestamp without time zone,
    CONSTRAINT anonymous_profile_pkey PRIMARY KEY (id)
);

GRANT SELECT,INSERT,UPDATE,DELETE,REFERENCES ON prereg.anonymous_profile TO prereguser;

ALTER TABLE prereg.reg_appointment DROP CONSTRAINT IF EXISTS fk_rappmnt_id CASCADE;

CREATE INDEX IF NOT EXISTS idx_app_demo_cr_by ON prereg.applicant_demographic USING btree (cr_by COLLATE pg_catalog."default" ASC NULLS LAST) TABLESPACE pg_default;
CREATE INDEX IF NOT EXISTS idx_app_demo_prid ON prereg.applicant_demographic USING btree (prereg_id COLLATE pg_catalog."default" ASC NULLS LAST) TABLESPACE pg_default;

ALTER TABLE prereg.prid_seq RENAME TO prid_seq_to_be_deleted;
ALTER TABLE prereg.transaction_type RENAME TO transaction_type_to_be_deleted;
ALTER TABLE prereg.language_transliteration RENAME TO language_transliteration_to_be_deleted;
ALTER TABLE prereg.prid_seed RENAME TO prid_seed_to_be_deleted;
ALTER TABLE prereg.pre_registration_transaction RENAME TO pre_registration_transaction_to_be_deleted;
ALTER TABLE prereg.processed_prereg_list DROP CONSTRAINT IF EXISTS pprlst_pregtrn_fk CASCADE;

INSERT INTO prereg.applications(application_id, booking_type, booking_status_code, regcntr_id, appointment_date, booking_date,
   slot_from_time, slot_to_time, contact_info, cr_by, cr_dtimes, upd_by, upd_dtimes, application_status_code)
   Select t1.prereg_id, 'NEW_PREREGISTRATION', t1.status_code, t2.regcntr_id, t2.appointment_date,
   t2.booking_dtimes, t2.slot_from_time, t2.slot_to_time, t1.cr_appuser_id, t1.cr_by, t1.cr_dtimes, t1.upd_by, t1.upd_dtimes,
   Case When t1.status_code='Application_Incomplete' THEN 'DRAFT' Else 'SUBMITTED' End
   From prereg.applicant_demographic t1
   LEFT Join prereg.reg_appointment t2 On t1.prereg_id=t2.prereg_id;

