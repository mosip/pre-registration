package io.mosip.preregistration.application.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.application.dto.ApplicationDetailResponseDTO;
import io.mosip.preregistration.application.dto.ApplicationsListDTO;
import io.mosip.preregistration.application.dto.UIAuditRequest;
import io.mosip.preregistration.application.service.ApplicationServiceIntf;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.entity.ApplicationEntity;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.util.RequestValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "application-controller", description = "Application Controller")
public class ApplicationController {

	@Autowired
	private RequestValidator requestValidator;

	@Autowired
	ApplicationServiceIntf applicationService;

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

	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetapplicationdetailsappid())")
	@GetMapping(path = "/applications/{applicationId}")
	@Operation(summary = "getApplication", description = "Fetch application details for applicationId", tags = "application-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<MainResponseDTO<ApplicationEntity>> getApplication(
			@PathVariable("applicationId") String applicationId) {
		return ResponseEntity.status(HttpStatus.OK).body(applicationService.getApplicationInfo(applicationId));
	}
	
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetapplicationsstatus())")
	@GetMapping(path = "/applications/status/{applicationId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "getApplicationStatus", description = "Fetch the status of a application", tags = "application-controller")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "All applications status fetched successfully"),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseEntity<MainResponseDTO<String>> getApplicationStatus(
			@PathVariable("applicationId") String applicationId) {
		return ResponseEntity.status(HttpStatus.OK).body(applicationService.getApplicationStatus(applicationId));
	}


	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetappointmentregistrationcenterid())")
	@GetMapping(path = "/applications/bookings/{regCenterId}")
	@Operation(summary = "getBookingsForRegCenter", description = "Fetch all bookings for regCenterId on the given appointmentDate", tags = "application-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<MainResponseDTO<List<ApplicationDetailResponseDTO>>> getBookingsForRegCenter(
			@PathVariable("regCenterId") String regCenterId, @RequestParam("appointmentDate") String appointmentFromDate,
			@RequestParam(required = false) String appointmentToDate) {
		return ResponseEntity.status(HttpStatus.OK)
				.body(applicationService.getBookingsForRegCenter(regCenterId, appointmentFromDate, appointmentToDate));
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetapplicationsall())")
	@GetMapping(path = "/applications")
	@Operation(summary = "getAllApplications", description = "Fetch all applications for current user", tags = "application-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<MainResponseDTO<ApplicationsListDTO>> getAllApplications(@RequestParam(required = false) String type) {
		if (type != null) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(applicationService.getAllApplicationsForUserForBookingType(type));
		}
		return ResponseEntity.status(HttpStatus.OK).body(applicationService.getAllApplicationsForUser());
	}

}
