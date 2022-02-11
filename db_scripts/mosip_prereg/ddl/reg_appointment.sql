-- This table saves the appointment details for the user’s application like the registration centre id, time slot etc.jddd
CREATE TABLE prereg.reg_appointment(
	id character varying(36) NOT NULL,
	regcntr_id character varying(10) NOT NULL,
	prereg_id character varying(36) NOT NULL,
	booking_dtimes timestamp NOT NULL,
	appointment_date date,
	slot_from_time time,
	slot_to_time time,
	lang_code character varying(3) NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	CONSTRAINT pk_rappmnt_id PRIMARY KEY (id),
	CONSTRAINT uk_rappmnt_id UNIQUE (prereg_id)
);
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
