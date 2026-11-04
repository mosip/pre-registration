package io.mosip.preregistration.booking.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import lombok.Getter;

/**
 * Thrown when an appointment is requested outside the allowed time span.
 */
@Getter
public class TimeSpanException extends BaseUncheckedException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5676105896729083192L;
	/** MOSIP envelope copied onto the error response. */
	private MainResponseDTO<?> mainResponseDTO;

	/**
	 * @param msg error text
	 */
	public TimeSpanException(String msg) {
		super("", msg);
	}

	public TimeSpanException(String msg, Throwable cause) {
		super("", msg, cause);
	}

	public TimeSpanException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage, null);
	}
	
	public TimeSpanException(String errorCode, String errorMessage,MainResponseDTO<?> response) {
		super(errorCode, errorMessage, null);
		this.mainResponseDTO=response;
	}

	public TimeSpanException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}

	public TimeSpanException() {
		super();
	}
}
