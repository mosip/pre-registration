package io.mosip.preregistration.captcha.dto;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.mosip.preregistration.core.common.dto.ExceptionJSONInfoDTO;
import lombok.Data;

@Data
public class GoogleCaptchaDTO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@JsonProperty("success")
	private boolean success;

	@JsonProperty("challenge_ts")
	private String challengeTs;

	@JsonProperty("hostname")
	private String hostname;
	
    @JsonProperty("errorCodes")
	private List<ExceptionJSONInfoDTO> errorCodes;

}
