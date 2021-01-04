package io.mosip.preregistration.application.exception;

import io.mosip.kernel.core.exception.BaseCheckedException;

public class PreRegLoginException extends BaseCheckedException {

	
	public PreRegLoginException() {
		super();
	}
	
	public PreRegLoginException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}
