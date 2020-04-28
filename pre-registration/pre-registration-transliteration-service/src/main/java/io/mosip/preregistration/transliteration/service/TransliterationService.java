/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.transliteration.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.transliteration.dto.TransliterationRequestDTO;
import io.mosip.preregistration.transliteration.dto.TransliterationResponseDTO;
import io.mosip.preregistration.transliteration.errorcode.ErrorCodes;
import io.mosip.preregistration.transliteration.errorcode.ErrorMessage;
import io.mosip.preregistration.transliteration.exception.MandatoryFieldRequiredException;
import io.mosip.preregistration.transliteration.exception.UnSupportedLanguageException;
import io.mosip.preregistration.transliteration.exception.util.TransliterationExceptionCatcher;
import io.mosip.preregistration.transliteration.repository.LanguageIdRepository;
import io.mosip.preregistration.transliteration.service.util.TransliterationServiceUtil;
import io.mosip.preregistration.transliteration.util.PreRegistrationTransliterator;

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
	 * Autowired reference for {@link #LanguageIdRepository}
	 */
	@Autowired
	private LanguageIdRepository idRepository;

	/**
	 * Autowired reference for {@link #translitrator}
	 */
	@Autowired
	private PreRegistrationTransliterator translitrator;

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
	public MainResponseDTO<TransliterationResponseDTO> translitratorService(MainRequestDTO<TransliterationRequestDTO> requestDTO) {
		MainResponseDTO<TransliterationResponseDTO> responseDTO = new MainResponseDTO<>();
		responseDTO.setId(requestDTO.getId());
		responseDTO.setVersion(requestDTO.getVersion());
		try {
				TransliterationRequestDTO transliterationRequestDTO = requestDTO.getRequest();
				if (serviceUtil.isEntryFieldsNull(transliterationRequestDTO)) {
					if(serviceUtil.supportedLanguageCheck(transliterationRequestDTO)) {
						String languageId = idRepository
								.findByFromLangAndToLang(transliterationRequestDTO.getFromFieldLang(),
										transliterationRequestDTO.getToFieldLang())
								.getLanguageId();
						String toFieldValue = translitrator.translitrator(languageId,
								transliterationRequestDTO.getFromFieldValue());
						responseDTO.setResponse(serviceUtil.responseSetter(toFieldValue, transliterationRequestDTO));
						responseDTO.setResponsetime(serviceUtil.getCurrentResponseTime());
					}
					else {
						throw new UnSupportedLanguageException(ErrorCodes.PRG_TRL_APP_008.getCode(), 
								ErrorMessage.UNSUPPORTED_LANGUAGE.getMessage(),responseDTO);
					}

					
				} else {
					throw new MandatoryFieldRequiredException(ErrorCodes.PRG_TRL_APP_002.getCode(),
							ErrorMessage.INCORRECT_MANDATORY_FIELDS.getMessage(),responseDTO);
				}
		} catch (Exception e) {
			e.printStackTrace();
			new TransliterationExceptionCatcher().handle(e,responseDTO);
		}
		return responseDTO;
	}
}
