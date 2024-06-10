/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.application.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import lombok.Getter;

/**
 * Exception class representing a failure when an unsupported language is
 * encountered. This class extends {@link BaseUncheckedException} and includes a
 * response DTO.
 * 
 * @author Kishan Rathore
 * @since 1.0.0
 * 
 */

@Getter
public class UnSupportedLanguageException extends BaseUncheckedException {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * The main response DTO associated with this exception.
	 */
	private MainResponseDTO<?> mainResponseDTO;

	/**
	 * Constructs a new {@code UnSupportedLanguageException} with the specified
	 * message and response.
	 * 
	 * @param msg      the detailed error message.
	 * @param response the main response DTO containing additional error details.
	 */
	public UnSupportedLanguageException(String msg, MainResponseDTO<?> response) {
		super("", msg);
		this.mainResponseDTO = response;
	}

	/**
	 * Constructs a new {@code UnSupportedLanguageException} with the specified
	 * error code, message, and response.
	 * 
	 * @param errCode  the error code representing the specific error condition.
	 * @param msg      the detailed error message.
	 * @param response the main response DTO containing additional error details.
	 */
	public UnSupportedLanguageException(String errCode, String msg, MainResponseDTO<?> response) {
		super(errCode, msg);
		this.mainResponseDTO = response;
	}

	/**
	 * Constructs a new {@code UnSupportedLanguageException} with the specified
	 * error code, message, cause, and response.
	 * 
	 * @param errCode  the error code representing the specific error condition.
	 * @param msg      the detailed error message.
	 * @param cause    the cause of the exception.
	 * @param response the main response DTO containing additional error details.
	 */
	public UnSupportedLanguageException(String errCode, String msg, Throwable cause, MainResponseDTO<?> response) {
		super(errCode, msg, cause);
		this.mainResponseDTO = response;
	}
}
