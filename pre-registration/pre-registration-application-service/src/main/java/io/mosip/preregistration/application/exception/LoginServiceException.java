package io.mosip.preregistration.application.exception;

import java.util.List;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;

/**
 * The LoginServiceException class represents an exception indicating a login
 * service error. This class extends {@link BaseUncheckedException} and includes
 * various constructors for different use cases, as well as a
 * {@link MainResponseDTO} object for additional error information.
 * 
 * @author Rajath KR
 * @since 1.0.0
 * 
 */
public class LoginServiceException extends BaseUncheckedException {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The validation error list. */
	private List<ServiceError> validationErrorList;

	/** The main response DTO associated with the exception. */
	private MainResponseDTO<?> mainResposneDTO;

	/**
	 * Gets the validation error list.
	 *
	 * @return the validation error list
	 */
	public List<ServiceError> getValidationErrorList() {
		return validationErrorList;
	}

	/**
	 * Constructs a new {@code LoginServiceException} with the specified validation error list, and response.
	 * 
	 * @param validationErrorList the validation error list.
	 * @param response     the {@link MainResponseDTO} object containing additional
	 *                     information about the error.
	 */
	public LoginServiceException(List<ServiceError> validationErrorList, MainResponseDTO<?> response) {
		this.validationErrorList = validationErrorList;
		this.mainResposneDTO = response;
	}

	/**
	 * Gets the main response DTO.
	 *
	 * @return the main response DTO
	 */
	public MainResponseDTO<?> getMainResposneDTO() {
		return mainResposneDTO;
	}
}