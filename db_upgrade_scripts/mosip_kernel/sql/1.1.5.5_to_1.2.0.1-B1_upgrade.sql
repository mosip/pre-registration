\c mosip_kernel

-- kernel.prid only (pridgenerator-service). Commons also REASSIGNs kernel-wide
-- privileges in this hop; those stay in mosip/commons.

CREATE INDEX IF NOT EXISTS idx_prid_status
    ON kernel.prid USING btree
    (prid_status COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;
