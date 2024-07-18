package io.mosip.preregistration.application.exception;

import java.util.List;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import lombok.Getter;

/**
 * The IdValidationException class represents an exception indicating validation
 * errors related to IDs. This class extends {@link BaseUncheckedException} and
 * includes various constructors for different use cases, as well as a
 * {@link MainResponseDTO} object for additional error information.
 * 
 * @author Rajath KR
 * @since 1.0.0
 * 
 */
@Getter
public class IdValidationException extends BaseUncheckedException {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** errorList the list of error messages. */
	private List<String> errorMessageList;

	/** The main response DTO associated with the exception. */
	private MainResponseDTO<?> mainResposneDTO;

	/**
	 * Constructs a new {@code IdValidationException} with the specified error code,
	 * errrorList and response.
	 * 
	 * @param errrorList errorList the list of error messages.
	 * @param response   the {@link MainResponseDTO} object containing additional
	 *                   information about the error.
	 */
	public IdValidationException(String errorCode, List<String> errrorList, MainResponseDTO<?> response) {
		super(errorCode, null);
		this.errorMessageList = errrorList;
		this.mainResposneDTO = response;
	}
}