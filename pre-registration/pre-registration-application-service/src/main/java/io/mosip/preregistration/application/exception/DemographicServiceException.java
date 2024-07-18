package io.mosip.preregistration.application.exception;

import java.util.List;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import lombok.Getter;

/**
 * The DemographicServiceException class represents an exception specific to
 * demographic service errors. This class extends {@link BaseUncheckedException}
 * and includes various constructors for different use cases, as well as a
 * {@link MainResponseDTO} object for additional error information.
 * 
 * 
 * @author Tapaswini Behera
 * @since 1.0.0
 * 
 */

@Getter
public class DemographicServiceException extends BaseUncheckedException {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The list of validation errors. */
	private List<ServiceError> validationErrorList;

	/** The main response DTO associated with the exception. */
	private MainResponseDTO<?> mainResposneDTO;

	/**
	 * Constructs a new {@code DemographicServiceException} with the specified
	 * validationErrorList, and response.
	 * 
	 * @param validationErrorList The list of validation errors.
	 * @param response            the {@link MainResponseDTO} object containing
	 *                            additional information about the error.
	 */
	public DemographicServiceException(List<ServiceError> validationErrorList, MainResponseDTO<?> response) {
		this.validationErrorList = validationErrorList;
		this.mainResposneDTO = response;
	}

	/**
	 * Constructs a new {@code DemographicServiceException} with the specified error
	 * code and error message.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 */
	public DemographicServiceException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}