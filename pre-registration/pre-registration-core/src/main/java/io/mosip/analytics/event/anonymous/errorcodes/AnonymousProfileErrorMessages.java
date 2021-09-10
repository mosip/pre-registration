/* 
 * Copyright
 * 
 */
package io.mosip.analytics.event.anonymous.errorcodes;

/**
 * 
 * This Enum provides the constant variables to define Error Messages.
 * 
 * @author Mayura D
 * @since 1.2.0
 *
 */
public enum AnonymousProfileErrorMessages {

	/**
	 * ErrorMessage for PRG_ANO_001
	 */
	UNABLE_TO_SAVE_ANONYMOUS_PROFILE("Unable to save the anonymous profile"),

	/**
	 * ErrorMessage for PRG_ANO_002
	 */
	UNBALE_TO_READ_IDENTITY_JSON("Failed to read the identity json from the server"),

	/**
	 * ErrorMessage for PRG_ANO_003
	 */
	SERVER_ERROR("Error while calling config server");

	private AnonymousProfileErrorMessages(String message) {
		this.message = message;
	}

	private final String message;

	/**
	 * @return message
	 */
	public String getMessage() {
		return message;
	}
}
