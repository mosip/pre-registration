package io.mosip.preregistration.application.dto;

import lombok.Data;

@Data
/**
 * Data transfer object carrying language value across Pre-Registration APIs.
 */
public class LanguageValueDto {

	/**
	 * language used by this Pre-Registration type.
	 */
	private String language;

	/**
	 * value used by this Pre-Registration type.
	 */
	private String value;
}
