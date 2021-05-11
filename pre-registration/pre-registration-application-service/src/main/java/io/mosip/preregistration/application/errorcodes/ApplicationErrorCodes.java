package io.mosip.preregistration.application.errorcodes;

public enum ApplicationErrorCodes {

	PRG_APP_001("PRG_APP_001"), // UNABLE_TO_CREATE_THE_PRE_REGISTRATION_UI_SPEC

	PRG_APP_002("PRG_APP_002"), // UNABLE_TO_UPDATE_THE_UI_SPEC

	PRG_APP_003("PRG_APP_003"), // UNABLE_TO_FETCH_THE_UI_SPEC

	PRG_APP_004("PRG_APP_004"), // FAILED_TO_DELETE_THE_UI_SPEC

	PRG_APP_005("PRG_APP_005"), // FAILED_TO_PUBLISH_THE_UI_SPEC

	PRG_APP_006("PRG_APP_006"), // UI_SPEC_VALUE_PARSE_ERROR
	
	PRG_APP_007("PRG_APP_007"); // Audit from UI Failed

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
