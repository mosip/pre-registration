

-- object: prereg.prereg.anonymous_profile | type: TABLE --
-- DROP TABLE IF EXISTS prereg.prereg.anonymous_profile CASCADE;



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

-- ddl-end --
