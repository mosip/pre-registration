package io.mosip.preregistration.application.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.application.dto.QRCodeResponseDTO;
import io.mosip.preregistration.application.service.GenerateQRcodeService;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
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

import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_SESSIONID;
import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_IDTYPE;
import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_ID;

/**
 * This class provides API's to generate the QR code operations on
 * pre-registration.
 * 
 * @author Sanober Noor
 * @since 1.0.0
 */
@RestController
@RequestMapping("/qrCode")
@Tag(name = "generate-q-rcode-controller", description = "Generate Q Rcode Controller")
public class GenerateQRcodeController {

	private Logger log = LoggerConfiguration.logConfig(GenerateQRcodeController.class);

	@Autowired
	private RequestValidator requestValidator;

	/** The Constant for GET UPDATED DATE TIME application. */
	private static final String QRCODE = "pre-registration.qrcode.generate";

	/**
	 * Inits the binder.
	 *
	 * @param binder the binder
	 */
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.addValidators(requestValidator);
	}

	@Autowired
	private GenerateQRcodeService service;

	/**
	 * @param data   pass the data for generating qr code
	 * @param errors
	 * @return QRCodeResponseDTO the response entity
	 */
	// @PreAuthorize("hasAnyRole('INDIVIDUAL')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostqrcodegenerate())")
	@PostMapping(path = "/generate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Generate QR Code", description = "Generate QR Code", tags = "generate-q-rcode-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<MainResponseDTO<QRCodeResponseDTO>> generateQRCode(
			@Validated @RequestBody MainRequestDTO<String> data, @ApiParam(hidden = true) Errors errors) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"In generateQRCode controller for generateQRCode generation with request " + data);
		requestValidator.validateId(QRCODE, data.getId(), errors);
		DataValidationUtil.validate(errors, QRCODE);
		return new ResponseEntity<>(service.generateQRCode(data), HttpStatus.OK);
	}
}