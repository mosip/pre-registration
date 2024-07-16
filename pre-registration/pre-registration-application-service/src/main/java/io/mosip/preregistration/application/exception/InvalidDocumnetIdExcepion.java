/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.application.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.application.errorcodes.DocumentErrorCodes;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;

/**
 * The InvalidDocumnetIdExcepion class represents an exception indicating an
 * invalid document ID. This class extends {@link BaseUncheckedException} and
 * includes various constructors for different use cases, as well as a
 * {@link MainResponseDTO} object for additional error information.
 * 
 * @author Kishan Rathore
 * @since 1.0.0
 * 
 */
public class InvalidDocumnetIdExcepion extends BaseUncheckedException {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 7303748392658525834L;

	/**
	 * Default constructor
	 */
	public InvalidDocumnetIdExcepion() {
		super();
	}

	/**
	 * Constructs a new {@code InvalidDocumnetIdExcepion} with the specified error
	 * message
	 * 
	 * @param errorMessage the detailed error message.
	 */
	public InvalidDocumnetIdExcepion(String errorMessage) {
		super(DocumentErrorCodes.PRG_PAM_DOC_009.toString(), errorMessage);
	}

	/**
	 * Constructs a new {@code InvalidDocumnetIdExcepion} with the specified error
	 * message, and rootCause.
	 * 
	 * @param errorMessage the detailed error message.
	 * @param rootCause    the cause of the error.
	 */
	public InvalidDocumnetIdExcepion(String errorMessage, Throwable rootCause) {
		super(DocumentErrorCodes.PRG_PAM_DOC_009.toString(), errorMessage, rootCause);
	}

	/**
	 * Constructs a new {@code InvalidDocumnetIdExcepion} with the specified error
	 * code, error message, and rootCause.
	 * 
	 * @param errorCode    the error code representing the specific error condition.
	 * @param errorMessage the detailed error message.
	 * @param rootCause    the cause of the error.
	 */
	public InvalidDocumnetIdExcepion(String errorCode, String message, Throwable rootCause) {
		super(errorCode, message, rootCause);
	}

	/**
	 * @param errorCode pass Error code
	 * @param message   pass Error Message
	 */
	public InvalidDocumnetIdExcepion(String errorCode, String message) {
		super(errorCode, message);
	}
}