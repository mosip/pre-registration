package io.mosip.preregistration.application.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import lombok.Getter;

/**
 * The QrCodeIOException class represents an exception that occurs when an I/O
 * error is encountered while working with QR codes. This class extends
 * {@link BaseUncheckedException} and includes various constructors for
 * different use cases, as well as a {@link MainResponseDTO} object for
 * additional error information.
 * 
 * @author Rajath KR
 * @since 1.0.0
 * 
 */

@Getter
public class QrCodeIOException extends BaseUncheckedException {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -6723728155340185347L;

	/** The main response DTO associated with the exception. */
	private MainResponseDTO<?> mainResponseDTO;

	/**
	 * Default constructor.
	 */
	public QrCodeIOException() {
		super();
	}

	/**
	 * Constructs a new {@code QrCodeIOException} with the specified error message.
	 * 
	 * @param errorMessage the detailed error message.
	 */
	public QrCodeIOException(String errorMessage) {
		super(errorMessage);
	}

	/**
	 * Constructs a new {@code QrCodeIOException} with the specified
	 * error code, error message, and response.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 * @param response     the {@link MainResponseDTO} object containing additional
	 *                     information about the error.
	 */
	public QrCodeIOException(String errorCode, String errorMessage, MainResponseDTO<?> response) {
		super(errorCode, errorMessage);
		this.mainResponseDTO = response;
	}

	/**
	 * Constructs a new {@code QrCodeIOException} with the specified
	 * error message and root cause.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 * @param rootCause    the cause of the error.
	 * @param response     the {@link MainResponseDTO} object containing additional
	 *                     information about the error.
	 */
	public QrCodeIOException(String errorCode, String errorMessage, Throwable rootCause, MainResponseDTO<?> response) {
		super(errorCode, errorMessage, rootCause);
		this.mainResponseDTO = response;
	}
}