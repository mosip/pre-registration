package io.mosip.preregistration.application.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import lombok.Getter;
import lombok.Setter;

/**
 * Exception class representing a failure in sending OTP (One-Time Password).
 * This class extends {@link BaseUncheckedException} and includes an error code,
 * error message, and a {@link MainResponseDTO} object.
 * 
 * @author Akshay Jain
 * @since 1.0.0
 *
 */

@Getter
@Setter
public class SendOtpFailedException extends BaseUncheckedException {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The main response DTO. */
	private MainResponseDTO<?> mainResposneDto;

	/**
	 * Constructs a new {@code SendOtpFailedException} with the specified error
	 * code, error message, and response.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 * @param response     the {@link MainResponseDTO} object containing additional
	 *                     information about the error.
	 */
	public SendOtpFailedException(String errorCode, String errorMessage, MainResponseDTO<?> response) {
		super(errorCode, errorMessage);
		this.mainResposneDto = response;
	}
}