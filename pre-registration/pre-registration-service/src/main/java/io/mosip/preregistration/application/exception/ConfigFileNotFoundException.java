package io.mosip.preregistration.application.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/**
 * Unchecked exception raised when config file not found fails in Pre-Registration.
 */
public class ConfigFileNotFoundException extends BaseUncheckedException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1298682891599963309L;

	/**
	 * main resposne dto used by this Pre-Registration type.
	 */
	private MainResponseDTO<?> mainResposneDto;

	/**
	 * Creates a config file not found exception.
	 *
	 * @param msg msg supplied to this constructor
	 * @param response response supplied to this constructor
	 */
	public ConfigFileNotFoundException(String msg, MainResponseDTO<?> response) {
		super("", msg);
		this.mainResposneDto = response;
	}

	/**
	 * Creates a config file not found exception.
	 *
	 * @param msg msg supplied to this constructor
	 * @param cause cause supplied to this constructor
	 * @param response response supplied to this constructor
	 */
	public ConfigFileNotFoundException(String msg, Throwable cause, MainResponseDTO<?> response) {
		super("", msg, cause);
		this.mainResposneDto = response;
	}

	/**
	 * Creates a config file not found exception.
	 *
	 * @param errorCode error code supplied to this constructor
	 * @param errorMessage error message supplied to this constructor
	 * @param response response supplied to this constructor
	 */
	public ConfigFileNotFoundException(String errorCode, String errorMessage, MainResponseDTO<?> response) {
		super(errorCode, errorMessage, null);
		this.mainResposneDto = response;
	}

	/**
	 * Creates a config file not found exception.
	 *
	 * @param errorCode error code supplied to this constructor
	 * @param errorMessage error message supplied to this constructor
	 * @param rootCause root cause supplied to this constructor
	 * @param response response supplied to this constructor
	 */
	public ConfigFileNotFoundException(String errorCode, String errorMessage, Throwable rootCause,
			MainResponseDTO<?> response) {
		super(errorCode, errorMessage, rootCause);
		this.mainResposneDto = response;
	}

	/**
	 * Creates a config file not found exception.
	 */
	public ConfigFileNotFoundException() {
		super();
	}
}
