package io.mosip.preregistration.application.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.application.dto.PageDTO;
import io.mosip.preregistration.application.dto.UISpecDTO;
import io.mosip.preregistration.application.dto.UISpecMetaDataDTO;
import io.mosip.preregistration.application.dto.UISpecResponseDTO;
import io.mosip.preregistration.application.service.UISpecService;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.swagger.annotations.ApiOperation;
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

	@PostMapping(path = "/uispec", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN','ZONAL_ADMIN','PRE_REGISTRATION_ADMIN')")
	@Operation(summary = "Service to define ui specification")
	public ResponseEntity<MainResponseDTO<UISpecResponseDTO>> createUISpec(
			@Valid @RequestBody MainRequestDTO<UISpecDTO> request) {
		log.info("In UISpecification Controller to createUISpec");
		return ResponseEntity.status(HttpStatus.OK).body(uiSpecService.saveUIspec(request.getRequest()));
	}

	@PutMapping(path = "/uispec")
	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN','ZONAL_ADMIN','PRE_REGISTRATION_ADMIN')")
	@Operation(summary = "Service to update draftted ui specification")
	public ResponseEntity<MainResponseDTO<UISpecResponseDTO>> updateUISpec(
			@RequestParam(name = "id", required = true) @ApiParam(value = "uispec id") String id,
			@Valid @RequestBody MainRequestDTO<UISpecDTO> request) {
		log.info("In UISpecification Controller to updateUISpec");
		return ResponseEntity.status(HttpStatus.OK).body(uiSpecService.updateUISpec(request.getRequest(), id));
	}

	@PutMapping(path = "/uispec/publish")
	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN','ZONAL_ADMIN','PRE_REGISTRATION_ADMIN')")
	@Operation(summary = "Service to publish draftted ui specification")
	public ResponseEntity<MainResponseDTO<String>> publishUISpec(
			@RequestParam(name = "id", required = true) @ApiParam(value = "uispec id") String id) {
		log.info("In UISpecification Controllerr to publishUISpec");
		return ResponseEntity.status(HttpStatus.OK).body(uiSpecService.publishUISpec(id));
	}

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

	@DeleteMapping("/uispec/{id}")
	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN','ZONAL_ADMIN','PRE_REGISTRATION_ADMIN')")
	@Operation(summary = "Service to delete drafted ui specification")
	public ResponseEntity<MainResponseDTO<String>> deleteUISpec(@PathVariable(name = "id", required = true) String id) {
		log.info("In UISpecification Controller to deleteUISpec with spec id {}", id);
		return ResponseEntity.status(HttpStatus.OK).body(uiSpecService.deleteUISpec(id));
	}
}
