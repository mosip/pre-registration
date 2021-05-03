package io.mosip.preregistration.application.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/proxy")
@CrossOrigin("*")
@Tag(name = "Proxy Controller")
public class ProxyMasterdataController {

	private Logger log = LoggerConfiguration.logConfig(ProxyMasterdataController.class);

	@Autowired
	private io.mosip.preregistration.application.service.ProxyMasterDataService service;

	@PreAuthorize("hasAnyRole('INDIVIDUAL')")
	@RequestMapping(path = "/**", produces = MediaType.APPLICATION_JSON_VALUE, method = { RequestMethod.GET,
			RequestMethod.POST })
	public ResponseEntity<?> masterDataProxyController(@RequestBody(required = false) String body,
			HttpServletRequest request) {
		log.info("sessionId", "idType", "id",
				"In masterDataProxyController method with request url" + request.getRequestURI() + body);
		return ResponseEntity.status(HttpStatus.OK).body(service.getMasterDataResponse(body, request));
	}

}
