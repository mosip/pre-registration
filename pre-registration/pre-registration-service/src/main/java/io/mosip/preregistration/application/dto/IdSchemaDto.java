package io.mosip.preregistration.application.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
/**
 * Data transfer object carrying id schema across Pre-Registration APIs.
 */
public class IdSchemaDto {
	
	/**
	 * id used by this Pre-Registration type.
	 */
	private String id;
	/**
	 * id version used by this Pre-Registration type.
	 */
	private double idVersion;
	/**
	 * schema json used by this Pre-Registration type.
	 */
	private String schemaJson;
	/**
	 * effective from used by this Pre-Registration type.
	 */
	private LocalDateTime effectiveFrom;
	/**
	 * schema used by this Pre-Registration type.
	 */
	private List<JsonNode> schema;
}
