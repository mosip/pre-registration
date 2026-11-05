package io.mosip.preregistration.application.controller;

import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_SESSIONID;
import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_IDTYPE;
import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_ID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/proxy")
@Tag(name = "proxy-masterdata-controller", description = "Proxy Masterdata Controller")
/**
 * REST controller for proxy masterdata APIs in MOSIP Pre-Registration.
 */
public class ProxyMasterdataController {

	/**
	 * Performs log config for MOSIP Pre-Registration.
	 *
	 * @return log config result
	 */
	private Logger log = LoggerConfiguration.logConfig(ProxyMasterdataController.class);

	@Autowired
	/**
	 * service used by this Pre-Registration type.
	 */
	private io.mosip.preregistration.application.service.ProxyMasterDataService service;

	@RequestMapping(path = "/**", produces = MediaType.APPLICATION_JSON_VALUE, method = { RequestMethod.GET })
	@Operation(summary = "GET Master data proxy", description = "Master data proxy", tags = "proxy-masterdata-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	/**
	 * Performs master data get proxy controller for MOSIP Pre-Registration.
	 *
	 * @param body body used by master data get proxy controller
	 * @param request request used by master data get proxy controller
	 * @return master data get proxy controller result
	 */
	public ResponseEntity<?> masterDataGetProxyController(@RequestBody(required = false) String body,
			HttpServletRequest request) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"In masterDataGetProxyController method with request url: " + request.getRequestURI());
		return ResponseEntity.status(HttpStatus.OK).body(service.getMasterDataResponse(body, request));
	}

	@RequestMapping(path = "/**", produces = MediaType.APPLICATION_JSON_VALUE, method = { RequestMethod.POST })
	@Operation(summary = "POST Master data proxy", description = "Master data proxy", tags = "proxy-masterdata-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	/**
	 * Performs master data post proxy controller for MOSIP Pre-Registration.
	 *
	 * @param body body used by master data post proxy controller
	 * @param request request used by master data post proxy controller
	 * @return master data post proxy controller result
	 */
	public ResponseEntity<?> masterDataPostProxyController(@RequestBody(required = false) String body,
			HttpServletRequest request) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"In masterDataPostProxyController method with request url" + request.getRequestURI());
		return ResponseEntity.status(HttpStatus.OK).body(service.getMasterDataResponse(body, request));
	}
}
