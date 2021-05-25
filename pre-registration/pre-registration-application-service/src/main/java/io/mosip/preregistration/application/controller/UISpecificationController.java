package io.mosip.preregistration.application.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.application.dto.PageDTO;
import io.mosip.preregistration.application.dto.UISpecMetaDataDTO;
import io.mosip.preregistration.application.service.UISpecService;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@CrossOrigin("*")
@Tag(name = "UISpecification Controller")
public class UISpecificationController {

	@Autowired
	UISpecService uiSpecService;

	private Logger log = LoggerConfiguration.logConfig(UISpecificationController.class);

	@GetMapping("/uispec/latest")
	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN','ZONAL_ADMIN','REGISTRATION_CLIENT','REGISTRATION_SUPERVISOR','REGISTRATION_OFFICER','REGISTRATION_PROCESSOR','RESIDENT','INDIVIDUAL')")
	@Operation(summary = "Service to fetch latest published ui specification")
	public ResponseEntity<MainResponseDTO<UISpecMetaDataDTO>> getLatestPublishedSchema(
			@RequestParam(name = "version", defaultValue = "0", required = false) @ApiParam(value = "version", defaultValue = "0") double version,
			@RequestParam(name = "identitySchemaVersion", defaultValue = "0", required = false) @ApiParam(value = "version", defaultValue = "0") double identitySchemaVersion) {
		log.info("In UISpecification Controller to getLatestPublishedSchema");
		log.info("version {} identitySchemaVersion {}", version, identitySchemaVersion);
		return ResponseEntity.status(HttpStatus.OK).body(uiSpecService.getLatestUISpec(version, identitySchemaVersion));
	}

	@GetMapping("/uispec/all")
	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN','ZONAL_ADMIN','REGISTRATION_CLIENT','REGISTRATION_SUPERVISOR','REGISTRATION_OFFICER','REGISTRATION_PROCESSOR','RESIDENT','INDIVIDUAL')")
	@Operation(summary = "Service to fetch all published ui specification")
	public ResponseEntity<MainResponseDTO<PageDTO<UISpecMetaDataDTO>>> getAllPublishedSchema(
			@RequestParam(name = "pageNumber", defaultValue = "0") @ApiParam(value = "page number", defaultValue = "0") int pageNumber,
			@RequestParam(name = "pageSize", defaultValue = "10") @ApiParam(value = "page size", defaultValue = "10") int pageSize) {
		log.info("In UISpecification Controller to getAllPublishedSchema");
		return ResponseEntity.status(HttpStatus.OK).body(uiSpecService.getAllUISpec(pageNumber, pageSize));
	}

}
