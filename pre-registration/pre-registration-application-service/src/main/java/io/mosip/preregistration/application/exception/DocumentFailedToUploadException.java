/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.application.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import lombok.Getter;

/**
 * The DocumentFailedToUploadException class represents an exception that occurs
 * document upload fails. This class extends {@link BaseUncheckedException} and
 * includes various constructors for different use cases, as well as a
 * {@link MainResponseDTO} object for additional error information.
 * 
 * @author Kishan Rathore
 * @since 1.0.0
 * 
 */

@Getter
public class DocumentFailedToUploadException extends BaseUncheckedException {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The main response DTO associated with the exception. */
	private MainResponseDTO<?> response;

	/**
	 * Default constructor
	 */
	public DocumentFailedToUploadException() {
		super();
	}

	/**
	 * Constructs a new {@code DocumentFailedToUploadException} with the specified
	 * error code, error message, and rootCause.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 * @param rootCause    the cause of the error.
	 */
	public DocumentFailedToUploadException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}

	/**
	 * Constructs a new {@code DocumentFailedToUploadException} with the specified
	 * error code and error message.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 */
	public DocumentFailedToUploadException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	/**
	 * Constructs a new {@code DocumentFailedToUploadException} with the specified
	 * error code, error errorMessage, and response.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error errorMessage.
	 * @param response     the {@link MainResponseDTO} object containing additional
	 *                     information about the error.
	 */
	public DocumentFailedToUploadException(String errorCode, String errorMessage, MainResponseDTO<?> response) {
		super(errorCode, errorMessage);
		this.response = response;
	}
}