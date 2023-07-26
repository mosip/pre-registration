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

COMMENT ON TABLE prereg.applicant_demographic_consumed IS 'Applicant Demographic Consumed: Stores demographic details of an applicant that was comsumed.';
COMMENT ON COLUMN prereg.applicant_demographic_consumed.prereg_id IS 'Pre Registration ID: Unique Id generated for an individual during the pre-registration process which will be referenced during registration process at a registration center.';
COMMENT ON COLUMN prereg.applicant_demographic_consumed.demog_detail IS 'Demographic Detail: Demographic details of an individual, stored in json format.';
COMMENT ON COLUMN prereg.applicant_demographic_consumed.demog_detail_hash IS 'Demographic Detail Hash: Hash value of the demographic details stored in json format in a separate column. This will be used to make sure that nobody has tampered the data.';
COMMENT ON COLUMN prereg.applicant_demographic_consumed.encrypted_dtimes IS 'Encrypted Data Time: Date and time when the data was encrypted. This will also be used  get the key for decrypting the data.';
COMMENT ON COLUMN prereg.applicant_demographic_consumed.status_code IS 'Status Code: Status of the pre-registration application. The application can be in draft / pending state or submitted state';
COMMENT ON COLUMN prereg.applicant_demographic_consumed.lang_code IS 'Language Code : For multilanguage implementation this attribute Refers master.language.code. The value of some of the attributes in current record is stored in this respective language.';
COMMENT ON COLUMN prereg.applicant_demographic_consumed.cr_appuser_id IS 'Applciation Created User Id: User ID of the individual who is submitting the pre-registration application. It can be for self or for others like family members.';
COMMENT ON COLUMN prereg.applicant_demographic_consumed.cr_by IS 'Created By : ID or name of the user who create / insert record.';
COMMENT ON COLUMN prereg.applicant_demographic_consumed.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
COMMENT ON COLUMN prereg.applicant_demographic_consumed.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
COMMENT ON COLUMN prereg.applicant_demographic_consumed.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';

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

COMMENT ON TABLE prereg.applicant_document_consumed IS 'Documents that are uploaded as part of pre-registration process which was consumed is maintained here. ';
COMMENT ON COLUMN prereg.applicant_document_consumed.id IS 'Unique id generated for the documents being uploaded as part of pre-registration process.';
COMMENT ON COLUMN prereg.applicant_document_consumed.prereg_id IS 'Id of the pre-registration application for which the documents are being uploaded.';
COMMENT ON COLUMN prereg.applicant_document_consumed.doc_name IS 'Name of the document that is uploaded';
COMMENT ON COLUMN prereg.applicant_document_consumed.doc_cat_code IS 'Document category code under which the document is being uploaded. Refers to master.document_category.code';
COMMENT ON COLUMN prereg.applicant_document_consumed.doc_typ_code IS 'Document type code under which the document is being uploaded. Refers to master.document_type.code';
COMMENT ON COLUMN prereg.applicant_document_consumed.doc_file_format IS 'Format in which the document is being uploaded. Refers to master.document_file_format.code';
COMMENT ON COLUMN prereg.applicant_document_consumed.doc_id IS 'ID of the document being uploaded';
COMMENT ON COLUMN prereg.applicant_document_consumed.doc_hash IS 'Hash value of the document being uploaded in document store. This will be used to make sure that nobody has tampered the document stored in a separate store. ';
COMMENT ON COLUMN prereg.applicant_document_consumed.doc_ref_id IS 'This is the ID to reference the document, This is entered by the end-user or it is populating using OCR of the document.';
COMMENT ON COLUMN prereg.applicant_document_consumed.encrypted_dtimes IS 'Date and time when the document was encrypted before uploading it on document store. This will also be used  get the key for decrypting the data.';
COMMENT ON COLUMN prereg.applicant_document_consumed.status_code IS 'Status Code: Status of the document that is being uploaded.';
COMMENT ON COLUMN prereg.applicant_document_consumed.lang_code IS 'For multilanguage implementation this attribute Refers master.language.code. The value of some of the attributes in current record is stored in this respective language.';
COMMENT ON COLUMN prereg.applicant_document_consumed.cr_by IS 'ID or name of the user who create / insert record.';
COMMENT ON COLUMN prereg.applicant_document_consumed.cr_dtimes IS 'Date and Timestamp when the record is created/inserted';
COMMENT ON COLUMN prereg.applicant_document_consumed.upd_by IS 'ID or name of the user who update the record with new values';
COMMENT ON COLUMN prereg.applicant_document_consumed.upd_dtimes IS 'Date and Timestamp when any of the fields in the record is updated with new values.';

COMMENT ON COLUMN prereg.applicant_document.id IS 'Id: Unique id generated for the documents being uploaded as part of pre-registration process.';
COMMENT ON COLUMN prereg.applicant_document.prereg_id IS 'Id of the pre-registration application for which the documents are being uploaded.';
COMMENT ON COLUMN prereg.applicant_document.doc_name IS 'Name of the document that is uploaded';
COMMENT ON COLUMN prereg.applicant_document.doc_cat_code IS 'Document category code under which the document is being uploaded. Refers to master.document_category.code';
COMMENT ON COLUMN prereg.applicant_document.doc_typ_code IS 'Document type code under which the document is being uploaded. Refers to master.document_type.code';
COMMENT ON COLUMN prereg.applicant_document.doc_file_format IS 'Format in which the document is being uploaded. Refers to master.document_file_format.code';
COMMENT ON COLUMN prereg.applicant_document.doc_id IS 'ID of the document being uploaded';
COMMENT ON COLUMN prereg.applicant_document.doc_hash IS 'Hash value of the document being uploaded in document store. Useful to check any tampering of documents';
COMMENT ON COLUMN prereg.applicant_document.doc_ref_id IS 'This is entered by the end-user or it is populating using OCR of the document.';
COMMENT ON COLUMN prereg.applicant_document.encrypted_dtimes IS 'Date and time when the document was encrypted before uploading it on document store. This will also be used  get the key for decrypting the data.';
COMMENT ON COLUMN prereg.applicant_document.status_code IS 'Status of the document that is being uploaded.';
COMMENT ON COLUMN prereg.applicant_document.lang_code IS 'For multilanguage implementation this attribute Refers master.language.code. The value of some of the attributes in current record is stored in this respective language.';
COMMENT ON COLUMN prereg.applicant_document.cr_by IS 'ID or name of the user who create / insert record.';
COMMENT ON COLUMN prereg.applicant_document.cr_dtimes IS 'Date and Timestamp when the record is created/inserted';
COMMENT ON COLUMN prereg.applicant_document.upd_by IS 'ID or name of the user who update the record with new values';
COMMENT ON COLUMN prereg.applicant_document.upd_dtimes IS 'Date and Timestamp when any of the fields in the record is updated with new values.';

COMMENT ON TABLE prereg.intf_processed_prereg_list IS 'Interface Processd Pre Registration List: Interface table to temporarily store the list of pre-registrations that were processed by registration processor application. The data can be removed once the data is updated in actual tables.';
COMMENT ON COLUMN prereg.intf_processed_prereg_list.prereg_id IS 'Pre registration id that was consumed by registration processor to generate UIN.';
COMMENT ON COLUMN prereg.intf_processed_prereg_list.received_dtimes IS 'Date time when the pre-registration id was recevied by pre-registrations application for marking it as comsumed/processed';
COMMENT ON COLUMN prereg.intf_processed_prereg_list.lang_code IS 'For multilanguage implementation this attribute Refers master.language.code. The value of some of the attributes in current record is stored in this respective language.';
COMMENT ON COLUMN prereg.intf_processed_prereg_list.cr_by IS 'ID or name of the user who create / insert record.';
COMMENT ON COLUMN prereg.intf_processed_prereg_list.cr_dtimes IS 'Date and Timestamp when the record is created/inserted';
COMMENT ON COLUMN prereg.intf_processed_prereg_list.upd_by IS 'ID or name of the user who update the record with new values';
COMMENT ON COLUMN prereg.intf_processed_prereg_list.upd_dtimes IS 'Date and Timestamp when any of the fields in the record is updated with new values.';
COMMENT ON COLUMN prereg.intf_processed_prereg_list.is_deleted IS 'Flag to mark whether the record is Soft deleted.';
COMMENT ON COLUMN prereg.intf_processed_prereg_list.del_dtimes IS 'Date and Timestamp when the record is soft deleted with is_deleted=TRUE';

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

COMMENT ON TABLE prereg.processed_prereg_list IS 'Table to store all the pre-registration list received from registration processor within pre-registration module';
COMMENT ON COLUMN prereg.processed_prereg_list.prereg_id IS 'Pre-registration id that was consumed by registration processor to generate UIN';
COMMENT ON COLUMN prereg.processed_prereg_list.first_received_dtimes IS 'Datetime when the pre-registration id was first recevied';
COMMENT ON COLUMN prereg.processed_prereg_list.status_code IS 'status of the pre-registration status update into actual tables';
COMMENT ON COLUMN prereg.processed_prereg_list.status_comments IS 'status comments of the pre-registration status update into actual tables';

COMMENT ON TABLE prereg.reg_appointment_consumed IS 'Stores all the appointment requests booked by an individual at a registration center that are consumed. ';
COMMENT ON COLUMN prereg.reg_appointment_consumed.id IS 'Unique id generated for the registration appointment booking.';
COMMENT ON COLUMN prereg.reg_appointment_consumed.regcntr_id IS 'Id of the Registration Center where the appointment is taken. Refers to master.registration_center.id';
COMMENT ON COLUMN prereg.reg_appointment_consumed.prereg_id IS 'Pre-registration id for which registration appointment is taken.';
COMMENT ON COLUMN prereg.reg_appointment_consumed.booking_dtimes IS 'Date and Time when the appointment booking is done.';
COMMENT ON COLUMN prereg.reg_appointment_consumed.appointment_date IS 'Date for which an individual has taken an aopointment for registration at a registration center';
COMMENT ON COLUMN prereg.reg_appointment_consumed.slot_from_time IS 'Start time of the appointment slot.';
COMMENT ON COLUMN prereg.reg_appointment_consumed.slot_to_time IS 'End time of the appointment slot.';
COMMENT ON COLUMN prereg.reg_appointment_consumed.lang_code IS 'For multilanguage implementation this attribute Refers master.language.code. The value of some of the attributes in current record is stored in this respective language.';
COMMENT ON COLUMN prereg.reg_appointment_consumed.cr_by IS 'ID or name of the user who create / insert record.';
COMMENT ON COLUMN prereg.reg_appointment_consumed.cr_dtimes IS 'Date and Timestamp when the record is created/inserted';
COMMENT ON COLUMN prereg.reg_appointment_consumed.upd_by IS 'ID or name of the user who update the record with new values';
COMMENT ON COLUMN prereg.reg_appointment_consumed.upd_dtimes IS 'Date and Timestamp when any of the fields in the record is updated with new values.';

COMMENT ON TABLE prereg.reg_appointment IS 'Stores all the appointment requests booked by an individual at a registration center. ';
COMMENT ON COLUMN prereg.reg_appointment.id IS 'Unique id generated for the registration appointment booking.';
COMMENT ON COLUMN prereg.reg_appointment.regcntr_id IS 'Id of the Registration Center where the appointment is taken. Refers to master.registration_center.id';
COMMENT ON COLUMN prereg.reg_appointment.prereg_id IS 'Pre-registration id for which registration appointment is taken.';
COMMENT ON COLUMN prereg.reg_appointment.booking_dtimes IS 'Date and Time when the appointment booking is done.';
COMMENT ON COLUMN prereg.reg_appointment.appointment_date IS 'Date for which an individual has taken an aopointment for registration at a registration center';
COMMENT ON COLUMN prereg.reg_appointment.slot_from_time IS 'Start time of the appointment slot.';
COMMENT ON COLUMN prereg.reg_appointment.slot_to_time IS 'End time of the appointment slot.';
COMMENT ON COLUMN prereg.reg_appointment.lang_code IS 'For multilanguage implementation this attribute Refers master.language.code. The value of some of the attributes in current record is stored in this respective language.';
COMMENT ON COLUMN prereg.reg_appointment.cr_by IS 'ID or name of the user who create / insert record.';
COMMENT ON COLUMN prereg.reg_appointment.cr_dtimes IS 'Date and Timestamp when the record is created/inserted';
COMMENT ON COLUMN prereg.reg_appointment.upd_by IS 'ID or name of the user who update the record with new values';
COMMENT ON COLUMN prereg.reg_appointment.upd_dtimes IS 'Date and Timestamp when any of the fields in the record is updated with new values.';

COMMENT ON TABLE prereg.reg_available_slot IS 'Slots available at a registration center for an individual to book for registrating themselves to get a UIN. ';
COMMENT ON COLUMN prereg.reg_available_slot.regcntr_id IS 'Id of the Registration Center where the appointment can be booded for registration process. Refers to master.registration_center.id';
COMMENT ON COLUMN prereg.reg_available_slot.availability_date IS 'Date when the registration center is available for registration process.';
COMMENT ON COLUMN prereg.reg_available_slot.slot_from_time IS 'Start time of the appointment slot available for booking at a registration center.';
COMMENT ON COLUMN prereg.reg_available_slot.slot_to_time IS 'End time of the appointment slot available for booking at a registration center.';
COMMENT ON COLUMN prereg.reg_available_slot.available_kiosks IS 'Number of kiosks available for booking at a registration center.';
COMMENT ON COLUMN prereg.reg_available_slot.cr_by IS 'ID or name of the user who create / insert record.';
COMMENT ON COLUMN prereg.reg_available_slot.cr_dtimes IS 'Date and Timestamp when the record is created/inserted';
COMMENT ON COLUMN prereg.reg_available_slot.upd_by IS 'ID or name of the user who update the record with new values';
COMMENT ON COLUMN prereg.reg_available_slot.upd_dtimes IS 'Date and Timestamp when any of the fields in the record is updated with new values.';
COMMENT ON COLUMN prereg.reg_available_slot.is_deleted IS 'Flag to mark whether the record is Soft deleted.';
COMMENT ON COLUMN prereg.reg_available_slot.del_dtimes IS 'Date and Timestamp when the record is soft deleted with is_deleted=TRUE';
