package io.mosip.preregistration.core.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception raised when user lookup fails in Pre-Registration.
 */
public class UserLookupException extends BaseUncheckedException {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a user lookup exception.
	 *
	 * @param errorCode error code supplied to this constructor
	 * @param errorMessage error message supplied to this constructor
	 */
	public UserLookupException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	/**
	 * Creates a user lookup exception.
	 *
	 * @param errorCode error code supplied to this constructor
	 * @param errorMessage error message supplied to this constructor
	 * @param rootCause root cause supplied to this constructor
	 */
	public UserLookupException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}

}
