package io.mosip.preregistration.application.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class AppointmentExecption extends BaseUncheckedException {

	public AppointmentExecption(String errorCode, String message) {
		this.errorCode = errorCode;
		this.errorMessage = message;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String errorCode;

	private String errorMessage;

}
