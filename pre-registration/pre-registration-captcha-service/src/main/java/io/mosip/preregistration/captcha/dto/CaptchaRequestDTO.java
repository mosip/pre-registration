package io.mosip.preregistration.captcha.dto;

import java.io.Serializable;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class CaptchaRequestDTO implements Serializable{
 
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String captchaToken;
}
