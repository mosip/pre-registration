package io.mosip.preregistration.core.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.core.exception.util.PreIssuanceExceptionCodes;

/**
 * The UserNameNotValidException class represents an exception that occurs
 * when the user name is not valid. This class extends
 * {@link BaseUncheckedException} and includes various constructors for
 * different use case.
 * 
 * @author Rajath KR
 * @since 1.0.0
 * 
 */

public class UserNameNotValidException extends BaseUncheckedException {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
     * Default Constructor
     */
	public UserNameNotValidException() {
		super();
	}

	/**
	 * Constructs a new {@code UserNameNotValidException} with the specified
	 * error errorMessage
	 * 
	 * @param errorMessage the detailed error errorMessage.
	 */
	public UserNameNotValidException(String errorMessage) {
		super(PreIssuanceExceptionCodes.INVALID_USER_NAME, errorMessage);
	}

	/**
	 * Constructs a new {@code UserNameNotValidException} with the specified
	 * error message, and rootCause.
	 * 
	 * @param errorMessage the detailed error message.
	 * @param rootCause    the cause of the error.
	 */
	public UserNameNotValidException(String errorMessage, Throwable rootCause) {
		super(PreIssuanceExceptionCodes.INVALID_USER_NAME, errorMessage, rootCause);
	}
}