package io.mosip.preregistration.application.test.controller;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.context.WebApplicationContext;

import io.mosip.preregistration.application.controller.LoginController;
import io.mosip.preregistration.application.dto.OtpRequestDTO;
import io.mosip.preregistration.application.dto.User;
import io.mosip.preregistration.application.exception.DeprecatedException;
import io.mosip.preregistration.application.service.LoginService;
import io.mosip.preregistration.application.util.LoginCommonUtil;
import io.mosip.preregistration.core.common.dto.AuthNResponse;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.dto.ResponseWrapper;
import io.mosip.preregistration.core.util.RequestValidator;
import net.minidev.json.parser.ParseException;

@RunWith(SpringRunner.class)
@WebMvcTest(LoginController.class)
@Import(LoginController.class)
public class LoginControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@MockBean
	private LoginService loginService;

	@MockBean
	private LoginCommonUtil logincommonUtil;

	@MockBean
	private RequestValidator loginValidator;

	@Autowired
	private LoginController controller;

	private AuthNResponse authNResposne;

	@Mock
	private HttpServletResponse res;

	@Mock
	private HttpServletRequest req;

	private MainResponseDTO<AuthNResponse> serviceResponse;

	private MainRequestDTO<Object> loginRequest = new MainRequestDTO<>();

	private ResponseEntity<String> responseEntity;

	@Before
	public void setup() throws URISyntaxException, FileNotFoundException, ParseException {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

		loginRequest.setId("mosip.pre-registration.login.sendotp");
		loginRequest.setVersion("v1");
		loginRequest.setRequesttime(new Date());
		ReflectionTestUtils.setField(controller, "loginValidator", loginValidator);
	}

	/**
	 * Test init binder.
	 */
	@Test
	public void testInitBinder() {
		controller.initBinder(Mockito.mock(WebDataBinder.class));
	}

	@Test(expected = DeprecatedException.class)
	public void sendOtpTest() throws Exception {
		MainResponseDTO<AuthNResponse> mainResponseDTO = new MainResponseDTO<>();
		Mockito.when(loginService.sendOTP(Mockito.any(), Mockito.anyString())).thenReturn(mainResponseDTO);
		MainRequestDTO<OtpRequestDTO> userOtpRequest = new MainRequestDTO<>();
		BeanPropertyBindingResult errors = new BeanPropertyBindingResult(userOtpRequest,
				"MainRequestDTO<OtpRequestDTO>");
		ResponseEntity<MainResponseDTO<AuthNResponse>> responseEntity = controller.sendOTP(userOtpRequest, errors);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
	}

	@Mock
	private ResponseWrapper responseWrapped;

	@Test
	public void validateWithUseridOtpTest() throws Exception {
		MainRequestDTO<User> userIdOtpRequest = new MainRequestDTO<>();
		loginRequest.setId("mosip.pre-registration.login.useridotp");
		HttpHeaders headers = new HttpHeaders();
		headers.add("Set-Cookie", "AuthToken=MOSIP");
		MainResponseDTO<AuthNResponse> serviceResposne = new MainResponseDTO<AuthNResponse>();
		authNResposne = new AuthNResponse("success", "success");
		serviceResposne.setResponse(authNResposne);

		Mockito.when(loginService.validateWithUserIdOtp(Mockito.any())).thenReturn(serviceResponse);

	}

	@Test
	public void invalidateTokenTest() throws Exception {
		MainResponseDTO<String> serviceResponse = new MainResponseDTO<>();
		Mockito.when(loginService.invalidateToken(Mockito.anyString())).thenReturn(serviceResponse);
		RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/login/invalidateToken")
				.contentType(MediaType.APPLICATION_JSON_VALUE).characterEncoding("UTF-8")
				.accept(MediaType.APPLICATION_JSON_VALUE).content(loginRequest.toString());
		mockMvc.perform(requestBuilder).andExpect(status().isOk());
	}

	@Test
	public void getConfigTest() throws Exception {
		MainResponseDTO<Map<String, String>> mainResponseDTO = new MainResponseDTO<>();
		Mockito.when(loginService.getConfig()).thenReturn(mainResponseDTO);
		RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/login/config")
				.contentType(MediaType.APPLICATION_JSON_VALUE).characterEncoding("UTF-8")
				.accept(MediaType.APPLICATION_JSON_VALUE);
		mockMvc.perform(requestBuilder).andExpect(status().isOk());
	}

	@Test
	public void refreshConfigTest() throws Exception {

		MainResponseDTO<String> mainResponseDTO = new MainResponseDTO<>();
		Mockito.when(loginService.refreshConfig()).thenReturn(mainResponseDTO);
		RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/login/refreshconfig")
				.contentType(MediaType.APPLICATION_JSON_VALUE).characterEncoding("UTF-8")
				.accept(MediaType.APPLICATION_JSON_VALUE);
		mockMvc.perform(requestBuilder).andExpect(status().isOk());
	}

}
