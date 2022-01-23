-- Once the user’s application is processed in MOSIP, then the application’s document details are archived and saved in this table by the Batch Job.
CREATE TABLE prereg.applicant_document_consumed(
	id character varying(36) NOT NULL,
	prereg_id character varying(36) NOT NULL,
	doc_name character varying(128) NOT NULL,
	doc_cat_code character varying(36) NOT NULL,
	doc_typ_code character varying(36) NOT NULL,
	doc_file_format character varying(36) NOT NULL,
	doc_id character varying(128) NOT NULL,
	doc_hash character varying(64) NOT NULL,
	doc_ref_id character varying,
	encrypted_dtimes timestamp NOT NULL,
	status_code character varying(36) NOT NULL,
	lang_code character varying(3) NOT NULL,
	cr_by character varying(256),
	cr_dtimes timestamp,
	upd_by character varying(256),
	upd_dtimes timestamp,
	CONSTRAINT pk_appldocc_prereg_id PRIMARY KEY (id)
);
create unique index idx_appldocc_prereg_id on prereg.applicant_document_consumed (prereg_id, doc_cat_code, doc_typ_code) ;

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
