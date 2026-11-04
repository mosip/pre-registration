package io.mosip.preregistration.application.dto;

import lombok.Data;

@Data
/**
 * Data transfer object carrying document type across Pre-Registration APIs.
 */
public class DocumentTypeDto {

	/**
	 * code used by this Pre-Registration type.
	 */
	private String code;

	/**
	 * name used by this Pre-Registration type.
	 */
	private String name;

	/**
	 * description used by this Pre-Registration type.
	 */
	private String description;

	/**
	 * lang code used by this Pre-Registration type.
	 */
	private String langCode;

	/**
	 * is active used by this Pre-Registration type.
	 */
	private Boolean isActive;
}
