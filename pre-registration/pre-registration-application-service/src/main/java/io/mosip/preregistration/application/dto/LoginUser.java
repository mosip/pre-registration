/* 
 * Copyright
 * 
 */
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

	private String userName;
	private String password;
	private String appId;

}
