package io.mosip.preregistration.application.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.application.dto.ApplicationRequestDTO;
import io.mosip.preregistration.application.dto.ApplicationResponseDTO;
import io.mosip.preregistration.application.dto.DeleteApplicationDTO;
import io.mosip.preregistration.application.service.ApplicationServiceIntf;
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

/**
 * Pre-registration miscellaneous purpose controller class.
 * 
 * @author Ritik Jain
 */
@RestController
@Tag(name = "miscellaneous-appointment-controller", description = "Miscellaneous Appointment Controller")
public class MiscellaneousAppointmentController {

	private Logger log = LoggerConfiguration.logConfig(MiscellaneousAppointmentController.class);

	/** The Constant CREATE_MISCELLANEOUS_PURPOSE application. */
	private static final String MISCELLANEOUS_PURPOSE_CREATE_ID = "preregistration.miscellaneouspurpose.create";

	@Autowired
	private RequestValidator requestValidator;

	@Autowired
	ApplicationServiceIntf applicationService;

	/**
	 * Inits the binder.
	 *
	 * @param binder the binder
	 */
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.addValidators(requestValidator);
	}

	/**
	 * This Post API is use to create a new application with booking type as
	 * MISCELLANEOUS_PURPOSE
	 * 
	 * @param jsonObject
	 * @param purpose
	 * @param errors
	 * @return List of response dto containing pre-id and group-id
	 */
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostapplications())")
	@PostMapping(path = "/applications/miscpurpose", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "addMiscellaneousPurposeApplication", description = "Creates a new application with Booking Type as MISCELLANEOUS_PURPOSE", tags = "miscellaneous-appointment-controller")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "New application with booking type as MISCELLANEOUS_PURPOSE successfully Created"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<MainResponseDTO<ApplicationResponseDTO>> addMiscellaneousPurposeApplication(
			@Validated @RequestBody(required = true) MainRequestDTO<ApplicationRequestDTO> jsonObject,
			@RequestParam("purpose") String purpose, @ApiIgnore Errors errors) {
		log.info("sessionId", "idType", "id",
				"In pre-registration MiscellaneousAppointmentController for createNewApplication with json object"
						+ jsonObject);
		requestValidator.validateId(MISCELLANEOUS_PURPOSE_CREATE_ID, jsonObject.getId(), errors);
		DataValidationUtil.validate(errors, MISCELLANEOUS_PURPOSE_CREATE_ID);
		return ResponseEntity.status(HttpStatus.OK).body(applicationService.addLostOrUpdateOrMiscellaneousApplication(
				jsonObject, BookingTypeCodes.MISCELLANEOUS_PURPOSE.toString() + "-" + purpose));
	}

	/**
	 * This method is used to delete the application with booking type as
	 * MISCELLANEOUS_PURPOSE
	 * 
	 * @param applicationId
	 * @return the deletion status of application for a applicationId
	 */
	@PreAuthorize("hasAnyRole(@authorizedRoles.getDeleteapplications())")
	@DeleteMapping(path = "/applications/miscpurpose/{applicationId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "deleteMiscellaneousPurposeApplication", description = "Delete application with booking type MISCELLANEOUS_PURPOSE.", tags = "miscellaneous-appointment-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Deletion of application is successful"),
			@ApiResponse(responseCode = "204", description = "No Content"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))), })
	public ResponseEntity<MainResponseDTO<DeleteApplicationDTO>> deleteMiscellaneousPurposeApplication(
			@PathVariable("applicationId") String applicationId) {
		log.info("sessionId", "idType", "id",
				"In pre-registration MiscellaneousAppointmentController for deleteApplication with preId "
						+ applicationId);
		return ResponseEntity.status(HttpStatus.OK)
				.body(applicationService.deleteLostOrUpdateOrMiscellaneousApplication(applicationId,
						BookingTypeCodes.MISCELLANEOUS_PURPOSE.toString()));
	}

}
