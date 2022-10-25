\c mosip_prereg sysadmin


\ir ../ddl/prereg-applications.sql

\ir  ../ddl/prereg-anonymous_profile.sql

ALTER TABLE prereg.reg_appointment DROP CONSTRAINT IF EXISTS fk_rappmnt_id CASCADE;

INSERT INTO prereg.applications(application_id, booking_type, booking_status_code, regcntr_id, appointment_date, booking_date,
   slot_from_time, slot_to_time, contact_info, cr_by, cr_dtimes, upd_by, upd_dtimes, application_status_code)
   Select t1.prereg_id, 'NEW_PREREGISTRATION', t1.status_code, t2.regcntr_id, t2.appointment_date,
   t2.booking_dtimes, t2.slot_from_time, t2.slot_to_time, t1.cr_appuser_id, t1.cr_by, t1.cr_dtimes, t1.upd_by, t1.upd_dtimes,
   Case When t1.status_code='Application_Incomplete' THEN 'DRAFT' Else 'SUBMITTED' End
   From prereg.applicant_demographic t1
   LEFT Join prereg.reg_appointment t2 On t1.prereg_id=t2.prereg_id;

