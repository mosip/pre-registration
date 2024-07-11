/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.application.code;

/**
 * 
 * This Enum provides the constant variables to accept input request.
 * 
 * @author Sanober Noor
 * @since 1.0.0
 * @author Aiham Hasan
 * @since 1.2.0
 */

public enum NotificationRequestCodes {

	/* preRegistration Id */
	PREID(""),
	/**
	* 
	*/
	SMS("sms"),
	/**
	 * 
	 */
	EMAIL("email"), MESSAGE("Email and sms request successfully submitted");

	/**
	 * @param code
	 */
	private NotificationRequestCodes(String code) {
		this.code = code;
	}

	/**
	 * Code
	 */
	private final String code;

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}
}
