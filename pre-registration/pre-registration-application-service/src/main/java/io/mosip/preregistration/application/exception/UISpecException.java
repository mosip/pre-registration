package io.mosip.preregistration.application.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UISpecException extends BaseUncheckedException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UISpecException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}
