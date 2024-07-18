/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.application.test.exception;

import java.sql.Timestamp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import io.mosip.kernel.core.transliteration.spi.Transliteration;
import io.mosip.preregistration.application.dto.TransliterationRequestDTO;
import io.mosip.preregistration.application.exception.MandatoryFieldRequiredException;
import io.mosip.preregistration.application.exception.MissingRequestParameterException;
import io.mosip.preregistration.application.exception.UnSupportedLanguageException;
import io.mosip.preregistration.application.service.TransliterationService;
import io.mosip.preregistration.application.service.util.TransliterationServiceUtil;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.util.RequestValidator;

/**
 * Test class to test the transliteration application exceptions
 * 
 * @author Kishan Rathore
 * @since 1.0.0
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TransliterationService.class)
public class TranslitrationExceptionTest {

	@MockBean
	private RequestValidator requestValidator;

	/**
	 * MockBean reference for $link{LanguageIdRepository}
	 */
	/*
	 * @MockBean private LanguageIdRepository idRepository;
	 */

	/**
	 * Mock reference for $link{TransliterationService}
	 */
	@Mock
	private TransliterationService serviceImpl2;

	/**
	 * Autowired reference for $link{TransliterationService}
	 */
	@Autowired
	private TransliterationService serviceImpl;

	@MockBean
	private Transliteration<String> translitrator;

	@MockBean
	private TransliterationServiceUtil util;

	/**
	 * Throws the InvalidRequestParameterException.
	 */
	@Test(expected = UnSupportedLanguageException.class)
	public void mandetoryRequestFieldsTest() {
		TransliterationRequestDTO mandatoryFielddto = new TransliterationRequestDTO();
		mandatoryFielddto.setFromFieldLang("eng");
		mandatoryFielddto.setFromFieldValue("Kishan");
		mandatoryFielddto.setToFieldLang("");

		MainRequestDTO<TransliterationRequestDTO> mandatoryFieldRequest = new MainRequestDTO<>();
		mandatoryFieldRequest.setId("mosip.pre-registration.transliteration.transliterate");
		mandatoryFieldRequest.setRequesttime(new Timestamp(System.currentTimeMillis()));
		mandatoryFieldRequest.setRequest(mandatoryFielddto);
		Mockito.when(serviceImpl.translitratorService(mandatoryFieldRequest))
				.thenThrow(UnSupportedLanguageException.class);

	}

	/**
	 * Throws the MandatoryFieldRequiredException.
	 */
	@Test
	public void mandetoryDtoFieldsTest() {
		MandatoryFieldRequiredException ex = new MandatoryFieldRequiredException();

		TransliterationRequestDTO mandatoryFielddto = new TransliterationRequestDTO();
		mandatoryFielddto.setFromFieldLang("");
		mandatoryFielddto.setFromFieldValue("Kishan");
		mandatoryFielddto.setToFieldLang("Arabic");

		MainRequestDTO<TransliterationRequestDTO> mandatoryFieldRequest = new MainRequestDTO<>();
		mandatoryFieldRequest.setId("mosip.pre-registration.transliteration.transliterate");
		mandatoryFieldRequest.setVersion("1.0");
		mandatoryFieldRequest.setRequesttime(new Timestamp(System.currentTimeMillis()));
		mandatoryFieldRequest.setRequest(mandatoryFielddto);

		Mockito.when(serviceImpl2.translitratorService(mandatoryFieldRequest)).thenThrow(ex);

	}

	/**
	 * Throws the MissingRequestParameterException.
	 */
	@Test
	public void illegalParamTest() {

		MissingRequestParameterException ex = new MissingRequestParameterException("MISSING_PARAM", null);
		TransliterationRequestDTO mandatoryFielddto = new TransliterationRequestDTO();
		mandatoryFielddto.setFromFieldLang("");
		mandatoryFielddto.setFromFieldValue("Kishan");
		mandatoryFielddto.setToFieldLang("Arabic");
		MainRequestDTO<TransliterationRequestDTO> mandatoryFieldRequest = new MainRequestDTO<>();
		mandatoryFieldRequest.setId("mosip.pre-registration.transliteration.transliterate");
		Mockito.when(serviceImpl2.translitratorService(mandatoryFieldRequest)).thenThrow(ex);

	}

}
