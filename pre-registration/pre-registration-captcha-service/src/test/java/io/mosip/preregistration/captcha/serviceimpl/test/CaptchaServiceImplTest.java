package io.mosip.preregistration.captcha.serviceimpl.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.mosip.preregistration.captcha.constants.CaptchaErrorCode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import io.mosip.preregistration.captcha.dto.CaptchaRequestDTO;
import io.mosip.preregistration.captcha.dto.CaptchaResposneDTO;
import io.mosip.preregistration.captcha.dto.GoogleCaptchaDTO;
import io.mosip.preregistration.captcha.exception.InvalidRequestCaptchaException;
import io.mosip.preregistration.captcha.serviceimpl.CaptchaServiceImpl;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;

@RunWith(JUnit4.class)
@SpringBootTest
@ContextConfiguration(classes = { CaptchaServiceImpl.class })
public class CaptchaServiceImplTest {

	@InjectMocks
	private CaptchaServiceImpl captchaServiceImpl;

	@Value("${mosip.preregistration.captcha.secretkey}")
	public String recaptchaSecret;

	@Value("${mosip.preregistration.captcha.recaptcha.verify.url}")
	public String recaptchaVerifyUrl;

	@Value("${mosip.preregistration.captcha.id.validate}")
	public String mosipcaptchaValidateId;

	@Value("${version}")
	private String version;

	@Mock
	private RestTemplate restTemplate;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		ReflectionTestUtils.setField(captchaServiceImpl, "recaptchaSecret", "demo");
		ReflectionTestUtils.setField(captchaServiceImpl, "recaptchaVerifyUrl",
				"https://www.google.com/recaptcha/api/siteverify");
		ReflectionTestUtils.setField(captchaServiceImpl, "mosipcaptchaValidateId", "123");
		ReflectionTestUtils.setField(captchaServiceImpl, "version", "2.0");
	}

	
	@Test
	public void validateCaptchaTest() {
		CaptchaRequestDTO captchaRequest = new CaptchaRequestDTO();
		MainResponseDTO<CaptchaResposneDTO> mainResponse = new MainResponseDTO<>();
		MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
		param.add("secret", recaptchaSecret);
		GoogleCaptchaDTO captchaResponse = new GoogleCaptchaDTO();
		captchaResponse.setHostname(recaptchaVerifyUrl);
		captchaResponse.setSuccess(true);
		captchaResponse.setChallengeTs("Success");

		captchaRequest.setCaptchaToken("temp");
		captchaRequest.getCaptchaToken();

		CaptchaResposneDTO res = new CaptchaResposneDTO();
		res.setMessage("captcha scuccessfully set");
		res.setSuccess(true);
		mainResponse.setResponse(res);
		mainResponse.setId(mosipcaptchaValidateId);
		mainResponse.setVersion(version);

		when(restTemplate.postForObject("https://www.google.com/recaptcha/api/siteverify",
				"{secret=[demo], response=[aRsasahksasa]}", GoogleCaptchaDTO.class)).thenReturn(captchaResponse);
		assertNotNull(captchaServiceImpl.validateCaptcha(captchaRequest));
	}

	@Test(expected = InvalidRequestCaptchaException.class)
	public void validateCaptchaExceptionTest() {
		CaptchaRequestDTO captchaRequest = new CaptchaRequestDTO();
		GoogleCaptchaDTO captchaResponse = new GoogleCaptchaDTO();
		captchaResponse.setHostname(recaptchaVerifyUrl);
		captchaResponse.setSuccess(true);
		captchaResponse.setChallengeTs("Success");
		captchaServiceImpl.validateCaptcha(captchaRequest);
	}

	@Test
	public void test_successful_captcha_validation() {
		CaptchaServiceImpl captchaService = new CaptchaServiceImpl();
		RestTemplate restTemplate = mock(RestTemplate.class);
		ReflectionTestUtils.setField(captchaService, "restTemplate", restTemplate);
		ReflectionTestUtils.setField(captchaService, "recaptchaSecret", "test-secret");
		ReflectionTestUtils.setField(captchaService, "recaptchaVerifyUrl", "https://test-url.com");
		ReflectionTestUtils.setField(captchaService, "mosipcaptchaValidateId", "mosip.pre-registration.captcha.id.validate");
		ReflectionTestUtils.setField(captchaService, "version", "1.0");

		CaptchaRequestDTO captchaRequest = new CaptchaRequestDTO();
		captchaRequest.setCaptchaToken("valid-token");

		GoogleCaptchaDTO googleResponse = new GoogleCaptchaDTO();
		googleResponse.setSuccess(true);
		googleResponse.setChallengeTs("2023-01-01T12:00:00Z");
		googleResponse.setHostname("test-host");

		MultiValueMap<String, String> expectedParams = new LinkedMultiValueMap<>();
		expectedParams.add("secret", "test-secret");
		expectedParams.add("response", "valid-token");

		when(restTemplate.postForObject(
				eq("https://test-url.com"),
				eq(expectedParams),
				eq(GoogleCaptchaDTO.class)
		)).thenReturn(googleResponse);

		MainResponseDTO<CaptchaResposneDTO> response =
				(MainResponseDTO<CaptchaResposneDTO>) captchaService.validateCaptcha(captchaRequest);

		assertNotNull(response);
		assertEquals("mosip.pre-registration.captcha.id.validate", response.getId());
		assertEquals("1.0", response.getVersion());
		assertEquals("2023-01-01T12:00:00Z", response.getResponsetime());
		assertNotNull(response.getResponse());
		assertTrue(response.getResponse().isSuccess());
		assertNull(response.getErrors());
	}

	@Test
	public void test_null_captcha_token_handling() {
		CaptchaServiceImpl captchaService = new CaptchaServiceImpl();
		CaptchaRequestDTO captchaRequest = new CaptchaRequestDTO();
		captchaRequest.setCaptchaToken(null);

		assertThrows(InvalidRequestCaptchaException.class, () -> {
			captchaService.validateCaptcha(captchaRequest);
		});

		try {
			captchaService.validateCaptcha(captchaRequest);
		} catch (InvalidRequestCaptchaException e) {
			assertEquals(CaptchaErrorCode.INVALID_CAPTCHA_REQUEST.getErrorCode(), e.getErrorCode());
			assertEquals(CaptchaErrorCode.INVALID_CAPTCHA_REQUEST.getErrorMessage(), e.getErrorMessage());
		}
	}

}
