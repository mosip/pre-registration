package io.mosip.preregistration.application.dto;

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
public class UserOtp {
	/**
	 * user id used by this Pre-Registration type.
	 */
	private String userId;
	/**
	 * otp used by this Pre-Registration type.
	 */
	private String otp;
	/**
	 * app id used by this Pre-Registration type.
	 */
	private String appId;
}
