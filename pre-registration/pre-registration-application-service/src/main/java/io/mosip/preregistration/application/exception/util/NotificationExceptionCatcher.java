/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.application.exception.util;


import java.text.ParseException;

import org.springframework.web.client.HttpServerErrorException;

import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.util.exception.JsonParseException;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.exception.IllegalParamException;
import io.mosip.preregistration.core.exception.InvalidRequestException;
import io.mosip.preregistration.core.exception.InvalidRequestParameterException;
import io.mosip.preregistration.core.exception.PreRegistrationException;
import io.mosip.preregistration.application.exception.RecordNotFoundException;
import io.mosip.preregistration.application.errorcodes.NotificationErrorCodes;
import io.mosip.preregistration.application.errorcodes.NotificationErrorMessages;
import io.mosip.preregistration.application.exception.BookingDetailsNotFoundException;
import io.mosip.preregistration.application.exception.DemographicDetailsNotFoundException;
import io.mosip.preregistration.application.exception.IOException;
import io.mosip.preregistration.application.exception.JsonValidationException;
import io.mosip.preregistration.application.exception.MandatoryFieldException;
import io.mosip.preregistration.application.exception.MissingRequestParameterException;
import io.mosip.preregistration.application.exception.NotificationSeriveException;
import io.mosip.preregistration.application.exception.RestCallException;


/**
 * This class is used to catch the exceptions that occur while creating the
 * acknowledgement application
 * 
 * @author Sanober Noor
 * @since 1.0.0
 *
 */
public class NotificationExceptionCatcher {

	/**
	 * Method to handle the respective exceptions
	 * 
	 * @param ex
	 *            pass the exception
	 */
	public void handle(Exception ex,MainResponseDTO<?> mainResponseDto) {
		if (ex instanceof MandatoryFieldException) {
			throw new MandatoryFieldException(((MandatoryFieldException) ex).getErrorCode(),((MandatoryFieldException) ex).getErrorText(),mainResponseDto);
		}else if (ex instanceof IOException||ex instanceof java.io.IOException) {
			throw new IOException(NotificationErrorCodes.PRG_PAM_ACK_005.getCode(), 
					NotificationErrorMessages.INPUT_OUTPUT_EXCEPTION.getMessage(),mainResponseDto);
		}else if (ex instanceof NullPointerException) {
			throw new IllegalParamException(NotificationErrorCodes.PRG_PAM_ACK_002.getCode(),
					NotificationErrorMessages.INCORRECT_MANDATORY_FIELDS.getMessage(), ex.getCause(),mainResponseDto);
		}
		else if (ex instanceof HttpServerErrorException) {
			throw new NotificationSeriveException();
		}
		 else if (ex instanceof InvalidRequestException) {
				throw new InvalidRequestException(((InvalidRequestException) ex).getErrorCode(),
						((InvalidRequestException) ex).getErrorText(), mainResponseDto);
			}
		else if (ex instanceof JsonParseException) {
			throw new JsonValidationException(NotificationErrorCodes.PRG_PAM_ACK_004.getCode(), NotificationErrorMessages.JSON_PARSING_FAILED.getMessage(),
					ex.getCause(),mainResponseDto);
		} else if (ex instanceof InvalidRequestParameterException) {
			throw new InvalidRequestParameterException(((InvalidRequestParameterException) ex).getErrorCode(),
					((InvalidRequestParameterException) ex).getErrorText(),mainResponseDto);
		} else if (ex instanceof MissingRequestParameterException) {
			throw new MissingRequestParameterException(((MissingRequestParameterException) ex).getErrorCode(),
					((MissingRequestParameterException) ex).getErrorText(),mainResponseDto);
		}
		
		else if (ex instanceof NotificationSeriveException) {
			throw new NotificationSeriveException(((NotificationSeriveException) ex).getValidationErrorList(),((NotificationSeriveException) ex).getMainResposneDTO());
		
		}

		else if (ex instanceof RestCallException) {
			throw new RestCallException(((RestCallException) ex).getErrorCode(),((RestCallException) ex).getErrorText(),((RestCallException) ex).getMainresponseDTO());
		
		}
		else if (ex instanceof BookingDetailsNotFoundException) {
			throw new BookingDetailsNotFoundException(((BookingDetailsNotFoundException) ex).getErrorList(),((BookingDetailsNotFoundException) ex).getMainResponseDTO());
		
		}
		else if (ex instanceof DemographicDetailsNotFoundException) {
			throw new DemographicDetailsNotFoundException(((DemographicDetailsNotFoundException) ex).getErrorList(),((DemographicDetailsNotFoundException) ex).getMainResponseDTO());
		
		}
		else if (ex instanceof ParseException) {
			throw new InvalidRequestParameterException(io.mosip.preregistration.core.errorcodes.ErrorCodes.PRG_CORE_REQ_003.getCode(), io.mosip.preregistration.core.errorcodes.ErrorMessages.INVALID_REQUEST_DATETIME.getMessage(), mainResponseDto);

		}
		else if (ex instanceof RecordNotFoundException) {
			throw new RecordNotFoundException(((RecordNotFoundException) ex).getErrorCode(),((RecordNotFoundException) ex).getErrorText(),mainResponseDto);

		}
		else {
			if (ex instanceof BaseUncheckedException) {
				throw new PreRegistrationException(((BaseUncheckedException) ex).getErrorCode(),
						((BaseUncheckedException) ex).getErrorText(), mainResponseDto);
			} else if (ex instanceof BaseCheckedException) {
				throw new PreRegistrationException(((BaseCheckedException) ex).getErrorCode(),
						((BaseCheckedException) ex).getErrorText(), mainResponseDto);
			}
		}
	}

}
