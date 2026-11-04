package io.mosip.preregistration.batchjob.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception raised when rest call fails in Pre-Registration.
 */
public class RestCallException extends BaseUncheckedException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4166660574837976618L;
	
	/**
	 * Creates a rest call exception.
	 *
	 * @param message message supplied to this constructor
	 */
	public RestCallException(String message) {
		super("",message);
	}
	/**
	 * Creates a rest call exception.
	 *
	 * @param errorCode error code supplied to this constructor
	 * @param errorMessage error message supplied to this constructor
	 */
	public RestCallException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	/**
	 * Creates a rest call exception.
	 *
	 * @param errorCode error code supplied to this constructor
	 * @param errorMessage error message supplied to this constructor
	 * @param rootCause root cause supplied to this constructor
	 */
	public RestCallException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}

}
