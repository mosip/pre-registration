package io.mosip.preregistration.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
/**
 * Data transfer object carrying client secret across Pre-Registration APIs.
 */
public class ClientSecretDTO {

	/**
	 * client id used by this Pre-Registration type.
	 */
	private String clientId;
	/**
	 * secret key used by this Pre-Registration type.
	 */
	private String secretKey;
	/**
	 * app id used by this Pre-Registration type.
	 */
	private String appId;
}
