package io.mosip.preregistration.batchjob.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import lombok.Getter;

/**
 * @author Kishan Rathore
 * @since 1.0.0
 *
 */
@Getter
public class NoValidPreIdFoundException extends BaseUncheckedException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6469856598814217956L;
	/**
	 * response used by this Pre-Registration type.
	 */
	private MainResponseDTO<?> response;
	/**
	 * Creates a no valid pre id found exception.
	 *
	 * @param message message supplied to this constructor
	 */
	public NoValidPreIdFoundException(String message) {
		super("",message);
	}
	/**
	 * Creates a no valid pre id found exception.
	 *
	 * @param errorCode error code supplied to this constructor
	 * @param errorMessage error message supplied to this constructor
	 */
	public NoValidPreIdFoundException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	/**
	 * Creates a no valid pre id found exception.
	 *
	 * @param errorCode error code supplied to this constructor
	 * @param errorMessage error message supplied to this constructor
	 * @param rootCause root cause supplied to this constructor
	 */
	public NoValidPreIdFoundException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}

	/**
	 * Creates a no valid pre id found exception.
	 *
	 * @param errorCode error code supplied to this constructor
	 * @param errorMessage error message supplied to this constructor
	 * @param responseDTO response dto supplied to this constructor
	 */
	public NoValidPreIdFoundException(String errorCode, String errorMessage,MainResponseDTO<?> responseDTO) {
		super(errorCode, errorMessage);
		this.response=responseDTO;
	}
}
