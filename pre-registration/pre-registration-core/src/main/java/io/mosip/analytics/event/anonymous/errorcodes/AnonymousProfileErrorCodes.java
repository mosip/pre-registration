/* 
 * Copyright
 * 
 */
package io.mosip.analytics.event.anonymous.errorcodes;

/**
 * 
 * This Enum provides the constant variables to define Error codes.
 * 
 * @author Mayura D
 * @since 1.2.0
 *
 */
public enum AnonymousProfileErrorCodes {

	/* ErrorCode for UNABLE_TO_SAVE_ANONYMOUS_PROFILE */
	PRG_ANO_001("PRG_ANO_001"),

	/* ErrorCode for UNBALE_TO_READ_IDENTITY_JSON */
	PRG_ANO_002("PRG_ANO_002"),

	/* ErrorCode for SERVER_ERROR */
	PRG_ANO_003("PRG_ANO_003");

	private AnonymousProfileErrorCodes(String code) {
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
