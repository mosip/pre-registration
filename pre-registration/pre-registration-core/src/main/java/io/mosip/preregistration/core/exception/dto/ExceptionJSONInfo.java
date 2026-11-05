package io.mosip.preregistration.core.exception.dto;

import java.io.Serializable;

/**
 * Class supporting exception json info in MOSIP Pre-Registration (io.mosip.preregistration.core.exception.dto).
 */
public class ExceptionJSONInfo implements Serializable {

	private static final long serialVersionUID = -4870610344003366727L;

	/**
	 * error code used by this Pre-Registration type.
	 */
	private String errorCode;
	/**
	 * message used by this Pre-Registration type.
	 */
	private String message;

	/**
	 * Creates a exception json info.
	 *
	 * @param errorcode errorcode supplied to this constructor
	 * @param message message supplied to this constructor
	 */
	public ExceptionJSONInfo(String errorcode, String message) {
		super();
		errorCode = errorcode;
		this.message = message;
	}

	/**
	 * Returns errorcode.
	 *
	 * @return get errorcode result
	 */
	public String getErrorcode() {
		return errorCode;
	}

	/**
	 * Sets errorcode.
	 *
	 * @param errorcode errorcode used by set errorcode
	 */
	public void setErrorcode(String errorcode) {
		errorCode = errorcode;
	}

	/**
	 * Returns message.
	 *
	 * @return get message result
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets message.
	 *
	 * @param message message used by set message
	 */
	public void setMessage(String message) {
		this.message = message;
	}

}
