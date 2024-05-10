/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.application.controller;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.application.dto.DeletePreRegistartionDTO;
import io.mosip.preregistration.application.dto.DemographicCreateResponseDTO;
import io.mosip.preregistration.application.dto.DemographicMetadataDTO;
import io.mosip.preregistration.application.dto.DemographicRequestDTO;
import io.mosip.preregistration.application.dto.DemographicUpdateResponseDTO;
import io.mosip.preregistration.application.dto.SchemaResponseDto;
import io.mosip.preregistration.application.service.DemographicServiceIntf;
import io.mosip.preregistration.core.common.dto.DemographicResponseDTO;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.dto.PreRegIdsByRegCenterIdDTO;
import io.mosip.preregistration.core.common.dto.PreRegistartionStatusDTO;
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

/**
 * This class provides different API's to perform operations on
 * pre-registration.
 * 
 * @author Rajath KR
 * @author Sanober Noor
 * @author Tapaswini Behera
 * @author Jagadishwari S
 * @author Ravi C Balaji
 * @since 1.0.0
 */

@RestController
@RequestMapping("/")
@Tag(name = "demographic-controller", description = "Demographic Controller")
public class DemographicController {

	/** Autowired reference for {@link #DemographicService}. */
	@Autowired
	private DemographicServiceIntf preRegistrationService;

	@Autowired
	private RequestValidator requestValidator;

	/** The Constant CREATE application. */
	private static final String CREATE = "preregistration.demographic.create";

	/** The Constant UPADTE application. */
	private static final String UPDATE = "preregistration.demographic.update";

	/** The Constant for GET UPDATED DATE TIME application. */
	private static final String UPDATEDTIME = "preregistration.demographic.retrieve.date";

	/**
	 * Inits the binder.
	 *
	 * @param binder
	 *            the binder
	 */
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.addValidators(requestValidator);
	}

	private Logger log = LoggerConfiguration.logConfig(DemographicController.class);

	/**
	 * This Post API is use to create a pre-registation application for
	 * registration.
	 *
	 * @param jsonObject
	 *            the json object
	 * @param errors
	 *            Errors
	 * @return List of response dto containing pre-id and group-id
	 */

	//@PreAuthorize("hasAnyRole('INDIVIDUAL','REGISTRATION_OFFICER','REGISTRATION_SUPERVISOR','REGISTRATION_ADMIN')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostapplications())")
	@PostMapping(path = "/applications/prereg", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "addPreRegDemographicData", description = "Add demographic data for a new preregistration application", tags = "demographic-controller")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Pre-Registration successfully Created"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseEntity<MainResponseDTO<DemographicCreateResponseDTO>> addPreRegDemographicData(
			@Validated @RequestBody(required = true) MainRequestDTO<DemographicRequestDTO> jsonObject,
			@ApiIgnore Errors errors) {
		log.info("sessionId", "idType", "id",
				"In pre-registration controller for add preregistration with json object" + jsonObject);
		requestValidator.validateId(CREATE, jsonObject.getId(), errors);
		DataValidationUtil.validate(errors, CREATE);
		return ResponseEntity.status(HttpStatus.OK).body(preRegistrationService.addPreRegistration(jsonObject));
	}

	/**
	 * This Put API use to update a pre-registation application.
	 * 
	 * @param preRegistrationId
	 *            preRegistrationId
	 * @param jsonObject
	 *            The json object
	 * @param errors
	 *            Errors
	 * @return List of response dto containing pre-id and group-id
	 */

	//@PreAuthorize("hasAnyRole('INDIVIDUAL','REGISTRATION_OFFICER','REGISTRATION_SUPERVISOR','REGISTRATION_ADMIN')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPutapplications())")
	@PutMapping(path = "/applications/prereg/{preRegistrationId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "updatePreRegDemographicData", description = "Update demographic data for a new preregistration application", tags = "demographic-controller")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Demographic data successfully Updated"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseEntity<MainResponseDTO<DemographicUpdateResponseDTO>> updatePreRegDemographicData(
			@PathVariable("preRegistrationId") String preRegistrationId,
			@Validated @RequestBody(required = true) MainRequestDTO<DemographicRequestDTO> jsonObject,
			@ApiIgnore Errors errors) {
		String userId = preRegistrationService.authUserDetails().getUserId();
		log.info("sessionId", "idType", "id",
				"In pre-registration controller for Update preregistration with json object" + jsonObject);
		requestValidator.validateId(UPDATE, jsonObject.getId(), errors);
		DataValidationUtil.validate(errors, UPDATE);
		return ResponseEntity.status(HttpStatus.OK)
				.body(preRegistrationService.updatePreRegistration(jsonObject, preRegistrationId, userId));
	}

	/**
	 * Get API to fetch all the Pre-registration data for a pre-id.
	 *
	 * @param preRegistraionId
	 *            the pre reg id
	 * @return the application data for a pre-id
	 */

	//@PreAuthorize("hasAnyRole('INDIVIDUAL','REGISTRATION_OFFICER','REGISTRATION_SUPERVISOR','REGISTRATION_ADMIN','PRE_REGISTRATION_ADMIN')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetapplications())")
	@GetMapping(path = "/applications/prereg/{preRegistrationId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "getPreRegDemographicData", description = "Get demographic data for a new preregistration application", tags = "demographic-controller")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Demographic data successfully retrieved"),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseEntity<MainResponseDTO<DemographicResponseDTO>> getPreRegDemographicData(
			@PathVariable("preRegistrationId") String preRegistraionId) {
		log.info("sessionId", "idType", "id",
				"In pre-registration controller for fetching all demographic data with preregistrationId"
						+ preRegistraionId);
		return ResponseEntity.status(HttpStatus.OK).body(preRegistrationService.getDemographicData(preRegistraionId));
	}

	/**
	 * Put API to update the status of the application.
	 *
	 * @param preRegId
	 *            the pre reg id
	 * @param status
	 *            the status
	 * @return the updation status of application for a pre-id
	 */

	//@PreAuthorize("hasAnyRole('INDIVIDUAL','PRE_REGISTRATION_ADMIN','REGISTRATION_OFFICER','REGISTRATION_SUPERVISOR','REGISTRATION_ADMIN')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPutapplicationsstatus())")
	@PutMapping(path = "/applications/prereg/status/{preRegistrationId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "updatePreRegApplicationStatus", description = "Update Pre-Registartion status", tags = "demographic-controller")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Pre-Registration Status successfully updated"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseEntity<MainResponseDTO<String>> updatePreRegApplicationStatus(
			@PathVariable("preRegistrationId") String preRegId,
			@RequestParam(value = "statusCode", required = true) String status) {
		String userId = preRegistrationService.authUserDetails().getUserId();
		log.info("sessionId", "idType", "id",
				"In pre-registration controller for fetching all demographic data with preRegId " + preRegId
						+ " and status " + status);
		return ResponseEntity.status(HttpStatus.OK)
				.body(preRegistrationService.updatePreRegistrationStatus(preRegId, status, userId));
	}

	/**
	 * Post api to fetch all the applications created by user.
	 *
	 * @param res
	 * @param pageIdx
	 * 
	 * @return List of applications created by User
	 */

	//@PreAuthorize("hasAnyRole('INDIVIDUAL','REGISTRATION_OFFICER','REGISTRATION_SUPERVISOR','REGISTRATION_ADMIN')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetapplicationsall())")
	@GetMapping(path = "/applications/prereg", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "getAllPreRegApplications", description = "Fetch all the prereg applications created by user", tags = "demographic-controller")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "All applications fetched successfully"),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseEntity<MainResponseDTO<DemographicMetadataDTO>> getAllPreRegApplications(HttpServletRequest res,
			@RequestParam(value = "pageIndex", required = false) String pageIdx) {
		String userId = preRegistrationService.authUserDetails().getUserId();
		log.info("sessionId", "idType", "id",
				"In pre-registration controller for fetching all applications with userId ");
		return ResponseEntity.status(HttpStatus.OK)
				.body(preRegistrationService.getAllApplicationDetails(userId, pageIdx));
	}

	/**
	 * Get API to fetch the status of a application.
	 *
	 * @param preId
	 *            the pre id
	 * @return status of application
	 */

	//@PreAuthorize("hasAnyRole('INDIVIDUAL','PRE_REGISTRATION_ADMIN','REGISTRATION_OFFICER','REGISTRATION_SUPERVISOR','REGISTRATION_ADMIN')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetapplicationsstatus())")
	@GetMapping(path = "/applications/prereg/status/{preRegistrationId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "getPreRegApplicationStatus", description = "Fetch the status of a application", tags = "demographic-controller")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "All applications status fetched successfully"),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseEntity<MainResponseDTO<PreRegistartionStatusDTO>> getPreRegApplicationStatus(
			@PathVariable("preRegistrationId") String preId) {
		String userId = preRegistrationService.authUserDetails().getUserId();
		log.info("sessionId", "idType", "id",
				"In pre-registration controller for fetching all applicationStatus with preId " + preId);
		return ResponseEntity.status(HttpStatus.OK).body(preRegistrationService.getApplicationStatus(preId, userId));
	}

	/**
	 * Delete API to delete the Individual applicant and documents associated with
	 * the PreId.
	 *
	 * @param preId
	 *            the pre id
	 * @return the deletion status of application for a pre-id
	 */

	//@PreAuthorize("hasAnyRole('INDIVIDUAL','REGISTRATION_OFFICER','REGISTRATION_SUPERVISOR','REGISTRATION_ADMIN')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getDeleteapplications())")
	@DeleteMapping(path = "/applications/prereg/{preRegistrationId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "deletePreRegApplication", description = "Discard prereg application", tags = "demographic-controller")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Deletion of individual is successfully"),
			@ApiResponse(responseCode = "204", description = "No Content"),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			})
	public ResponseEntity<MainResponseDTO<DeletePreRegistartionDTO>> deletePreRegApplication(
			@PathVariable("preRegistrationId") String preId) {
		String userId = preRegistrationService.authUserDetails().getUserId();
		log.info("sessionId", "idType", "id",
				"In pre-registration controller for deletion of individual with preId " + preId);

		return ResponseEntity.status(HttpStatus.OK).body(preRegistrationService.deleteIndividual(preId, userId));
	}


	//@PreAuthorize("hasAnyRole('INDIVIDUAL','REGISTRATION_OFFICER','REGISTRATION_SUPERVISOR','REGISTRATION_ADMIN','REGISTRATION_PROCESSOR')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostapplicationsupdatedtime())")
	@PostMapping(path = "/applications/prereg/updatedTime", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "getUpdatedDateTimeByPreIds", description = "Get Updated Date Time for List of Pre-Registration Id",
			tags = "demographic-controller")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Updated Date Time successfully fetched for list of pre-registration ids"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseEntity<MainResponseDTO<Map<String, String>>> getUpdatedDateTimeByPreIds(
			@Validated @RequestBody MainRequestDTO<PreRegIdsByRegCenterIdDTO> mainRequestDTO,
			@ApiIgnore Errors errors) {
		requestValidator.validateId(UPDATEDTIME, mainRequestDTO.getId(), errors);
		DataValidationUtil.validate(errors, UPDATEDTIME);
		return ResponseEntity.status(HttpStatus.OK)
				.body(preRegistrationService.getUpdatedDateTimeForPreIds(mainRequestDTO.getRequest()));
	}
	
	
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetapplicationsinfo())")
	@GetMapping(path = "/applications/prereg/info/{preregistrationId}")
	@Operation(summary = "getPreRegDemographicAndDocumentData", description = "Retrive Application demographic and document info for given prid", tags = "application-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<MainResponseDTO<?>> getPreRegDemographicAndDocumentData(
			@PathVariable("preregistrationId") String preregistrationId) {
		log.info("In demographic controller to getFullPreRegApplication {}", preregistrationId);
		return ResponseEntity.status(HttpStatus.OK).body(preRegistrationService.getPregistrationInfo(preregistrationId));
	}


}
