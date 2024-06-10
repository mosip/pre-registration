package io.mosip.preregistration.core.exception;

import java.util.List;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import lombok.Getter;

/**
 * The NotificationException class represents an exception that occurs when
 * Notification error occurs. This class extends {@link BaseUncheckedException}
 * and includes various constructors for different use cases, as well as a
 * {@link MainResponseDTO} object for additional error information.
 *
 */

@Getter
public class NotificationException extends BaseUncheckedException {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The main response DTO associated with the exception. */
	private MainResponseDTO<?> mainResponseDTO;

	/** The List of exception as ServiceError */
	private List<ServiceError> validationErrorList;

	/**
	 * Default Constructor
	 */
	public NotificationException() {
		super();
	}

	/**
	 * Constructs a new {@code NotificationException} with the specified
	 * validationErrorList, and response.
	 * 
	 * @param validationErrorList The List of exception as ServiceError.
	 * @param response            the {@link MainResponseDTO} object containing
	 *                            additional information about the error.
	 */
	public NotificationException(List<ServiceError> validationErrorList, MainResponseDTO<?> response) {
		this.validationErrorList = validationErrorList;
		this.mainResponseDTO = response;
	}

	/**
	 * Constructs a new {@code NotificationException} with the specified error code,
	 * error message, and rootCause.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 * @param rootCause    the cause of the error.
	 * @param response     the {@link MainResponseDTO} object containing additional
	 *                     information about the error.
	 */
	public NotificationException(String errorCode, String errorMessage, Throwable rootCause,
			MainResponseDTO<?> response) {
		super(errorCode, errorMessage, rootCause);
		this.mainResponseDTO = response;
	}

	/**
	 * Constructs a new {@code NotificationException} with the specified error code,
	 * error message, and response.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 * @param response     the {@link MainResponseDTO} object containing additional
	 *                     information about the error.
	 */
	public NotificationException(String errorCode, String errorMessage, MainResponseDTO<?> response) {
		super(errorCode, errorMessage);
		this.mainResponseDTO = response;
	}

	/**
	 * Constructs a new {@code NotificationException} with the specified error
	 * message, and response.
	 * 
	 * @param errorMessage the detailed error message.
	 * @param response     the {@link MainResponseDTO} object containing additional
	 *                     information about the error.
	 */
	public NotificationException(String errorMessage, MainResponseDTO<?> response) {
		super(errorMessage);
		this.mainResponseDTO = response;
	}
}