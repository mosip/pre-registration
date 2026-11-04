package io.mosip.preregistration.application.test.controller;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

import io.mosip.preregistration.application.controller.ApplicationController;
import io.mosip.preregistration.application.dto.ApplicationDetailResponseDTO;
import io.mosip.preregistration.application.dto.ApplicationsListDTO;
import io.mosip.preregistration.application.dto.UIAuditRequest;
import io.mosip.preregistration.application.service.ApplicationServiceIntf;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.entity.ApplicationEntity;
import io.mosip.preregistration.core.util.RequestValidator;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ApplicationController.class)
@Import(ApplicationController.class)
@WithMockUser(username = "individual", authorities = { "INDIVIDUAL", "REGISTRATION_OFFICER" })
public class ApplicationControllerTest {

	@MockitoBean
	private RequestValidator requestValidator;

	@MockitoBean
	ApplicationServiceIntf applicationService;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	private MockMvc mockmvc;

	UIAuditRequest auditRequest = new UIAuditRequest();

	@Mock
	private ApplicationController controller;

	@Before
	public void setup() {
		MockitoAnnotations.openMocks(this);
		mockmvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}

	/**
	 * Test init binder.
	 */
	@Test
	public void testInitBinder() {
		controller.initBinder(Mockito.mock(WebDataBinder.class));
	}

	@Test
	public void logAugitTest() throws Exception {
		Mockito.when(requestValidator.supports(Mockito.any())).thenReturn(true);

		UIAuditRequest uiAuditRequest = new UIAuditRequest();
		uiAuditRequest.setEventName("test");
		uiAuditRequest.setDescription("desc");
		uiAuditRequest.setActionTimeStamp("2026-01-01T00:00:00.000Z");
		uiAuditRequest.setActionUserId("user");
		uiAuditRequest.setModuleName("pre-reg");
		uiAuditRequest.setModuleId("PREREG");
		MainRequestDTO<UIAuditRequest> auditMainRequest = new MainRequestDTO<>();
		auditMainRequest.setId("mosip.pre-registration.audit");
		auditMainRequest.setVersion("1.0");
		auditMainRequest.setRequesttime(new Date());
		auditMainRequest.setRequest(uiAuditRequest);
		MainResponseDTO<String> response = new MainResponseDTO<>();
		response.setResponse("success");
		Mockito.when(applicationService.saveUIEventAudit(Mockito.any())).thenReturn(response);
		RequestBuilder request = MockMvcRequestBuilders.post("/logAudit")
				.content(new ObjectMapper().writeValueAsString(auditMainRequest))
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON);
		mockmvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	@WithMockUser(roles = "USER")
	public void getApplicationTest() throws Exception {
		MainResponseDTO<ApplicationEntity> response = new MainResponseDTO<ApplicationEntity>();
		String applicationId = "123456";
		Mockito.when(applicationService.getApplicationInfo(applicationId)).thenReturn(response);

		RequestBuilder request = MockMvcRequestBuilders.get("/applications/{applicationId}", applicationId)
				.param("applicationId", applicationId).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON);
		mockmvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	public void getApplicationStatusTest() throws Exception {
		MainResponseDTO<String> response = new MainResponseDTO<String>();
		String applicationId = "123456";
		Mockito.when(applicationService.getApplicationStatus(applicationId)).thenReturn(response);

		RequestBuilder request = MockMvcRequestBuilders.get("/applications/status/{applicationId}", applicationId)
				.param("applicationId", applicationId).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON);
		mockmvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	public void getBookingsForRegCenterTest() throws Exception {
		MainResponseDTO<List<ApplicationDetailResponseDTO>> response = new MainResponseDTO<>();
		String regCenterId = "123456";
		String appointmentDate = LocalDateTime.now().toString();
		Mockito.when(applicationService.getBookingsForRegCenter(regCenterId, appointmentDate, null))
				.thenReturn(response);

		RequestBuilder request = MockMvcRequestBuilders.get("/applications/bookings/{regCenterId}", regCenterId)
				.param("regCenterId", regCenterId).param("appointmentDate", appointmentDate)
				.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON);
		mockmvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	public void getAllApplicationsTest() throws Exception {
		MainResponseDTO<ApplicationsListDTO> response = new MainResponseDTO<ApplicationsListDTO>();
		Mockito.when(applicationService.getAllApplicationsForUser()).thenReturn(response);

		RequestBuilder request = MockMvcRequestBuilders.get("/applications").accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON);
		mockmvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	public void getAllApplicationsTypeTest() throws Exception {
		MainResponseDTO<ApplicationsListDTO> response = new MainResponseDTO<>();
		String type = "Booked";
		Mockito.when(applicationService.getAllApplicationsForUserForBookingType(type)).thenReturn(response);

		RequestBuilder request = MockMvcRequestBuilders.get("/applications").param("type", type)
				.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON);
		mockmvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
	}

}
