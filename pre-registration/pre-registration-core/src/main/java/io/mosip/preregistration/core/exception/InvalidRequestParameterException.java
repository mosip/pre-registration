package io.mosip.preregistration.core.exception;

import java.util.List;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.core.common.dto.ExceptionJSONInfoDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import lombok.Getter;
import lombok.Setter;

/**
 * The InvalidRequestParameterException class represents an exception that
 * occurs an invalid request parameter is encountered.. This class extends
 * {@link BaseUncheckedException} and includes various constructors for
 * different use cases, as well as a {@link MainResponseDTO} and
 * {@link ExceptionJSONInfoDTO} object for additional error information.
 * 
 * @author M1046129
 *
 */

@Getter
@Setter
public class InvalidRequestParameterException extends BaseUncheckedException {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The main response DTO associated with the exception. */
	private MainResponseDTO<?> mainResponseDto;

	/** List of detailed exception information. */
	private List<ExceptionJSONInfoDTO> exptionList;

	/** The operation during which the exception occurred. */
	private String operation;

	/**
	 * Default constructor.
	 */
	public InvalidRequestParameterException() {
		super();
	}

	/**
	 * Constructs a new {@code InvalidRequestParameterException} with the specified
	 * error code, error message, and response.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 * @param response     the {@link MainResponseDTO} object containing additional
	 *                     information about the error.
	 */
	public InvalidRequestParameterException(String errorCode, String errorMessage, MainResponseDTO<?> response) {
		super(errorCode, errorMessage);
		this.mainResponseDto = response;
	}

	/**
	 * Constructs a new {@code InvalidRequestParameterException} with the specified
	 * error code, error message, rootCause, and response.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 * @param rootCause    the cause of the error.
	 * @param response     the {@link MainResponseDTO} object containing additional
	 *                     information about the error.
	 */
	public InvalidRequestParameterException(String errorCode, String errorMessage, Throwable rootCause,
			MainResponseDTO<?> response) {
		super(errorCode, errorMessage, rootCause);
		this.mainResponseDto = response;
	}

	/**
	 * Constructs a new {@code InvalidRequestParameterException} with the specified
	 * exptionList, and response.
	 * 
	 * @param exptionList the list of detailed exception information.
	 * @param response    the {@link MainResponseDTO} object containing additional
	 *                    information about the error.
	 */
	public InvalidRequestParameterException(List<ExceptionJSONInfoDTO> exptionList, MainResponseDTO<?> response) {
		this.mainResponseDto = response;
		this.exptionList = exptionList;
	}

	/**
	 * Constructs a new {@code InvalidRequestParameterException} with the specified
	 * exptionList, operation, and response.
	 * 
	 * @param exptionList the list of detailed exception information.
	 * @param operation   the operation during which the exception occurred.
	 * @param response    the {@link MainResponseDTO} object containing additional
	 *                    information about the error.
	 */
	public InvalidRequestParameterException(List<ExceptionJSONInfoDTO> exptionList, String operation,
			MainResponseDTO<?> response) {
		this.mainResponseDto = response;
		this.exptionList = exptionList;
		this.operation = operation;
	}
}
