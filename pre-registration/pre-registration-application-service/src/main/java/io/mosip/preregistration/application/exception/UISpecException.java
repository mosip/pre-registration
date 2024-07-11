package io.mosip.preregistration.application.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import lombok.Getter;
import lombok.Setter;

/**
 * Exception class representing a specific UI specification error. This class
 * extends {@link BaseUncheckedException} and includes error code and message.
 * 
 * @version 1.0.0
 */

@Getter
@Setter
public class UISpecException extends BaseUncheckedException {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new {@code UISpecException} with the specified error code and
	 * message.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 */
	public UISpecException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}