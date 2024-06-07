/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.application.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import lombok.Getter;

/**
 * The MandatoryFieldRequiredException class represents an exception indicating
 * that a mandatory field is required. This class extends
 * {@link BaseUncheckedException} and includes various constructors for
 * different use cases, as well as a {@link MainResponseDTO} object for
 * additional error information.
 * 
 * @author Kishan Rathore
 * @since 1.0.0
 *
 */

@Getter
public class MandatoryFieldRequiredException extends BaseUncheckedException {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -620822827826136129L;

	/** The main response DTO associated with the exception. */
	private MainResponseDTO<?> mainResponseDTO;

	/**
	 * Constructs a new MandatoryFieldRequiredException.
	 */
	public MandatoryFieldRequiredException() {
		super();
	}

	/**
	 * Constructs a new {@code MandatoryFieldRequiredException} with the specified
	 * error message, and response.
	 * 
	 * @param errorMessage the detailed error message.
	 * @param response     the {@link MainResponseDTO} object containing additional
	 *                     information about the error.
	 */
	public MandatoryFieldRequiredException(String errorMessage, MainResponseDTO<?> response) {
		super("", errorMessage);
		this.mainResponseDTO = response;
	}

	/**
	 * Constructs a new {@code MandatoryFieldRequiredException} with the specified
	 * error message, rootCause, and response.
	 * 
	 * @param errorMessage the detailed error message.
	 * @param rootCause    the cause of the error.
	 * @param response     the {@link MainResponseDTO} object containing additional
	 *                     information about the error.
	 */
	public MandatoryFieldRequiredException(String errorMessage, Throwable rootCause, MainResponseDTO<?> response) {
		super("", errorMessage, rootCause);
		this.mainResponseDTO = response;
	}

	/**
	 * Constructs a new {@code MandatoryFieldRequiredException} with the specified
	 * error code, error message, and response.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 * @param response     the {@link MainResponseDTO} object containing additional
	 *                     information about the error.
	 */
	public MandatoryFieldRequiredException(String errorCode, String errorMessage, MainResponseDTO<?> response) {
		super(errorCode, errorMessage, null);
		this.mainResponseDTO = response;
	}

	/**
	 * Constructs a new {@code MandatoryFieldRequiredException} with the specified
	 * error code, error message, and rootCause.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 * @param rootCause    the cause of the error.
	 */
	public MandatoryFieldRequiredException(String errorCode, String errorMessage, Throwable rootCause,
			MainResponseDTO<?> response) {
		super(errorCode, errorMessage, rootCause);
		this.mainResponseDTO = response;
	}
}