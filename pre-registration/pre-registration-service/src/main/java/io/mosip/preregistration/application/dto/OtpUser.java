package io.mosip.preregistration.application.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * This DTO class is used to define the initial request parameters.
 * 
 * @author Akshay Jain
 * @since 1.0.0
 *
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
public class OtpUser {
	/**
	 * user id used by this Pre-Registration type.
	 */
	private String userId;
	/**
	 * otp channel used by this Pre-Registration type.
	 */
	private List<String> otpChannel;
	/**
	 * app id used by this Pre-Registration type.
	 */
	private String appId;
	/**
	 * useridtype used by this Pre-Registration type.
	 */
	private String useridtype;
	/**
	 * template variables used by this Pre-Registration type.
	 */
	private Map<String,Object> templateVariables;
	/**
	 * context used by this Pre-Registration type.
	 */
	private String context; 
}
