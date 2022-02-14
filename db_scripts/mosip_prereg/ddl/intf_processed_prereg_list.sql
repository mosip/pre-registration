-- This table saves details about the pre-registration ids which are processed by the MOSIP.  

CREATE TABLE prereg.intf_processed_prereg_list(
	prereg_id character varying(36) NOT NULL,
	received_dtimes timestamp NOT NULL,
	lang_code character varying(3) NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean,
	del_dtimes timestamp,
	CONSTRAINT ipprlst_pk PRIMARY KEY (prereg_id,received_dtimes)

);
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
