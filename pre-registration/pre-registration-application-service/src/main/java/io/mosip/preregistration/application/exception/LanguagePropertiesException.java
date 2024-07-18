package io.mosip.preregistration.application.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import lombok.Getter;

/**
 * The LanguagePropertiesException class represents an exception indicating an
 * error related to language properties. This class extends
 * {@link BaseUncheckedException} and includes various constructors for
 * different use cases, as well as a {@link MainResponseDTO} object for
 * additional error information.
 * 
 * @author Rajath KR
 * @since 1.0.0
 * 
 */

@Getter
public class LanguagePropertiesException extends BaseUncheckedException {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * The error code associated with the audit failure.
	 */
	private final String errorCode;

	/**
	 * A descriptive message explaining the reason for the audit failure.
	 */
	private final String errorMessage;

	/**
	 * Constructs a new {@code LanguagePropertiesException} with the specified error
	 * code and error message.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 */
	public LanguagePropertiesException(String errorCode, String errorMessage) {
		super();
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}
}