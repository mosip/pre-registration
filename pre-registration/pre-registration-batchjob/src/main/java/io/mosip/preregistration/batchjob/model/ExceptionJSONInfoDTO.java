package io.mosip.preregistration.batchjob.model;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;

/**
 * Exception json Info
 * 
 * @author Kishan Rathore
 * @since 1.0.0
 *
 */
public class ExceptionJSONInfoDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3999014525078508265L;

	@ApiModelProperty(value = "Error Code", position = 1)
	/**
	 * error code used by this Pre-Registration type.
	 */
	private String errorCode;
	@ApiModelProperty(value = "Error Message", position = 2)
	/**
	 * message used by this Pre-Registration type.
	 */
	private String message;

	/**
	 * Creates a exception json info dto.
	 *
	 * @param errorcode errorcode supplied to this constructor
	 * @param message message supplied to this constructor
	 */
	public ExceptionJSONInfoDTO(String errorcode, String message) {
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
	 * Returns message.
	 *
	 * @return get message result
	 */
	public String getMessage() {
		return message;
	}


}
