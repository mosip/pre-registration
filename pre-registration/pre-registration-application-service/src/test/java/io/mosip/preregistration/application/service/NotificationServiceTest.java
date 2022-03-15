package io.mosip.preregistration.application.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

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

import javax.annotation.PostConstruct;

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
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.exception.IOException;
import io.mosip.kernel.core.util.exception.JsonMappingException;
import io.mosip.kernel.core.util.exception.JsonParseException;
import io.mosip.preregistration.application.service.util.NotificationServiceUtil;
import io.mosip.preregistration.core.code.AuditLogVariables;
import io.mosip.preregistration.core.common.dto.AuditRequestDto;
import io.mosip.preregistration.core.common.dto.BookingRegistrationDTO;
import io.mosip.preregistration.core.common.dto.DemographicResponseDTO;
import io.mosip.preregistration.core.common.dto.ExceptionJSONInfoDTO;
import io.mosip.preregistration.core.common.dto.KeyValuePairDto;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.dto.NotificationDTO;
import io.mosip.preregistration.core.common.dto.NotificationResponseDTO;
import io.mosip.preregistration.core.common.dto.TemplateResponseDTO;
import io.mosip.preregistration.core.common.dto.TemplateResponseListDTO;
import io.mosip.preregistration.core.util.AuditLogUtil;
import io.mosip.preregistration.core.util.NotificationUtil;
import io.mosip.preregistration.core.util.ValidationUtil;
import io.mosip.preregistration.application.dto.QRCodeResponseDTO;
import io.mosip.preregistration.application.exception.MandatoryFieldException;
import io.mosip.preregistration.application.errorcodes.NotificationErrorCodes;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Sanober Noor
 * @since 1.0.0
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
	MainResponseDTO<BookingRegistrationDTO> bookingResultDto = new MainResponseDTO<>();
	MainResponseDTO<DemographicResponseDTO> demographicdto = new MainResponseDTO<>();

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
		Mockito.when(NotificationUtil.notify("sms", notificationDTO, file)).thenReturn(responselist);
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
		Mockito.when(demographicServiceIntf.getDemographicData(Mockito.any())).thenReturn(demographicdto);
		Mockito.when(notificationUtil.getAppointmentDetails(preId)).thenReturn(bookingResultDto);
		MainResponseDTO<DemographicResponseDTO> response = notificationService
				.notificationDtoValidation(notificationDTO);
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
		Mockito.when(demographicServiceIntf.getDemographicData(Mockito.any())).thenReturn(demographicdto);
		Mockito.when(notificationUtil.getAppointmentDetails(preId)).thenReturn(bookingResultDto);
		notificationService.notificationDtoValidation(notificationDTO);
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
		Mockito.when(demographicServiceIntf.getDemographicData(Mockito.any())).thenReturn(demographicdto);
		Mockito.when(notificationUtil.getAppointmentDetails(preId)).thenReturn(bookingResultDto);
		notificationService.notificationDtoValidation(notificationDTO);
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
		Mockito.when(demographicServiceIntf.getDemographicData(Mockito.any())).thenReturn(demographicdto);
		Mockito.when(notificationUtil.getAppointmentDetails(preId)).thenReturn(bookingResultDto);
		notificationService.notificationDtoValidation(notificationDTO);
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
		Mockito.when(demographicServiceIntf.getDemographicData(Mockito.any())).thenReturn(demographicdto);
		Mockito.when(notificationUtil.getAppointmentDetails(preId)).thenReturn(bookingResultDto);
		notificationService.notificationDtoValidation(notificationDTO);
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
		Mockito.when(NotificationUtil.notify("sms", notificationDTO, file)).thenReturn(responselist);
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
		Mockito.when(NotificationUtil.notify("sms", notificationDTO, file)).thenReturn(responselist);
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
		Mockito.when(NotificationUtil.notify("sms", notificationDTO, file)).thenReturn(responselist);
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
}