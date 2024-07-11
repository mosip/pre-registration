package io.mosip.preregistration.application.test.controller;

import java.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.preregistration.application.controller.UpdateRegistrationController;
import io.mosip.preregistration.application.dto.ApplicationRequestDTO;
import io.mosip.preregistration.application.dto.ApplicationResponseDTO;
import io.mosip.preregistration.application.dto.DeleteApplicationDTO;
import io.mosip.preregistration.application.service.ApplicationServiceIntf;
import io.mosip.preregistration.core.code.BookingTypeCodes;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.util.RequestValidator;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = UpdateRegistrationController.class)
@Import(UpdateRegistrationController.class)
@WithMockUser(username = "individual", authorities = { "INDIVIDUAL", "REGISTRATION_OFFICER" })
public class UpdateRegistrationControllerTest {

	@MockBean
	ApplicationServiceIntf applicationService;

	@MockBean
	private RequestValidator requestValidator;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Mock
	private UpdateRegistrationController updateController;

	private MockMvc mockmvc;

	@Value("${mosip.id.preregistration.updateregistration.create}")
	private String createId;

	@Value("${mosip.id.preregistration.updateregistration.delete}")
	private String deleteId;

	@Before
	public void setup() {
		mockmvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}

	/**
	 * Test init binder.
	 */
	@Test
	public void testInitBinder() {
		updateController.initBinder(Mockito.mock(WebDataBinder.class));
	}

	@Test
	public void addLostUinApplicationTest() throws Exception {
		Mockito.when(requestValidator.supports(Mockito.any())).thenReturn(true);

		MainResponseDTO<ApplicationResponseDTO> mainResponseDto = new MainResponseDTO<ApplicationResponseDTO>();
		ApplicationResponseDTO applicationDto = new ApplicationResponseDTO();
		MainRequestDTO<ApplicationRequestDTO> mainRequestDto = new MainRequestDTO<ApplicationRequestDTO>();
		ApplicationRequestDTO applicationRequestDto = new ApplicationRequestDTO();

		applicationRequestDto.setLangCode("eng");
		mainRequestDto.setVersion("1.0");
		mainRequestDto.setId(createId);
		mainRequestDto.setRequest(applicationRequestDto);

		applicationDto.setApplicationId("123456789");
		applicationDto.setApplicationStatusCode("SUBMITTED");
		applicationDto.setBookingStatusCode("Pending_Appointment");
		applicationDto.setBookingType("UPDATE_REGISTRATION");
		mainResponseDto.setResponse(applicationDto);
		mainResponseDto.setId(createId);
		mainResponseDto.setResponsetime(LocalDateTime.now().toString());

		Mockito.when(applicationService.addLostOrUpdateApplication(mainRequestDto,
				BookingTypeCodes.UPDATE_REGISTRATION.toString())).thenReturn(mainResponseDto);
		String uri = "/applications/updateregistration";
		mockmvc.perform(MockMvcRequestBuilders.post(uri).contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(asJsonString(mainRequestDto)).accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	public void deleteLostUinApplicationTest() throws Exception {
		String applicationId = "123456789";
		String bookingType = BookingTypeCodes.UPDATE_REGISTRATION.toString();
		MainResponseDTO<DeleteApplicationDTO> response = new MainResponseDTO<DeleteApplicationDTO>();
		response.setId(deleteId);
		Mockito.when(applicationService.deleteLostOrUpdateApplication(applicationId, bookingType)).thenReturn(response);
		RequestBuilder request = MockMvcRequestBuilders
				.delete("/applications/updateregistration/{applicationId}", applicationId)
				.param("applicationId", applicationId).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON);
		mockmvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
	}

	public static String asJsonString(final Object obj) {
		try {
			return new ObjectMapper().writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
