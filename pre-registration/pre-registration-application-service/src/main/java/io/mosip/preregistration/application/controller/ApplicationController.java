package io.mosip.preregistration.application.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.application.service.ApplicationService;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.config.LoggerConfiguration;

@RestController
@CrossOrigin("*")
public class ApplicationController {

	@Autowired
	ApplicationService service;

	private Logger log = LoggerConfiguration.logConfig(ApplicationController.class);

	@PreAuthorize("hasAnyRole('INDIVIDUAL')")
	@GetMapping(path = "/applications/info/{preregistrationId}")
	public ResponseEntity<MainResponseDTO<?>> getPreregistrationofPrid(
			@PathVariable("preregistrationId") String preregistrationId) {
		log.info("In application controller to getpreregistrationInfo {}", preregistrationId);
		return ResponseEntity.status(HttpStatus.OK).body(service.getPregistrationInfo(preregistrationId));
	}

}
