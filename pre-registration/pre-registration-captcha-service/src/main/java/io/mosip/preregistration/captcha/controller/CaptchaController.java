package io.mosip.preregistration.captcha.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.captcha.dto.CaptchaRequestDTO;
import io.mosip.preregistration.captcha.service.CaptchaService;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.util.DataValidationUtil;
import io.mosip.preregistration.core.util.RequestValidator;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@CrossOrigin("*")
public class CaptchaController {

	private static final String VALIDATE = "validate";

	private Logger log = LoggerConfiguration.logConfig(CaptchaController.class);
	
	@Autowired
	private RequestValidator requestValidator;
	
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.addValidators(requestValidator);
	}
	
	@Autowired
	private CaptchaService captchaService;

	@PostMapping(path = "/validatecaptcha" , consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> validateCaptcha(
		@Validated	@RequestBody MainRequestDTO<CaptchaRequestDTO> captchaRequest, @ApiIgnore Errors errors) {
		log.info("sessionId", "idType", "id",
				"In pre-registration captcha controller to validate the recaptcha token" + captchaRequest);
		System.out.println("In pre-registration captcha controller to validate the recaptcha token" + "  "+captchaRequest);
		requestValidator.validateId(VALIDATE, captchaRequest.getId(), errors);
		DataValidationUtil.validate(errors, VALIDATE);
		return new ResponseEntity<>(this.captchaService.validateCaptcha(captchaRequest.getRequest()), HttpStatus.OK);
	}

}
