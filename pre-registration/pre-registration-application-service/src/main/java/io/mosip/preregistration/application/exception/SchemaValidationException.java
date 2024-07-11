package io.mosip.preregistration.application.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import lombok.Getter;

/**
 * Exception class representing a schema validation failure. This class extends
 * {@link BaseUncheckedException} and includes various constructors for
 * different use cases, as well as a {@link MainResponseDTO} object for
 * additional error information.
 * 
 * @author Kishan Rathore
 * @since 1.0.0
 * 
 */

@Getter
public class SchemaValidationException extends BaseUncheckedException {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The main response DTO containing additional error information. */
	private MainResponseDTO<?> mainresponseDTO;

	/**
	 * Default constructor
	 */
	public SchemaValidationException() {
		super();
	}

	/**
	 * Constructs a new {@code SchemaValidationException} with the specified error
	 * message.
	 * 
	 * @param errorMessage the detailed error message.
	 */
	public SchemaValidationException(String errorMessage) {
		super("", errorMessage);
	}

	/**
	 * Constructs a new {@code SchemaValidationException} with the specified error
	 * code and error message.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 */
	public SchemaValidationException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage, null);
	}

	/**
	 * Constructs a new {@code SchemaValidationException} with the specified error
	 * code, error message, and response.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 * @param response     the {@link MainResponseDTO} object containing additional
	 *                     information about the error.
	 */
	public SchemaValidationException(String errorCode, String errorMessage, MainResponseDTO<?> response) {
		super(errorCode, errorMessage, null);
		this.mainresponseDTO = response;
	}

	/**
	 * Constructs a new {@code SchemaValidationException} with the specified error
	 * message and root cause.
	 * 
	 * @param errorMessage the detailed error message.
	 * @param rootCause    the cause of the error.
	 */
	public SchemaValidationException(String errorMessage, Throwable rootCause) {
		super("", errorMessage, rootCause);
	}

	/**
	 * Constructs a new {@code SchemaValidationException} with the specified error
	 * code, error message, and root cause.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 * @param rootCause    the cause of the error.
	 */
	public SchemaValidationException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}
}