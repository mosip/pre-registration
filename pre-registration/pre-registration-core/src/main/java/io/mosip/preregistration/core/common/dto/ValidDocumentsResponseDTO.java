package io.mosip.preregistration.core.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
/**
 * Data transfer object carrying valid documents response across Pre-Registration APIs.
 */
public class ValidDocumentsResponseDTO {

	/**
	 * doc type code used by this Pre-Registration type.
	 */
	private String docTypeCode;

	/**
	 * doc category code used by this Pre-Registration type.
	 */
	private String docCategoryCode;

	/**
	 * is active used by this Pre-Registration type.
	 */
	private Boolean isActive;
}
