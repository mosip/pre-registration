package io.mosip.preregistration.core.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * The InvalidPreRegistrationIdException class represents an exception that
 * occurs when an invalid pre-registration ID is encountered. This class extends
 * {@link BaseUncheckedException} and includes various constructors for
 * different use cases.
 *
 * @author M1046129
 *
 */
public class InvalidPreRegistrationIdException extends BaseUncheckedException {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -3898906527162403384L;

	/**
	 * Constructs a new {@code InvalidPreRegistrationIdException} with the specified
	 * error code and error message.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 */
	public InvalidPreRegistrationIdException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}