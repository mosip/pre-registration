package io.mosip.preregistration.captcha.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception raised when captcha fails in Pre-Registration.
 */
public class CaptchaException extends BaseUncheckedException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a captcha exception.
	 *
	 * @param errorCode error code supplied to this constructor
	 * @param errorMessage error message supplied to this constructor
	 */
	public CaptchaException(String errorCode, String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;

	}

	/**
	 * error code used by this Pre-Registration type.
	 */
	private String errorCode;

	/**
	 * error message used by this Pre-Registration type.
	 */
	private String errorMessage;

	/**
	 * Returns error code.
	 *
	 * @return get error code result
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * Sets error code.
	 *
	 * @param errorCode error code used by set error code
	 */
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * Returns error message.
	 *
	 * @return get error message result
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * Sets error message.
	 *
	 * @param errorMessage error message used by set error message
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
}
