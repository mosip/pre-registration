package io.mosip.preregistration.application.dto;

import tools.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
/**
 * Class supporting ui spec key value pair in MOSIP Pre-Registration (io.mosip.preregistration.application.dto).
 */
public class UISpecKeyValuePair {	
	/**
	 * type used by this Pre-Registration type.
	 */
	private String type;
	/**
	 * spec used by this Pre-Registration type.
	 */
	private JsonNode spec;
}
