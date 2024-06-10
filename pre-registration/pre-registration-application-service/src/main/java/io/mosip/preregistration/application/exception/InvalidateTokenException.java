package io.mosip.preregistration.application.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import lombok.Getter;
import lombok.Setter;

/**
 * The InvalidateTokenException class represents an exception that occurs when
 * invalidate token failed scenario. This class extends
 * {@link BaseUncheckedException} and includes various constructors for
 * different use cases, as well as a {@link MainResponseDTO} object for
 * additional error information.
 * 
 * @author Akshay Jain
 * @since 1.0.0
 *
 */

@Getter
@Setter
public class InvalidateTokenException extends BaseUncheckedException {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The main response DTO associated with the exception. */
	private MainResponseDTO<?> mainResponseDto;

	/**
	 * Constructs a new {@code OperationNotAllowedException} with the specified
	 * error code, error message, and response.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 * @param response     the {@link MainResponseDTO} object containing additional
	 *                     information about the error.
	 */
	public InvalidateTokenException(String errorCode, String errorMessage, MainResponseDTO<?> response) {
		super(errorCode, errorMessage);
		this.mainResponseDto = response;
	}
}