package io.mosip.preregistration.core.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import lombok.Getter;

@Getter
public class PreRegistrationException extends BaseUncheckedException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2195117679715213502L;

	private MainResponseDTO<?> mainresponseDTO;

	/**
	 * @param msg
	 *            pass the error message
	 */
	public PreRegistrationException(String msg) {
		super("", msg);
	}

	/**
	 * @param errCode
	 *            pass the error code
	 * @param msg
	 *            pass the error message
	 */
	public PreRegistrationException(String errCode, String msg) {
		super(errCode, msg);
	}

	/**
	 * @param errCode
	 *            pass the error code
	 * @param msg
	 *            pass the error message
	 */
	public PreRegistrationException(String errCode, String msg, MainResponseDTO<?> response) {
		super(errCode, msg);
		this.mainresponseDTO = response;
	}

	/**
	 * @param errCode
	 *            pass the error code
	 * @param msg
	 *            pass the error message
	 * @param cause
	 *            pass the cause
	 */
	public PreRegistrationException(String errCode, String msg, Throwable cause) {
		super(errCode, msg, cause);
	}

}
