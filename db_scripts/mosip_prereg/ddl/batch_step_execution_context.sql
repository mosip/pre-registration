-- This is required by Spring Batch framework

CREATE TABLE prereg.batch_step_execution_context
(
	STEP_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
	SHORT_CONTEXT VARCHAR(2500) NOT NULL,
	SERIALIZED_CONTEXT TEXT
)
WITH (
    OIDS = FALSE
);