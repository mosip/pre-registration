/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.application.test.service;


import static org.junit.Assert.assertEquals;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import io.mosip.kernel.core.transliteration.spi.Transliteration;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.preregistration.application.dto.TransliterationRequestDTO;
import io.mosip.preregistration.application.dto.TransliterationResponseDTO;
import io.mosip.preregistration.application.entity.LanguageIdEntity;
import io.mosip.preregistration.application.exception.UnSupportedLanguageException;
import io.mosip.preregistration.application.repository.LanguageIdRepository;
import io.mosip.preregistration.application.service.TransliterationService;
import io.mosip.preregistration.application.service.util.TransliterationServiceUtil;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;

@RunWith(JUnit4.class)
@SpringBootTest
@ContextConfiguration(classes = { TransliterationService.class })
public class TransliterationServiceTest {
	
	@Mock
	private LanguageIdRepository idRepository;
	@Mock
	private TransliterationServiceUtil serviceUtil;


	@Mock
	private Transliteration<String> translitrator;

	@InjectMocks
	private TransliterationService transliterationServiceImpl;
	
	private LanguageIdEntity idEntity;
	
	JSONParser parser = new JSONParser();
	
	private MainRequestDTO< TransliterationRequestDTO> requestDto=null;
	private TransliterationRequestDTO transliterationRequest=null;
	boolean requestValidatorFlag = false;
	Map<String, String> requestMap = new HashMap<>();
	Map<String, String> requiredRequestMap = new HashMap<>();
	String times = null;
	MainResponseDTO<TransliterationResponseDTO> responseDTO = null;
	
	@Value("${version}")
	String versionUrl;

	@Value("${mosip.pre-registration.transliteration.transliterate.id}")
	String idUrl;

	
	@Before
	public void setUp()  {
		MockitoAnnotations.initMocks(this);
		String dateTimeFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
		idEntity=new LanguageIdEntity();
		times = DateUtils.formatDate(new Date(), dateTimeFormat);
		idEntity.setFromLang("English");
		idEntity.setLanguageId("Latin-Arabic");
		idEntity.setToLang("Arabic");
		
		transliterationRequest=new TransliterationRequestDTO();
		transliterationRequest.setFromFieldLang("eng");
		transliterationRequest.setFromFieldValue("Kishan");
		transliterationRequest.setToFieldLang("ara");
		
		
		responseDTO = new MainResponseDTO<TransliterationResponseDTO>();
		responseDTO.setResponsetime(times);
		Mockito.when(serviceUtil.getCurrentResponseTime()).thenReturn(times);
		
		responseDTO.setErrors(null);
	}
	
	@Test
	public void successTest() {
		Mockito.when(serviceUtil.isEntryFieldsNull(Mockito.any())).thenReturn(true);
		
		Mockito.when(translitrator.transliterate(Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn("abc");
		Mockito.when(idRepository.findByFromLangAndToLang(Mockito.any(), Mockito.any())).thenReturn(idEntity);
		TransliterationResponseDTO transliterationRequest2=new TransliterationResponseDTO();
		transliterationRequest2.setFromFieldLang("eng");
		transliterationRequest2.setFromFieldValue("Kishan");
		transliterationRequest2.setToFieldLang("ara");
		transliterationRequest2.setToFieldValue("كِسهَن");
		MainRequestDTO<TransliterationRequestDTO> requestDto=new MainRequestDTO<TransliterationRequestDTO>();
		requestDto.setId("mosip.pre-registration.transliteration.transliterate");
		requestDto.setRequesttime(new Timestamp(System.currentTimeMillis()));
		requestDto.setVersion("1.0");
		requestDto.setRequest(transliterationRequest);
		responseDTO.setResponse(transliterationRequest2);
		Mockito.when(serviceUtil.responseSetter(Mockito.any(), Mockito.any())).thenReturn(transliterationRequest2);
		MainResponseDTO<TransliterationResponseDTO> result=transliterationServiceImpl.translitratorService(requestDto);
		assertEquals(result.getResponse().getToFieldValue(), responseDTO.getResponse().getToFieldValue());
		
	}

	
	@Test(expected=UnSupportedLanguageException.class)
	public void mandatoryFieldFailTest() {
		Mockito.when(idRepository.findByFromLangAndToLang(Mockito.any(), Mockito.any())).thenReturn(idEntity);		
		Mockito.when(serviceUtil.isEntryFieldsNull(Mockito.any())).thenReturn(false);
		
		TransliterationResponseDTO transliterationRequest2=new TransliterationResponseDTO();
		TransliterationRequestDTO request=new TransliterationRequestDTO();
		request.setFromFieldLang("");
		request.setFromFieldValue("Kishan");
		request.setToFieldLang("ara");
		requestDto=new MainRequestDTO<TransliterationRequestDTO>();
		requestDto.setId("mosip.pre-registration.transliteration.transliterate");
		requestDto.setRequesttime(new Timestamp(System.currentTimeMillis()));
		requestDto.setVersion("1.0");
		requestDto.setRequest(request);
		responseDTO.setResponse(transliterationRequest2);
		transliterationServiceImpl.translitratorService(requestDto);
		
	}
	@Test(expected=UnSupportedLanguageException.class)
	public void unSupportedLangTest() {
		Mockito.when(idRepository.findByFromLangAndToLang(Mockito.any(), Mockito.any())).thenReturn(idEntity);
		TransliterationRequestDTO transliterationRequest=new TransliterationRequestDTO();
		transliterationRequest=new TransliterationRequestDTO();
		transliterationRequest.setFromFieldLang("enl");
		transliterationRequest.setFromFieldValue("Kishan");
		transliterationRequest.setToFieldLang("ara");
		requestDto=new MainRequestDTO<TransliterationRequestDTO>();
		requestDto.setId("mosip.pre-registration.transliteration.transliterate");
		requestDto.setRequesttime(new Timestamp(System.currentTimeMillis()));
		requestDto.setVersion("1.0");
		requestDto.setRequest(transliterationRequest);
		transliterationServiceImpl.translitratorService(requestDto);
	}

	
}

