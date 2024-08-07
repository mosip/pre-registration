package io.mosip.preregistration.application.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import lombok.Getter;

/**
 * The DeprecatedException class represents an exception that occurs when
 * function is deprecated.
 * 
 * 
 * @author Tapaswini Behera
 * @since 1.0.0
 * 
 */

@Getter
public class DeprecatedException extends BaseUncheckedException {
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
	 * Constructs a new DeprecatedException with the specified error code and
	 * message.
	 * 
	 * @param errorCode    The error code associated with the audit failure.
	 * @param errorMessage A descriptive message explaining the reason for the audit
	 *                     failure.
	 */
	public DeprecatedException(String errorCode, String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}
}