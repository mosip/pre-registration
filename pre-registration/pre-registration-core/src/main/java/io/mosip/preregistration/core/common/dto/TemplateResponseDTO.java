package io.mosip.preregistration.core.common.dto;

import lombok.Data;

@Data
/**
 * Data transfer object carrying template response across Pre-Registration APIs.
 */
public class TemplateResponseDTO {
	/**
	 * id used by this Pre-Registration type.
	 */
	private String id;

	/**
	 * name used by this Pre-Registration type.
	 */
	private String name;

	/**
	 * description used by this Pre-Registration type.
	 */
	private String description;

	/**
	 * file format code used by this Pre-Registration type.
	 */
	private String fileFormatCode;

	/**
	 * model used by this Pre-Registration type.
	 */
	private String model;

	/**
	 * file text used by this Pre-Registration type.
	 */
	private String fileText;

	/**
	 * module id used by this Pre-Registration type.
	 */
	private String moduleId;

	/**
	 * module name used by this Pre-Registration type.
	 */
	private String moduleName;

	/**
	 * template type code used by this Pre-Registration type.
	 */
	private String templateTypeCode;

	/**
	 * lang code used by this Pre-Registration type.
	 */
	private String langCode;

	/**
	 * is active used by this Pre-Registration type.
	 */
	private Boolean isActive;
}
