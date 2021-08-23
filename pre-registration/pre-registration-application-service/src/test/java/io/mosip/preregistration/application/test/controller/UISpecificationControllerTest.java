package io.mosip.preregistration.application.test.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import io.mosip.preregistration.application.controller.UISpecificationController;
import io.mosip.preregistration.application.dto.PageDTO;
import io.mosip.preregistration.application.dto.UISpecMetaDataDTO;
import io.mosip.preregistration.application.service.UISpecService;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = UISpecificationController.class)
@Import(UISpecificationController.class)
@WithMockUser(username = "individual", authorities = { "INDIVIDUAL", "REGISTRATION_OFFICER" })
public class UISpecificationControllerTest {

	private MockMvc mockmvc;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@MockBean
	private UISpecService service;

	@Before
	public void setup() {
		mockmvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}

	@Test
	public void getAllUISpecTest() throws Exception {

		MainResponseDTO<PageDTO<UISpecMetaDataDTO>> response = new MainResponseDTO<PageDTO<UISpecMetaDataDTO>>();

		Mockito.when(service.getAllUISpec(Mockito.anyInt(), Mockito.anyInt())).thenReturn(response);

		RequestBuilder request = MockMvcRequestBuilders.get("/uispec/all");
		mockmvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());

	}

	@Test
	public void getLatestUISpecTest() throws Exception {

		MainResponseDTO<UISpecMetaDataDTO> response = new MainResponseDTO<UISpecMetaDataDTO>();

		Mockito.when(service.getLatestUISpec(Mockito.anyDouble(), Mockito.anyDouble())).thenReturn(response);

		RequestBuilder request = MockMvcRequestBuilders.get("/uispec/latest");
		mockmvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());

	}

}
