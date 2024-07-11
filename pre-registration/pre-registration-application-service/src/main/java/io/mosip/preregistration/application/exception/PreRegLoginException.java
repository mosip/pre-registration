package io.mosip.preregistration.application.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;

/**
 * The PreRegLoginException class represents an exception that occurs during
 * pre-registration login. This class extends {@link BaseUncheckedException} and
 * includes various constructors for different use cases, as well as a
 * {@link MainResponseDTO} object for additional error information.
 * 
 * @author Rajath KR
 * @since 1.0.0
 * 
 */

public class PreRegLoginException extends BaseUncheckedException {
	 /** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	 /**
     * Default constructor.
     */
	public PreRegLoginException() {
		super();
	}

	/**
	 * Constructs a new {@code PreRegLoginException} with the specified
	 * error code and error message.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 */
	public PreRegLoginException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}