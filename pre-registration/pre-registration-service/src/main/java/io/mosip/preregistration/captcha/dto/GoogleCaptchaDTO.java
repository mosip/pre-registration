package io.mosip.preregistration.captcha.dto;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.mosip.preregistration.core.common.dto.ExceptionJSONInfoDTO;
import lombok.Data;

@Data
/**
 * Data transfer object carrying google captcha across Pre-Registration APIs.
 */
public class GoogleCaptchaDTO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@JsonProperty("success")
	/**
	 * success used by this Pre-Registration type.
	 */
	private boolean success;

	@JsonProperty("challenge_ts")
	/**
	 * challenge ts used by this Pre-Registration type.
	 */
	private String challengeTs;

	@JsonProperty("hostname")
	/**
	 * hostname used by this Pre-Registration type.
	 */
	private String hostname;
	
    @JsonProperty("errorCodes")
	/**
	 * error codes used by this Pre-Registration type.
	 */
	private List<ExceptionJSONInfoDTO> errorCodes;

}
