package io.mosip.preregistration.application.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import lombok.Getter;

/**
 * The InvalidDateFormatException class represents an exception indicating an
 * invalid date format. This class extends {@link BaseUncheckedException} and
 * includes various constructors for different use cases, as well as a
 * {@link MainResponseDTO} object for additional error information.
 * 
 * @author Rajath KR
 * @since 1.0.0
 * 
 */

@Getter
public class InvalidDateFormatException extends BaseUncheckedException {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The main response DTO associated with the exception. */
	private MainResponseDTO<?> mainResponseDTO;
	
	 /**
     * Default constructor.
     */
	public InvalidDateFormatException() {
		super();
	}

	/**
	 * Constructs a new {@code InvalidDateFormatException} with the specified
	 * error code, error message, and rootCause.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 * @param rootCause    the cause of the error.
	 */
	public InvalidDateFormatException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}

	/**
	 * Constructs a new {@code InvalidDateFormatException} with the specified
	 * error code and error message.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 */
	public InvalidDateFormatException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	/**
	 * Constructs a new {@code InvalidDateFormatException} with the specified
	 * error code, error message, and response.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 * @param response     the {@link MainResponseDTO} object containing additional
	 *                     information about the error.
	 */
	public InvalidDateFormatException(String errorCode, String errorMessage, MainResponseDTO<?> response) {
		super(errorCode, errorMessage);
		this.mainResponseDTO = response;
	}

	/**
	 * Constructs a new {@code InvalidDateFormatException} with the specified
	 * error message
	 * 
	 * @param errorMessage the detailed error message.
	 */
	public InvalidDateFormatException(String errorMessage) {
		super(errorMessage);
	}
}