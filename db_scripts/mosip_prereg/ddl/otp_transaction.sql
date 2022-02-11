-- This is used to save the OTP for the user whenever user requests for one using the email id / phone number to log into the application.

CREATE TABLE prereg.otp_transaction(
	id character varying(36) NOT NULL,
	ref_id character varying(64) NOT NULL,
	otp_hash character varying(512) NOT NULL,
	generated_dtimes timestamp,
	expiry_dtimes timestamp,
	validation_retry_count smallint,
	status_code character varying(36),
	lang_code character varying(3),
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean,
	del_dtimes timestamp,
	CONSTRAINT pk_otpt_id PRIMARY KEY (id)
);

COMMENT ON TABLE prereg.otp_transaction IS 'All OTP related data and validation details are maintained here for Pre Registration module.';
COMMENT ON COLUMN prereg.otp_transaction.id IS 'OTP id is a unique identifier (UUID) used as an unique key to identify the OTP transaction';
COMMENT ON COLUMN prereg.otp_transaction.ref_id IS 'Reference ID is a reference information received from OTP requester which can be used while validating the OTP. AM: please give examples of ref_id';
COMMENT ON COLUMN prereg.otp_transaction.otp_hash IS 'Hash of id, ref_id and otp which is generated based on the configuration setup and sent to the requester application / module.';
COMMENT ON COLUMN prereg.otp_transaction.generated_dtimes IS 'Date and Time when the OTP was generated';
COMMENT ON COLUMN prereg.otp_transaction.expiry_dtimes IS 'Date Time when the OTP will be expired';
COMMENT ON COLUMN prereg.otp_transaction.validation_retry_count IS 'Validation retry counts of this OTP request. If the validation retry crosses the threshold limit, then the OTP will be de-activated.';
COMMENT ON COLUMN prereg.otp_transaction.status_code IS 'Current status of the transaction. Refers to code field of master.status_list table.';
COMMENT ON COLUMN prereg.otp_transaction.lang_code IS 'For multilanguage implementation this attribute Refers master.language.code. The value of some of the attributes in current record is stored in this respective language.';
COMMENT ON COLUMN prereg.otp_transaction.cr_by IS 'ID or name of the user who create / insert record.';
COMMENT ON COLUMN prereg.otp_transaction.cr_dtimes IS 'Date and Timestamp when the record is created/inserted';
COMMENT ON COLUMN prereg.otp_transaction.upd_by IS 'ID or name of the user who update the record with new values';
COMMENT ON COLUMN prereg.otp_transaction.upd_dtimes IS 'Date and Timestamp when any of the fields in the record is updated with new values.';
COMMENT ON COLUMN prereg.otp_transaction.is_deleted IS 'Flag to mark whether the record is Soft deleted.';
COMMENT ON COLUMN prereg.otp_transaction.del_dtimes IS 'Date and Timestamp when the record is soft deleted with is_deleted=TRUE';
