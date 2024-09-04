/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.application.exception.util;

import org.json.JSONException;
import org.postgresql.util.PSQLException;

import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.exception.IOException;
import io.mosip.kernel.core.exception.ParseException;
import io.mosip.kernel.core.fsadapter.exception.FSAdapterException;
import io.mosip.kernel.core.util.exception.JsonMappingException;
import io.mosip.kernel.core.util.exception.JsonParseException;
import io.mosip.kernel.core.virusscanner.exception.VirusScannerException;
import io.mosip.preregistration.application.errorcodes.DocumentErrorCodes;
import io.mosip.preregistration.application.errorcodes.DocumentErrorMessages;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.exception.DecryptionFailedException;
import io.mosip.preregistration.core.exception.EncryptionFailedException;
import io.mosip.preregistration.core.exception.InvalidRequestException;
import io.mosip.preregistration.core.exception.InvalidRequestParameterException;
import io.mosip.preregistration.core.exception.MasterDataNotAvailableException;
import io.mosip.preregistration.core.exception.PreRegistrationException;
import io.mosip.preregistration.core.exception.TableNotAccessibleException;
import io.mosip.preregistration.application.exception.DTOMappigException;
import io.mosip.preregistration.application.exception.DemographicGetDetailsException;
import io.mosip.preregistration.application.exception.DocumentFailedToCopyException;
import io.mosip.preregistration.application.exception.DocumentFailedToUploadException;
import io.mosip.preregistration.application.exception.DocumentNotFoundException;
import io.mosip.preregistration.application.exception.DocumentNotValidException;
import io.mosip.preregistration.application.exception.DocumentSizeExceedException;
import io.mosip.preregistration.application.exception.DocumentVirusScanException;
import io.mosip.preregistration.application.exception.FSServerException;
import io.mosip.preregistration.application.exception.InvalidDocumentIdExcepion;
import io.mosip.preregistration.application.exception.MandatoryFieldNotFoundException;
import io.mosip.preregistration.application.exception.ParsingException;
import io.mosip.preregistration.application.exception.PrimaryKeyValidationException;
import io.mosip.preregistration.application.exception.RecordNotFoundException;

/**
 * This class is used to catch the exceptions that occur while uploading the
 * document
 * 
 * @author Rajath KR
 * @since 1.0.0
 *
 */
public class DocumentExceptionCatcher {
	/**
	 * Handles exceptions and throws specific exceptions based on the type of the original exception.
	 * 
	 * @param ex              The original exception to handle.
	 * @param mainResponseDTO The main response DTO associated with the exception.
	 */
	public void handle(Exception ex, MainResponseDTO<?> response) {
		if (ex instanceof DocumentFailedToUploadException ex1) {
			throw new DocumentFailedToUploadException(ex1.getErrorCode(), ex1.getErrorText(), response);
		} else if (ex instanceof IOException ex1) {
			// kernel exception
			throw new DTOMappigException(ex1.getErrorCode(), ex1.getErrorText(), response);
		} else if (ex instanceof JsonMappingException ex1) {
			throw new DTOMappigException(ex1.getErrorCode(), ex1.getErrorText(), response);
			// kernel exception
		} else if (ex instanceof JsonParseException ex1) {
			// kernel exception
			throw new DTOMappigException(ex1.getErrorCode(), ex1.getErrorText(), response);
		} else if (ex instanceof JSONException || ex instanceof ParseException) {
			throw new ParsingException(DocumentErrorCodes.PRG_PAM_DOC_015.toString(),
					DocumentErrorMessages.JSON_EXCEPTION.getMessage(), response);
		} else if (ex instanceof InvalidRequestParameterException ex1) {
			throw new InvalidRequestParameterException(ex1.getErrorCode(), ex1.getErrorText(), response);
		} else if (ex instanceof MandatoryFieldNotFoundException ex1) {
			throw new MandatoryFieldNotFoundException(ex1.getErrorCode(), ex1.getErrorText(), response);
		} else if (ex instanceof DocumentNotValidException ex1) {
			throw new DocumentNotValidException(ex1.getErrorCode(), ex1.getErrorText(), response);
		} else if (ex instanceof DocumentSizeExceedException ex1) {
			throw new DocumentSizeExceedException(ex1.getErrorCode(), ex1.getErrorText(), response);
		} else if (ex instanceof VirusScannerException ex1) {
			throw new DocumentVirusScanException(ex1.getErrorCode(), ex1.getErrorText(), response);
		} else if (ex instanceof DocumentVirusScanException ex1) {
			throw new DocumentVirusScanException(ex1.getErrorCode(), ex1.getErrorText(), response);
		} else if (ex instanceof DocumentNotFoundException ex1) {
			throw new DocumentNotFoundException(ex1.getErrorCode(), ex1.getErrorText(), response);
		} else if (ex instanceof DocumentFailedToCopyException ex1) {
			throw new DocumentFailedToCopyException(ex1.getErrorCode(), ex1.getErrorText(), response);
		} else if (ex instanceof InvalidDocumentIdExcepion ex1) {
			throw new InvalidDocumentIdExcepion(ex1.getErrorCode(), ex1.getErrorText(), response);
		} else if (ex instanceof DemographicGetDetailsException ex1) {
			throw new DemographicGetDetailsException(ex1.getErrorCode(), ex1.getErrorText(), response);
		} else if (ex instanceof FSServerException ex1) {
			throw new FSServerException(ex1.getErrorCode(), ex1.getErrorText(), response);
		} else if (ex instanceof TableNotAccessibleException ex1) {
			throw new TableNotAccessibleException(ex1.getErrorCode(), ex1.getErrorText(), response);
		} else if (ex instanceof PSQLException) {
			throw new PrimaryKeyValidationException(DocumentErrorCodes.PRG_PAM_DOC_021.toString(),
					DocumentErrorMessages.DOCUMENT_ALREADY_PRESENT.getMessage(), response);
		} else if (ex instanceof FSAdapterException ex1) {
			throw new FSServerException(ex1.getErrorCode(), ex1.getErrorText(), response);
		} else if (ex instanceof DecryptionFailedException ex1) {
			throw new EncryptionFailedException(ex1.getErrorCode(), ex1.getErrorText(), response);
		} else if (ex instanceof EncryptionFailedException ex1) {
			throw new EncryptionFailedException(ex1.getValidationErrorList(), response);
		} else if (ex instanceof MasterDataNotAvailableException ex1) {
			throw new EncryptionFailedException(ex1.getErrorCode(), ex1.getErrorText(), response);
		} else if (ex instanceof InvalidRequestException ex1) {
			throw new InvalidRequestException(ex1.getErrorCode(), ex1.getErrorText(), response);
		} else if (ex instanceof RecordNotFoundException ex1) {
			throw new RecordNotFoundException(ex1.getErrorCode(), ex1.getErrorText(), response);
		} else if (ex instanceof java.text.ParseException) {
			throw new InvalidRequestParameterException(
					io.mosip.preregistration.core.errorcodes.ErrorCodes.PRG_CORE_REQ_003.getCode(),
					io.mosip.preregistration.core.errorcodes.ErrorMessages.INVALID_REQUEST_DATETIME.getMessage(),
					response);

		} else {
			if (ex instanceof BaseUncheckedException ex1) {
				throw new PreRegistrationException(ex1.getErrorCode(), ex1.getErrorText(), response);
			} else if (ex instanceof BaseCheckedException ex1) {
				throw new PreRegistrationException(ex1.getErrorCode(), ex1.getErrorText(), response);
			}
		}
	}
}