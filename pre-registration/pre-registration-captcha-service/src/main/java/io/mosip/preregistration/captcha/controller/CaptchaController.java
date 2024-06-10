package io.mosip.preregistration.captcha.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
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
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "captcha-controller", description = "Captcha Controller")
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

	@PostMapping(path = "/validatecaptcha", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "validate captcha", description = "validate captcha", tags = "captcha-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<?> validateCaptcha(@Validated @RequestBody MainRequestDTO<CaptchaRequestDTO> captchaRequest,
			@ApiParam(hidden = true) Errors errors) {
		log.info("sessionId", "idType", "id",
				"In pre-registration captcha controller to validate the recaptcha token" + captchaRequest);
		requestValidator.validateId(VALIDATE, captchaRequest.getId(), errors);
		DataValidationUtil.validate(errors, VALIDATE);
		return new ResponseEntity<>(this.captchaService.validateCaptcha(captchaRequest.getRequest()), HttpStatus.OK);
	}
}