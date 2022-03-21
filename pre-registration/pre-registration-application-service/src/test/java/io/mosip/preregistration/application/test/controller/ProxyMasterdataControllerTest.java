package io.mosip.preregistration.application.test.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import io.mosip.preregistration.application.controller.ProxyMasterdataController;
import io.mosip.preregistration.application.service.ProxyMasterDataService;
import io.mosip.preregistration.application.util.ProxyMasterdataServiceUtil;
import net.minidev.json.parser.ParseException;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(controllers = ProxyMasterdataController.class)
@Import(ProxyMasterdataController.class)
@WithMockUser(username = "individual", authorities = { "INDIVIDUAL", "REGISTRATION_OFFICER" })
public class ProxyMasterdataControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@MockBean
	private ProxyMasterDataService proxyMasterDataService;

	@MockBean
	private ProxyMasterdataServiceUtil util;

	@Mock
	private ProxyMasterdataController proxyMasterdataController;

	@Before
	public void setup() throws URISyntaxException, FileNotFoundException, ParseException {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}

	@Test
	public void masterDataGetProxyControllerTest() throws Exception {
		ResponseEntity<Object> response = new ResponseEntity<Object>(HttpStatus.OK);
		Mockito.when(proxyMasterDataService.getMasterDataResponse(Mockito.any(), Mockito.any())).thenReturn(response);
		RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/proxy/test")
				.contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON_VALUE);
		mockMvc.perform(requestBuilder).andExpect(status().isOk());
	}

	@Test
	public void masterDataPostProxyControllerTest() throws Exception {
		ResponseEntity<Object> response = new ResponseEntity<Object>(HttpStatus.OK);
		Mockito.when(proxyMasterDataService.getMasterDataResponse(Mockito.any(), Mockito.any())).thenReturn(response);
		RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/proxy/test")
				.contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON_VALUE);
		mockMvc.perform(requestBuilder).andExpect(status().isOk());
	}

}
