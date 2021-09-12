package io.mosip.analytics.event.anonymous.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnonymousProfileException extends BaseUncheckedException {

	private static final long serialVersionUID = 4396999850114156057L;

	public AnonymousProfileException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}
