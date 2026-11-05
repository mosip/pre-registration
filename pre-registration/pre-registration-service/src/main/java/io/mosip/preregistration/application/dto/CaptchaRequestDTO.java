package io.mosip.preregistration.application.dto;

import java.io.Serializable;

import lombok.Data;

@Data
/**
 * Data transfer object carrying captcha request across Pre-Registration APIs.
 */
public class CaptchaRequestDTO implements Serializable{
 
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * captcha token used by this Pre-Registration type.
	 */
	private String captchaToken;
}
