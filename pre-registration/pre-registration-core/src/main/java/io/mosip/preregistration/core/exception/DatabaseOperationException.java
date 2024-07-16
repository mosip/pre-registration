package io.mosip.preregistration.core.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.core.exception.util.PreIssuanceExceptionCodes;

/**
 * The DatabaseOperationException class represents an exception that occurs when
 * database operation fails. This class extends {@link BaseUncheckedException} and
 * includes various constructors for different use cases.
 *
 *
 * @author Jagadishwari S
 * @since 1.0.0
 *
 */

public class DatabaseOperationException  extends BaseUncheckedException {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Default Constructor
	 */
	public DatabaseOperationException() {
		super();
	}

	/**
	 * Constructs a new {@code DatabaseOperationException} with the specified error
	 * errorMessage
	 * 
	 * @param errorMessage the detailed error errorMessage.
	 */
	public DatabaseOperationException(String errorMessage) {
		super(PreIssuanceExceptionCodes.USER_INSERTION, errorMessage);
	}

	/**
	 * Constructs a new {@code DatabaseOperationException} with the specified error
	 * message, and rootCause.
	 * 
	 * @param errorMessage the detailed error message.
	 * @param rootCause    the rootCause of the error.
	 */
	public DatabaseOperationException(String errorMessage, Throwable rootCause) {
		super(PreIssuanceExceptionCodes.USER_INSERTION, errorMessage, rootCause);
	}
}