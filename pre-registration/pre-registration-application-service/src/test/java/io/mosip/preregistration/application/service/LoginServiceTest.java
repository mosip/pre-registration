package io.mosip.preregistration.application.service;

import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_ID;
import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_IDTYPE;
import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_SESSIONID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.preregistration.application.exception.InvalidOtpOrUseridException;
import io.mosip.preregistration.application.exception.InvalidateTokenException;
import io.mosip.preregistration.application.exception.SendOtpFailedException;
import io.mosip.preregistration.application.exception.LoginServiceException;
import io.mosip.preregistration.core.common.dto.AuthNResponse;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.dto.ResponseWrapper;
import io.mosip.preregistration.core.common.dto.AuditRequestDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.preregistration.application.dto.CaptchaResposneDTO;
import io.mosip.preregistration.application.dto.OTPRequestWithLangCodeAndCaptchaToken;
import io.mosip.preregistration.application.dto.OtpRequestDTO;
import io.mosip.preregistration.application.dto.OtpUser;
import io.mosip.preregistration.application.dto.User;
import io.mosip.preregistration.application.util.LoginCommonUtil;
import io.mosip.preregistration.core.util.AuditLogUtil;

@RunWith(JUnit4.class)
@SpringBootTest
@ContextConfiguration(classes = { LoginService.class })
public class LoginServiceTest {

	@Mock
	private MainResponseDTO<AuthNResponse> mainResponseDTO;

	@Mock
	private MainRequestDTO<OtpRequestDTO> otpRequest;

	@Mock
	MainRequestDTO<User> userRequest;

	@Mock
	private OtpRequestDTO otp;

	@Mock
	private User user;

	@Mock
	private LoginCommonUtil authCommonUtil;

	@Mock
	private OtpUser otpUser;

	@Mock
	OTPManager otpmanager;

	@Mock
	private ResponseEntity<String> responseEntity;

	@Mock
	private AuditLogUtil auditLogUtil;

	@Mock
	ResponseEntity<ResponseWrapper<AuthNResponse>> responseEntityAudit;

	@Mock
	private AuthNResponse authNResposne;

	@Mock
	private ResponseWrapper responseWrapped;

	@Mock
	private HttpHeaders headers;

	@Mock
	MainResponseDTO<AuthNResponse> sendOtpResponse;

	@Mock
	OTPRequestWithLangCodeAndCaptchaToken otpRequestlang;

	@Value("${prereg.auth.jwt.token.expiration}")
	private String jwtTokenExpiryTime;

	@InjectMocks
	private LoginService authService;

	private LoginService spyAuthService;

	private List<String> list;

	private Map<String, String> requestMap;

	@Value("${mosip.preregistration.login.id.sendotp}")
	private String sendOtpId;

	@Value("${mosip.preregistration.login.id.validateotp}")
	private String userIdOtpId;

	@Value("${mosip.preregistration.login.id.invalidatetoken}")
	private String invalidateTokenId;

	@Value("${mosip.preregistration.login.id.config}")
	private String configId;

	@Value("${mosip.preregistration.login.service.version}")
	private String version;

	@Value("${ui.config.params}")
	private String uiConfigParams;

	@Value("${prereg.auth.jwt.audience}")
	private String jwtAudience;

	@Value("${prereg.auth.jwt.secret}")
	private String jwtSecret;

	@Mock
	private Environment env;

	MainRequestDTO<OTPRequestWithLangCodeAndCaptchaToken> request = new MainRequestDTO<OTPRequestWithLangCodeAndCaptchaToken>();

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		list = new ArrayList<>();
		spyAuthService = Mockito.spy(authService);
		requestMap = new HashMap<>();
		requestMap.put("version", version);
		// ReflectionTestUtils.setField(spyAuthService, "uiConfigParams", "abcd");
		// ReflectionTestUtils.setField(this, "uiConfigParams", "abcd");
		ReflectionTestUtils.setField(authService, "uiConfigParams", uiConfigParams);
		// ReflectionTestUtils.setField(authService, "globalFileName", "abcd");
		// ReflectionTestUtils.setField(authService, "preRegFileName", "abcd");
		ReflectionTestUtils.setField(authService, "configId", "mosip.preregistration.login.id.config");
		ReflectionTestUtils.setField(authService, "jwtTokenExpiryTime", "1800");
		ReflectionTestUtils.setField(authService, "jwtAudience", "adad");
		ReflectionTestUtils.setField(authService, "jwtSecret", "Azcds");
		ReflectionTestUtils.setField(authService, "uiConfigParams", "test1,test2");

	}

	@Test
	public void sendOtpTest() throws Exception {
		boolean otpSent = true;
		list.add("mobile");
		requestMap.put("id", sendOtpId);
		when(authCommonUtil.createRequestMap(otpRequest)).thenReturn(requestMap);
		when(otpRequest.getRequest()).thenReturn(otp);
		when(otp.getUserId()).thenReturn("a@a.com");
		when(authCommonUtil.validateUserId(Mockito.any())).thenReturn(list);
		when(otpmanager.sendOtp(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(otpSent);
		doReturn(responseEntity).when(authCommonUtil).callAuthService(Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		doReturn(mainResponseDTO).when(authCommonUtil).getMainResponseDto(Mockito.any());
		when(responseEntity.getBody()).thenReturn("authNResposne");
		when(authCommonUtil.requestBodyExchange(Mockito.any())).thenReturn(responseWrapped);
		when(responseWrapped.getResponse()).thenReturn("MOSIP");
		Mockito.doNothing().when(spyAuthService).setAuditValues(Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		when(authCommonUtil.requestBodyExchangeObject(Mockito.any(), Mockito.any())).thenReturn(authNResposne);
		when(authCommonUtil.responseToString(Mockito.any())).thenReturn("MOSIP");
		authNResposne.setMessage("success");
		Mockito.doNothing().when(mainResponseDTO).setResponse(Mockito.any());
		assertNotNull(spyAuthService.sendOTP(otpRequest, "eng"));
	}

	@Test
	public void invalidateToken() {
		String authHeader = "Authorization=Mosip-TokeneyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI5NzQ4MTA3Mzg2IiwibW9iaWxlIjoiOTc0ODEwNzM4NiIsIm1haWwiOiIiLCJuYW1lIjoiOTc0ODEwNzM4NiIsImlzT3RwUmVxdWlyZWQiOnRydWUsImlzT3RwVmVyaWZpZWQiOnRydWUsImlhdCI6MTU1MjM4NDk1NCwiZXhwIjoxNTUyMzkwOTU0fQ.burEVnDRF4YVyRGMdx0vYP2DkZbiCKnUdl-7YDlBgcy3u40W5iE9_P8q9kdrlt2xjk4NuXnjPkb7uaFbzYcHog; Max-Age=6000000; Expires=Mon, 20-May-2019 20:42:34 GMT; Path=/; Secure; HttpOnly";
		doReturn(responseEntity).when(authCommonUtil).callAuthService(Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		authNResposne.setMessage("Success");
		when(responseEntity.getBody()).thenReturn("authNResposne");
		when(authCommonUtil.requestBodyExchange(Mockito.any())).thenReturn(responseWrapped);
		when(authCommonUtil.getUserDetailsFromToken(Mockito.any())).thenReturn("userid");
		when(responseWrapped.getResponse()).thenReturn(authNResposne);
		// Mockito.when(authCommonUtil.responseToString(Mockito.any())).thenReturn(authNResposne.toString());
		when(authCommonUtil.requestBodyExchangeObject(Mockito.any(), Mockito.any())).thenReturn(authNResposne);
		Mockito.doNothing().when(spyAuthService).setAuditValues(Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		assertNotNull(spyAuthService.invalidateToken(authHeader));
	}

	@Test
	public void getConfigSuccessTest() throws Exception {
		Map<String, String> configParams = new HashMap<>();
		configParams.put("mosip.mandatory-languages", "eng");
		MainResponseDTO<Map<String, String>> response = new MainResponseDTO<>();
		response = authService.getConfig();
		assertNotNull(response.getResponse());
	}

	@Test
	public void setAuditValuesTest() {
		list.add("Mosip");
		Mockito.doNothing().when(auditLogUtil).saveAuditDetails(Mockito.any());
		doReturn(responseEntityAudit).when(authCommonUtil).callAuthService(Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		when(responseEntityAudit.getHeaders()).thenReturn(headers);
		when(responseEntityAudit.getBody()).thenReturn(responseWrapped);
		when(headers.get(Mockito.any())).thenReturn(list);

		LoginService spyAuthService = mock(LoginService.class);
		Mockito.doNothing().when(spyAuthService).

				setAuditValues(Mockito.isA(String.class), Mockito.isA(String.class), Mockito.isA(String.class),
						Mockito.isA(String.class), Mockito.isA(String.class), Mockito.isA(String.class),
						Mockito.isA(String.class));

		spyAuthService.setAuditValues("eventId", "eventName", "eventType", "description", "idType", "userId",
				"userName");
		Mockito.verify(spyAuthService, Mockito.times(1)).setAuditValues("eventId", "eventName", "eventType",
				"description", "idType", "userId", "userName");
	}

	@Test
	public void setAuditValuesTestLoginException() {
		list.add("Mosip");
		Mockito.doNothing().when(auditLogUtil).saveAuditDetails(Mockito.any());
		doReturn(responseEntityAudit).when(authCommonUtil).callAuthService(Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		when(responseEntityAudit.getHeaders()).thenReturn(headers);
		when(responseEntityAudit.getBody()).thenReturn(responseWrapped);
		when(responseWrapped.getErrors()).thenReturn(list);
		when(headers.get(Mockito.any())).thenReturn(list);
		LoginService spyAuthService = mock(LoginService.class);
		Mockito.doNothing().when(spyAuthService).

				setAuditValues(Mockito.isA(String.class), Mockito.isA(String.class), Mockito.isA(String.class),
						Mockito.isA(String.class), Mockito.isA(String.class), Mockito.isA(String.class),
						Mockito.isA(String.class));

		spyAuthService.setAuditValues("eventId", "eventName", "eventType", "description", "idType", "userId",
				"userName");
		Mockito.verify(spyAuthService, Mockito.times(1)).setAuditValues("eventId", "eventName", "eventType",
				"description", "idType", "userId", "userName");
	}

	@Test
	public void validateWithUserIdOtp() {
		list.add("Token");
		requestMap.put("id", userIdOtpId);
		when(authCommonUtil.createRequestMap(otpRequest)).thenReturn(requestMap);
		when(userRequest.getRequest()).thenReturn(user);
		when(user.getUserId()).thenReturn("a@a.com");
		doReturn(mainResponseDTO).when(authCommonUtil).getMainResponseDto(userRequest);
		doReturn(responseEntity).when(authCommonUtil).callAuthService(Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		when(authCommonUtil.requestBodyExchange(Mockito.any())).thenReturn(responseWrapped);
		when(authCommonUtil.requestBodyExchangeObject(Mockito.any(), Mockito.any())).thenReturn(authNResposne);
		when(authNResposne.getStatus()).thenReturn("success");
		when(responseEntity.getBody()).thenReturn("authNResposne");
		when(responseEntity.getHeaders()).thenReturn(headers);
		when(headers.get(Mockito.any())).thenReturn(list);
		when(otpmanager.validateOtp(user.getOtp(), user.getUserId())).thenReturn(true);
		Mockito.doNothing().when(spyAuthService).setAuditValues(Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		assertNotNull(spyAuthService.validateWithUserIdOtp(userRequest));
	}

	@Test(expected = InvalidOtpOrUseridException.class)
	public void validateWithUserIdOtpException() {
		requestMap.put("id", userIdOtpId);
		when(authCommonUtil.createRequestMap(otpRequest)).thenReturn(requestMap);
		when(userRequest.getRequest()).thenReturn(user);
		when(user.getUserId()).thenReturn("a@a.com");
		doReturn(mainResponseDTO).when(authCommonUtil).getMainResponseDto(userRequest);
		doReturn(responseEntity).when(authCommonUtil).callAuthService(Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		when(authCommonUtil.requestBodyExchange(Mockito.any())).thenReturn(responseWrapped);
		when(responseWrapped.getResponse()).thenReturn("MOSIP");
		when(authCommonUtil.requestBodyExchangeObject(Mockito.any(), Mockito.any())).thenReturn(authNResposne);
		when(authNResposne.getStatus()).thenReturn("failure");
		when(responseEntity.getBody()).thenReturn("authNResposne");
		Mockito.doNothing().when(spyAuthService).setAuditValues(Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		spyAuthService.validateWithUserIdOtp(userRequest);
	}

	@Test
	public void testValidateCaptchaAndSendOtp() {
		LoginService spyAuthService1 = Mockito.spy(authService);
		otpRequestlang.setUserId("123");
		otpRequestlang.setCaptchaToken("temp");
		otpRequestlang.setLangCode("1234");
		request.setRequest(otpRequestlang);
		String captchaToken = request.getRequest().getCaptchaToken();
		when((MainResponseDTO<AuthNResponse>) authCommonUtil.getMainResponseDto(request))
				.thenReturn(mainResponseDTO);
		CaptchaResposneDTO captchaResponse = new CaptchaResposneDTO();
		mainResponseDTO.setResponsetime(LocalDateTime.now().toString());
		when(authCommonUtil.validateCaptchaToken(captchaToken)).thenReturn(captchaResponse);
		when(authCommonUtil.createRequestMap(otpRequest)).thenReturn(requestMap);
		doReturn(sendOtpResponse).when(spyAuthService1).sendOTP(otpRequest, "eng");
		when(user.getUserId()).thenReturn("a@a.com");
		doReturn(mainResponseDTO).when(authCommonUtil).getMainResponseDto(userRequest);
		assertNotNull(spyAuthService.validateCaptchaAndSendOtp(request));
		spyAuthService.validateCaptchaAndSendOtp(request);
	}

	@Test(expected = SendOtpFailedException.class)
	public void testValidateCaptchaAndSendOtpException() {
		LoginService spyAuthService1 = Mockito.spy(authService);
		MainResponseDTO<AuthNResponse> response = new MainResponseDTO<AuthNResponse>();
		otpRequestlang.setUserId("123");
		otpRequestlang.setCaptchaToken("temp");
		otpRequestlang.setLangCode("1234");
		request.setRequest(otpRequestlang);
		otp.setUserId(userIdOtpId);
		otpRequest.setRequest(otp);
		when((MainResponseDTO<AuthNResponse>) authCommonUtil.getMainResponseDto(Mockito.any()))
				.thenReturn(response);
		doReturn(sendOtpResponse).when(spyAuthService1).sendOTP(otpRequest, "eng");
		when(authCommonUtil.createRequestMap(otpRequest)).thenReturn(requestMap);
		when(user.getUserId()).thenReturn("a@a.com");
		doReturn(mainResponseDTO).when(authCommonUtil).getMainResponseDto(userRequest);
		spyAuthService.validateCaptchaAndSendOtp(request);
	}

	@Test
	public void testSendOTPSuccessJwtToken() {
		String userId = "123";
		when(authCommonUtil.sendOtpJwtToken(userId)).thenReturn(userId);
		assertEquals(spyAuthService.sendOTPSuccessJwtToken(userId), userId);
		spyAuthService.sendOTPSuccessJwtToken(userId);
	}

	@Test
	public void getLoginTokenTest() {
		String userIdOtpId = "123";
		String configId = "1";
		assertNotNull(authService.getLoginToken(userIdOtpId, configId));
	}

	@Test(expected = InvalidateTokenException.class)
	public void getLogoutTokenExceptionTest() {
		String token = "Authorization=test";
		authService.getLogoutToken(token);
	}

	@Test(expected = InvalidateTokenException.class)
	public void invalidateTokenTest2() {
		String token = "Demo";
		authService.invalidateToken(token);
	}

	@Test
	public void test_setAuditValues_handles_loginServiceException() {
		LoginService loginService = new LoginService();
		AuditLogUtil mockAuditLogUtil = mock(AuditLogUtil.class);

		io.mosip.kernel.core.logger.spi.Logger mockLogger = mock(io.mosip.kernel.core.logger.spi.Logger.class);

		ReflectionTestUtils.setField(loginService, "auditLogUtil", mockAuditLogUtil);
		ReflectionTestUtils.setField(loginService, "log", mockLogger);

		String eventId = "TEST_EVENT_ID";
		String eventName = "TEST_EVENT_NAME";
		String eventType = "TEST_EVENT_TYPE";
		String description = "Test description";
		String idType = "TEST_ID_TYPE";
		String userId = "testuser";
		String userName = "Test User";

		List<ServiceError> validationErrorList = new ArrayList<>();
		ServiceError error = new ServiceError("ERR_001", "Test error");
		validationErrorList.add(error);

		MainResponseDTO<Object> response = new MainResponseDTO<>();
		LoginServiceException exception = new LoginServiceException(validationErrorList, response);

		Mockito.doThrow(exception).when(mockAuditLogUtil).saveAuditDetails(Mockito.any(AuditRequestDto.class));

		loginService.setAuditValues(eventId, eventName, eventType, description, idType, userId, userName);

		Mockito.verify(mockLogger).error(
				eq(LOGGER_SESSIONID),
				eq(LOGGER_IDTYPE),
				eq(LOGGER_ID),
				contains("In setAuditvalue of login service:")
		);
	}

	@Test
	public void test_getConfig_returns_ui_configuration_parameters() {
		LoginService loginService = new LoginService();
		LoginCommonUtil mockLoginCommonUtil = mock(LoginCommonUtil.class);
		Environment mockEnv = mock(Environment.class);
		ReflectionTestUtils.setField(loginService, "loginCommonUtil", mockLoginCommonUtil);
		ReflectionTestUtils.setField(loginService, "env", mockEnv);
		ReflectionTestUtils.setField(loginService, "uiConfigParams", "param1,param2");
		ReflectionTestUtils.setField(loginService, "configId", "mosip.pre-registration.config");
		ReflectionTestUtils.setField(loginService, "version", "1.0");

		Mockito.doAnswer(invocation -> {
			Map<String, String> map = invocation.getArgument(0);
			map.put("mosip.mandatory.languages", "eng");
			return null;
		}).when(mockLoginCommonUtil).validateLanguageProperties(Mockito.anyMap());

		when(mockEnv.getProperty("param1")).thenReturn("value1");
		when(mockEnv.getProperty("param2")).thenReturn("value2");

		MainResponseDTO<Map<String, String>> response = loginService.getConfig();

		assertNotNull(response);
		assertNotNull(response.getResponse());
		assertEquals("mosip.pre-registration.config", response.getId());
		assertEquals("1.0", response.getVersion());
		assertNotNull(response.getResponsetime());
		assertEquals("value1", response.getResponse().get("param1"));
		assertEquals("value2", response.getResponse().get("param2"));
		assertEquals("eng", response.getResponse().get("mosip.mandatory.languages"));
	}
}
