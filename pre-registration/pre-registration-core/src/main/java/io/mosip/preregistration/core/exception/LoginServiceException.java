/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.core.exception;

import java.util.List;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.exception.ServiceError;
import lombok.Getter;

/**
 * The LoginServiceException class represents an exception that occurs when
 * Login service throws error. This class extends {@link BaseUncheckedException}
 * and includes various constructors for different use cases.
 * 
 * @author Kishan Rathore
 * @since 1.0.0
 *
 */
@Getter
public class LoginServiceException extends BaseUncheckedException {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The List of Exception as ServiceError. */
	private List<ServiceError> validationErrorList;

	/**
	 * Constructs a new {@code LoginServiceException} with the specified
	 * validationErrorList
	 * 
	 * @param validationErrorList The List of Exception as ServiceError.
	 */
	public LoginServiceException(List<ServiceError> validationErrorList) {
		super();
		this.validationErrorList = validationErrorList;
	}
}