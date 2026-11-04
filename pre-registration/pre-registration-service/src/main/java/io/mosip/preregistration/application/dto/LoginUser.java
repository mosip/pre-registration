package io.mosip.preregistration.application.dto;

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
	 * user name used by this Pre-Registration type.
	 */
	private String userName;
	/**
	 * password used by this Pre-Registration type.
	 */
	private String password;
	/**
	 * app id used by this Pre-Registration type.
	 */
	private String appId;

}
