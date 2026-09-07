package io.mosip.preregistration.core.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

public class UserLookupException extends BaseUncheckedException {

	private static final long serialVersionUID = 1L;

	public UserLookupException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	public UserLookupException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}

}
