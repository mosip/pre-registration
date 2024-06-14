/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.application.exception.util;

import org.springframework.stereotype.Component;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.core.exception.ParseException;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.exception.IllegalParamException;
import io.mosip.preregistration.core.exception.InvalidRequestException;
import io.mosip.preregistration.core.exception.InvalidRequestParameterException;
import io.mosip.preregistration.core.exception.TableNotAccessibleException;
import io.mosip.preregistration.application.errorcodes.TransliterationErrorCodes;
import io.mosip.preregistration.application.errorcodes.TransliterationErrorMessage;
import io.mosip.preregistration.application.exception.JsonParseException;
import io.mosip.preregistration.application.exception.MandatoryFieldRequiredException;
import io.mosip.preregistration.application.exception.MissingRequestParameterException;
import io.mosip.preregistration.application.exception.UnSupportedLanguageException;

/**
 * This class is used to catch the exceptions that occur while creating the
 * transliteration application
 * 
 * @author Kishan Rathore
 * @since 1.0.0
 *
 */
@Component
public class TransliterationExceptionCatcher {
	/**
	 * Handles exceptions and throws specific exceptions based on the type of the original exception.
	 * 
	 * @param ex              The original exception to handle.
	 * @param mainResponseDTO The main response DTO associated with the exception.
	 */
	public void handle(Exception ex, MainResponseDTO<?> mainResponseDTO) {
		if (ex instanceof DataAccessLayerException) {
			throw new TableNotAccessibleException(TransliterationErrorCodes.PRG_TRL_APP_005.getCode(),
					TransliterationErrorMessage.PRE_REG_TRANSLITRATION_TABLE_NOT_ACCESSIBLE.getMessage(),
					ex.getCause());
		} else if (ex instanceof NullPointerException) {
			throw new IllegalParamException(TransliterationErrorCodes.PRG_TRL_APP_002.getCode(),
					TransliterationErrorMessage.INCORRECT_MANDATORY_FIELDS.getMessage(), ex.getCause(), mainResponseDTO);
		} else if (ex instanceof ParseException) {
			throw new JsonParseException(TransliterationErrorCodes.PRG_TRL_APP_006.getCode(),
					TransliterationErrorMessage.JSON_PARSING_FAILED.getMessage(), ex.getCause(), mainResponseDTO);
		} else if (ex instanceof InvalidRequestParameterException ex1) {
			throw new InvalidRequestParameterException(ex1.getErrorCode(), ex1.getErrorText(), mainResponseDTO);
		} else if (ex instanceof MissingRequestParameterException ex1) {
			throw new MissingRequestParameterException(ex1.getErrorCode(), ex1.getErrorText(), mainResponseDTO);
		} else if (ex instanceof InvalidRequestException ex1) {
			throw new InvalidRequestException(ex1.getErrorCode(), ex1.getErrorText(), mainResponseDTO);
		} else if (ex instanceof MandatoryFieldRequiredException ex1) {
			throw new MandatoryFieldRequiredException(ex1.getErrorCode(), ex1.getErrorText(), mainResponseDTO);
		} else if (ex instanceof UnSupportedLanguageException ex1) {
			throw new UnSupportedLanguageException(ex1.getErrorCode(), ex1.getErrorText(), mainResponseDTO);
		}
	}
}