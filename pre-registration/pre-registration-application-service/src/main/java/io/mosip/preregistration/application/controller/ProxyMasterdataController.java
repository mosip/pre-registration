package io.mosip.preregistration.application.controller;

import javax.servlet.http.HttpServletRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/proxy")
@Tag(name = "proxy-masterdata-controller", description = "Proxy Masterdata Controller")
public class ProxyMasterdataController {

	private Logger log = LoggerConfiguration.logConfig(ProxyMasterdataController.class);

	@Autowired
	private io.mosip.preregistration.application.service.ProxyMasterDataService service;

	@RequestMapping(path = "/**", produces = MediaType.APPLICATION_JSON_VALUE, method = { RequestMethod.GET,
			RequestMethod.POST })
	@Operation(summary  = "Master data proxy", description = "Master data proxy", tags = "proxy-masterdata-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseEntity<?> masterDataProxyController(@RequestBody(required = false) String body,
			HttpServletRequest request) {
		log.info("sessionId", "idType", "id",
				"In masterDataProxyController method with request url" + request.getRequestURI() + body);
		return ResponseEntity.status(HttpStatus.OK).body(service.getMasterDataResponse(body, request));
	}

}
