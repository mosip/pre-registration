/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.batchjob.code;

/**
 * This Enum provides the constant variables to define Error codes.
 * 
 * @author Kishan Rathore
 * @since 1.0.0
 *
 */
public enum ErrorCodes {

	PRG_PAM_BAT_001("PRG_PAM_BAT_001"),
	PRG_PAM_BAT_002("PRG_PAM_BAT_002"),
	PRG_PAM_BAT_003("PRG_PAM_BAT_003"),
	PRG_PAM_BAT_004("PRG_PAM_BAT_004"),
	PRG_PAM_BAT_005("PRG_PAM_BAT_005"), 
	PRG_PAM_BAT_006("PRG_PAM_BAT_006"),
	PRG_PAM_BAT_007("PRG_PAM_BAT_007"), 
	PRG_PAM_BAT_008("PRG_PAM_BAT_008"), 
	PRG_PAM_BAT_009("PRG_PAM_BAT_009"), 
	PRG_PAM_BAT_010("PRG_PAM_BAT_010"),
	PRG_PAM_BAT_011("PRG_PAM_BAT_011"),
	PRG_PAM_BAT_012("PRG_PAM_BAT_012"),
	PRG_PAM_BAT_013("PRG_PAM_BAT_013"),
	PRG_PAM_BAT_014("PRG_PAM_BAT_014"),
	PRG_PAM_BAT_015("PRG_PAM_BAT_015"),
	PRG_PAM_BAT_016("PRG_PAM_BAT_016"),
	PRG_PAM_BAT_017("PRG_PAM_BAT_017"),
	PRG_PAM_BAT_018("PRG_PAM_BAT_018");
	
	/**
	 * @param code
	 */
	private ErrorCodes(String code) {
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
