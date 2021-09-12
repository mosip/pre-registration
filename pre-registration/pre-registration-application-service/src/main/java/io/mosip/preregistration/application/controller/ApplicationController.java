package io.mosip.preregistration.application.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.application.dto.ApplicationDetailResponseDTO;
import io.mosip.preregistration.application.dto.UIAuditRequest;
import io.mosip.preregistration.application.service.ApplicationService;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@CrossOrigin("*")
@Tag(name = "Application Controller")
public class ApplicationController {

	@Autowired
	ApplicationService service;

	private Logger log = LoggerConfiguration.logConfig(ApplicationController.class);

	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetapplicationsinfo())")
	@GetMapping(path = "/applications/info/{preregistrationId}")
	@Operation(summary = "Retrive Application demographic and document info for given prid")
	public ResponseEntity<MainResponseDTO<?>> getPreregistrationofPrid(
			@PathVariable("preregistrationId") String preregistrationId) {
		log.info("In application controller to getpreregistrationInfo {}", preregistrationId);
		return ResponseEntity.status(HttpStatus.OK).body(service.getPregistrationInfo(preregistrationId));
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostlogaudit())")
	@PostMapping(path = "/logAudit")
	@Operation(summary = "log audit events from ui")
	public ResponseEntity<MainResponseDTO<String>> logAugit(
			@Valid @RequestBody(required = true) MainRequestDTO<UIAuditRequest> auditRequest) {
		log.info("In application controller to log UI audit capture {}", auditRequest);
		return ResponseEntity.status(HttpStatus.OK).body(service.saveUIEventAudit(auditRequest.getRequest()));
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getUpdateapplicationstatusappid())")
	@GetMapping(path = "/applications/status/info/{applicationId}")
	@Operation(summary = "update booking status code in applications table")
	public ResponseEntity<MainResponseDTO<String>> getApplicationStatusInfo(
			@PathVariable("applicationId") String applicationId) {
		return ResponseEntity.status(HttpStatus.OK).body(service.getApplicationsStatusForApplicationId(applicationId));
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetapplicationdetailsappid())")
	@GetMapping(path = "/applications/appointment/info/{regCenterId}")
	@Operation(summary = "Fetch application details for applicationId")
	public ResponseEntity<MainResponseDTO<List<ApplicationDetailResponseDTO>>> getApplicationInfo(
			@PathVariable("regCenterId") String regCenterId, @RequestParam("appointmentDate") String appointmentDate) {
		return ResponseEntity.status(HttpStatus.OK)
				.body(service.getApplicationsForApplicationId(regCenterId, appointmentDate));
	}

}
