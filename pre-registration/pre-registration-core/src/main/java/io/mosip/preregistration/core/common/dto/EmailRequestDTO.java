package io.mosip.preregistration.core.common.dto;

import lombok.Data;

@Data
/**
 * Data transfer object carrying email request across Pre-Registration APIs.
 */
public class EmailRequestDTO {
	/**
	 * 
	 */
	private String emaild;
	/**
	 * 
	 */
	private String ccEmail;
	/**
	 * 
	 */
	private String bccEmail;
}
