package io.mosip.preregistration.core.exception.util;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/**
 * Unchecked exception raised when parse response fails in Pre-Registration.
 */
public class ParseResponseException extends BaseUncheckedException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * main response dto used by this Pre-Registration type.
	 */
	private MainResponseDTO<?> mainResponseDto;
	
	/**
	 * Creates a parse response exception.
	 *
	 * @param errorCode error code supplied to this constructor
	 * @param errorMessage error message supplied to this constructor
	 * @param response response supplied to this constructor
	 */
	public ParseResponseException(String errorCode, String errorMessage,MainResponseDTO<?> response) {
		super(errorCode, errorMessage);
		this.mainResponseDto=response;
		
	}

	/**
	 * Creates a parse response exception.
	 *
	 * @param errorCode error code supplied to this constructor
	 * @param errorMessage error message supplied to this constructor
	 * @param rootCause root cause supplied to this constructor
	 * @param response response supplied to this constructor
	 */
	public ParseResponseException(String errorCode, String errorMessage,Throwable rootCause,MainResponseDTO<?> response) {
		super(errorCode, errorMessage,rootCause);
		this.mainResponseDto=response;
		
	}


}
