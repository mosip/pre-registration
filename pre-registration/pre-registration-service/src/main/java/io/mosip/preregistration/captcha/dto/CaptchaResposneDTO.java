package io.mosip.preregistration.captcha.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
/**
 * Data transfer object carrying captcha resposne across Pre-Registration APIs.
 */
public class CaptchaResposneDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@JsonProperty("success")
	/**
	 * success used by this Pre-Registration type.
	 */
	private boolean success;
	
	@JsonProperty("message")
	/**
	 * message used by this Pre-Registration type.
	 */
	private String message;

}
