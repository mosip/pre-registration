package io.mosip.preregistration.application.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
/**
 * Data transfer object carrying otp request across Pre-Registration APIs.
 */
public class OtpRequestDTO {
	
	/**
	 * user id used by this Pre-Registration type.
	 */
	private  String userId;
	
}
