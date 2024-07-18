package io.mosip.preregistration.core.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import lombok.Getter;

/**
 * The TablenotAccessibleException class represents an exception that occurs
 * system is not able to access registration table. This class extends
 * {@link BaseUncheckedException} and includes various constructors for
 * different use cases, as well as a {@link MainResponseDTO} object for
 * additional error information.
 *
 * @author M1046129
 *
 */

@Getter
public class RestCallException extends BaseUncheckedException {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The main response DTO associated with the exception. */
	private MainResponseDTO<?> mainResponseDTO;

	/**
	 * Default Constructor
	 */
	public RestCallException() {
		super();
	}

	/**
	 * Constructs a new {@code RestCallException} with the specified error
	 * errorMessage
	 * 
	 * @param errorMessage the detailed error errorMessage.
	 */
	public RestCallException(String errorMessage) {
		super("", errorMessage);
	}

	/**
	 * Constructs a new {@code RestCallException} with the specified error
	 * message, and rootCause.
	 * 
	 * @param errorMessage the detailed error message.
	 * @param rootCause    the cause of the error.
	 */
	public RestCallException(String errorMessage, Throwable rootCause) {
		super("", errorMessage, rootCause);
	}

	/**
	 * Constructs a new {@code RestCallException} with the specified error
	 * code and error message.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 */
	public RestCallException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage, null);
	}

	/**
	 * Constructs a new {@code RestCallException} with the specified error
	 * code, error message, and response.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 * @param response     the {@link MainResponseDTO} object containing additional
	 *                     information about the error.
	 */
	public RestCallException(String errorCode, String errorMessage, MainResponseDTO<?> response) {
		super(errorCode, errorMessage, null);
		this.mainResponseDTO = response;
	}

	/**
	 * Constructs a new {@code RestCallException} with the specified error
	 * code, error message, and rootCause.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 * @param rootCause    the cause of the error.
	 */
	public RestCallException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}
}