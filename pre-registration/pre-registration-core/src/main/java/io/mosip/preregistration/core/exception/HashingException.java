package io.mosip.preregistration.core.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import lombok.Getter;

/**
 * The HashingException class represents an exception that occurs when a hashing
 * operation fails. This class extends {@link BaseUncheckedException} and
 * includes various constructors for different use cases, as well as a
 * {@link MainResponseDTO} object for additional error information.
 *
 * @author Sanober Noor
 * @since 1.0.0
 *
 */
@Getter
public class HashingException extends BaseUncheckedException {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The main response DTO associated with the exception. */
	private MainResponseDTO<?> mainresponseDTO;

	/**
	 * Default Constructor
	 */
	public HashingException() {
		super();
	}

	/**
	 * Constructs a new {@code HashingException} with the specified error
	 * errorMessage
	 * 
	 * @param errorMessage the detailed error errorMessage.
	 */
	public HashingException(String errorMessage) {
		super(errorMessage);
	}

	/**
	 * Constructs a new {@code HashingException} with the specified error code and
	 * error message.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 */
	public HashingException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	/**
	 * Constructs a new {@code HashingException} with the specified error code,
	 * error message, and rootCause.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 * @param rootCause    the cause of the error.
	 */
	public HashingException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}

	/**
	 * Constructs a new {@code HashingException} with the specified error code,
	 * error message, and response.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 * @param response     the {@link MainResponseDTO} object containing additional
	 *                     information about the error.
	 */
	public HashingException(String errorCode, String errorMessage, MainResponseDTO<?> response) {
		super(errorCode, errorMessage);
		this.mainresponseDTO = response;
	}
}