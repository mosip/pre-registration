package io.mosip.preregistration.captcha.constants;

/**
 * Enumerated captcha error code values used by MOSIP Pre-Registration.
 */
public enum CaptchaErrorCode {

	INVALID_CAPTCHA_CODE("PRG-PAM-005", "Invalid Captcha entered"),
	INVALID_CAPTCHA_REQUEST("PRG-PAM-006", "Invalid request , Request can't be null or empty");

	/**
	 * error code used by this Pre-Registration type.
	 */
	private final String errorCode;
	/**
	 * error message used by this Pre-Registration type.
	 */
	private final String errorMessage;

	/**
	 * Creates a captcha error code.
	 *
	 * @param errorCode error code supplied to this constructor
	 * @param errorMessage error message supplied to this constructor
	 */
	private CaptchaErrorCode(String errorCode, String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	/**
	 * Returns error code.
	 *
	 * @return get error code result
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * Returns error message.
	 *
	 * @return get error message result
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

}
