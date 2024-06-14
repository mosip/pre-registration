/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.application.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import lombok.Getter;

/**
 * The RecordNotFoundForPreIdsException class represents an exception that
 * occurs when records are not found for the given pre-registration IDs. This
 * class extends {@link BaseUncheckedException} and includes various
 * constructors for different use cases, as well as a {@link MainResponseDTO}
 * object for additional error information.
 * 
 * @author Jagadishwari S
 * @since 1.0.0
 * 
 */
@Getter
public class RecordNotFoundForPreIdsException extends BaseUncheckedException {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	/** The main response DTO containing additional error information. */
	private MainResponseDTO<?> mainResponseDTO;

	/**
	 * Constructs a new {@code RecordNotFoundForPreIdsException} with the specified
	 * error message.
	 * 
	 * @param errorMessage the detailed error message.
	 */
	public RecordNotFoundForPreIdsException(String errorMessage) {
		super("", errorMessage);
	}

	/**
	 * Constructs a new {@code RecordNotFoundForPreIdsException} with the specified
	 * error code and error message.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 */
	public RecordNotFoundForPreIdsException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	/**
	 * Constructs a new {@code RecordNotFoundForPreIdsException} with the specified
	 * error code, error message, and response.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 * @param response     the {@link MainResponseDTO} object containing additional
	 *                     information about the error.
	 */
	public RecordNotFoundForPreIdsException(String errorCode, String errorMessage, MainResponseDTO<?> response) {
		super(errorCode, errorMessage);
		this.mainResponseDTO = response;
	}

	/**
	 * Constructs a new {@code RecordNotFoundForPreIdsException} with the specified
	 * error message and root cause.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 * @param rootCause    the cause of the error.
	 */
	public RecordNotFoundForPreIdsException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}
}