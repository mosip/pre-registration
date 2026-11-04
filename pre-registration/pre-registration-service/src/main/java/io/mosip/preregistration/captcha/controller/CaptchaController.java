package io.mosip.preregistration.captcha.controller;

import static io.mosip.preregistration.core.constant.PreRegCoreConstant.LOGGER_ID;
import static io.mosip.preregistration.core.constant.PreRegCoreConstant.LOGGER_IDTYPE;
import static io.mosip.preregistration.core.constant.PreRegCoreConstant.LOGGER_SESSIONID;

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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "captcha-controller", description = "Captcha Controller")
/**
 * REST controller for captcha APIs in MOSIP Pre-Registration.
 */
public class CaptchaController {

	/**
	 * validate used by this Pre-Registration type.
	 */
	private static final String VALIDATE = "validate";

	/**
	 * Performs log config for MOSIP Pre-Registration.
	 *
	 * @return log config result
	 */
	private Logger log = LoggerConfiguration.logConfig(CaptchaController.class);

	@Autowired
	/**
	 * request validator used by this Pre-Registration type.
	 */
	private RequestValidator requestValidator;

	@InitBinder
	/**
	 * Performs init binder for MOSIP Pre-Registration.
	 *
	 * @param binder binder used by init binder
	 */
	public void initBinder(WebDataBinder binder) {
		binder.addValidators(requestValidator);
	}

	@Autowired
	/**
	 * captcha service used by this Pre-Registration type.
	 */
	private CaptchaService captchaService;

	@PostMapping(path = "/validatecaptcha", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "validate captcha", description = "validate captcha", tags = "captcha-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	/**
	 * Performs validate captcha for MOSIP Pre-Registration.
	 *
	 * @param captchaRequest captcha request used by validate captcha
	 * @param errors errors used by validate captcha
	 * @return validate captcha result
	 */
	public ResponseEntity<?> validateCaptcha(@Validated @RequestBody MainRequestDTO<CaptchaRequestDTO> captchaRequest,
			@Parameter(hidden = true) Errors errors) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, 
				"In pre-registration captcha controller to validate the recaptcha token" + captchaRequest);
		requestValidator.validateId(VALIDATE, captchaRequest.getId(), errors);
		DataValidationUtil.validate(errors, VALIDATE);
		return new ResponseEntity<>(this.captchaService.validateCaptcha(captchaRequest.getRequest()), HttpStatus.OK);
	}
}
