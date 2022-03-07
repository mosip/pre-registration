package io.mosip.preregistration.application.test.controller;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.NestedServletException;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.preregistration.application.controller.LoginController;
import io.mosip.preregistration.application.dto.OTPRequestWithLangCodeAndCaptchaToken;
import io.mosip.preregistration.application.dto.OTPWithLangCodeDTO;
import io.mosip.preregistration.application.dto.User;
import io.mosip.preregistration.application.service.LoginService;
import io.mosip.preregistration.application.util.LoginCommonUtil;
import io.mosip.preregistration.core.common.dto.AuthNResponse;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.util.RequestValidator;
import net.minidev.json.parser.ParseException;

@RunWith(SpringRunner.class)
@WebMvcTest(LoginController.class)
@Import(LoginController.class)
@WithMockUser(username = "individual", authorities = { "INDIVIDUAL", "REGISTRATION_OFFICER" })
public class LoginControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@MockBean
	private LoginService loginService;

	@MockBean
	private LoginCommonUtil logincommonUtil;

	@MockBean
	private RequestValidator loginValidator;

	@Mock
	private LoginController controller;

	@Mock
	private HttpServletResponse res;

	@Mock
	private HttpServletRequest req;

	private MainRequestDTO<Object> loginRequest = new MainRequestDTO<>();

	@Value("${mosip.id.preregistration.login.sendotp}")
	private String sendOtpId;

	@Value("${mosip.id.preregistration.login.validateotp}")
	private String validateOtpId;

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

	@Test(expected = NestedServletException.class)
	public void sendOtpTest() throws Exception {
		Mockito.when(loginValidator.supports(Mockito.any())).thenReturn(true);

		MainResponseDTO<AuthNResponse> mainResponseDTO = new MainResponseDTO<AuthNResponse>();
		Mockito.when(loginService.sendOTP(Mockito.any(), Mockito.anyString())).thenReturn(mainResponseDTO);
		String uri = "/login/sendOtp";
		MainRequestDTO<OTPWithLangCodeDTO> mainRequestDto = new MainRequestDTO<OTPWithLangCodeDTO>();
		OTPWithLangCodeDTO optRequestDto = new OTPWithLangCodeDTO();
		optRequestDto.setUserId("test@test.com");
		optRequestDto.setLangCode("eng");
		mainRequestDto.setId(sendOtpId);
		mainRequestDto.setRequest(optRequestDto);
		mainRequestDto.setVersion("1.0");
		mainRequestDto.setRequesttime(new Date());
		//try {
			mockMvc.perform(MockMvcRequestBuilders.post(uri).contentType(MediaType.APPLICATION_JSON_VALUE)
					.content(asJsonString(mainRequestDto)).accept(MediaType.APPLICATION_JSON_VALUE));
//		} catch (Exception e) {
//			assertEquals(HttpStatus.OK, HttpStatus.OK);
//		}

	}

	@Test
	public void sendOTPWithLangCodeTest() throws Exception {
		Mockito.when(loginValidator.supports(Mockito.any())).thenReturn(true);

		MainResponseDTO<AuthNResponse> mainResponseDto = new MainResponseDTO<AuthNResponse>();
		Mockito.when(loginService.sendOTP(Mockito.any(), Mockito.anyString())).thenReturn(mainResponseDto);
		String uri = "/login/sendOtp/langcode";
		MainRequestDTO<OTPWithLangCodeDTO> mainRequestDto = new MainRequestDTO<OTPWithLangCodeDTO>();
		OTPWithLangCodeDTO optRequestDto = new OTPWithLangCodeDTO();
		optRequestDto.setUserId("test@test.com");
		optRequestDto.setLangCode("eng");
		mainRequestDto.setId(sendOtpId);
		mainRequestDto.setRequest(optRequestDto);
		mainRequestDto.setVersion("1.0");
		mainRequestDto.setRequesttime(new Date());
		mockMvc.perform(MockMvcRequestBuilders.post(uri).contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(asJsonString(mainRequestDto)).accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}
	
	@Test
	public void sendOtpWithCaptchaTest() throws Exception {
		Mockito.when(loginValidator.supports(Mockito.any())).thenReturn(true);

		MainResponseDTO<AuthNResponse> mainResponseDto = new MainResponseDTO<AuthNResponse>();
		Mockito.when(loginService.validateCaptchaAndSendOtp(Mockito.any())).thenReturn(mainResponseDto);
		String uri = "/login/sendOtpWithCaptcha";
		MainRequestDTO<OTPRequestWithLangCodeAndCaptchaToken> mainRequestDto = new MainRequestDTO<OTPRequestWithLangCodeAndCaptchaToken>();
		OTPRequestWithLangCodeAndCaptchaToken optRequestDto = new OTPRequestWithLangCodeAndCaptchaToken();
		optRequestDto.setUserId("test@test.com");
		optRequestDto.setLangCode("eng");
		optRequestDto.setCaptchaToken("Test");
		
		mainRequestDto.setId(sendOtpId);
		mainRequestDto.setRequest(optRequestDto);
		mainRequestDto.setVersion("1.0");
		mainRequestDto.setRequesttime(new Date());
		
		mockMvc.perform(MockMvcRequestBuilders.post(uri).contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(asJsonString(mainRequestDto)).accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	public void validateWithUseridOtpTest() throws Exception {
		Mockito.when(loginValidator.supports(Mockito.any())).thenReturn(true);

		MainRequestDTO<User> mainRequestDto = new MainRequestDTO<User>();
		User user = new User();
		user.setUserId("test@test.com");
		user.setOtp("123456");
		mainRequestDto.setId(validateOtpId);
		mainRequestDto.setRequest(user);
		mainRequestDto.setVersion("1.0");
		mainRequestDto.setRequesttime(new Date());

		HttpHeaders headers = new HttpHeaders();
		headers.add("Set-Cookie", "AuthToken=MOSIP");
		MainResponseDTO<AuthNResponse> mainResponseDto = new MainResponseDTO<AuthNResponse>();
		AuthNResponse authNResposne = new AuthNResponse();
		authNResposne.setStatus("success");
		authNResposne.setMessage("Successfully validated");
		mainResponseDto.setResponse(authNResposne);

		Mockito.when(loginService.validateWithUserIdOtp(Mockito.any())).thenReturn(mainResponseDto);

		String uri = "/login/validateOtp";

		mockMvc.perform(MockMvcRequestBuilders.post(uri).contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(asJsonString(mainRequestDto)).accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(MockMvcResultMatchers.status().isOk());
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

	public static String asJsonString(final Object obj) {
		try {
			return new ObjectMapper().writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
