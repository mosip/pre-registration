package io.mosip.preregistration.application.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import lombok.Getter;
import lombok.Setter;

/**
 * Exception class representing a failure when an OTP (One-Time Password) verification fails for a user ID.
 * This class extends {@link BaseUncheckedException} and includes a response DTO.
 *  
 * @author Akshay Jain
 * @since 1.0.0
 *
 */

@Getter
@Setter
public class UserIdOtpFaliedException extends BaseUncheckedException {
	private static final long serialVersionUID = 1L;

	 /**
     * The main response DTO associated with this exception.
     */
	private MainResponseDTO<?> mainResponseDto;

	/**
     * Constructs a new {@code UserIdOtpFaliedException} with the specified error code, error message, and response.
     * 
     * @param errorCode the error code representing the specific error condition.
     * @param errorMessage the detailed error message.
     * @param response the main response DTO containing additional error details.
     */
	public UserIdOtpFaliedException(String errorCode, String errorMessage, MainResponseDTO<?> response) {
		super(errorCode, errorMessage);
		this.mainResponseDto = response;
	}
}