
package io.mosip.preregistration.notification.service;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.auth.adapter.model.AuthUserDetails;
import io.mosip.kernel.core.exception.IOException;
import io.mosip.kernel.core.util.exception.JsonMappingException;
import io.mosip.kernel.core.util.exception.JsonParseException;
import io.mosip.preregistration.booking.service.BookingServiceIntf;
import io.mosip.preregistration.core.code.AuditLogVariables;
import io.mosip.preregistration.core.common.dto.AuditRequestDto;
import io.mosip.preregistration.core.common.dto.BookingRegistrationDTO;
import io.mosip.preregistration.core.common.dto.DemographicResponseDTO;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.dto.NotificationDTO;
import io.mosip.preregistration.core.common.dto.NotificationResponseDTO;
import io.mosip.preregistration.core.common.dto.TemplateResponseDTO;
import io.mosip.preregistration.core.common.dto.TemplateResponseListDTO;
import io.mosip.preregistration.core.exception.InvalidRequestException;
import io.mosip.preregistration.core.util.AuditLogUtil;
import io.mosip.preregistration.core.util.NotificationUtil;
import io.mosip.preregistration.core.util.ValidationUtil;
import io.mosip.preregistration.demographic.service.DemographicServiceIntf;
import io.mosip.preregistration.notification.NotificationApplicationTest;
import io.mosip.preregistration.notification.dto.QRCodeResponseDTO;
import io.mosip.preregistration.notification.dto.ResponseDTO;
import io.mosip.preregistration.notification.exception.DemographicDetailsNotFoundException;
import io.mosip.preregistration.notification.exception.MandatoryFieldException;
import io.mosip.preregistration.notification.exception.RestCallException;

/**
 * @author Sanober Noor
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { NotificationApplicationTest.class })
public class NotificationServiceTest {

	@Autowired
	private NotificationService service;

	@Autowired
	private ValidationUtil serviceUtil;
	
	@MockBean
	private DemographicServiceIntf demographicServiceIntf;
	
	@MockBean
	private BookingServiceIntf bookingServiceIntf;

	@Autowired
	private ObjectMapper mapper;

	@MockBean(name = "restTemplate")
	private RestTemplate restTemplate;

	@Value("${mosip.utc-datetime-pattern}")
	private String utcDateTimePattern;
	
	@Value("#{'${mosip.notificationtype}'.split('\\|')}")
	private List<String> notificationTypeList;

	@MockBean
	private NotificationUtil NotificationUtil;
	private NotificationDTO notificationDTO;
	boolean requestValidatorFlag = false;
	TemplateResponseDTO templateResponseDTO = new TemplateResponseDTO();
	MainResponseDTO<ResponseDTO> responseDTO = new MainResponseDTO<>();
	MainResponseDTO<NotificationResponseDTO> responselist = new MainResponseDTO<>();
	MainResponseDTO<QRCodeResponseDTO> qrCodeResponseDTO = new MainResponseDTO<>();
	NotificationResponseDTO notificationResponseDTO = new NotificationResponseDTO();
	MainRequestDTO<NotificationDTO> mainReqDto = new MainRequestDTO<>();
	List<TemplateResponseDTO> tepmlateList = new ArrayList<>();
	ResponseDTO response = new ResponseDTO();

	JSONParser parser = new JSONParser();
	private JSONObject jsonTestObject;
	private JSONObject jsonObject;
	AuditRequestDto auditRequestDto = new AuditRequestDto();
	@MockBean
	private AuditLogUtil auditLogUtil;
	MainResponseDTO<BookingRegistrationDTO> bookingResultDto = new MainResponseDTO<>();
	MainResponseDTO<DemographicResponseDTO> demographicdto = new MainResponseDTO<>();

	@Before
	public void beforeSet()
			throws ParseException, FileNotFoundException, java.io.IOException, org.json.simple.parser.ParseException {

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
		notificationDTO.setAdditionalRecipient(true);
		notificationDTO.setIsBatch(false);
		mainReqDto.setId("mosip.pre-registration.notification.notify");
		mainReqDto.setVersion("1.0");
		mapper.setTimeZone(TimeZone.getDefault());
		mainReqDto.setRequesttime(new Timestamp(System.currentTimeMillis()));
		mainReqDto.setRequest(notificationDTO);
		responseDTO = new MainResponseDTO<>();
		response.setMessage("Email and sms request successfully submitted");
		responseDTO.setResponse(response);
		responseDTO.setResponsetime(serviceUtil.getCurrentResponseTime());
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
		
		DemographicResponseDTO demo= new DemographicResponseDTO();
		demo.setPreRegistrationId("20180396713560");
		JSONParser jsonParser = new JSONParser();
		JSONObject demoString = (JSONObject) jsonParser.parse(jsonTestObject.toJSONString());
		JSONObject demoResponseData = (JSONObject) demoString.get("response");
		org.json.simple.JSONObject demoDetailsData= (JSONObject) demoResponseData.get("demographicDetails");
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

		String stringjson = mapper.writeValueAsString(mainReqDto);
		String langCode = "fra";
		MultipartFile file = new MockMultipartFile("test.txt", "test.txt", null, new byte[1100]);
		Mockito.when(demographicServiceIntf.getDemographicData(Mockito.anyString())).thenReturn(demographicdto);
		Mockito.when(bookingServiceIntf.getAppointmentDetails(Mockito.anyString())).thenReturn(bookingResultDto);

		TemplateResponseListDTO templateResponseListDTO = new TemplateResponseListDTO();
		templateResponseListDTO.setTemplates(tepmlateList);
		Mockito.when(NotificationUtil.notify("sms", notificationDTO, langCode, file)).thenReturn(responselist);
		ResponseEntity<TemplateResponseListDTO> res = new ResponseEntity<TemplateResponseListDTO>(
				templateResponseListDTO, HttpStatus.OK);
		Mockito.when(restTemplate.getForEntity(Mockito.anyString(), Mockito.eq(TemplateResponseListDTO.class)))
				.thenReturn(res);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		ResponseEntity<NotificationResponseDTO> resp = new ResponseEntity<NotificationResponseDTO>(
				notificationResponseDTO, HttpStatus.OK);
		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.eq(HttpMethod.POST), Mockito.any(),
				Mockito.eq(NotificationResponseDTO.class))).thenReturn(resp);
		MainResponseDTO<ResponseDTO> response = service.sendNotification(stringjson, langCode, file);
		assertEquals(responseDTO.getResponse(), response.getResponse());
	}

	@Test
	public void callGetDemographicDetailsWithPreIdTest()
			throws JsonParseException, JsonMappingException, IOException, java.io.IOException {
		ResponseEntity<String> respEntity = new ResponseEntity<String>(jsonTestObject.toString(), HttpStatus.OK);
		notificationDTO.setAdditionalRecipient(false);

		mainReqDto.setRequest(notificationDTO);
		String stringjson = mapper.writeValueAsString(mainReqDto);
		String langCode = "fra";
		MultipartFile file = new MockMultipartFile("test.txt", "test.txt", null, new byte[1100]);
		Mockito.when(demographicServiceIntf.getDemographicData(Mockito.anyString())).thenReturn(demographicdto);
		Mockito.when(bookingServiceIntf.getAppointmentDetails(Mockito.anyString())).thenReturn(bookingResultDto);

		TemplateResponseListDTO templateResponseListDTO = new TemplateResponseListDTO();
		templateResponseListDTO.setTemplates(tepmlateList);
		Mockito.when(NotificationUtil.notify("sms", notificationDTO, langCode, file)).thenReturn(responselist);
		ResponseEntity<TemplateResponseListDTO> res = new ResponseEntity<TemplateResponseListDTO>(
				templateResponseListDTO, HttpStatus.OK);
		Mockito.when(restTemplate.getForEntity(Mockito.anyString(), Mockito.eq(TemplateResponseListDTO.class)))
				.thenReturn(res);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		ResponseEntity<NotificationResponseDTO> resp = new ResponseEntity<NotificationResponseDTO>(
				notificationResponseDTO, HttpStatus.OK);
		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.eq(HttpMethod.POST), Mockito.any(),
				Mockito.eq(NotificationResponseDTO.class))).thenReturn(resp);
		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.eq(HttpMethod.GET), Mockito.any(),
				Mockito.eq(String.class))).thenReturn(respEntity);
		MainResponseDTO<ResponseDTO> response = service.sendNotification(stringjson, langCode, file);
		assertEquals(responseDTO.getResponse(), response.getResponse());
	}



	/**
	 * This method is for failure case of sendNotification
	 * 
	 * @throws JsonProcessingException
	 */
	@Test(expected = InvalidRequestException.class)
	public void invalidRequestParameterExceptionTest() throws JsonProcessingException {
		notificationDTO = new NotificationDTO();
		notificationDTO.setName("sanober Noor");
		notificationDTO.setPreRegistrationId("1234567890");
		notificationDTO.setMobNum(null);
		notificationDTO.setEmailID(null);
		notificationDTO.setAppointmentDate(null);
		notificationDTO.setAppointmentTime("22:57");
		notificationDTO.setAdditionalRecipient(true);
		notificationDTO.setIsBatch(false);
		mainReqDto.setRequest(notificationDTO);
		responseDTO = new MainResponseDTO<>();
		response.setMessage("Email and sms request successfully submitted");
		responseDTO.setResponse(response);
		responseDTO.setResponsetime(serviceUtil.getCurrentResponseTime());
		String stringjson = mapper.writeValueAsString(mainReqDto);
		Mockito.when(demographicServiceIntf.getDemographicData(Mockito.anyString())).thenReturn(demographicdto);	
		MultipartFile file = new MockMultipartFile("test.txt", "test.txt", null, new byte[1100]);
		service.sendNotification(stringjson, "eng", file);

	}
	
	//@Test(expected = RestCallException.class)
	public void mandatoryFieldExceptionTest() throws JsonProcessingException {
		notificationDTO = new NotificationDTO();
		notificationDTO.setName("");
		notificationDTO.setPreRegistrationId("1234567890");
		notificationDTO.setMobNum(null);
		notificationDTO.setEmailID(null);
		notificationDTO.setAppointmentDate("2019-01-20");
		notificationDTO.setAppointmentTime("22:57");
		notificationDTO.setAdditionalRecipient(true);
		notificationDTO.setIsBatch(false);
		mainReqDto.setRequest(notificationDTO);
		responseDTO = new MainResponseDTO<>();
		response.setMessage("Email and sms request successfully submitted");
		responseDTO.setResponse(response);
		responseDTO.setResponsetime(serviceUtil.getCurrentResponseTime());
		String stringjson = mapper.writeValueAsString(mainReqDto);
		Mockito.when(demographicServiceIntf.getDemographicData(Mockito.anyString())).thenReturn(demographicdto);
	
		MultipartFile file = new MockMultipartFile("test.txt", "test.txt", null, new byte[1100]);
		service.sendNotification(stringjson, "fra", file);

	}
	
	@Test(expected = DemographicDetailsNotFoundException.class)
	public void demographicDetailsNotFoundExceptionTest() throws JsonProcessingException {
		notificationDTO = new NotificationDTO();
		notificationDTO.setName("sanober Noor");
		notificationDTO.setPreRegistrationId("1234567890");
		notificationDTO.setMobNum(null);
		notificationDTO.setEmailID(null);
		notificationDTO.setAppointmentDate("2019-01-22");
		notificationDTO.setAppointmentTime("22:57");
		notificationDTO.setAdditionalRecipient(true);
		notificationDTO.setIsBatch(false);
		mainReqDto.setRequest(notificationDTO);
		responseDTO = new MainResponseDTO<>();
		response.setMessage("Email and sms request successfully submitted");
		responseDTO.setResponse(response);
		responseDTO.setResponsetime(serviceUtil.getCurrentResponseTime());
		String stringjson = mapper.writeValueAsString(mainReqDto);
		Mockito.when(demographicServiceIntf.getDemographicData(Mockito.anyString())).thenThrow(DemographicDetailsNotFoundException.class);
		MultipartFile file = new MockMultipartFile("test.txt", "test.txt", null, new byte[1100]);
		service.sendNotification(stringjson, "fra", file);

	}
	
	@Test(expected = MandatoryFieldException.class)
	public void sendNotificationExceptionTest4() throws JsonProcessingException {
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
		responseDTO.setResponsetime(serviceUtil.getCurrentResponseTime());
		String stringjson = mapper.writeValueAsString(mainReqDto);
		Mockito.when(demographicServiceIntf.getDemographicData(Mockito.anyString())).thenReturn(demographicdto);
		Mockito.when(bookingServiceIntf.getAppointmentDetails(Mockito.anyString())).thenReturn(bookingResultDto);
		MultipartFile file = new MockMultipartFile("test.txt", "test.txt", null, new byte[1100]);
        service.sendNotification(stringjson, "fra", file);

	}

	@Test(expected = MandatoryFieldException.class)
	public void sendNotificationExceptionTest5() throws JsonProcessingException {
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
		responseDTO.setResponsetime(serviceUtil.getCurrentResponseTime());
		String stringjson = mapper.writeValueAsString(mainReqDto);
		Mockito.when(demographicServiceIntf.getDemographicData(Mockito.anyString())).thenReturn(demographicdto);
		Mockito.when(bookingServiceIntf.getAppointmentDetails(Mockito.anyString())).thenReturn(bookingResultDto);

		MultipartFile file = new MockMultipartFile("test.txt", "test.txt", null, new byte[1100]);
		service.sendNotification(stringjson, "fra", file);

	}
	
	@Test(expected = MandatoryFieldException.class)
	public void sendNotificationExceptionTest6() throws JsonProcessingException {
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
		responseDTO.setResponsetime(serviceUtil.getCurrentResponseTime());
		String stringjson = mapper.writeValueAsString(mainReqDto);
		Mockito.when(demographicServiceIntf.getDemographicData(Mockito.anyString())).thenReturn(demographicdto);
		Mockito.when(bookingServiceIntf.getAppointmentDetails(Mockito.anyString())).thenReturn(bookingResultDto);

		MultipartFile file = new MockMultipartFile("test.txt", "test.txt", null, new byte[1100]);
		service.sendNotification(stringjson, "fra", file);

	}
	@Test(expected = MandatoryFieldException.class)
	public void sendNotificationExceptionTest7() throws JsonProcessingException {
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
		responseDTO.setResponsetime(serviceUtil.getCurrentResponseTime());
		String stringjson = mapper.writeValueAsString(mainReqDto);
		
		Mockito.when(demographicServiceIntf.getDemographicData(Mockito.anyString())).thenReturn(demographicdto);
		Mockito.when(bookingServiceIntf.getAppointmentDetails(Mockito.anyString())).thenReturn(bookingResultDto);

		MultipartFile file = new MockMultipartFile("test.txt", "test.txt", null, new byte[1100]);
		service.sendNotification(stringjson, "fra", file);

	}
}

