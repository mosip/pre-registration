/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.application.exception.util;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.dao.DataIntegrityViolationException;

import io.mosip.kernel.core.crypto.exception.InvalidDataException;
import io.mosip.kernel.core.crypto.exception.InvalidKeyException;
import io.mosip.kernel.core.crypto.exception.InvalidParamSpecException;
import io.mosip.kernel.core.crypto.exception.NullDataException;
import io.mosip.kernel.core.crypto.exception.NullKeyException;
import io.mosip.kernel.core.crypto.exception.NullMethodException;
import io.mosip.kernel.core.crypto.exception.SignatureException;
import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.exception.IOException;
import io.mosip.kernel.core.exception.NoSuchAlgorithmException;
import io.mosip.kernel.core.exception.ParseException;
import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectIOException;
import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectValidationFailedException;
import io.mosip.kernel.core.util.exception.JsonMappingException;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.exception.DecryptionFailedException;
import io.mosip.preregistration.core.exception.EncryptionFailedException;
import io.mosip.preregistration.core.exception.HashingException;
import io.mosip.preregistration.core.exception.InvalidRequestException;
import io.mosip.preregistration.core.exception.InvalidRequestParameterException;
import io.mosip.preregistration.core.exception.PreIdInvalidForUserIdException;
import io.mosip.preregistration.core.exception.PreRegistrationException;
import io.mosip.preregistration.core.exception.RecordFailedToDeleteException;
import io.mosip.preregistration.core.exception.RestCallException;
import io.mosip.preregistration.core.exception.TableNotAccessibleException;
import io.mosip.preregistration.application.errorcodes.DemographicErrorCodes;
import io.mosip.preregistration.application.errorcodes.DemographicErrorMessages;
import io.mosip.preregistration.application.exception.BookingDeletionFailedException;
import io.mosip.preregistration.application.exception.CryptocoreException;
import io.mosip.preregistration.application.exception.DemographicServiceException;
import io.mosip.preregistration.application.exception.DocumentFailedToDeleteException;
import io.mosip.preregistration.application.exception.IdValidationException;
import io.mosip.preregistration.application.exception.InvalidDateFormatException;
import io.mosip.preregistration.application.exception.MissingRequestParameterException;
import io.mosip.preregistration.application.exception.OperationNotAllowedException;
import io.mosip.preregistration.application.exception.RecordFailedToUpdateException;
import io.mosip.preregistration.application.exception.RecordNotFoundException;
import io.mosip.preregistration.application.exception.RecordNotFoundForPreIdsException;
import io.mosip.preregistration.application.exception.SchemaValidationException;
import io.mosip.preregistration.demographic.exception.system.DateParseException;
import io.mosip.preregistration.demographic.exception.system.JsonParseException;
import io.mosip.preregistration.demographic.exception.system.JsonValidationException;
import io.mosip.preregistration.demographic.exception.system.SystemFileIOException;
import io.mosip.preregistration.demographic.exception.system.SystemIllegalArgumentException;
import io.mosip.preregistration.demographic.exception.system.SystemUnsupportedEncodingException;

/**
 * This class is used to catch the exceptions that occur while creating the
 * pre-registration
 * 
 * @author Ravi C Balaji
 * 
 * @since 1.0.0
 *
 */
public class DemographicExceptionCatcher {
	/**
	 * Handles exceptions and throws specific exceptions based on the type of the original exception.
	 * 
	 * @param ex              The original exception to handle.
	 * @param mainResponseDTO The main response DTO associated with the exception.
	 */
	public void handle(Exception ex, MainResponseDTO<?> mainResponsedto) {
		if (ex instanceof DataAccessLayerException ex1) {
			throw new TableNotAccessibleException(ex1.getErrorCode(), ex1.getErrorText(), mainResponsedto);
		} else if (ex instanceof ParseException ex1) {
			throw new JsonParseException(ex1.getErrorCode(), ex1.getErrorText(), mainResponsedto);
		} else if (ex instanceof RecordNotFoundException ex1) {
			throw new RecordNotFoundException(ex1.getErrorCode(), ex1.getErrorText(), mainResponsedto);
		} else if (ex instanceof RecordNotFoundForPreIdsException ex1) {
			throw new RecordNotFoundForPreIdsException(ex1.getErrorCode(), ex1.getErrorText(), mainResponsedto);
		} else if (ex instanceof InvalidRequestParameterException ex1) {
			throw new InvalidRequestParameterException(ex1.getErrorCode(), ex1.getErrorText(), mainResponsedto);
		} else if (ex instanceof MissingRequestParameterException ex1) {
			throw new MissingRequestParameterException(ex1.getErrorCode(), ex1.getErrorText(), mainResponsedto);
		} else if (ex instanceof DocumentFailedToDeleteException ex1) {
			throw new DocumentFailedToDeleteException(ex1.getErrorCode(), ex1.getErrorText(), mainResponsedto);
		} else if (ex instanceof SystemIllegalArgumentException ex1) {
			throw new SystemIllegalArgumentException(ex1.getErrorCode(), ex1.getErrorText(), mainResponsedto);
		} else if (ex instanceof SystemUnsupportedEncodingException ex1) {
			throw new SystemUnsupportedEncodingException(ex1.getErrorCode(), ex1.getErrorText(), mainResponsedto);
		} else if (ex instanceof DateParseException ex1) {
			throw new DateParseException(ex1.getErrorCode(), ex1.getErrorText(), mainResponsedto);
		} else if (ex instanceof RecordFailedToUpdateException ex1) {
			throw new RecordFailedToUpdateException(ex1.getErrorCode(), ex1.getErrorText(), mainResponsedto);
		} else if (ex instanceof RecordFailedToDeleteException ex1) {
			throw new RecordFailedToDeleteException(ex1.getErrorCode(), ex1.getErrorText(), mainResponsedto);
		} else if (ex instanceof InvalidDateFormatException ex1) {
			throw new InvalidDateFormatException(ex1.getErrorCode(), ex1.getErrorText(), mainResponsedto);
		} else if (ex instanceof BookingDeletionFailedException ex1) {
			throw new BookingDeletionFailedException(ex1.getErrorCode(), ex1.getErrorText(), mainResponsedto);
		} else if (ex instanceof HashingException ex1) {
			throw new HashingException(ex1.getErrorCode(), ex1.getErrorText(), mainResponsedto);
		} else if (ex instanceof OperationNotAllowedException ex1) {
			throw new OperationNotAllowedException(ex1.getErrorCode(), ex1.getErrorText(), mainResponsedto);
		} else if (ex instanceof DecryptionFailedException ex1) {
			throw new DecryptionFailedException(ex1.getErrorCode(), ex1.getErrorText(), mainResponsedto);
		} else if (ex instanceof JsonMappingException ex1) {
			throw new JsonValidationException(ex1.getErrorCode(), ex1.getErrorText(), mainResponsedto);
		} else if (ex instanceof IOException ex1) {
			throw new JsonValidationException(ex1.getErrorCode(), ex1.getErrorText(), mainResponsedto);
		} else if (ex instanceof RestCallException ex1) {
			throw new RestCallException(ex1.getErrorCode(), ex1.getErrorText(), mainResponsedto);
		} else if (ex instanceof SchemaValidationException ex1) {
			throw new SchemaValidationException(ex1.getErrorCode(), ex1.getErrorText(), mainResponsedto);
		} else if (ex instanceof IdObjectIOException ex1) {
			throw new SchemaValidationException(ex1.getErrorCode(), ex1.getErrorText(), mainResponsedto);
		} else if (ex instanceof IdObjectValidationFailedException ex1) {
			throw new IdValidationException(ex1.getErrorCode(), ex1.getErrorTexts(), mainResponsedto);
		} else if (ex instanceof PreIdInvalidForUserIdException ex1) {
			throw new PreIdInvalidForUserIdException(ex1.getErrorCode(), ex1.getErrorText(), mainResponsedto);
		} else if (ex instanceof EncryptionFailedException ex1) {
			throw new EncryptionFailedException(ex1.getValidationErrorList(), mainResponsedto);
		} else if (ex instanceof BeanCreationException) {
			throw new SchemaValidationException(
					io.mosip.preregistration.core.errorcodes.ErrorCodes.PRG_CORE_REQ_016.getCode(),
					ex.getLocalizedMessage(), mainResponsedto);
		} else if (ex instanceof SystemFileIOException ex1) {
			throw new SystemFileIOException(ex1.getErrorCode(), ex1.getErrorText(), mainResponsedto);
		} else if (ex instanceof DemographicServiceException ex1) {
			throw new DemographicServiceException(ex1.getValidationErrorList(), mainResponsedto);
		} else if (ex instanceof InvalidDataException ex1) {
			throw new CryptocoreException(ex1.getErrorCode(), ex1.getErrorText(), mainResponsedto);
		} else if (ex instanceof SignatureException ex1) {
			throw new CryptocoreException(ex1.getErrorCode(), ex1.getErrorText(), mainResponsedto);
		} else if (ex instanceof InvalidKeyException ex1) {
			throw new CryptocoreException(ex1.getErrorCode(), ex1.getErrorText(), mainResponsedto);
		} else if (ex instanceof InvalidParamSpecException ex1) {
			throw new CryptocoreException(ex1.getErrorCode(), ex1.getErrorText(), mainResponsedto);
		} else if (ex instanceof NullDataException ex1) {
			throw new CryptocoreException(ex1.getErrorCode(), ex1.getErrorText(), mainResponsedto);
		} else if (ex instanceof NullKeyException ex1) {
			throw new CryptocoreException(ex1.getErrorCode(), ex1.getErrorText(), mainResponsedto);
		} else if (ex instanceof NullMethodException ex1) {
			throw new CryptocoreException(ex1.getErrorCode(), ex1.getErrorText(), mainResponsedto);
		} else if (ex instanceof NoSuchAlgorithmException ex1) {
			throw new InvalidRequestParameterException(ex1.getErrorCode(), ex1.getErrorText(), mainResponsedto);
		} else if (ex instanceof InvalidRequestException ex1) {
			throw new InvalidRequestException(ex1.getErrorCode(), ex1.getErrorText(), mainResponsedto);
		} else if (ex instanceof DataIntegrityViolationException) {
			throw new io.mosip.preregistration.application.exception.DuplicatePridKeyException(
					DemographicErrorCodes.PRG_PAM_APP_021.getCode(),
					DemographicErrorMessages.DUPLICATE_KEY.getMessage(), mainResponsedto);
		} else if (ex instanceof ConstraintViolationException) {
			throw new io.mosip.preregistration.application.exception.DuplicatePridKeyException(
					DemographicErrorCodes.PRG_PAM_APP_021.getCode(),
					DemographicErrorMessages.DUPLICATE_KEY.getMessage(), mainResponsedto);
		} else {
			if (ex instanceof BaseUncheckedException ex1) {
				throw new PreRegistrationException(ex1.getErrorCode(), ex1.getErrorText(), mainResponsedto);
			} else if (ex instanceof BaseCheckedException ex1) {
				throw new PreRegistrationException(ex1.getErrorCode(), ex1.getErrorText(), mainResponsedto);
			}
		}
	}
}