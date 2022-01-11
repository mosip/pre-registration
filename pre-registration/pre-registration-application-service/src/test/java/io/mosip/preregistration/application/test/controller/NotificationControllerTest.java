
package io.mosip.preregistration.application.test.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.Charset;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.preregistration.application.controller.NotificationController;
import io.mosip.preregistration.application.dto.NotificationResponseDTO;
import io.mosip.preregistration.application.service.NotificationService;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.dto.NotificationDTO;
import io.mosip.preregistration.core.util.RequestValidator;
import io.mosip.preregistration.core.util.ValidationUtil;

/**
 * @author Sanober Noor
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@WebMvcTest(NotificationController.class)
@Import(NotificationController.class)
@WithMockUser(username = "individual", authorities = { "INDIVIDUAL", "REGISTRATION_OFFICER" })
public class NotificationControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext webAppContext;

	@Autowired
	private ObjectMapper mapper;

	@Mock
	private RequestValidator requestValidator;

	@MockBean
	private ValidationUtil validationUtil;

	/**
	 * /** Creating Mock Bean for NotificationService
	 */

	@MockBean
	private NotificationService service;

	private NotificationDTO notificationDTO;

	MainResponseDTO<NotificationResponseDTO> responseDTO = new MainResponseDTO<>();

	MainResponseDTO<Map<String, String>> configRes = new MainResponseDTO<>();
	NotificationResponseDTO respDTO = new NotificationResponseDTO();

	@Before
	public void setUp() {

		mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext).build();

		notificationDTO = new NotificationDTO();
		notificationDTO.setName("sanober Noor");
		notificationDTO.setPreRegistrationId("1234567890");
		notificationDTO.setMobNum("1234567890");
		notificationDTO.setEmailID("sanober,noor2@mindtree.com");
		notificationDTO.setAppointmentDate("2019-01-22");
		notificationDTO.setAppointmentTime("22:57");
		respDTO.setMessage("Email and sms request successfully submitted");
		responseDTO.setResponse(respDTO);
		responseDTO.setResponsetime(validationUtil.getCurrentResponseTime());

	}

	/**
	 * This test method is for success sendNotification method
	 * 
	 * @throws Exception
	 */

	@Test
	public void sendNotificationTest() throws Exception {
		String stringjson = mapper.writeValueAsString(notificationDTO);
		String langCode = "eng";
		Mockito.when(service.sendNotification(stringjson, "eng", null, false)).thenReturn(responseDTO);

		mockMvc.perform(MockMvcRequestBuilders.multipart("/notification/notify")
				.file(new MockMultipartFile("NotificationRequestDTO", stringjson, "application/json",
						stringjson.getBytes(Charset.forName("UTF-8"))))
				.file(new MockMultipartFile("langCode", langCode, "application/json",
						langCode.getBytes(Charset.forName("UTF-8")))))
				.andExpect(status().isOk());

	}

	@Test
	public void sendNotificationsTest() throws Exception {
		String stringjson = mapper.writeValueAsString(notificationDTO);
		String langCode = "eng";
		Mockito.when(service.sendNotification(stringjson, "eng", null, true)).thenReturn(responseDTO);

		mockMvc.perform(MockMvcRequestBuilders.multipart("/notification")
				.file(new MockMultipartFile("NotificationRequestDTO", stringjson, "application/json",
						stringjson.getBytes(Charset.forName("UTF-8"))))
				.file(new MockMultipartFile("langCode", langCode, "application/json",
						langCode.getBytes(Charset.forName("UTF-8")))))
				.andExpect(status().isOk());

	}

	@Test
	public void sendNotificationInternalTest() throws Exception {
		String stringjson = mapper.writeValueAsString(notificationDTO);
		String langCode = "eng";
		Mockito.when(service.sendNotification(stringjson, "eng", null, false)).thenReturn(responseDTO);

		mockMvc.perform(MockMvcRequestBuilders.multipart("/internal/notification")
				.file(new MockMultipartFile("NotificationRequestDTO", stringjson, "application/json",
						stringjson.getBytes(Charset.forName("UTF-8"))))
				.file(new MockMultipartFile("langCode", langCode, "application/json",
						langCode.getBytes(Charset.forName("UTF-8")))))
				.andExpect(status().isOk());

	}

}
