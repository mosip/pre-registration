package io.mosip.preregistration.batchjob.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception raised when no record found fails in Pre-Registration.
 */
public class NoRecordFoundException extends BaseUncheckedException {


	/**
	 * 
	 */
	private static final long serialVersionUID = -2209784832378345717L;

	/**
	 * Creates a no record found exception.
	 *
	 * @param message message supplied to this constructor
	 */
	public NoRecordFoundException(String message) {
		super("",message);
	}
	/**
	 * Creates a no record found exception.
	 *
	 * @param errorCode error code supplied to this constructor
	 * @param errorMessage error message supplied to this constructor
	 */
	public NoRecordFoundException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	/**
	 * Creates a no record found exception.
	 *
	 * @param errorCode error code supplied to this constructor
	 * @param errorMessage error message supplied to this constructor
	 * @param rootCause root cause supplied to this constructor
	 */
	public NoRecordFoundException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}
}
