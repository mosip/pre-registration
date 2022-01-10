-- This table saves all the demographic details in the user’s application in an encrypted JSON format.

CREATE TABLE prereg.applicant_demographic(
	prereg_id character varying(36) NOT NULL,
	demog_detail bytea NOT NULL,
	demog_detail_hash character varying(64) NOT NULL,
	encrypted_dtimes timestamp NOT NULL,
	status_code character varying(36) NOT NULL,
	lang_code character varying(3) NOT NULL,
	cr_appuser_id character varying(256) NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	CONSTRAINT pk_appldem_prereg_id PRIMARY KEY (prereg_id)
);

CREATE INDEX IF NOT EXISTS idx_app_demo_cr_by ON prereg.applicant_demographic USING btree (cr_by);
CREATE INDEX IF NOT EXISTS idx_app_demo_prid ON prereg.applicant_demographic USING btree (prereg_id);
COMMENT ON TABLE prereg.applicant_demographic IS 'Applicant Demographic: Stores demographic details of an applicant. The demographic information is stored in json format.';
COMMENT ON COLUMN prereg.applicant_demographic.prereg_id IS 'Pre Registration ID: Unique Id generated for an individual during the pre-registration process which will be referenced during registration process at a registration center.';
COMMENT ON COLUMN prereg.applicant_demographic.demog_detail IS 'Demographic Detail: Demographic details of an individual, stored in json format.';
COMMENT ON COLUMN prereg.applicant_demographic.demog_detail_hash IS 'Demographic Detail Hash: Hash value of the demographic details stored in json format in a separate column. This will be used to make sure that nobody has tampered the data.';
COMMENT ON COLUMN prereg.applicant_demographic.encrypted_dtimes IS 'Encrypted Data Time: Date and time when the data was encrypted. This will also be used  get the key for decrypting the data.';
COMMENT ON COLUMN prereg.applicant_demographic.status_code IS 'Status Code: Status of the pre-registration application. The application can be in draft / pending state or submitted state';
COMMENT ON COLUMN prereg.applicant_demographic.lang_code IS 'Language Code : For multilanguage implementation this attribute Refers master.language.code. The value of some of the attributes in current record is stored in this respective language.';
COMMENT ON COLUMN prereg.applicant_demographic.cr_appuser_id IS 'Applciation Created User Id: User ID of the individual who is submitting the pre-registration application. It can be for self or for others like family members.';
COMMENT ON COLUMN prereg.applicant_demographic.cr_by IS 'Created By : ID or name of the user who create / insert record.';
COMMENT ON COLUMN prereg.applicant_demographic.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
COMMENT ON COLUMN prereg.applicant_demographic.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
COMMENT ON COLUMN prereg.applicant_demographic.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';

