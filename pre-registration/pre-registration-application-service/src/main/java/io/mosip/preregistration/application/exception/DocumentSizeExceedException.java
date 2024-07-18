/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.application.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import lombok.Getter;

/**
 * The DocumentSizeExceedException class represents an exception that occurs
 * when the document size exceeds the limit. This class extends
 * {@link BaseUncheckedException} and includes various constructors for
 * different use cases, as well as a {@link MainResponseDTO} object for
 * additional error information.
 * 
 * @author Kishan Rathore
 * @since 1.0.0
 * 
 */

@Getter
public class DocumentSizeExceedException extends BaseUncheckedException {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The main response DTO associated with the exception. */
	private MainResponseDTO<?> response;

	/**
	 * Default constructor
	 */
	public DocumentSizeExceedException() {
		super();
	}

	/**
	 * Constructs a new {@code DocumentSizeExceedException} with the specified error
	 * code, error errorMessage, and rootCause.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error errorMessage.
	 * @param rootCause    the cause of the error.
	 */
	public DocumentSizeExceedException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}

	/**
	 * Constructs a new {@code DocumentSizeExceedException} with the specified error
	 * message, and rootCause.
	 * 
	 * @param errorMessage the detailed error message.
	 * @param rootCause    the cause of the error.
	 */
	public DocumentSizeExceedException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	/**
	 * Constructs a new {@code DocumentSizeExceedException} with the specified error
	 * code, error message, and response.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 * @param response     the {@link MainResponseDTO} object containing additional
	 *                     information about the error.
	 */
	public DocumentSizeExceedException(String errorCode, String errorMessage, MainResponseDTO<?> response) {
		super(errorCode, errorMessage);
		this.response = response;
	}
}