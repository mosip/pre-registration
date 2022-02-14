-- This table saves details about all time slots available for all the active registration centres.

CREATE TABLE prereg.reg_available_slot(
	regcntr_id character varying(10) NOT NULL,
	availability_date date NOT NULL,
	slot_from_time time NOT NULL,
	slot_to_time time,
	available_kiosks smallint,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean,
	del_dtimes timestamp,
	CONSTRAINT pk_ravlslt_id PRIMARY KEY (regcntr_id,availability_date,slot_from_time)
);

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
