package io.mosip.preregistration.application.service;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import io.mosip.preregistration.core.common.dto.*;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.exception.IOException;
import io.mosip.kernel.core.util.exception.JsonMappingException;
import io.mosip.kernel.core.util.exception.JsonParseException;
import io.mosip.preregistration.application.service.util.NotificationServiceUtil;
import io.mosip.preregistration.core.code.AuditLogVariables;
import io.mosip.preregistration.core.code.BookingTypeCodes;
import io.mosip.preregistration.core.common.entity.ApplicationEntity;
import io.mosip.preregistration.core.common.service.UserDetailsService;
import io.mosip.preregistration.core.util.AuditLogUtil;
import io.mosip.preregistration.core.util.NotificationUtil;
import io.mosip.preregistration.core.util.ValidationUtil;
import io.mosip.preregistration.application.dto.QRCodeResponseDTO;
import io.mosip.preregistration.application.code.NotificationRequestCodes;
import io.mosip.preregistration.application.exception.DemographicDetailsNotFoundException;
import io.mosip.preregistration.application.exception.MandatoryFieldException;
import io.mosip.preregistration.application.exception.RecordNotFoundException;
import io.mosip.preregistration.application.errorcodes.ApplicationErrorCodes;
import io.mosip.preregistration.application.errorcodes.ApplicationErrorMessages;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Sanober Noor
 * @since 1.0.0
 * @author Aiham Hasan
 * @since 1.2.0
 */

@RunWith(JUnit4.class)
@SpringBootTest
@ContextConfiguration(classes = { NotificationService.class })
public class NotificationServiceTest {

	@InjectMocks
	private NotificationService notificationService;

	@Mock
	private NotificationUtil notificationUtil;

	@Mock
	private ValidationUtil validationUtil;

	@Mock
	private DemographicServiceIntf demographicServiceIntf;

	@Mock
	private ApplicationServiceIntf applicationServiceIntf;

	@Mock
	private NotificationServiceUtil notificationServiceUtil;

	@Mock
	private ObjectMapper mapper;

	@MockBean(name = "restTemplateConfig")
	private RestTemplate restTemplate;

	@Value("${mosip.utc-datetime-pattern}")
	private String utcDateTimePattern;

	@Value("#{'${mosip.notificationtype}'.split('\\|')}")
	private List<String> notificationTypeList;

	@Value("${mosip.pre-registration.notification.id}")
	private String Id;

	@Value("${preregistration.identity}")
	private String identity;

	@Value("${preregistration.identity.name}")
	private String fullName;

	@Value("${version}")
	private String version;

	@Value("${preregistration.identity.email}")
	private String email;

	@Value("${preregistration.identity.phone}")
	private String phone;

	@Value("${preregistration.notification.nameFormat}")
	private String nameFormat;

	@Mock
	private NotificationUtil NotificationUtil;
	private NotificationDTO notificationDTO;
	boolean requestValidatorFlag = false;
	TemplateResponseDTO templateResponseDTO = new TemplateResponseDTO();
	MainResponseDTO<NotificationResponseDTO> responseDTO = new MainResponseDTO<>();
	MainResponseDTO<NotificationResponseDTO> responselist = new MainResponseDTO<>();
	MainResponseDTO<QRCodeResponseDTO> qrCodeResponseDTO = new MainResponseDTO<>();
	NotificationResponseDTO notificationResponseDTO = new NotificationResponseDTO();
	MainRequestDTO<NotificationDTO> mainReqDto = new MainRequestDTO<>();
	List<TemplateResponseDTO> tepmlateList = new ArrayList<>();
	NotificationResponseDTO response = new NotificationResponseDTO();

	JSONParser parser = new JSONParser();
	private JSONObject jsonTestObject;
	private JSONObject jsonObject;
	AuditRequestDto auditRequestDto = new AuditRequestDto();

	Map<String, String> requiredRequestMap = new HashMap<>();

	@Mock
	private AuditLogUtil auditLogUtil;

	/**
	 * Backs the contact_info recovery path. Without this the injected field is null and any test
	 * reaching the fallback with a canonical contact_info fails with an NPE instead of exercising
	 * recovery.
	 */
	@Mock
	private UserDetailsService userDetailsService;
	MainResponseDTO<BookingRegistrationDTO> bookingResultDto = new MainResponseDTO<>();
	MainResponseDTO<DemographicResponseDTO> demographicdto = new MainResponseDTO<>();
	MainResponseDTO<ApplicationEntity> appEntity = new MainResponseDTO<>();

	@Before
	public void beforeSet()
			throws ParseException, FileNotFoundException, java.io.IOException, org.json.simple.parser.ParseException {
		MockitoAnnotations.initMocks(this);
		ReflectionTestUtils.setField(notificationService, "version", version);
		ReflectionTestUtils.setField(notificationService, "Id", "1");
		ReflectionTestUtils.setField(notificationService, "identity", "identity");
		ReflectionTestUtils.setField(notificationService, "fullName", "fullName");
		ReflectionTestUtils.setField(notificationService, "email", "email");
		ReflectionTestUtils.setField(notificationService, "phone", "phone");
		ReflectionTestUtils.setField(notificationService, "nameFormat", "fullName");

		ClassLoader classLoader = getClass().getClassLoader();
		File fileTest = new File(classLoader.getResource("pre-registration.json").getFile());
		FileReader reader = new FileReader(fileTest);
		jsonTestObject = (JSONObject) parser.parse(reader);

		File fileTest1 = new File(classLoader.getResource("pre-registration-test.json").getFile());
		jsonObject = (JSONObject) parser.parse(new FileReader(fileTest1));

		mapper.setTimeZone(TimeZone.getDefault());
		notificationDTO = new NotificationDTO();
		notificationDTO.setName("sanober Noor");
		notificationDTO.setPreRegistrationId("20180396713560");
		notificationDTO.setMobNum("8124567898");
		notificationDTO.setEmailID("sanober.noor2@mindtree.com");
		notificationDTO.setAppointmentDate("2019-01-22");
		notificationDTO.setAppointmentTime("09:00 AM");
		notificationDTO.setAdditionalRecipient(false);
		notificationDTO.setIsBatch(true);
		mainReqDto.setId("mosip.pre-registration.notification.notify");
		mainReqDto.setVersion("1.0");
		mapper.setTimeZone(TimeZone.getDefault());
		mainReqDto.setRequesttime(new Timestamp(System.currentTimeMillis()));
		mainReqDto.setRequest(notificationDTO);
		responseDTO = new MainResponseDTO<>();
		response.setMessage("Email and sms request successfully submitted");
		responseDTO.setResponse(response);
		responseDTO.setResponsetime(validationUtil.getCurrentResponseTime());
		templateResponseDTO.setFileText("Email message");
		tepmlateList.add(templateResponseDTO);

		notificationResponseDTO.setMessage("Notification send successfully");
		notificationResponseDTO.setStatus("True");

		auditRequestDto.setActionTimeStamp(LocalDateTime.now(ZoneId.of("UTC")));
		auditRequestDto.setApplicationId(AuditLogVariables.MOSIP_1.toString());
		auditRequestDto.setApplicationName(AuditLogVariables.PREREGISTRATION.toString());
		auditRequestDto.setCreatedBy(AuditLogVariables.SYSTEM.toString());
		auditRequestDto.setHostIp(auditLogUtil.getServerIp());
		auditRequestDto.setHostName(auditLogUtil.getServerName());
		auditRequestDto.setId(AuditLogVariables.NO_ID.toString());
		auditRequestDto.setIdType(AuditLogVariables.PRE_REGISTRATION_ID.toString());
		auditRequestDto.setSessionUserId(AuditLogVariables.SYSTEM.toString());
		auditRequestDto.setSessionUserName(AuditLogVariables.SYSTEM.toString());
		AuthUserDetails applicationUser = Mockito.mock(AuthUserDetails.class);
		Authentication authentication = Mockito.mock(Authentication.class);
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(applicationUser);

		BookingRegistrationDTO bookingResponse = new BookingRegistrationDTO();
		bookingResponse.setRegDate("2019-01-22");
		bookingResponse.setRegistrationCenterId("1");
		bookingResponse.setSlotFromTime("09:00");
		bookingResponse.setSlotToTime("10:00");
		bookingResultDto.setResponse(bookingResponse);

		DemographicResponseDTO demo = new DemographicResponseDTO();
		demo.setPreRegistrationId("20180396713560");
		JSONParser jsonParser = new JSONParser();
		JSONObject demoString = (JSONObject) jsonParser.parse(jsonTestObject.toJSONString());
		JSONObject demoResponseData = (JSONObject) demoString.get("request");
		org.json.simple.JSONObject demoDetailsData = (JSONObject) demoResponseData.get("demographicDetails");
		demo.setDemographicDetails(demoDetailsData);
		demographicdto.setResponse(demo);

		ApplicationEntity appEntityResp = new ApplicationEntity();
		appEntityResp.setApplicationId("20180396713560");
		appEntityResp.setBookingType(BookingTypeCodes.NEW_PREREGISTRATION.toString());
		appEntity.setResponse(appEntityResp);

	}

	/**
	 * This test method is for success case of sendNotificationSuccess
	 * 
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @throws java.io.IOException
	 */
	//
	@Test
	public void sendNotificationSuccessTest()
			throws JsonParseException, JsonMappingException, IOException, java.io.IOException {

//		String stringjson = mapper.writeValueAsString(mainReqDto);
		String langCode = "fra";
		MultipartFile file = new MockMultipartFile("test.txt", "test.txt", null, new byte[1100]);
		Mockito.when(applicationServiceIntf.getApplicationInfoInternal(Mockito.any())).thenReturn(appEntity);
		Mockito.when(demographicServiceIntf.getDemographicData(Mockito.any())).thenReturn(demographicdto);
		Mockito.when(notificationUtil.getAppointmentDetails(Mockito.anyString())).thenReturn(bookingResultDto);

//		String stringjson = mapper.writeValueAsString(mainReqDto);
		String stringjson = null;
		try {
			stringjson = mapper.writeValueAsString(mainReqDto);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Mockito.when(validationUtil.requestValidator(Mockito.any(), Mockito.any())).thenReturn(true);

		try {
			Mockito.when(notificationServiceUtil.createNotificationDetails(null, "fra", false)).thenReturn(mainReqDto);
		} catch (RuntimeException | io.mosip.kernel.core.util.exception.JsonMappingException
				| io.mosip.kernel.core.exception.IOException | JSONException | java.text.ParseException
				| io.mosip.kernel.core.util.exception.JsonParseException ex) {
		} catch (com.fasterxml.jackson.core.JsonParseException
				| com.fasterxml.jackson.databind.JsonMappingException ex) {
		}

		TemplateResponseListDTO templateResponseListDTO = new TemplateResponseListDTO();
		templateResponseListDTO.setTemplates(tepmlateList);
		Mockito.when(NotificationUtil.notify("sms", notificationDTO, file, appEntity.getResponse().getBookingType()))
				.thenReturn(responselist);
		ResponseEntity<TemplateResponseListDTO> res = new ResponseEntity<TemplateResponseListDTO>(
				templateResponseListDTO, HttpStatus.OK);
//		Mockito.when(restTemplate.getForEntity(Mockito.anyString(), Mockito.eq(TemplateResponseListDTO.class)))
//				.thenReturn(res);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		ResponseEntity<NotificationResponseDTO> resp = new ResponseEntity<NotificationResponseDTO>(
				notificationResponseDTO, HttpStatus.OK);
//		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.eq(HttpMethod.POST), Mockito.any(),
//				Mockito.eq(NotificationResponseDTO.class))).thenReturn(resp);
		MainResponseDTO<io.mosip.preregistration.application.dto.NotificationResponseDTO> response = notificationService
				.sendNotification(stringjson, langCode, file, false);
		assertEquals(responseDTO.getResponse().getMessage(), response.getResponse().getMessage());
	}

	@Test
	public void sendNotificationSuccessTest2()
			throws JsonParseException, JsonMappingException, IOException, java.io.IOException {
		NotificationDTO notificationDTO = new NotificationDTO();
		MainResponseDTO<ApplicationEntity> appEntity = new MainResponseDTO<>();
		ApplicationEntity appEntityResp = new ApplicationEntity();
		appEntityResp.setApplicationId("24346587843");
		appEntityResp.setBookingType(BookingTypeCodes.LOST_FORGOTTEN_UIN.toString());
		appEntity.setResponse(appEntityResp);

		String langCode = "fra";
		MultipartFile file = new MockMultipartFile("test.txt", "test.txt", null, new byte[1100]);
		Mockito.when(applicationServiceIntf.getApplicationInfoInternal(Mockito.any())).thenReturn(appEntity);
		Mockito.when(demographicServiceIntf.getDemographicData(Mockito.any())).thenReturn(demographicdto);
		Mockito.when(notificationUtil.getAppointmentDetails(Mockito.anyString())).thenReturn(bookingResultDto);

		String stringjson = null;
		try {
			stringjson = mapper.writeValueAsString(mainReqDto);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		Mockito.when(validationUtil.requestValidator(Mockito.any(), Mockito.any())).thenReturn(true);

		try {
			Mockito.when(notificationServiceUtil.createNotificationDetails(null, "fra", false)).thenReturn(mainReqDto);
		} catch (RuntimeException | io.mosip.kernel.core.util.exception.JsonMappingException
				| io.mosip.kernel.core.exception.IOException | JSONException | java.text.ParseException
				| io.mosip.kernel.core.util.exception.JsonParseException ex) {
		} catch (com.fasterxml.jackson.core.JsonParseException
				| com.fasterxml.jackson.databind.JsonMappingException ex) {
		}

		TemplateResponseListDTO templateResponseListDTO = new TemplateResponseListDTO();
		templateResponseListDTO.setTemplates(tepmlateList);
		Mockito.when(NotificationUtil.notify("sms", notificationDTO, file, appEntity.getResponse().getBookingType()))
				.thenReturn(responselist);
		ResponseEntity<TemplateResponseListDTO> res = new ResponseEntity<TemplateResponseListDTO>(
				templateResponseListDTO, HttpStatus.OK);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		ResponseEntity<NotificationResponseDTO> resp = new ResponseEntity<NotificationResponseDTO>(
				notificationResponseDTO, HttpStatus.OK);
		MainResponseDTO<io.mosip.preregistration.application.dto.NotificationResponseDTO> response = notificationService
				.sendNotification(stringjson, langCode, file, false);
		assertEquals(responseDTO.getResponse().getMessage(), response.getResponse().getMessage());
	}

	@Test
	public void notificationDtoValidationTest() throws java.io.IOException, org.json.simple.parser.ParseException {
		String preId = "20180396713560";
		NotificationDTO notificationDTO = new NotificationDTO();
		notificationDTO.setName("Sanober");
		notificationDTO.setPreRegistrationId("20180396713560");
		notificationDTO.setMobNum("9876543210");
		notificationDTO.setEmailID("test@gmail.com");
		notificationDTO.setAppointmentDate("2019-01-22");
		notificationDTO.setAppointmentTime("09:00 AM");
		notificationDTO.setAdditionalRecipient(false);
		notificationDTO.setIsBatch(false);
		notificationDTO.setLanguageCode("eng");
		Mockito.when(applicationServiceIntf.getApplicationInfoInternal(Mockito.any())).thenReturn(appEntity);
		Mockito.when(demographicServiceIntf.getDemographicData(Mockito.any())).thenReturn(demographicdto);
		Mockito.when(notificationUtil.getAppointmentDetails(preId)).thenReturn(bookingResultDto);
		MainResponseDTO<DemographicResponseDTO> response = notificationService
				.notificationDtoValidationV2(BookingTypeCodes.NEW_PREREGISTRATION.toString(), notificationDTO);
		assertEquals(preId, response.getResponse().getPreRegistrationId());
	}

	@Test(expected = MandatoryFieldException.class)
	public void notificationDtoMandatoryFieldExceptionTest()
			throws java.io.IOException, org.json.simple.parser.ParseException {
		String preId = "20180396713560";
		NotificationDTO notificationDTO = new NotificationDTO();
		notificationDTO.setName("Sanober");
		notificationDTO.setPreRegistrationId("20180396713560");
		notificationDTO.setMobNum("9876543210");
		notificationDTO.setEmailID("test@gmail.com");
		notificationDTO.setAppointmentTime("09:30 AM");
		notificationDTO.setIsBatch(false);
		Mockito.when(applicationServiceIntf.getApplicationInfoInternal(Mockito.any())).thenReturn(appEntity);
		Mockito.when(demographicServiceIntf.getDemographicData(Mockito.any())).thenReturn(demographicdto);
		Mockito.when(notificationUtil.getAppointmentDetails(preId)).thenReturn(bookingResultDto);
		notificationService.notificationDtoValidationV2(BookingTypeCodes.NEW_PREREGISTRATION.toString(), notificationDTO);
	}

	@Test(expected = MandatoryFieldException.class)
	public void notificationDtoTimeNotCorrectExceptionTest()
			throws java.io.IOException, org.json.simple.parser.ParseException {
		String preId = "20180396713560";
		NotificationDTO notificationDTO = new NotificationDTO();
		notificationDTO.setName("Sanober");
		notificationDTO.setPreRegistrationId("20180396713560");
		notificationDTO.setMobNum("9876543210");
		notificationDTO.setEmailID("test@gmail.com");
		notificationDTO.setAppointmentDate("2019-01-22");
		notificationDTO.setAppointmentTime("09:30 AM");
		notificationDTO.setAdditionalRecipient(false);
		notificationDTO.setIsBatch(false);
		Mockito.when(applicationServiceIntf.getApplicationInfoInternal(Mockito.any())).thenReturn(appEntity);
		Mockito.when(demographicServiceIntf.getDemographicData(Mockito.any())).thenReturn(demographicdto);
		Mockito.when(notificationUtil.getAppointmentDetails(preId)).thenReturn(bookingResultDto);
		notificationService.notificationDtoValidationV2(BookingTypeCodes.NEW_PREREGISTRATION.toString(), notificationDTO);
	}

	@Test(expected = MandatoryFieldException.class)
	public void notificationDtoValidationExceptionTest()
			throws java.io.IOException, org.json.simple.parser.ParseException {
		String preId = "20180396713560";
		NotificationDTO notificationDTO = new NotificationDTO();
		notificationDTO.setName("Sanober");
		notificationDTO.setPreRegistrationId("20180396713560");
		notificationDTO.setMobNum("9876543210");
		notificationDTO.setEmailID("test@gmail.com");
		notificationDTO.setAppointmentDate("2019-01-22");
		notificationDTO.setAdditionalRecipient(false);
		notificationDTO.setIsBatch(false);
		Mockito.when(applicationServiceIntf.getApplicationInfoInternal(Mockito.any())).thenReturn(appEntity);
		Mockito.when(demographicServiceIntf.getDemographicData(Mockito.any())).thenReturn(demographicdto);
		Mockito.when(notificationUtil.getAppointmentDetails(preId)).thenReturn(bookingResultDto);
		notificationService.notificationDtoValidationV2(BookingTypeCodes.NEW_PREREGISTRATION.toString(), notificationDTO);
	}

	@Test(expected = MandatoryFieldException.class)
	public void notificationDtoDateNotCorrectExcpetionTest()
			throws java.io.IOException, org.json.simple.parser.ParseException {
		String preId = "20180396713560";
		NotificationDTO notificationDTO = new NotificationDTO();
		notificationDTO.setName("Sanober");
		notificationDTO.setPreRegistrationId("20180396713560");
		notificationDTO.setMobNum("9876543210");
		notificationDTO.setEmailID("test@gmail.com");
		notificationDTO.setAppointmentDate("2022-03-22");
		notificationDTO.setAppointmentTime("09:00 AM");
		notificationDTO.setAdditionalRecipient(false);
		notificationDTO.setIsBatch(false);
		Mockito.when(applicationServiceIntf.getApplicationInfoInternal(Mockito.any())).thenReturn(appEntity);
		Mockito.when(demographicServiceIntf.getDemographicData(Mockito.any())).thenReturn(demographicdto);
		Mockito.when(notificationUtil.getAppointmentDetails(preId)).thenReturn(bookingResultDto);
		notificationService.notificationDtoValidationV2(BookingTypeCodes.NEW_PREREGISTRATION.toString(), notificationDTO);
	}

	@Test
	public void setupBookingServiceTest() {
		notificationService.setupBookingService();
	}

	@Test
	public void sendNotificationSuccess1Test()
			throws JsonParseException, JsonMappingException, IOException, java.io.IOException {

		notificationDTO = new NotificationDTO();
		notificationDTO.setName("sanober Noor");
		notificationDTO.setPreRegistrationId("20180396713560");
		notificationDTO.setMobNum("8124567898");
		notificationDTO.setEmailID("sanober.noor2@mindtree.com");
		notificationDTO.setAppointmentDate("2019-01-22");
		notificationDTO.setAppointmentTime("09:00 AM");
		notificationDTO.setIsBatch(true);
		notificationDTO.setAdditionalRecipient(true);
		mainReqDto.setId("mosip.pre-registration.notification.notify");
		mainReqDto.setVersion("1.0");
		mapper.setTimeZone(TimeZone.getDefault());
		mainReqDto.setRequesttime(new Timestamp(System.currentTimeMillis()));
		mainReqDto.setRequest(notificationDTO);

		Mockito.when(validationUtil.phoneValidator(notificationDTO.getMobNum())).thenReturn(true);
		Mockito.when(validationUtil.emailValidator(notificationDTO.getEmailID())).thenReturn(true);

		String langCode = "fra";
		MultipartFile file = new MockMultipartFile("test.txt", "test.txt", null, new byte[1100]);
		Mockito.when(applicationServiceIntf.getApplicationInfoInternal(Mockito.any())).thenReturn(appEntity);
		Mockito.when(demographicServiceIntf.getDemographicData(Mockito.any())).thenReturn(demographicdto);
		Mockito.when(notificationUtil.getAppointmentDetails(Mockito.anyString())).thenReturn(bookingResultDto);
		String stringjson = null;
		Mockito.when(validationUtil.requestValidator(Mockito.any(), Mockito.any())).thenReturn(true);
		try {
			Mockito.when(notificationServiceUtil.createNotificationDetails(null, "fra", false)).thenReturn(mainReqDto);
		} catch (RuntimeException | io.mosip.kernel.core.util.exception.JsonMappingException
				| io.mosip.kernel.core.exception.IOException | JSONException | java.text.ParseException
				| io.mosip.kernel.core.util.exception.JsonParseException ex) {
		} catch (com.fasterxml.jackson.core.JsonParseException
				| com.fasterxml.jackson.databind.JsonMappingException ex) {
		}

		TemplateResponseListDTO templateResponseListDTO = new TemplateResponseListDTO();
		templateResponseListDTO.setTemplates(tepmlateList);
		Mockito.when(NotificationUtil.notify("sms", notificationDTO, file, appEntity.getResponse().getBookingType()))
				.thenReturn(responselist);
		ResponseEntity<TemplateResponseListDTO> res = new ResponseEntity<TemplateResponseListDTO>(
				templateResponseListDTO, HttpStatus.OK);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		ResponseEntity<NotificationResponseDTO> resp = new ResponseEntity<NotificationResponseDTO>(
				notificationResponseDTO, HttpStatus.OK);
		MainResponseDTO<io.mosip.preregistration.application.dto.NotificationResponseDTO> response = notificationService
				.sendNotification(stringjson, langCode, file, false);
	}

	@Test(expected = MandatoryFieldException.class)
	public void sendNotificationException1Test()
			throws JsonParseException, JsonMappingException, IOException, java.io.IOException {

		notificationDTO = new NotificationDTO();
		notificationDTO.setName("sanober Noor");
		notificationDTO.setPreRegistrationId("20180396713560");
		notificationDTO.setMobNum("8124567898");
		notificationDTO.setEmailID("sanober.noor2@mindtree.com");
		notificationDTO.setAppointmentDate("2019-01-22");
		notificationDTO.setAppointmentTime("09:00 AM");
		notificationDTO.setIsBatch(true);
		notificationDTO.setAdditionalRecipient(true);
		mainReqDto.setId("mosip.pre-registration.notification.notify");
		mainReqDto.setVersion("1.0");
		mapper.setTimeZone(TimeZone.getDefault());
		mainReqDto.setRequesttime(new Timestamp(System.currentTimeMillis()));
		mainReqDto.setRequest(notificationDTO);

		Mockito.when(validationUtil.phoneValidator(notificationDTO.getMobNum())).thenReturn(true);

		String langCode = "fra";
		MultipartFile file = new MockMultipartFile("test.txt", "test.txt", null, new byte[1100]);
		Mockito.when(applicationServiceIntf.getApplicationInfoInternal(Mockito.any())).thenReturn(appEntity);
		Mockito.when(demographicServiceIntf.getDemographicData(Mockito.any())).thenReturn(demographicdto);
		Mockito.when(notificationUtil.getAppointmentDetails(Mockito.anyString())).thenReturn(bookingResultDto);
		String stringjson = null;
		Mockito.when(validationUtil.requestValidator(Mockito.any(), Mockito.any())).thenReturn(true);
		try {
			Mockito.when(notificationServiceUtil.createNotificationDetails(null, "fra", false)).thenReturn(mainReqDto);
		} catch (RuntimeException | io.mosip.kernel.core.util.exception.JsonMappingException
				| io.mosip.kernel.core.exception.IOException | JSONException | java.text.ParseException
				| io.mosip.kernel.core.util.exception.JsonParseException ex) {
		} catch (com.fasterxml.jackson.core.JsonParseException
				| com.fasterxml.jackson.databind.JsonMappingException ex) {
		}

		TemplateResponseListDTO templateResponseListDTO = new TemplateResponseListDTO();
		templateResponseListDTO.setTemplates(tepmlateList);
		Mockito.when(NotificationUtil.notify("sms", notificationDTO, file, appEntity.getResponse().getBookingType()))
				.thenReturn(responselist);
		ResponseEntity<TemplateResponseListDTO> res = new ResponseEntity<TemplateResponseListDTO>(
				templateResponseListDTO, HttpStatus.OK);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		ResponseEntity<NotificationResponseDTO> resp = new ResponseEntity<NotificationResponseDTO>(
				notificationResponseDTO, HttpStatus.OK);
		MainResponseDTO<io.mosip.preregistration.application.dto.NotificationResponseDTO> response = notificationService
				.sendNotification(stringjson, langCode, file, false);
	}

	@Test(expected = MandatoryFieldException.class)
	public void sendNotificationException2Test()
			throws JsonParseException, JsonMappingException, IOException, java.io.IOException {

		notificationDTO = new NotificationDTO();
		notificationDTO.setName("sanober Noor");
		notificationDTO.setPreRegistrationId("20180396713560");
		notificationDTO.setMobNum("8124567898");
		notificationDTO.setEmailID("sanober.noor2@mindtree.com");
		notificationDTO.setAppointmentDate("2019-01-22");
		notificationDTO.setAppointmentTime("09:00 AM");
		notificationDTO.setIsBatch(true);
		notificationDTO.setAdditionalRecipient(true);
		mainReqDto.setId("mosip.pre-registration.notification.notify");
		mainReqDto.setVersion("1.0");
		mapper.setTimeZone(TimeZone.getDefault());
		mainReqDto.setRequesttime(new Timestamp(System.currentTimeMillis()));
		mainReqDto.setRequest(notificationDTO);

		String langCode = "fra";
		MultipartFile file = new MockMultipartFile("test.txt", "test.txt", null, new byte[1100]);
		Mockito.when(applicationServiceIntf.getApplicationInfoInternal(Mockito.any())).thenReturn(appEntity);
		Mockito.when(demographicServiceIntf.getDemographicData(Mockito.any())).thenReturn(demographicdto);
		Mockito.when(notificationUtil.getAppointmentDetails(Mockito.anyString())).thenReturn(bookingResultDto);
		String stringjson = null;
		Mockito.when(validationUtil.requestValidator(Mockito.any(), Mockito.any())).thenReturn(true);
		try {
			Mockito.when(notificationServiceUtil.createNotificationDetails(null, "fra", false)).thenReturn(mainReqDto);
		} catch (RuntimeException | io.mosip.kernel.core.util.exception.JsonMappingException
				| io.mosip.kernel.core.exception.IOException | JSONException | java.text.ParseException
				| io.mosip.kernel.core.util.exception.JsonParseException ex) {
		} catch (com.fasterxml.jackson.core.JsonParseException
				| com.fasterxml.jackson.databind.JsonMappingException ex) {
		}

		TemplateResponseListDTO templateResponseListDTO = new TemplateResponseListDTO();
		templateResponseListDTO.setTemplates(tepmlateList);
		Mockito.when(NotificationUtil.notify("sms", notificationDTO, file, appEntity.getResponse().getBookingType()))
				.thenReturn(responselist);
		ResponseEntity<TemplateResponseListDTO> res = new ResponseEntity<TemplateResponseListDTO>(
				templateResponseListDTO, HttpStatus.OK);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		ResponseEntity<NotificationResponseDTO> resp = new ResponseEntity<NotificationResponseDTO>(
				notificationResponseDTO, HttpStatus.OK);
		MainResponseDTO<io.mosip.preregistration.application.dto.NotificationResponseDTO> response = notificationService
				.sendNotification(stringjson, langCode, file, false);
	}

	@Test(expected = MandatoryFieldException.class)
	public void sendNotificationException3Test() throws java.io.IOException {
		notificationDTO = new NotificationDTO();
		notificationDTO.setName("sanober Noor");
		notificationDTO.setPreRegistrationId("1234567890");
		notificationDTO.setMobNum(null);
		notificationDTO.setEmailID(null);
		notificationDTO.setAppointmentDate("2019-01-22");
		notificationDTO.setAppointmentTime(null);
		notificationDTO.setAdditionalRecipient(true);
		notificationDTO.setIsBatch(false);
		mainReqDto.setRequest(notificationDTO);
		responseDTO = new MainResponseDTO<>();
		response.setMessage("Email and sms request successfully submitted");
		responseDTO.setResponse(response);
		responseDTO.setResponsetime(validationUtil.getCurrentResponseTime());
		String stringjson = null;
		try {
			stringjson = mapper.writeValueAsString(mainReqDto);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Mockito.when(applicationServiceIntf.getApplicationInfoInternal(Mockito.any())).thenReturn(appEntity);
		Mockito.when(demographicServiceIntf.getDemographicData(Mockito.any())).thenReturn(demographicdto);
		Mockito.when(notificationUtil.getAppointmentDetails(Mockito.anyString())).thenReturn(bookingResultDto);

		Mockito.when(validationUtil.requestValidator(Mockito.any(), Mockito.any())).thenReturn(true);

		try {
			Mockito.when(notificationServiceUtil.createNotificationDetails(null, "fra", false)).thenReturn(mainReqDto);
		} catch (RuntimeException | io.mosip.kernel.core.util.exception.JsonMappingException
				| io.mosip.kernel.core.exception.IOException | JSONException | java.text.ParseException
				| io.mosip.kernel.core.util.exception.JsonParseException ex) {
		} catch (com.fasterxml.jackson.core.JsonParseException
				| com.fasterxml.jackson.databind.JsonMappingException ex) {
		}
		MultipartFile file = new MockMultipartFile("test.txt", "test.txt", null, new byte[1100]);
		notificationService.sendNotification(stringjson, "fra", file, false);

	}

	@Test(expected = MandatoryFieldException.class)
	public void sendNotificationExceptionTest5() throws java.io.IOException, JsonProcessingException {
		notificationDTO = new NotificationDTO();
		notificationDTO.setName("sanober Noor");
		notificationDTO.setPreRegistrationId("1234567890");
		notificationDTO.setMobNum(null);
		notificationDTO.setEmailID(null);
		notificationDTO.setAppointmentDate("2019-01-22");
		notificationDTO.setAppointmentTime("09:00 AM");
		notificationDTO.setAdditionalRecipient(true);
		notificationDTO.setIsBatch(false);
		mainReqDto.setRequest(notificationDTO);
		responseDTO = new MainResponseDTO<>();
		response.setMessage("Email and sms request successfully submitted");
		responseDTO.setResponse(response);
		responseDTO.setResponsetime(validationUtil.getCurrentResponseTime());
//		String stringjson = mapper.writeValueAsString(mainReqDto);
		String stringjson = null;
		try {
			stringjson = mapper.writeValueAsString(mainReqDto);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Mockito.when(applicationServiceIntf.getApplicationInfoInternal(Mockito.any())).thenReturn(appEntity);
		Mockito.when(demographicServiceIntf.getDemographicData(Mockito.any())).thenReturn(demographicdto);
		Mockito.when(notificationUtil.getAppointmentDetails(Mockito.anyString())).thenReturn(bookingResultDto);

		Mockito.when(validationUtil.requestValidator(Mockito.any(), Mockito.any())).thenReturn(true);

		try {
			Mockito.when(notificationServiceUtil.createNotificationDetails(null, "fra", true)).thenReturn(mainReqDto);
		} catch (RuntimeException | io.mosip.kernel.core.util.exception.JsonMappingException
				| io.mosip.kernel.core.exception.IOException | JSONException | java.text.ParseException
				| io.mosip.kernel.core.util.exception.JsonParseException ex) {
		} catch (com.fasterxml.jackson.core.JsonParseException
				| com.fasterxml.jackson.databind.JsonMappingException ex) {
		}
		MultipartFile file = new MockMultipartFile("test.txt", "test.txt", null, new byte[1100]);
		notificationService.sendNotification(stringjson, "fra", file, true);

	}

	@Test(expected = MandatoryFieldException.class)
	public void sendNotificationExceptionTest6() throws java.io.IOException, JsonProcessingException {
		notificationDTO = new NotificationDTO();
		notificationDTO.setName("sanober Noor");
		notificationDTO.setPreRegistrationId("1234567890");
		notificationDTO.setMobNum("23456677");
		notificationDTO.setEmailID("@mindtree.com");
		notificationDTO.setAppointmentDate("2019-01-22");
		notificationDTO.setAppointmentTime("09:00 AM");
		notificationDTO.setAdditionalRecipient(true);
		notificationDTO.setIsBatch(false);
		mainReqDto.setRequest(notificationDTO);
		responseDTO = new MainResponseDTO<>();
		response.setMessage("Email and sms request successfully submitted");
		responseDTO.setResponse(response);
		responseDTO.setResponsetime(validationUtil.getCurrentResponseTime());
//		String stringjson = mapper.writeValueAsString(mainReqDto);
		String stringjson = null;
		try {
			stringjson = mapper.writeValueAsString(mainReqDto);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Mockito.when(applicationServiceIntf.getApplicationInfoInternal(Mockito.any())).thenReturn(appEntity);
		Mockito.when(demographicServiceIntf.getDemographicData(Mockito.any())).thenReturn(demographicdto);
		Mockito.when(notificationUtil.getAppointmentDetails(Mockito.anyString())).thenReturn(bookingResultDto);

		Mockito.when(validationUtil.requestValidator(Mockito.any(), Mockito.any())).thenReturn(true);

		try {
			Mockito.when(notificationServiceUtil.createNotificationDetails(null, "fra", true)).thenReturn(mainReqDto);
		} catch (RuntimeException | io.mosip.kernel.core.util.exception.JsonMappingException
				| io.mosip.kernel.core.exception.IOException | JSONException | java.text.ParseException
				| io.mosip.kernel.core.util.exception.JsonParseException ex) {
		} catch (com.fasterxml.jackson.core.JsonParseException
				| com.fasterxml.jackson.databind.JsonMappingException ex) {
		}
		MultipartFile file = new MockMultipartFile("test.txt", "test.txt", null, new byte[1100]);
		notificationService.sendNotification(stringjson, "fra", file, true);

	}

	@Test(expected = MandatoryFieldException.class)
	public void sendNotificationExceptionTest7() throws java.io.IOException, JsonProcessingException {
		notificationDTO = new NotificationDTO();
		notificationDTO.setName("sanober Noor");
		notificationDTO.setPreRegistrationId("1234567890");
		notificationDTO.setMobNum(null);
		notificationDTO.setEmailID("@mindtree.com");
		notificationDTO.setAppointmentDate("2019-01-22");
		notificationDTO.setAppointmentTime("09:00 AM");
		notificationDTO.setAdditionalRecipient(true);
		notificationDTO.setIsBatch(false);
		mainReqDto.setRequest(notificationDTO);
		responseDTO = new MainResponseDTO<>();
		response.setMessage("Email and sms request successfully submitted");
		responseDTO.setResponse(response);
		responseDTO.setResponsetime(validationUtil.getCurrentResponseTime());
//		String stringjson = mapper.writeValueAsString(mainReqDto);
		String stringjson = null;
		try {
			stringjson = mapper.writeValueAsString(mainReqDto);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Mockito.when(applicationServiceIntf.getApplicationInfoInternal(Mockito.any())).thenReturn(appEntity);
		Mockito.when(demographicServiceIntf.getDemographicData(Mockito.any())).thenReturn(demographicdto);
		Mockito.when(notificationUtil.getAppointmentDetails(Mockito.anyString())).thenReturn(bookingResultDto);

		Mockito.when(validationUtil.requestValidator(Mockito.any(), Mockito.any())).thenReturn(true);

		try {
			Mockito.when(notificationServiceUtil.createNotificationDetails(null, "fra", true)).thenReturn(mainReqDto);
		} catch (RuntimeException | io.mosip.kernel.core.util.exception.JsonMappingException
				| io.mosip.kernel.core.exception.IOException | JSONException | java.text.ParseException
				| io.mosip.kernel.core.util.exception.JsonParseException ex) {
		} catch (com.fasterxml.jackson.core.JsonParseException
				| com.fasterxml.jackson.databind.JsonMappingException ex) {
		}

		MultipartFile file = new MockMultipartFile("test.txt", "test.txt", null, new byte[1100]);
		notificationService.sendNotification(stringjson, "fra", file, true);

	}

	@Test
	public void getAppointmentDetailsRestServiceTest() {
		String preId = "1234";
		Mockito.when(notificationUtil.getAppointmentDetails(preId)).thenReturn(bookingResultDto);
		Assert.assertNotNull(notificationService.getAppointmentDetailsRestService(preId));
	}

	@Test(expected = DemographicDetailsNotFoundException.class)
	public void sendNotificationInvalidPridLegacyTest() throws java.io.IOException {
		String langCode = "fra";
		MultipartFile file = new MockMultipartFile("test.txt", "test.txt", null, new byte[1100]);
		String stringjson = null;
		try {
			stringjson = mapper.writeValueAsString(mainReqDto);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		Mockito.when(validationUtil.requestValidator(Mockito.any(), Mockito.any())).thenReturn(true);
		try {
			Mockito.when(notificationServiceUtil.createNotificationDetails(null, "fra", true)).thenReturn(mainReqDto);
		} catch (RuntimeException | io.mosip.kernel.core.util.exception.JsonMappingException
				| io.mosip.kernel.core.exception.IOException | JSONException | java.text.ParseException
				| io.mosip.kernel.core.util.exception.JsonParseException ex) {
		} catch (com.fasterxml.jackson.core.JsonParseException
				| com.fasterxml.jackson.databind.JsonMappingException ex) {
		}
		MainResponseDTO<DemographicResponseDTO> demoError = new MainResponseDTO<>();
		List<ExceptionJSONInfoDTO> errors = new ArrayList<>();
		ExceptionJSONInfoDTO err =
				new ExceptionJSONInfoDTO(
						"PRG_PAM_APP_005", "No data found for the requested pre-registration id");
		errors.add(err);
		demoError.setErrors(errors);
		Mockito.when(demographicServiceIntf.getDemographicData(Mockito.any())).thenReturn(demoError);
		notificationService.sendNotification(stringjson, langCode, file, true);
	}

	@Test
	public void sendNotificationV2SuccessTest() throws java.io.IOException {
		String langCode = "fra";
		MultipartFile file = new MockMultipartFile("test.txt", "test.txt", null, new byte[1100]);
		String stringjson = null;
		try {
			stringjson = mapper.writeValueAsString(mainReqDto);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		Mockito.when(validationUtil.requestValidator(Mockito.any(), Mockito.any())).thenReturn(true);
		try {
			Mockito.when(notificationServiceUtil.createNotificationDetails(null, "fra", true)).thenReturn(mainReqDto);
		} catch (RuntimeException | io.mosip.kernel.core.util.exception.JsonMappingException
				| io.mosip.kernel.core.exception.IOException | JSONException | java.text.ParseException
				| io.mosip.kernel.core.util.exception.JsonParseException ex) {
		} catch (com.fasterxml.jackson.core.JsonParseException
				| com.fasterxml.jackson.databind.JsonMappingException ex) {
		}
		Mockito.when(applicationServiceIntf.getApplicationInfoInternal(Mockito.any())).thenReturn(appEntity);
		Mockito.when(demographicServiceIntf.getDemographicData(Mockito.any())).thenReturn(demographicdto);

		MainResponseDTO<io.mosip.preregistration.application.dto.NotificationResponseDTO> response = notificationService
				.sendNotificationV2(stringjson, langCode, file, true);
		assertEquals(NotificationRequestCodes.MESSAGE.getCode(), response.getResponse().getMessage());
	}

	/**
	 * Builds the exact condition the contact_info fallback exists for: a non-NEW booking type whose
	 * request carries neither an email nor a mobile number, so the only recipient available is
	 * applications.contact_info.
	 */
	private MainRequestDTO<NotificationDTO> contactInfoFallbackRequest() {
		NotificationDTO fallbackDto = new NotificationDTO();
		fallbackDto.setName("Test Applicant");
		fallbackDto.setPreRegistrationId("24346587843");
		fallbackDto.setAppointmentDate("2026-08-10");
		fallbackDto.setAppointmentTime("09:00 AM");
		fallbackDto.setAdditionalRecipient(false);
		// Batch mode skips the appointment date/time cross-check in notificationDtoValidationV2, which
		// is unrelated to the contact_info path under test and would otherwise need a booking fixture.
		fallbackDto.setIsBatch(true);
		fallbackDto.setLanguageCode("eng");
		// emailID and mobNum deliberately left null — that is what triggers the fallback.
		MainRequestDTO<NotificationDTO> fallbackRequest = new MainRequestDTO<>();
		fallbackRequest.setId("mosip.pre-registration.notification.notify");
		fallbackRequest.setVersion("1.0");
		fallbackRequest.setRequesttime(new Timestamp(System.currentTimeMillis()));
		fallbackRequest.setRequest(fallbackDto);
		return fallbackRequest;
	}

	private MainResponseDTO<ApplicationEntity> lostUinApplicationWithContactInfo(String contactInfo) {
		MainResponseDTO<ApplicationEntity> lostUinEntity = new MainResponseDTO<>();
		ApplicationEntity applicationEntity = new ApplicationEntity();
		applicationEntity.setApplicationId("24346587843");
		applicationEntity.setBookingType(BookingTypeCodes.LOST_FORGOTTEN_UIN.toString());
		applicationEntity.setContactInfo(contactInfo);
		lostUinEntity.setResponse(applicationEntity);
		return lostUinEntity;
	}

	/**
	 * Since the PII migration contact_info holds a canonical id, which passes neither validator. It
	 * must be decrypted back to the real address before the validators run, or the applicant is
	 * silently never notified.
	 */
	@Test
	public void sendNotificationV2RecoversCanonicalContactInfoBeforeNotifying() throws java.io.IOException {
		String canonicalContact = "5d59ed4d-cce9-41c9-8397-030f5a36b25c";
		MultipartFile file = new MockMultipartFile("test.txt", "test.txt", null, new byte[1100]);
		MainRequestDTO<NotificationDTO> fallbackRequest = contactInfoFallbackRequest();

		Mockito.when(validationUtil.requestValidator(Mockito.any(), Mockito.any())).thenReturn(true);
		try {
			Mockito.when(notificationServiceUtil.createNotificationDetails(Mockito.any(), Mockito.anyString(),
					Mockito.anyBoolean())).thenReturn(fallbackRequest);
		} catch (Exception ex) {
			Assert.fail("unexpected stubbing failure");
		}
		Mockito.when(applicationServiceIntf.getApplicationInfoInternal(Mockito.any()))
				.thenReturn(lostUinApplicationWithContactInfo(canonicalContact));
		Mockito.when(demographicServiceIntf.getDemographicData(Mockito.any())).thenReturn(demographicdto);
		Mockito.when(userDetailsService.recoverIdentifier(canonicalContact)).thenReturn("applicant@example.com");
		Mockito.when(validationUtil.emailValidator("applicant@example.com")).thenReturn(true);

		notificationService.sendNotificationV2(null, "eng", file, true);

		// The recovered address, not the UUID, is what reaches the notifier.
		Assert.assertEquals("applicant@example.com", fallbackRequest.getRequest().getEmailID());
		Mockito.verify(notificationUtil).notify(Mockito.eq(NotificationRequestCodes.EMAIL.getCode()),
				Mockito.any(NotificationDTO.class), Mockito.any(), Mockito.anyString());
	}

	/**
	 * If the identifier cannot be recovered the request must still complete — the notification is
	 * dropped and logged rather than failing the caller or falling back to a raw value.
	 */
	@Test
	public void sendNotificationV2DropsNotificationWhenContactInfoCannotBeRecovered() throws java.io.IOException {
		String canonicalContact = "5d59ed4d-cce9-41c9-8397-030f5a36b25c";
		MultipartFile file = new MockMultipartFile("test.txt", "test.txt", null, new byte[1100]);
		MainRequestDTO<NotificationDTO> fallbackRequest = contactInfoFallbackRequest();

		Mockito.when(validationUtil.requestValidator(Mockito.any(), Mockito.any())).thenReturn(true);
		try {
			Mockito.when(notificationServiceUtil.createNotificationDetails(Mockito.any(), Mockito.anyString(),
					Mockito.anyBoolean())).thenReturn(fallbackRequest);
		} catch (Exception ex) {
			Assert.fail("unexpected stubbing failure");
		}
		Mockito.when(applicationServiceIntf.getApplicationInfoInternal(Mockito.any()))
				.thenReturn(lostUinApplicationWithContactInfo(canonicalContact));
		Mockito.when(demographicServiceIntf.getDemographicData(Mockito.any())).thenReturn(demographicdto);
		Mockito.when(userDetailsService.recoverIdentifier(canonicalContact)).thenReturn(null);

		notificationService.sendNotificationV2(null, "eng", file, true);

		Assert.assertNull(fallbackRequest.getRequest().getEmailID());
		Mockito.verify(notificationUtil, Mockito.never()).notify(Mockito.anyString(),
				Mockito.any(NotificationDTO.class), Mockito.any(), Mockito.anyString());
	}

	@Test
	public void sendNotificationV2InvalidPridTest() throws java.io.IOException {
		String langCode = "fra";
		MultipartFile file = new MockMultipartFile("test.txt", "test.txt", null, new byte[1100]);
		String stringjson = null;
		try {
			stringjson = mapper.writeValueAsString(mainReqDto);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		Mockito.when(validationUtil.requestValidator(Mockito.any(), Mockito.any())).thenReturn(true);
		try {
			Mockito.when(notificationServiceUtil.createNotificationDetails(null, "fra", true)).thenReturn(mainReqDto);
		} catch (RuntimeException | io.mosip.kernel.core.util.exception.JsonMappingException
				| io.mosip.kernel.core.exception.IOException | JSONException | java.text.ParseException
				| io.mosip.kernel.core.util.exception.JsonParseException ex) {
		} catch (com.fasterxml.jackson.core.JsonParseException
				| com.fasterxml.jackson.databind.JsonMappingException ex) {
		}
		String expectedCode = ApplicationErrorCodes.PRG_APP_013.getCode();
		String expectedMessage = ApplicationErrorMessages.NO_RECORD_FOUND.getMessage();
		Mockito.when(applicationServiceIntf.getApplicationInfoInternal(Mockito.any()))
				.thenThrow(new RecordNotFoundException(expectedCode, expectedMessage));
		try {
			notificationService.sendNotificationV2(stringjson, langCode, file, true);
			Assert.fail("Expected RecordNotFoundException");
		} catch (RecordNotFoundException ex) {
			assertEquals(expectedCode, ex.getErrorCode());
			assertEquals(expectedMessage, ex.getErrorText());
		}
	}
}
