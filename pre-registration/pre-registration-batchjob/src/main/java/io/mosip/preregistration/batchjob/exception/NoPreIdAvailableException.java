package io.mosip.preregistration.batchjob.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import lombok.Getter;

/**
 * @author Kishan Rathore
 * @since 1.0.0
 *
 */
@Getter
public class NoPreIdAvailableException extends BaseUncheckedException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8146722453630331685L;
	/**
	 * Creates a no pre id available exception.
	 *
	 * @param message message supplied to this constructor
	 */
	public NoPreIdAvailableException(String message) {
		super("",message);
	}
	/**
	 * Creates a no pre id available exception.
	 *
	 * @param errorCode error code supplied to this constructor
	 * @param errorMessage error message supplied to this constructor
	 */
	public NoPreIdAvailableException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	/**
	 * Creates a no pre id available exception.
	 *
	 * @param errorCode error code supplied to this constructor
	 * @param errorMessage error message supplied to this constructor
	 * @param rootCause root cause supplied to this constructor
	 */
	public NoPreIdAvailableException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}

}
