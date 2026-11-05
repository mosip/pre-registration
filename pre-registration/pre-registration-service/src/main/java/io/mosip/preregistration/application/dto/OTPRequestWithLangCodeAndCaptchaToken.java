package io.mosip.preregistration.application.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
/**
 * Class supporting otp request with lang code and captcha token in MOSIP Pre-Registration (io.mosip.preregistration.application.dto).
 */
public class OTPRequestWithLangCodeAndCaptchaToken {

	@NotBlank
	/**
	 * user id used by this Pre-Registration type.
	 */
	private String userId;
	
	@NotBlank
	/**
	 * lang code used by this Pre-Registration type.
	 */
	private String langCode;
	
	/**
	 * captcha token used by this Pre-Registration type.
	 */
	private String captchaToken;
	
	
}
