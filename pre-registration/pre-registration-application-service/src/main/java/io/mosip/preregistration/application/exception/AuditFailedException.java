package io.mosip.preregistration.application.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import lombok.Data;

/**
 * The AuditFailedException class represents an exception that occurs
 * when audit fails.
 * 
 * 
 * @author Tapaswini Behera
 * @since 1.0.0
 * 
 */

@Data
public class AuditFailedException extends BaseUncheckedException {
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
	 * Constructs a new AuditFailedException with the specified error code and
	 * message.
	 * 
	 * @param errorCode    The error code associated with the audit failure.
	 * @param errorMessage A descriptive message explaining the reason for the audit
	 *                     failure.
	 */
	public AuditFailedException(String errorCode, String errorMessage) {
		super();
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}
}