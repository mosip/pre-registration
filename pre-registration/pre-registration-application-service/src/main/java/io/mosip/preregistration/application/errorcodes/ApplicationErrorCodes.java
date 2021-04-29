package io.mosip.preregistration.application.errorcodes;

public enum ApplicationErrorCodes {

	/* ErrorCode for UNABLE_TO_CREATE_THE_PRE_REGISTRATION_UI_SPEC */
	
	PRG_APP_001("PRG_APP_001"),
	
	PRG_APP_002("PRG_APP_002"),
	
	PRG_APP_003("PRG_APP_003"),
	
	PRG_APP_004("PRG_APP_004"),
	
	PRG_APP_005("PRG_APP_005");
	

	private ApplicationErrorCodes(String code)
	{
		this.code =code;}

	private final String code;

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}
}
