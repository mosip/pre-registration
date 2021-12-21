package io.mosip.preregistration.application.test.controller;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import io.mosip.preregistration.application.controller.LostUINController;
import io.mosip.preregistration.application.dto.ApplicationRequestDTO;
import io.mosip.preregistration.application.dto.ApplicationResponseDTO;
import io.mosip.preregistration.application.dto.DeleteApplicationDTO;
import io.mosip.preregistration.application.service.ApplicationServiceIntf;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.util.RequestValidator;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = LostUINController.class)
@Import(LostUINController.class)
@WithMockUser(username = "individual", authorities = { "INDIVIDUAL", "REGISTRATION_OFFICER" })
public class LostUINControllerTest {

	@MockBean
	ApplicationServiceIntf applicationService;

	@MockBean
	private RequestValidator requestValidator;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Mock
	private LostUINController lostuinController;

	private MockMvc mockmvc;

	private String LOST_UIN_CREATE_ID = "preregistration.lostuin.create";

	MainResponseDTO<ApplicationResponseDTO> responseDTO = new MainResponseDTO<ApplicationResponseDTO>();
	ApplicationResponseDTO response = new ApplicationResponseDTO();
	MainRequestDTO<ApplicationRequestDTO> jsonObject = new MainRequestDTO<ApplicationRequestDTO>();
	ApplicationRequestDTO request = new ApplicationRequestDTO();

	@Before
	public void setup() {
		mockmvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

		request.setLangCode("12345");
		jsonObject.setVersion("123");
		jsonObject.setId("1234");
		jsonObject.setRequest(request);

		response.setApplicationId("123456");
		response.setApplicationStatusCode("44");
		response.setBookingStatusCode("656");
		response.setBookingType("Booked");
		responseDTO.setResponse(response);
		responseDTO.setId("354");
		responseDTO.setResponsetime(DateTime.now().toString());
	}

//	@Test
//	public void addLostUinApplicationTest() throws Exception {
//		Mockito.when(applicationService.addLostOrUpdateApplication(jsonObject, "LOST_FORGOTTEN_UIN"))
//				.thenReturn(responseDTO);
//
//		RequestBuilder request = MockMvcRequestBuilders.post("/applications/lostuin").content()
//				.param("jsonObject", jsonObject.toString()).accept(MediaType.APPLICATION_JSON_UTF8)
//				.contentType(MediaType.APPLICATION_JSON_UTF8);
//		mockmvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
//	}

	@Test
	public void deleteLostUinApplicationTest() throws Exception {
		String applicationId = "123456789";
		String bookingType = "UPDATE_REGISTRATION";
		MainResponseDTO<DeleteApplicationDTO> response = new MainResponseDTO<DeleteApplicationDTO>();

		Mockito.when(applicationService.deleteLostOrUpdateApplication(applicationId, bookingType)).thenReturn(response);
		RequestBuilder request = MockMvcRequestBuilders.delete("/applications/lostuin/{applicationId}", applicationId)
				.param("applicationId", applicationId).accept(MediaType.APPLICATION_JSON_UTF8)
				.contentType(MediaType.APPLICATION_JSON_UTF8);
		mockmvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
	}

}
