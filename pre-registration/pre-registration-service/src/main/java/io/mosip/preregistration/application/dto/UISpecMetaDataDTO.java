package io.mosip.preregistration.application.dto;

import java.time.LocalDateTime;

import tools.jackson.databind.JsonNode;

import lombok.Data;

@Data
/**
 * Data transfer object carrying ui spec meta data across Pre-Registration APIs.
 */
public class UISpecMetaDataDTO {

	/**
	 * id used by this Pre-Registration type.
	 */
	private String id;
	/**
	 * version used by this Pre-Registration type.
	 */
	private double version;
	/**
	 * title used by this Pre-Registration type.
	 */
	private String title;
	/**
	 * description used by this Pre-Registration type.
	 */
	private String description;
	/**
	 * identity schema id used by this Pre-Registration type.
	 */
	private String identitySchemaId;
	/**
	 * id schema version used by this Pre-Registration type.
	 */
	private double idSchemaVersion;
	/**
	 * json spec used by this Pre-Registration type.
	 */
	private JsonNode jsonSpec;
	/**
	 * status used by this Pre-Registration type.
	 */
	private String status;
	/**
	 * effective from used by this Pre-Registration type.
	 */
	private LocalDateTime effectiveFrom;
	/**
	 * created on used by this Pre-Registration type.
	 */
	private LocalDateTime createdOn;
	/**
	 * updated on used by this Pre-Registration type.
	 */
	private LocalDateTime updatedOn;

}
