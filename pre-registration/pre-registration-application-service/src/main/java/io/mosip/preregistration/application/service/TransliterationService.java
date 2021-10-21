/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.transliteration.spi.Transliteration;
import io.mosip.preregistration.application.dto.TransliterationRequestDTO;
import io.mosip.preregistration.application.dto.TransliterationResponseDTO;
import io.mosip.preregistration.application.errorcodes.TransliterationErrorCodes;
import io.mosip.preregistration.application.errorcodes.TransliterationErrorMessage;
import io.mosip.preregistration.application.exception.MandatoryFieldRequiredException;
import io.mosip.preregistration.application.exception.UnSupportedLanguageException;
import io.mosip.preregistration.application.exception.util.TransliterationExceptionCatcher;
import io.mosip.preregistration.application.service.util.TransliterationServiceUtil;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;

/**
 * This class provides the service implementation for Transliteration
 * application.
 * 
 * @author Kishan Rathore
 * @since 1.0.0
 *
 */
@Service
public class TransliterationService {

	/**
	 * Autowired reference
	 */
	@Autowired
	private Transliteration<String> translitrator;

	/**
	 * Autowired reference for {@link #serviceUtil}
	 */
	@Autowired
	private TransliterationServiceUtil serviceUtil;

	/**
	 * 
	 * This method is used to transliterate the given data.
	 * 
	 * @param requestDTO
	 * @return responseDto with transliterated value
	 */
	public MainResponseDTO<TransliterationResponseDTO> translitratorService(
			MainRequestDTO<TransliterationRequestDTO> requestDTO) {
		MainResponseDTO<TransliterationResponseDTO> responseDTO = new MainResponseDTO<>();
		responseDTO.setId(requestDTO.getId());
		responseDTO.setVersion(requestDTO.getVersion());
		try {
			TransliterationRequestDTO transliterationRequestDTO = requestDTO.getRequest();
			if (serviceUtil.isEntryFieldsNull(transliterationRequestDTO)) {
				String toFieldValue = translitrator.transliterate(transliterationRequestDTO.getFromFieldLang(),
						transliterationRequestDTO.getToFieldLang(), transliterationRequestDTO.getFromFieldValue());
				responseDTO.setResponse(serviceUtil.responseSetter(toFieldValue, transliterationRequestDTO));
				responseDTO.setResponsetime(serviceUtil.getCurrentResponseTime());
			} else {
				throw new MandatoryFieldRequiredException(TransliterationErrorCodes.PRG_TRL_APP_002.getCode(),
						TransliterationErrorMessage.INCORRECT_MANDATORY_FIELDS.getMessage(), responseDTO);
			}
		} catch (Exception e) {
			throw new UnSupportedLanguageException(TransliterationErrorCodes.PRG_TRL_APP_002.getCode(),
					TransliterationErrorMessage.UNSUPPORTED_LANGUAGE.getMessage(), responseDTO);
		}
		return responseDTO;
	}
}
