package io.mosip.preregistration.application.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import lombok.Data;

/**
 * The InvalidOtpOrUseridException class represents an exception indicating an
 * invalid OTP or User ID. This class extends {@link BaseUncheckedException} and
 * includes various constructors for different use cases, as well as a
 * {@link MainResponseDTO} object for additional error information.
 * 
 * @author Rajath KR
 * @since 1.0.0
 * 
 */

@Data
public class InvalidOtpOrUseridException extends BaseUncheckedException {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The main response DTO associated with the exception. */
	private MainResponseDTO<?> mainResponseDto;

	/**
	 * Default constructor
	 */
	public InvalidOtpOrUseridException() {
		super();
	}

	/**
	 * Constructs a new {@code InvalidOtpOrUseridException} with the specified error
	 * code, error message, rootCause, and response.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 * @param rootCause    the cause of the error.
	 * @param response     the {@link MainResponseDTO} object containing additional
	 *                     information about the error.
	 */
	public InvalidOtpOrUseridException(String errorCode, String errorMessage, Throwable rootCause,
			MainResponseDTO<?> response) {
		super(errorCode, errorMessage, rootCause);
		this.mainResponseDto = response;
	}

	/**
	 * Constructs a new {@code InvalidOtpOrUseridException} with the specified error
	 * code, error message, and response.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 * @param response     the {@link MainResponseDTO} object containing additional
	 *                     information about the error.
	 */
	public InvalidOtpOrUseridException(String errorCode, String errorMessage, MainResponseDTO<?> response) {
		super(errorCode, errorMessage);
		this.mainResponseDto = response;
	}

	/**
	 * Constructs a new {@code InvalidOtpOrUseridException} with the specified error
	 * message, and response.
	 * 
	 * @param errorMessage the detailed error message.
	 * @param response     the {@link MainResponseDTO} object containing additional
	 *                     information about the error.
	 */
	public InvalidOtpOrUseridException(String errorMessage, MainResponseDTO<?> response) {
		super(errorMessage);
		this.mainResponseDto = response;
	}
}