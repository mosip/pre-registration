package io.mosip.preregistration.application.exception;

import java.util.List;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import lombok.Getter;

/**
 * The NotificationSeriveException class represents an exception that occurs in
 * the notification service. This class extends {@link BaseUncheckedException}
 * and includes various constructors for different use cases, as well as a
 * {@link MainResponseDTO} object for additional error information.
 * 
 * @author Rajath KR
 * @since 1.0.0
 * 
 */

@Getter
public class NotificationSeriveException extends BaseUncheckedException {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 8039562312343751179L;

	 /** The validation error list. */
	private List<ServiceError> validationErrorList;
	/** The main response DTO associated with the exception. */
	private MainResponseDTO<?> mainResponseDTO;

	/**
     * Gets the validation error list.
     *
     * @return the validation error list
     */
	public List<ServiceError> getValidationErrorList() {
		return validationErrorList;
	}

	/**
     * Gets the main response DTO.
     *
     * @return the main response DTO
     */
	public MainResponseDTO<?> getMainResposneDTO() {
		return mainResponseDTO;
	}

	/**
     * Default constructor.
     */
	public NotificationSeriveException() {
		super();
	}

	/**
	 * Constructs a new {@code NotificationSeriveException} with the specified
	 * error code, error message, rootCause, and response.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 * @param rootCause    the cause of the error.
	 * @param response     the {@link MainResponseDTO} object containing additional
	 *                     information about the error.
	 */
	public NotificationSeriveException(String errorCode, String errorMessage, Throwable rootCause, MainResponseDTO<?> response) {
		super(errorCode, errorMessage, rootCause);
		this.mainResponseDTO = response;
	}

	/**
	 * Constructs a new {@code NotificationSeriveException} with the specified
	 * error code, error message, and response.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 * @param response     the {@link MainResponseDTO} object containing additional
	 *                     information about the error.
	 */
	public NotificationSeriveException(String errorCode, String errorMessage, MainResponseDTO<?> response) {
		super(errorCode, errorMessage);
		this.mainResponseDTO = response;
	}

	/**
	 * Constructs a new {@code NotificationSeriveException} with the specified
	 * error message, and response.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param response     the {@link MainResponseDTO} object containing additional
	 *                     information about the error.
	 */
	public NotificationSeriveException(String errorMessage, MainResponseDTO<?> response) {
		super(errorMessage);
		this.mainResponseDTO = response;
	}

	/**
	 * Constructs a new {@code NotificationSeriveException} with the specified
	 * validationErrorList, and response.
	 * 
	 * @param validationErrorList the validation error list
	 * @param response     the {@link MainResponseDTO} object containing additional
	 *                     information about the error.
	 */
	public NotificationSeriveException(List<ServiceError> validationErrorList, MainResponseDTO<?> response) {
		this.validationErrorList = validationErrorList;
		this.mainResponseDTO = response;
	}
}