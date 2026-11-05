package io.mosip.preregistration.captcha.controller.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;

import io.mosip.preregistration.captcha.controller.CaptchaController;
import io.mosip.preregistration.captcha.dto.CaptchaRequestDTO;
import io.mosip.preregistration.captcha.dto.CaptchaResposneDTO;
import io.mosip.preregistration.captcha.service.CaptchaService;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.util.RequestValidator;

@RunWith(MockitoJUnitRunner.class)
public class CaptchaControllerTest {

	@InjectMocks
	private CaptchaController captchaController;

	@Mock
	private CaptchaService captchaService;

	@Mock
	private RequestValidator requestValidator;

	@Test
	public void initBinderTest() {
		WebDataBinder binder = Mockito.mock(WebDataBinder.class);
		captchaController.initBinder(binder);
		Mockito.verify(binder).addValidators(requestValidator);
	}

	@Test
	public void validateCaptchaTest() {
		MainRequestDTO<CaptchaRequestDTO> request = new MainRequestDTO<>();
		CaptchaRequestDTO captchaRequest = new CaptchaRequestDTO();
		captchaRequest.setCaptchaToken("token");
		request.setId("validate");
		request.setRequest(captchaRequest);
		Errors errors = new BeanPropertyBindingResult(request, "captchaRequest");

		MainResponseDTO<CaptchaResposneDTO> response = new MainResponseDTO<>();
		CaptchaResposneDTO body = new CaptchaResposneDTO();
		body.setSuccess(true);
		response.setResponse(body);
		Mockito.when(captchaService.validateCaptcha(captchaRequest)).thenReturn(response);

		ResponseEntity<?> result = captchaController.validateCaptcha(request, errors);
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertNotNull(result.getBody());
	}
}
