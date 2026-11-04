package io.mosip.preregistration.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * Data transfer object carrying otp with lang code across Pre-Registration APIs.
 */
public class OTPWithLangCodeDTO {
	
	/**
	 * user id used by this Pre-Registration type.
	 */
	private String userId;
	
	/**
	 * lang code used by this Pre-Registration type.
	 */
	private String langCode;

}
