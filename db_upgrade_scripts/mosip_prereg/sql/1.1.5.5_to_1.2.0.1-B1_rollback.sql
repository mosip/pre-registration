DROP TABLE IF EXISTS prereg-applications;
DROP TABLE IF EXISTS prereg-anonymous_profile;

ALTER TABLE prereg.reg_appointment ADD CONSTRAINT fk_rappmnt_id FOREIGN KEY (prereg_id)
REFERENCES prereg.applicant_demographic(prereg_id) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;
