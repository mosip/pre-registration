package io.mosip.preregistration.captcha.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception raised when invalid request captcha fails in Pre-Registration.
 */
public class InvalidRequestCaptchaException extends BaseUncheckedException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a invalid request captcha exception.
	 *
	 * @param errorCode error code supplied to this constructor
	 * @param errorMessage error message supplied to this constructor
	 */
	public InvalidRequestCaptchaException(String errorCode, String errorMessage) {
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

}
