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
	 * Handles exceptions and throws specific exceptions based on the type of the original exception.
	 * 
	 * @param ex              The original exception to handle.
	 * @param mainResponseDTO The main response DTO associated with the exception.
	 */
	public void handle(Exception ex, MainResponseDTO<?> mainResponseDto) {
		if (ex instanceof MandatoryFieldException ex1) {
			throw new MandatoryFieldException( ex1.getErrorCode(),
					 ex1.getErrorText(), mainResponseDto);
		} else if (ex instanceof IOException || ex instanceof java.io.IOException) {
			throw new IOException(NotificationErrorCodes.PRG_PAM_ACK_005.getCode(),
					NotificationErrorMessages.INPUT_OUTPUT_EXCEPTION.getMessage(), mainResponseDto);
		} else if (ex instanceof NullPointerException) {
			throw new IllegalParamException(NotificationErrorCodes.PRG_PAM_ACK_002.getCode(),
					NotificationErrorMessages.INCORRECT_MANDATORY_FIELDS.getMessage(), ex.getCause(), mainResponseDto);
		} else if (ex instanceof HttpServerErrorException) {
			throw new NotificationSeriveException();
		} else if (ex instanceof InvalidRequestException ex1) {
			throw new InvalidRequestException(ex1.getErrorCode(), ex1.getErrorText(), mainResponseDto);
		} else if (ex instanceof JsonParseException) {
			throw new JsonValidationException(NotificationErrorCodes.PRG_PAM_ACK_004.getCode(),
					NotificationErrorMessages.JSON_PARSING_FAILED.getMessage(), ex.getCause(), mainResponseDto);
		} else if (ex instanceof InvalidRequestParameterException ex1) {
			throw new InvalidRequestParameterException(ex1.getErrorCode(), ex1.getErrorText(), mainResponseDto);
		} else if (ex instanceof MissingRequestParameterException ex1) {
			throw new MissingRequestParameterException(ex1.getErrorCode(), ex1.getErrorText(), mainResponseDto);
		} else if (ex instanceof NotificationSeriveException ex1) {
			throw new NotificationSeriveException(ex1.getValidationErrorList(), ex1.getMainResposneDTO());
		} else if (ex instanceof RestCallException ex1) {
			throw new RestCallException(ex1.getErrorCode(), ex1.getErrorText(), ex1.getMainresponseDTO());
		} else if (ex instanceof BookingDetailsNotFoundException ex1) {
			throw new BookingDetailsNotFoundException(ex1.getErrorList(), ex1.getMainResponseDTO());
		} else if (ex instanceof DemographicDetailsNotFoundException ex1) {
			throw new DemographicDetailsNotFoundException(ex1.getErrorList(), ex1.getMainResponseDTO());
		} else if (ex instanceof ParseException) {
			throw new InvalidRequestParameterException(
					io.mosip.preregistration.core.errorcodes.ErrorCodes.PRG_CORE_REQ_003.getCode(),
					io.mosip.preregistration.core.errorcodes.ErrorMessages.INVALID_REQUEST_DATETIME.getMessage(),
					mainResponseDto);
		} else if (ex instanceof RecordNotFoundException ex1) {
			throw new RecordNotFoundException(ex1.getErrorCode(), ex1.getErrorText(), mainResponseDto);
		} else {
			if (ex instanceof BaseUncheckedException ex1) {
				throw new PreRegistrationException(ex1.getErrorCode(), ex1.getErrorText(), mainResponseDto);
			} else if (ex instanceof BaseCheckedException ex1) {
				throw new PreRegistrationException(ex1.getErrorCode(), ex1.getErrorText(), mainResponseDto);
			}
		}
	}
}