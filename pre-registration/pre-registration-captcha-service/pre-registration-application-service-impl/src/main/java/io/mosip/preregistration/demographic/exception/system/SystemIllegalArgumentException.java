/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.demographic.exception.system;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import lombok.Getter;

/**
 * This class defines the SystemIllegalArgumentException
 * 
 * @author Rajath KR
 * @since 1.0.0
 * 
 */
@Getter
public class SystemIllegalArgumentException extends BaseUncheckedException {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	private MainResponseDTO<?> mainresponseDTO;


	/**
	 * @param msg pass the error message
	 */
	public SystemIllegalArgumentException(String msg) {
		super("", msg);
	}

	/**
	 * @param errCode  pass the error code
	 * @param msg  pass the error message
	 */
	public SystemIllegalArgumentException(String errCode, String msg) {
		super(errCode, msg);
	}
	/**
	 * @param errCode  pass the error code
	 * @param msg  pass the error message
	 */
	public SystemIllegalArgumentException(String errCode, String msg,MainResponseDTO<?> response) {
		super(errCode, msg);
		this.mainresponseDTO=response;
	}

	/**
	 * @param errCode  pass the error Code
	 * @param msg  pass the error message
	 * @param cause  pass the error cause
	 */
	public SystemIllegalArgumentException(String errCode, String msg, Throwable cause) {
		super(errCode, msg, cause);
	}

}
