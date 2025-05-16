package io.mosip.preregistration.application.constant;

public enum PreRegLoginErrorConstants {

	OTP_REQUEST_FLOODED("PRE-REG-OTP-101", "Innumerous OTP requests received"),
	OTP_GENERATION_FAILED("PRE-REG-OTP-401", "Could not generate/send OTP"),
	EXPIRED_OTP("PRE-REG-OTP-402", "OTP has expired - Please regenerate OTP and try again after sometime"),
	INVALID_OTP("PRE-REG-OTP-403", "OTP is invalid - Please provide correct OTP value"),
	INVALID_INPUT_PARAMETER("PRE-REG-OTP-404", "Invalid Input Parameter - %s"),
	OTP_ALREADY_SENT("PRE-REG-OTP-408", "OTP is already sent - Please use the Sent OTP or try again after sometime"),

	DATA_VALIDATION_FAILED("PRE-REG-OTP-406", "Input Data Validation Failed"),
	TOKEN_GENERATION_FAILED("PRE-REG-OTP-407", "Token generation failed"),
	MISSING_INPUT_PARAMETER("PRE-REG-OTP-405", "Missing Input Parameter - %s"),
	BLOCKED_OTP_VALIDATE("PRE-REG-OTP-405", "Missing Input Parameter - %s"),
	UNABLE_TO_PROCESS("PRE-REG-OTP-405", "Missing Input Parameter - %s"),
	SERVER_ERROR("PRE-REG-OTP-405", "Missing Input Parameter - %s"),
	CAPTCHA_SEVER_ERROR("PRE-REG-CAP-102", "Captcha could not be validated"),
	CAPTCHA_ERROR("PRE-REG-CAP-101", "Captcha token is empty");

	final String errorCode;
	private final String errorMessage;


	/**
	 * Constructor for {@link IdAuthenticationErrorConstants}
	 * 
	 * @param errorCode    - id-usage error codes which follows
	 *                     "<product>-<module>-<component>-<number>" pattern
	 * @param errorMessage - short error message
	 */
	private PreRegLoginErrorConstants(String errorCode, String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	/**
	 * Getter for errorCode
	 * 
	 * @return the errorCode
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * Getter for errorMessage
	 * 
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

}
