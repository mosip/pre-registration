package io.mosip.preregistration.core.util.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.preregistration.core.code.BookingTypeCodes;
import io.mosip.preregistration.core.common.dto.KeyValuePairDto;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.dto.NotificationDTO;
import io.mosip.preregistration.core.common.dto.NotificationResponseDTO;
import io.mosip.preregistration.core.common.dto.ResponseWrapper;
import io.mosip.preregistration.core.common.dto.TemplateResponseDTO;
import io.mosip.preregistration.core.common.dto.TemplateResponseListDTO;
import io.mosip.preregistration.core.common.entity.ApplicationEntity;
import io.mosip.preregistration.core.util.NotificationUtil;
import io.mosip.preregistration.core.util.RequestValidator;
import io.mosip.preregistration.core.util.TemplateUtil;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NotificationUtilTest {

	@Value("${emailResourse.url}")
	private String emailResourseUrl;

	@Value("${smsResourse.url}")
	private String smsResourseUrl;

	@MockBean
	private RequestValidator validator;

	@Autowired
	private TemplateUtil templateUtil;

	@Autowired
	private ObjectMapper mapper;

	@MockBean(name = "selfTokenRestTemplate")
	RestTemplate restTemplate;

	@Autowired
	NotificationUtil notificationUtil;

	private NotificationDTO notificationDTO;
	boolean requestValidatorFlag = false;
	TemplateResponseDTO templateResponseDTO = new TemplateResponseDTO();
	MainResponseDTO<NotificationDTO> responseDTO = new MainResponseDTO<>();
	NotificationResponseDTO notificationResponseDTO = new NotificationResponseDTO();
	List<TemplateResponseDTO> tepmlateList = new ArrayList<>();
	TemplateResponseListDTO templateResponseListDTO = new TemplateResponseListDTO();
	ResponseEntity<NotificationResponseDTO> resp = null;
	MainResponseDTO<ApplicationEntity> appEntity = new MainResponseDTO<>();

	@Before
	public void setUp() throws Exception {

		List<KeyValuePairDto<String, String>> languageNamePairs = new ArrayList<KeyValuePairDto<String, String>>();
		KeyValuePairDto languageNamePair = new KeyValuePairDto();
		languageNamePair.setKey("eng");
		languageNamePair.setValue("Test01");
		languageNamePairs.add(languageNamePair);

		List<KeyValuePairDto<String, String>> regCenterName = new ArrayList<KeyValuePairDto<String, String>>();
		List<KeyValuePairDto<String, String>> regCenterAddress = new ArrayList<KeyValuePairDto<String, String>>();
		KeyValuePairDto<String, String> registrationCenterName = new KeyValuePairDto();
		KeyValuePairDto<String, String> registrationCenterAdd = new KeyValuePairDto();
		registrationCenterName.setKey("eng");
		registrationCenterName.setValue("abc");
		registrationCenterAdd.setKey("eng");
		registrationCenterAdd.setValue("xyz");
		regCenterAddress.add(registrationCenterAdd);
		regCenterName.add(registrationCenterName);
		KeyValuePairDto<String, String> address = new KeyValuePairDto();
		notificationDTO = new NotificationDTO();
		notificationDTO.setName("sanober Noor");
		notificationDTO.setPreRegistrationId("1234567890");
		notificationDTO.setMobNum("1234567890");
		notificationDTO.setEmailID("sanober,noor2@mindtree.com");
		notificationDTO.setAppointmentDate("2019-01-22");
		notificationDTO.setAppointmentTime("22:57");
		notificationDTO.setIsBatch(false);
		notificationDTO.setRegistrationCenterName(regCenterName);
		notificationDTO.setAddress(regCenterAddress);
		notificationDTO.setFullName(languageNamePairs);
		notificationDTO.setRegistrationCenterName(languageNamePairs);
		notificationDTO.setAddress(languageNamePairs);
		responseDTO = new MainResponseDTO<>();
		responseDTO.setResponse(notificationDTO);
		// responseDTO.setStatus(Boolean.TRUE);
		templateResponseDTO.setFileText("Email message");
		tepmlateList.add(templateResponseDTO);

		notificationResponseDTO.setMessage("Notification send successfully");
		notificationResponseDTO.setStatus("True");

		templateResponseListDTO.setTemplates(tepmlateList);

		ApplicationEntity appEntityResp = new ApplicationEntity();
		appEntityResp.setApplicationId("423288662552");
		appEntityResp.setBookingType(BookingTypeCodes.LOST_FORGOTTEN_UIN.toString());
		appEntity.setResponse(appEntityResp);
	}

	@Test
	public void notifyEmailsuccessTest() throws IOException {

		String langCode = "eng";
		MultipartFile file = new MockMultipartFile("test.txt", "test.txt", null, new byte[1100]);
		ResponseWrapper<TemplateResponseListDTO> templateResponseListDTO = new ResponseWrapper<>();
		TemplateResponseListDTO templates = new TemplateResponseListDTO();
		templates.setTemplates(tepmlateList);
		templateResponseListDTO.setResponse(templates);
		ResponseEntity<ResponseWrapper<TemplateResponseListDTO>> res = new ResponseEntity<>(templateResponseListDTO,
				HttpStatus.OK);
		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.eq(HttpMethod.GET), Mockito.any(),
				Mockito.eq(new ParameterizedTypeReference<ResponseWrapper<TemplateResponseListDTO>>() {
				}))).thenReturn(res);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		ResponseWrapper<NotificationResponseDTO> notificationres = new ResponseWrapper<>();
		notificationres.setResponse(notificationResponseDTO);
		ResponseEntity<ResponseWrapper<NotificationResponseDTO>> resp = new ResponseEntity<>(notificationres,
				HttpStatus.OK);
		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.eq(HttpMethod.POST), Mockito.any(),
				Mockito.eq(new ParameterizedTypeReference<ResponseWrapper<NotificationResponseDTO>>() {
				}))).thenReturn(resp);

		MainResponseDTO<NotificationResponseDTO> response = notificationUtil.notify("email", notificationDTO, file,
				appEntity.getResponse().getBookingType());
		assertEquals(notificationResponseDTO.getMessage(), response.getResponse().getMessage());
	}

	@Test
	public void notifySMSsuccessTest() throws IOException {

		String langCode = "eng";
		MultipartFile file = new MockMultipartFile("test.txt", "test.txt", null, new byte[1100]);
		ResponseWrapper<TemplateResponseListDTO> templateResponseListDTO = new ResponseWrapper<>();
		TemplateResponseListDTO templates = new TemplateResponseListDTO();
		templates.setTemplates(tepmlateList);
		templateResponseListDTO.setResponse(templates);
		ResponseEntity<ResponseWrapper<TemplateResponseListDTO>> res = new ResponseEntity<>(templateResponseListDTO,
				HttpStatus.OK);
		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.eq(HttpMethod.GET), Mockito.any(),
				Mockito.eq(new ParameterizedTypeReference<ResponseWrapper<TemplateResponseListDTO>>() {
				}))).thenReturn(res);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		ResponseWrapper<NotificationResponseDTO> notificationres = new ResponseWrapper<>();
		notificationres.setResponse(notificationResponseDTO);
		ResponseEntity<ResponseWrapper<NotificationResponseDTO>> resp = new ResponseEntity<>(notificationres,
				HttpStatus.OK);
		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.eq(HttpMethod.POST), Mockito.any(),
				Mockito.eq(new ParameterizedTypeReference<ResponseWrapper<NotificationResponseDTO>>() {
				}))).thenReturn(resp);

		MainResponseDTO<NotificationResponseDTO> response = notificationUtil.notify("sms", notificationDTO, file,
				appEntity.getResponse().getBookingType());
		assertEquals(notificationResponseDTO.getMessage(), response.getResponse().getMessage());
	}

}