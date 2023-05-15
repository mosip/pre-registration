-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_prereg
-- Table Name 	: prereg.anonymous_profile
-- Purpose    	: Anonymous profile: 
--           
-- Create By   	: Ram 
-- Created Date	: Sept-2021
-- ------------------------------------------------------------------------------------------
-- 
-- ------------------------------------------------------------------------------------------

-- object: prereg.prereg.anonymous_profile | type: TABLE --
-- DROP TABLE IF EXISTS prereg.prereg.anonymous_profile CASCADE;



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

-- ddl-end --
