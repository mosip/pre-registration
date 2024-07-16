package io.mosip.preregistration.application.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import lombok.Getter;

/**
 * The MandatoryFieldException class represents an exception indicating a
 * mandatory field error. This class extends {@link BaseUncheckedException} and
 * includes various constructors for different use cases, as well as a
 * {@link MainResponseDTO} object for additional error information.
 * 
 * @author Sanober Noor
 * @since 1.0.0
 */

@Getter
public class MandatoryFieldException extends BaseUncheckedException {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1298682891599963309L;
	/** The main response DTO associated with the exception. */
	private final MainResponseDTO<?> mainResponseDTO;

	/**
	 * Constructs a new {@code MandatoryFieldException} with the specified error
	 * message, and response.
	 * 
	 * @param errorMessage the detailed error message.
	 * @param response     the {@link MainResponseDTO} object containing additional
	 *                     information about the error.
	 */
	public MandatoryFieldException(String errorMessage, MainResponseDTO<?> response) {
		super("", errorMessage);
		this.mainResponseDTO = response;
	}

	/**
	 * Constructs a new {@code MandatoryFieldException} with the specified error
	 * message, rootCause, and response.
	 * 
	 * @param errorMessage the detailed error message.
	 * @param rootCause    the cause of the error.
	 * @param response     the {@link MainResponseDTO} object containing additional
	 *                     information about the error.
	 */
	public MandatoryFieldException(String errorMessage, Throwable rootCause, MainResponseDTO<?> response) {
		super("", errorMessage, rootCause);
		this.mainResponseDTO = response;
	}

	/**
	 * Constructs a new {@code MandatoryFieldException} with the specified error
	 * code, error message, and response.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 * @param response     the {@link MainResponseDTO} object containing additional
	 *                     information about the error.
	 */
	public MandatoryFieldException(String errorCode, String errorMessage, MainResponseDTO<?> response) {
		super(errorCode, errorMessage, null);
		this.mainResponseDTO = response;
	}

	/**
	 * Constructs a new {@code MandatoryFieldException} with the specified error
	 * code, error message, rootCause, and response.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 * @param rootCause    the cause of the error.
	 * @param response     the {@link MainResponseDTO} object containing additional
	 *                     information about the error.
	 */
	public MandatoryFieldException(String errorCode, String errorMessage, Throwable rootCause,
			MainResponseDTO<?> response) {
		super(errorCode, errorMessage, rootCause);
		this.mainResponseDTO = response;
	}
}