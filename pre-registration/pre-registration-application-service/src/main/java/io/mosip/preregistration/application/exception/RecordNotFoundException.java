/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.application.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import lombok.Getter;

/**
 * The RecordNotFoundException class represents an exception that occurs when a
 * record is not found. This class extends {@link BaseUncheckedException} and
 * includes various constructors for different use cases, as well as a
 * {@link MainResponseDTO} object for additional error information.
 * 
 * @author Tapaswini Behera
 * @since 1.0.0
 * 
 */
@Getter
public class RecordNotFoundException extends BaseUncheckedException {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	/** The main response DTO containing additional error information. */
	private MainResponseDTO<?> mainresponseDTO;

	/**
	 * Constructs a new {@code RecordNotFoundException} with the specified error
	 * message.
	 * 
	 * @param errorMessage the detailed error message.
	 */
	public RecordNotFoundException(String errorMessage) {
		super("", errorMessage);
	}

	/**
	 * Constructs a new {@code RecordNotFoundException} with the specified error
	 * code and error message.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 */
	public RecordNotFoundException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	/**
	 * Constructs a new {@code RecordNotFoundException} with the specified error
	 * code, error message, and response.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 * @param response     the {@link MainResponseDTO} object containing additional
	 *                     information about the error.
	 */
	public RecordNotFoundException(String errorCode, String errorMessage, MainResponseDTO<?> response) {
		super(errorCode, errorMessage);
		this.mainresponseDTO = response;
	}

	/**
	 * Constructs a new {@code RecordNotFoundException} with the specified error
	 * message and root cause.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 * @param rootCause    the cause of the error.
	 */
	public RecordNotFoundException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}
}