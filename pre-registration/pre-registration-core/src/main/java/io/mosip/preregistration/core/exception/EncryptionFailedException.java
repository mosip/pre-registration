package io.mosip.preregistration.core.exception;

import java.util.List;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import lombok.Getter;

/**
 * The EncryptionFailedException class represents an exception that occurs when
 * encryption fails. This class extends {@link BaseUncheckedException} and
 * includes various constructors for different use cases, as well as a
 * {@link MainResponseDTO} object for additional error information.
 *
 *
 * @author Jagadishwari S
 * @since 1.0.0
 *
 */
@Getter
public class EncryptionFailedException extends BaseUncheckedException {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The main response DTO associated with the exception. */
	private MainResponseDTO<?> mainresponseDTO;

	/** The List of exception as ServiceError. */
	private List<ServiceError> validationErrorList;

	/**
	 * Default Constructor
	 */
	public EncryptionFailedException() {
		super();
	}

	/**
	 * Constructs a new {@code EncryptionFailedException} with the specified error
	 * errorMessage
	 * 
	 * @param errorMessage the detailed error errorMessage.
	 */
	public EncryptionFailedException(String errorMessage) {
		super("", errorMessage);
	}

	/**
	 * Constructs a new {@code EncryptionFailedException} with the specified error
	 * code and error message.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 */
	public EncryptionFailedException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage, null);
	}

	/**
	 * Constructs a new {@code EncryptionFailedException} with the specified error
	 * message, and rootCause.
	 * 
	 * @param errorMessage the detailed error message.
	 * @param rootCause    the rootCause of the error.
	 */
	public EncryptionFailedException(String errorMessage, Throwable rootCause) {
		super("", errorMessage, rootCause);
	}

	/**
	 * Constructs a new {@code EncryptionFailedException} with the specified error
	 * code, error message, and response.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 * @param response     the {@link MainResponseDTO} object containing additional
	 *                     information about the error.
	 */
	public EncryptionFailedException(String errorCode, String errorMessage, MainResponseDTO<?> response) {
		super(errorCode, errorMessage, null);
		this.mainresponseDTO = response;
	}

	/**
	 * Constructs a new {@code EncryptionFailedException} with the specified error
	 * code, error message, and rootCause.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 * @param rootCause    the cause of the error.
	 */
	public EncryptionFailedException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}

	/**
	 * Constructs a new {@code EncryptionFailedException} with the specified list,
	 * and response.
	 * 
	 * @param list     the List of exception as ServiceError.
	 * @param response the {@link MainResponseDTO} object containing additional
	 *                 information about the error.
	 */
	public EncryptionFailedException(List<ServiceError> list, MainResponseDTO<?> response) {
		super();
		this.validationErrorList = list;
		this.mainresponseDTO = response;
	}
}