-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_prereg
-- Table Name 	: prereg.applications
-- Purpose    	: Applications: 
--           
-- Create By   	: Ram Bhatt
-- Created Date	: Aug-2021
--
-- Modified Date        Modified By         Comments / Remarks
-- ------------------------------------------------------------------------------------------
-- 
-- ------------------------------------------------------------------------------------------
-- object: prereg.applications | type: TABLE --
-- DROP TABLE IF EXISTS prereg.applications CASCADE;
CREATE TABLE prereg.applications(
	application_id character varying(36) NOT NULL,
	booking_type character varying(256) NOT NULL,
	booking_status_code character varying(256),
	application_status_code character varying(256),
	regcntr_id character varying(10),
	appointment_date date,
	booking_date date,
	slot_from_time timestamp without time zone,
	slot_to_time timestamp without time zone,
	contact_info character varying(256),
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp without time zone NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp without time zone,
	CONSTRAINT appid_pk PRIMARY KEY (application_id)

);
-- ddl-end --
