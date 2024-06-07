package io.mosip.preregistration.core.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.exception.util.PreIssuanceExceptionCodes;
import lombok.Getter;
import lombok.Setter;

/**
 * The TablenotAccessibleException class represents an exception that occurs
 * system is not able to access registration table. This class extends
 * {@link BaseUncheckedException} and includes various constructors for
 * different use cases, as well as a {@link MainResponseDTO} object for
 * additional error information.
 *
 */
@Setter
@Getter
public class TableNotAccessibleException extends BaseUncheckedException {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The main response DTO associated with the exception. */
	private MainResponseDTO<?> mainResposneDTO;

	/**
	 * Default Constructor
	 */
	public TableNotAccessibleException() {
		super();
	}

	/**
	 * Constructs a new {@code TableNotAccessibleException} with the specified error
	 * errorMessage
	 * 
	 * @param errorMessage the detailed error errorMessage.
	 */
	public TableNotAccessibleException(String errorMessage) {
		super(PreIssuanceExceptionCodes.TABLE_NOT_FOUND_EXCEPTION, errorMessage);
	}

	/**
	 * Constructs a new {@code TableNotAccessibleException} with the specified error
	 * message, and rootCause.
	 * 
	 * @param errorMessage the detailed error message.
	 * @param rootCause    the cause of the error.
	 */
	public TableNotAccessibleException(String errorMessage, Throwable rootCause) {
		super(PreIssuanceExceptionCodes.TABLE_NOT_FOUND_EXCEPTION + EMPTY_SPACE, errorMessage, rootCause);
	}

	/**
	 * Constructs a new {@code TableNotAccessibleException} with the specified error
	 * code and error message.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 */
	public TableNotAccessibleException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	/**
	 * Constructs a new {@code TableNotAccessibleException} with the specified error
	 * code, error message, and response.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 * @param response     the {@link MainResponseDTO} object containing additional
	 *                     information about the error.
	 */
	public TableNotAccessibleException(String errorCode, String errorMessage, MainResponseDTO<?> response) {
		super(errorCode, errorMessage);
		this.mainResposneDTO = response;
	}

	/**
	 * Constructs a new {@code TableNotAccessibleException} with the specified error
	 * code, error message, and rootCause.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 * @param rootCause    the cause of the error.
	 */
	public TableNotAccessibleException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}
}