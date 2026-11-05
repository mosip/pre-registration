package io.mosip.preregistration.datasync.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import lombok.Getter;

/**
 *  ParseResponseException class.
 *
 * @author Akshay
 */

@Getter
public class ParseResponseException extends BaseUncheckedException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 3383837827871687253L;
	/**
	 * response used by this Pre-Registration type.
	 */
	private MainResponseDTO<?> response;
	/**
	 * Instantiates a new parses the response exception.
	 *
	 * @param errorCode
	 *            the error code
	 * @param errorMessage
	 *            the error message
	 * @param rootCause
	 *            the root cause
	 */
	public ParseResponseException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);

	}

	/**
	 * Instantiates a new parses the response exception.
	 *
	 * @param errorCode
	 *            the error code
	 * @param errorMessage
	 *            the error message
	 */
	public ParseResponseException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);

	}
	
	/**
	 * Creates a parse response exception.
	 *
	 * @param errorCode error code supplied to this constructor
	 * @param errorMessage error message supplied to this constructor
	 * @param response response supplied to this constructor
	 */
	public ParseResponseException(String errorCode, String errorMessage,MainResponseDTO<?> response) {
		super(errorCode, errorMessage);
		this.response=response;
	}

}
