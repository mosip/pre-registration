/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.application.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.application.errorcodes.DemographicErrorCodes;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import lombok.Getter;

/**
 * The DocumentFailedToDeleteException class represents an exception that occurs
 * when the document fails to delete. This class extends
 * {@link BaseUncheckedException} and includes various constructors for
 * different use cases, as well as a {@link MainResponseDTO} object for
 * additional error information.
 * 
 * 
 * @author Tapaswini Behera
 * @since 1.0.0
 * 
 */
@Getter
public class DocumentFailedToDeleteException extends BaseUncheckedException {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The main response DTO associated with the exception. */
	private MainResponseDTO<?> mainresponseDTO;

	/**
	 * Default constructore
	 */
	public DocumentFailedToDeleteException() {
		super();
	}

	/**
	 * Constructs a new {@code DocumentFailedToDeleteException} with the specified
	 * error message
	 * 
	 * @param errorMessage the detailed error message.
	 */
	public DocumentFailedToDeleteException(String errorMessage) {
		super(DemographicErrorCodes.PRG_PAM_DOC_015.toString(), errorMessage);
	}

	/**
	 * Constructs a new {@code DocumentFailedToDeleteException} with the specified
	 * error code and error message.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 */
	public DocumentFailedToDeleteException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage, null);
	}

	/**
	 * Constructs a new {@code DocumentFailedToDeleteException} with the specified
	 * error code, error message, and response.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 * @param response     the {@link MainResponseDTO} object containing additional
	 *                     information about the error.
	 */
	public DocumentFailedToDeleteException(String errorCode, String errorMessage, MainResponseDTO<?> response) {
		super(errorCode, errorMessage, null);
		this.mainresponseDTO = response;
	}

	/**
	 * Constructs a new {@code DocumentFailedToDeleteException} with the specified
	 * error message, and rootCause.
	 * 
	 * @param errorMessage the detailed error message.
	 * @param rootCause    the cause of the error.
	 */
	public DocumentFailedToDeleteException(String errorMessage, Throwable rootCause) {
		super(DemographicErrorCodes.PRG_PAM_DOC_015.toString(), errorMessage, rootCause);
	}

	/**
	 * Constructs a new {@code DocumentFailedToDeleteException} with the specified
	 * error code, error message, and rootCause.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 * @param rootCause    the cause of the error.
	 */
	public DocumentFailedToDeleteException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}
}