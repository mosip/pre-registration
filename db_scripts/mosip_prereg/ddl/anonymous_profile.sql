--- This table saves anonymous details about the user like year of birth, gender, documents uploaded etc. for each pre-registration application created by the end user. This information is to be used for analytics.
CREATE TABLE IF NOT EXISTS prereg.anonymous_profile
(
    id character varying(36) COLLATE pg_catalog."default" NOT NULL,
    profile character varying COLLATE pg_catalog."default" NOT NULL,
    cr_by character varying(256) COLLATE pg_catalog."default" NOT NULL,
    cr_dtimes timestamp without time zone NOT NULL,
    upd_by character varying(256) COLLATE pg_catalog."default",
    upd_dtimes timestamp without time zone,
    is_deleted boolean,
    del_dtimes timestamp without time zone,
    CONSTRAINT anonymous_profile_pkey PRIMARY KEY (id)
);

