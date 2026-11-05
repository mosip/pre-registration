package io.mosip.analytics.event.anonymous.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/**
 * Unchecked exception raised when anonymous profile fails in Pre-Registration.
 */
public class AnonymousProfileException extends BaseUncheckedException {

	private static final long serialVersionUID = 4396999850114156057L;

	/**
	 * Creates a anonymous profile exception.
	 *
	 * @param errorCode error code supplied to this constructor
	 * @param errorMessage error message supplied to this constructor
	 */
	public AnonymousProfileException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}
