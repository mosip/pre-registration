/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.application.code;

/**
 * 
 * This Enum provides the constant variables to accept input request.
 * 
 * @author Kishan Rathore
 * @since 1.0.0
 *
 */
public enum TransliterationRequestCodes {

	/* id */
	ID("id"), 

	/* version */
	VER("version"), 

	/* request date time */
	REQ_TIME("requesttime"), 

	/* request object */
	REQUEST("request");
	
	/**
	 * @param code
	 */
	private TransliterationRequestCodes(String code) {
		this.code = code;
	}

	private final String code;

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}
}
