package io.mosip.preregistration.application.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/**
 * Class supporting user in MOSIP Pre-Registration (io.mosip.preregistration.application.dto).
 */
public class User {
	/**
	 * user id used by this Pre-Registration type.
	 */
	private String userId;
	/**
	 * otp used by this Pre-Registration type.
	 */
	private String otp;
}
