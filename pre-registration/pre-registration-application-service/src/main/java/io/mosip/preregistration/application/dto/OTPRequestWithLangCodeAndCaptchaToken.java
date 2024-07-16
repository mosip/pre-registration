package io.mosip.preregistration.application.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class OTPRequestWithLangCodeAndCaptchaToken {

	@NotBlank
	private String userId;
	
	@NotBlank
	private String langCode;
	
	private String captchaToken;
	
	
}
