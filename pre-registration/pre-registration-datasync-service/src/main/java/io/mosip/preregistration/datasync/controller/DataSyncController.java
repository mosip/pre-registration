package io.mosip.preregistration.datasync.controller;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.util.ResponseFilter;
import io.mosip.preregistration.datasync.dto.ApplicationsDTO;
import io.mosip.preregistration.datasync.dto.DataSyncRequestDTO;
import io.mosip.preregistration.datasync.dto.PreRegArchiveDTO;
import io.mosip.preregistration.datasync.dto.PreRegistrationIdsDTO;
import io.mosip.preregistration.datasync.dto.ReverseDataSyncRequestDTO;
import io.mosip.preregistration.datasync.dto.ReverseDatasyncReponseDTO;
import io.mosip.preregistration.datasync.service.DataSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * This class provides different api to perform operation for datasync
 *
 * @author M1046129 - Jagadishwari
 * @since 1.0.0
 *
 */
@RestController
@RequestMapping("/")
@Tag(name = "Data-Sync", description = "Data-Sync Controller")
public class DataSyncController {

	@Autowired
	private DataSyncService dataSyncService;

	private Logger log = LoggerConfiguration.logConfig(DataSyncController.class);

	/**
	 * This POST api use to retrieve all PreRegistrationIds based on registration
	 * center id, from date and to date
	 *
	 * @param DataSyncDTO
	 * @return responseDto
	 */
	//@PreAuthorize("hasAnyRole('REGISTRATION_OFFICER','REGISTRATION_SUPERVISOR','REGISTRATION_ ADMIN')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostpreregsync())")
	@ResponseFilter
	@PostMapping(path = "/sync", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Fetch all PreRegistrationIds", description = "Fetch all PreRegistrationIds", tags = "Data-Sync")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "All PreRegistrationIds fetched successfully"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<MainResponseDTO<PreRegistrationIdsDTO>> retrieveAllPreRegids(
			@RequestBody(required = true) MainRequestDTO<DataSyncRequestDTO> dataSyncDto) {
		log.info("sessionId", "idType", "id",
				"In Datasync controller for retreiving all the pre-registrations for object  " + dataSyncDto);
		return ResponseEntity.status(HttpStatus.OK).body(dataSyncService.retrieveAllPreRegIds(dataSyncDto));
	}
	
	/**
	 * This POST API used to retrieve all appointments for a registration
	 * center id, booked between a from date and to date
	 * 
	 * @param MainRequestDTO<DataSyncRequestDTO>
	 * @return ResponseEntity<MainResponseDTO<ApplicationIdsDTO>>
	 */
	//@PreAuthorize("hasAnyRole('REGISTRATION_OFFICER','REGISTRATION_SUPERVISOR','REGISTRATION_ ADMIN')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostpreregsync())")
	@ResponseFilter
	@PostMapping(path = "/syncV2", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Fetch all application ids for all booking types", description = "Fetch all Application Ids for all booking types", tags = "Data-Sync")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "All Application Ids for all booking types fetched successfully"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<MainResponseDTO<ApplicationsDTO>> retrieveAllAppointmentsSyncV2(
			@RequestBody(required = true) MainRequestDTO<DataSyncRequestDTO> dataSyncDto) {
		log.info("sessionId", "idType", "id",
				"In Datasync controller for retreiving all the appointments for  " + dataSyncDto);
		return ResponseEntity.status(HttpStatus.OK).body(dataSyncService.retrieveAllAppointmentsSyncV2(dataSyncDto));
	}

	/**
	 * This Get api use to retrieve the details for an PreRegistrationId
	 *
	 * @param preRegistrationId
	 * @return zip file to download
	 */
	//@PreAuthorize("hasAnyRole('REGISTRATION_OFFICER','REGISTRATION_SUPERVISOR','REGISTRATION_ ADMIN')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetsyncpreregistrationid())")
	@ResponseFilter
	@GetMapping(path = "/sync/{preRegistrationId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Retrieve Pre-Registrations", description = "Retrieve Pre-Registrations", tags = "Data-Sync")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Data Sync records fetched"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<MainResponseDTO<PreRegArchiveDTO>> retrievePreRegistrations(
			@PathVariable(required = true, value = "preRegistrationId") String preRegistrationId) {
		log.info("sessionId", "idType", "id",
				"In Datasync controller for retreiving pre-registration data with preRegId " + preRegistrationId);
		return ResponseEntity.status(HttpStatus.OK).body(dataSyncService.getPreRegistrationData(preRegistrationId));
	}


	//@PreAuthorize("hasAnyRole('REGISTRATION_OFFICER','REGISTRATION_SUPERVISOR','REGISTRATION_ ADMIN')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetsyncpreregistrationidmachineid())")
	@ResponseFilter
	@GetMapping(path = "/sync/{preRegistrationId}/{machineId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Retrieve Pre-Registrations", description = "Retrieve Pre-Registrations", tags = "Data-Sync")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Data Sync records fetched"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<MainResponseDTO<PreRegArchiveDTO>> retrievePreRegistrations(
			@PathVariable(required = true, value = "preRegistrationId") String preRegistrationId,
			@PathVariable(required = true, value = "machineId") String machineId) {
		log.info("sessionId", "idType", "id",
				"In Datasync controller for retreiving pre-registration data with preRegId and machineId "
						+ preRegistrationId + " " + machineId);
		return ResponseEntity.status(HttpStatus.OK)
				.body(dataSyncService.fetchPreRegistrationData(preRegistrationId, machineId));
	}

	/**
	 * This POST api is used to retrieve all processed pre-registration ids and
	 * store in pre-registration database
	 *
	 * @param consumedData
	 * @return response object
	 */
	//@PreAuthorize("hasAnyRole('REGISTRATION_OFFICER','REGISTRATION_SUPERVISOR','REGISTRATION_ ADMIN','REGISTRATION_PROCESSOR')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostsyncconsumedpreregids())")
	@ResponseFilter
	@PostMapping(path = "/sync/consumedPreRegIds", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Store consumed Pre-Registrations", description = "Store consumed Pre-Registrations", tags = "Data-Sync")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Consumed Pre-Registrations saved"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<MainResponseDTO<ReverseDatasyncReponseDTO>> storeConsumedPreRegistrationsIds(
			@NotNull @RequestBody(required = true) MainRequestDTO<ReverseDataSyncRequestDTO> consumedData) {
		log.info("sessionId", "idType", "id",
				"In Datasync controller for storing the consumed preregistration with object" + consumedData);
		return ResponseEntity.status(HttpStatus.OK).body(dataSyncService.storeConsumedPreRegistrations(consumedData));
	}

}
