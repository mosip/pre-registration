package io.mosip.preregistration.core.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.core.exception.util.PreIssuanceExceptionCodes;

/**
 * The UserAlreadyExistsException class represents an exception that occurs when
 * the user already registered. This class extends
 * {@link BaseUncheckedException} and includes various constructors for
 * different use case.
 *
 */
public class UserAlreadyExistsException extends BaseUncheckedException {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
     * Default Constructor
     */
	public UserAlreadyExistsException() {
		super();
	}

	/**
	 * Constructs a new {@code UserAlreadyExistsException} with the specified
	 * error errorMessage
	 * 
	 * @param errorMessage the detailed error errorMessage.
	 */
	public UserAlreadyExistsException(String errorMessage) {
		super(PreIssuanceExceptionCodes.USER_ALREADY_EXIST, errorMessage);
	}

	/**
	 * Constructs a new {@code UserNameNotValidException} with the specified
	 * error message, and rootCause.
	 * 
	 * @param errorMessage the detailed error message.
	 * @param rootCause    the cause of the error.
	 */
	public UserAlreadyExistsException(String errorMessage, Throwable rootCause) {
		super(PreIssuanceExceptionCodes.USER_ALREADY_EXIST, errorMessage, rootCause);
	}
}