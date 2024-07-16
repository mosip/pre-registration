package io.mosip.preregistration.core.exception;

/**
 * The IllegalParamException class represents an exception
 * occurs when an illegal request parameter is encountered.. This class extends
 * {@link BaseUncheckedException} and includes various constructors for
 * different use cases, as well as a {@link MainResponseDTO} object for additional error information.
 * 
 * @author Sanober Noor
 * @since 1.0.0
 *
 */
import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import lombok.Getter;

@Getter
public class IllegalParamException extends BaseUncheckedException {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The main response DTO associated with the exception. */
	private MainResponseDTO<?> mainResponseDto;
	/**
	 * Constructs a new {@code IllegalParamException} with the specified
	 * error message, and response.
	 * 
	 * @param errorMessage the detailed error message.
	 * @param response     the {@link MainResponseDTO} object containing additional
	 *                     information about the error.
	 */
	public IllegalParamException(String errorMessage, MainResponseDTO<?> response) {
		super("", errorMessage);
		this.mainResponseDto = response;
	}

	/**
	 * Constructs a new {@code IllegalParamException} with the specified
	 * error code, error message, and response.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 * @param response     the {@link MainResponseDTO} object containing additional
	 *                     information about the error.
	 */
	public IllegalParamException(String errorCode, String errorMessage, MainResponseDTO<?> response) {
		super(errorCode, errorMessage);
		this.mainResponseDto = response;
	}

	/**
	 * Constructs a new {@code IllegalParamException} with the specified
	 * error code, error message, rootCause, and response.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 * @param rootCause    the rootCause of the error.
	 * @param response     the {@link MainResponseDTO} object containing additional
	 *                     information about the error.
	 */
	public IllegalParamException(String errorCode, String errorMessage, Throwable rootCause, MainResponseDTO<?> response) {
		super(errorCode, errorMessage, rootCause);
		this.mainResponseDto = response;
	}
}