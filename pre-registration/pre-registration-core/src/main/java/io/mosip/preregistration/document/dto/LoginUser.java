package io.mosip.preregistration.document.dto;

import lombok.Data;

/**
 * This is a ResponseWrapper class used in Rest call to kernel authmanager.
 * 
 * @author Kishan Rathore
 * @since 1.0.0
 */
@Data
public class LoginUser {
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
