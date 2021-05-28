package io.mosip.preregistration.application.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

public class PreRegLoginException extends BaseUncheckedException {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PreRegLoginException() {
		super();
	}
	
	public PreRegLoginException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}
