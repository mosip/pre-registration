package io.mosip.preregistration.application.controller;

import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_ID;
import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_IDTYPE;
import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_SESSIONID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "UISpecification Controller", description = "UI Specification Controller")
public class UISpecificationController {

	@Autowired
	UISpecService uiSpecService;

	private Logger log = LoggerConfiguration.logConfig(UISpecificationController.class);

	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetuispeclatest())")
	@GetMapping("/uispec/latest")
	@Operation(summary = "Service to fetch latest published ui specification", description = "Service to fetch latest published ui specification", tags = {
			"UISpecification Controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<MainResponseDTO<UISpecMetaDataDTO>> getLatestPublishedSchema(
			@RequestParam(name = "version", defaultValue = "0", required = false) @ApiParam(value = "version", defaultValue = "0") double version,
			@RequestParam(name = "identitySchemaVersion", defaultValue = "0", required = false) @ApiParam(value = "version", defaultValue = "0") double identitySchemaVersion) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,"In UISpecification Controller to getLatestPublishedSchema");
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "version {" + version + "} identitySchemaVersion {" + identitySchemaVersion + "}");
		return ResponseEntity.status(HttpStatus.OK).body(uiSpecService.getLatestUISpec(version, identitySchemaVersion));
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetuispecall())")
	@GetMapping("/uispec/all")
	@Operation(summary = "Service to fetch all published ui specification", description = "Service to fetch all published ui specification", tags = {
			"UISpecification Controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<MainResponseDTO<PageDTO<UISpecMetaDataDTO>>> getAllPublishedSchema(
			@RequestParam(name = "pageNumber", defaultValue = "0") @ApiParam(value = "page number", defaultValue = "0") int pageNumber,
			@RequestParam(name = "pageSize", defaultValue = "10") @ApiParam(value = "page size", defaultValue = "10") int pageSize) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "In UISpecification Controller to getAllPublishedSchema");
		return ResponseEntity.status(HttpStatus.OK).body(uiSpecService.getAllUISpec(pageNumber, pageSize));
	}
}