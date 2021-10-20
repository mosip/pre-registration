package io.mosip.preregistration.application.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.application.dto.ApplicationDetailResponseDTO;
import io.mosip.preregistration.application.dto.ApplicationRequestDTO;
import io.mosip.preregistration.application.dto.ApplicationResponseDTO;
import io.mosip.preregistration.application.dto.DemographicCreateResponseDTO;
import io.mosip.preregistration.application.dto.DemographicRequestDTO;
import io.mosip.preregistration.application.dto.UIAuditRequest;
import io.mosip.preregistration.application.service.ApplicationService;
import io.mosip.preregistration.application.service.ApplicationServiceIntf;
import io.mosip.preregistration.application.service.DemographicServiceIntf;
import io.mosip.preregistration.core.code.BookingTypeCodes;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.util.DataValidationUtil;
import io.mosip.preregistration.core.util.RequestValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@CrossOrigin("*")
@Tag(name = "application-controller", description = "Application Controller")
public class ApplicationController {

	@Autowired
	private RequestValidator requestValidator;

	@Autowired
	ApplicationServiceIntf applicationService;

	/** The Constant CREATE_LOST_FORGOTTEN_UIN application. */
	private static final String CREATE_LOST_FORGOTTEN_UIN = "preregistration.lost.applications.create";

	/** The Constant UPDATE_REGISTRATION_DETAILS application. */
	private static final String CREATE_UPDATE_REGISTRATION_DETAILS = "preregistration.update.applications.create";

	private Logger log = LoggerConfiguration.logConfig(ApplicationController.class);

	/**
	 * Inits the binder.
	 *
	 * @param binder the binder
	 */
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.addValidators(requestValidator);
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetapplicationsinfo())")
	@GetMapping(path = "/applications/info/{preregistrationId}")
	@Operation(summary = "getPreregistrationofPrid", description = "Retrive Application demographic and document info for given prid", tags = "application-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<MainResponseDTO<?>> getPreregistrationofPrid(
			@PathVariable("preregistrationId") String preregistrationId) {
		log.info("In application controller to getpreregistrationInfo {}", preregistrationId);
		return ResponseEntity.status(HttpStatus.OK).body(applicationService.getPregistrationInfo(preregistrationId));
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostlogaudit())")
	@PostMapping(path = "/logAudit")
	@Operation(summary = "logAugit", description = "log audit events from ui", tags = "application-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<MainResponseDTO<String>> logAugit(
			@Valid @RequestBody(required = true) MainRequestDTO<UIAuditRequest> auditRequest) {
		log.info("In application controller to log UI audit capture {}", auditRequest);
		return ResponseEntity.status(HttpStatus.OK)
				.body(applicationService.saveUIEventAudit(auditRequest.getRequest()));
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getUpdateapplicationstatusappid())")
	@GetMapping(path = "/applications/status/info/{applicationId}")
	@Operation(summary = "getApplicationStatusInfo", description = "update booking status code in applications table", tags = "application-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<MainResponseDTO<String>> getApplicationStatusInfo(
			@PathVariable("applicationId") String applicationId) {
		return ResponseEntity.status(HttpStatus.OK)
				.body(applicationService.getApplicationsStatusForApplicationId(applicationId));
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetapplicationdetailsappid())")
	@GetMapping(path = "/applications/appointment/info/{regCenterId}")
	@Operation(summary = "getApplicationInfo", description = "Fetch application details for applicationId", tags = "application-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<MainResponseDTO<List<ApplicationDetailResponseDTO>>> getApplicationInfo(
			@PathVariable("regCenterId") String regCenterId, @RequestParam("appointmentDate") String appointmentDate) {
		return ResponseEntity.status(HttpStatus.OK)
				.body(applicationService.getApplicationsForApplicationId(regCenterId, appointmentDate));
	}

	/**
	 * This Post API is use to create a new application with booking type as
	 * LOST_FORGOTTEN_UIN.
	 *
	 * @param jsonObject the json object
	 * @param errors     Errors
	 * @return List of response dto containing pre-id and group-id
	 */
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostapplications())")
	@PostMapping(path = "/applications/createLostApplication", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "new application", description = "Creates a new application with Booking Type as LOST_FORGOTTEN_UIN", tags = "application-controller")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "New application with booking type as LOST_FORGOTTEN_UIN successfully Created"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<MainResponseDTO<ApplicationResponseDTO>> createLostApplication(
			@Validated @RequestBody(required = true) MainRequestDTO<ApplicationRequestDTO> jsonObject,
			@ApiIgnore Errors errors) {
		log.info("sessionId", "idType", "id",
				"In pre-registration controller for createLostApplication with json object" + jsonObject);
		requestValidator.validateId(CREATE_LOST_FORGOTTEN_UIN, jsonObject.getId(), errors);
		DataValidationUtil.validate(errors, CREATE_LOST_FORGOTTEN_UIN);
		return ResponseEntity.status(HttpStatus.OK).body(applicationService.addLostOrUpdateApplication(jsonObject,
				BookingTypeCodes.LOST_FORGOTTEN_UIN.toString()));
	}

	/**
	 * This Post API is use to create a new application with booking type as
	 * UPDATE_REGISTRATION_DETAILS.
	 * 
	 * @param jsonObject the json object
	 * @param errors     Errors
	 * @return List of response dto containing pre-id and group-id
	 */
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostapplications())")
	@PostMapping(path = "/applications/createUpdateApplication", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "new application", description = "Creates a new application with Booking Type as UPDATE_REGISTRATION_DETAILS", tags = "application-controller")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "New Application with booking type as UPDATE_REGISTRATION_DETAILS successfully Created"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<MainResponseDTO<ApplicationResponseDTO>> createUpdateApplication(
			@Validated @RequestBody(required = true) MainRequestDTO<ApplicationRequestDTO> jsonObject,
			@ApiIgnore Errors errors) {
		log.info("sessionId", "idType", "id",
				"In pre-registration controller for createUpdateApplication with json object" + jsonObject);
		requestValidator.validateId(CREATE_UPDATE_REGISTRATION_DETAILS, jsonObject.getId(), errors);
		DataValidationUtil.validate(errors, CREATE_UPDATE_REGISTRATION_DETAILS);
		return ResponseEntity.status(HttpStatus.OK).body(applicationService.addLostOrUpdateApplication(jsonObject,
				BookingTypeCodes.UPDATE_REGISTRATION_DETAILS.toString()));
	}
}
