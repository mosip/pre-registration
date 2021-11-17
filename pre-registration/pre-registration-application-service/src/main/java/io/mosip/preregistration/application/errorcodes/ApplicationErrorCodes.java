package io.mosip.preregistration.application.errorcodes;

public enum ApplicationErrorCodes {

	PRG_APP_001("PRG_APP_001"), // UNABLE_TO_CREATE_THE_PRE_REGISTRATION_UI_SPEC

	PRG_APP_002("PRG_APP_002"), // UNABLE_TO_UPDATE_THE_UI_SPEC

	PRG_APP_003("PRG_APP_003"), // UNABLE_TO_FETCH_THE_UI_SPEC

	PRG_APP_004("PRG_APP_004"), // FAILED_TO_DELETE_THE_UI_SPEC

	PRG_APP_005("PRG_APP_005"), // FAILED_TO_PUBLISH_THE_UI_SPEC

	PRG_APP_006("PRG_APP_006"), // UI_SPEC_VALUE_PARSE_ERROR
	
	PRG_APP_007("PRG_APP_007"), // Audit from UI Failed
	
	PRG_APP_008("PRG_APP_008"), // deprecated error code
	
	PRG_APP_009("PRG_APP_009"), // failed to save/update applications table
	
	PRG_APP_010("PRG_APP_009"), // status update in applications table failed
	
	PRG_APP_011("PRG_APP_011"), //  delete operation failed for application in applications table
	
	PRG_APP_012("PRG_APP_012"), // No Record found 

	PRG_APP_013("PRG_APP_013"), // Invalid request argument
	
	PRG_APP_014("PRG_APP_014"), // Invalid request application Id
	
	/* ErrorCode for INVALID_APPLICATION_ID_FOR_USER */
	PRG_APP_015("PRG_APP_015"),
	
	/* ErrorCode for INVALID_BOOKING_TYPE */
	PRG_APP_016("PRG_APP_016");

	private ApplicationErrorCodes(String code) {
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
