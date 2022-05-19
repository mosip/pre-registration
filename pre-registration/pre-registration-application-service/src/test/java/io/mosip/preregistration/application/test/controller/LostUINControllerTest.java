package io.mosip.preregistration.application.test.controller;

import java.util.Date;

import org.joda.time.DateTime;
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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.preregistration.application.controller.LostUINController;
import io.mosip.preregistration.application.dto.ApplicationRequestDTO;
import io.mosip.preregistration.application.dto.ApplicationResponseDTO;
import io.mosip.preregistration.application.dto.DeleteApplicationDTO;
import io.mosip.preregistration.application.service.ApplicationServiceIntf;
import io.mosip.preregistration.core.code.BookingTypeCodes;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.util.RequestValidator;

@RunWith(SpringJUnit4ClassRunner.class)
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

	@Autowired
	private MockMvc mockmvc;

	@Value("${mosip.id.preregistration.lostuin.create}")
	private String createId;

	@Value("${mosip.id.preregistration.lostuin.delete}")
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
		lostuinController.initBinder(Mockito.mock(WebDataBinder.class));
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
		mainRequestDto.setRequesttime(new Date());

		applicationDto.setApplicationId("123456789");
		applicationDto.setApplicationStatusCode("SUBMITTED");
		applicationDto.setBookingStatusCode("Pending_Appointment");
		applicationDto.setBookingType("LOST_FORGOTTEN_UIN");
		mainResponseDto.setResponse(applicationDto);
		mainResponseDto.setId(createId);
		mainResponseDto.setResponsetime(DateTime.now().toString());
		Mockito.when(applicationService.addLostOrUpdateOrMiscellaneousApplication(Mockito.any(), Mockito.any()))
				.thenReturn(mainResponseDto);
		String uri = "/applications/lostuin";
		 mockmvc
				.perform(MockMvcRequestBuilders.post(uri).contentType(MediaType.APPLICATION_JSON_VALUE)
						.content(asJsonString(mainRequestDto)).accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	public void deleteLostUinApplicationTest() throws Exception {
		String applicationId = "123456789";
		String bookingType = BookingTypeCodes.LOST_FORGOTTEN_UIN.toString();
		MainResponseDTO<DeleteApplicationDTO> response = new MainResponseDTO<DeleteApplicationDTO>();
		response.setId(deleteId);
		Mockito.when(applicationService.deleteLostOrUpdateOrMiscellaneousApplication(applicationId, bookingType)).thenReturn(response);
		RequestBuilder request = MockMvcRequestBuilders.delete("/applications/lostuin/{applicationId}", applicationId)
				.param("applicationId", applicationId).accept(MediaType.APPLICATION_JSON_VALUE)
				.contentType(MediaType.APPLICATION_JSON_VALUE);
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
