package io.mosip.preregistration.application.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.application.service.AppointmentService;
import io.mosip.preregistration.booking.dto.AvailabilityDto;
import io.mosip.preregistration.booking.dto.BookingRequestDTO;
import io.mosip.preregistration.booking.dto.BookingStatus;
import io.mosip.preregistration.booking.dto.BookingStatusDTO;
import io.mosip.preregistration.booking.dto.MultiBookingRequest;
import io.mosip.preregistration.core.common.dto.BookingRegistrationDTO;
import io.mosip.preregistration.core.common.dto.CancelBookingResponseDTO;
import io.mosip.preregistration.core.common.dto.DeleteBookingDTO;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
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
@Tag(name = "appointment-controller", description = "Appointment Controller")
public class AppointmentController {

	@Autowired
	private AppointmentService appointmentService;

	private Logger log = LoggerConfiguration.logConfig(AppointmentController.class);

	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetslotsavailablity())")
	@GetMapping(path = "/applications/appointment/slots/availability/{registrationCenterId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Fetch availability Data", description = "Fetch availability Data", tags = "appointment-controller")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Availablity details fetched successfully"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<MainResponseDTO<AvailabilityDto>> getAvailability(
			@PathVariable("registrationCenterId") String registrationCenterId) {
		log.info("fetch availablity for regID: {}", registrationCenterId);
		return ResponseEntity.status(HttpStatus.OK).body(appointmentService.getSlotAvailablity(registrationCenterId));
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostappointmentpregid())")
	@PostMapping(path = "/applications/appointment/{preRegistrationId}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Booking Appointment", description = "Booking Appointment", tags = "appointment-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Appointment Booked Successfully"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<MainResponseDTO<BookingStatusDTO>> bookAppoinment(
			@PathVariable("preRegistrationId") String preRegistrationId,
			@Validated @RequestBody(required = true) MainRequestDTO<BookingRequestDTO> bookingDTO,
			@ApiParam(hidden = true) Errors errors, @RequestHeader(value = "User-Agent") String userAgent) {
		log.info("Book an appointment for preRegId: {}", preRegistrationId);
		return ResponseEntity.status(HttpStatus.OK)
				.body(appointmentService.makeAppointment(bookingDTO, preRegistrationId, userAgent));
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetappointmentdetailspreregid())")
	@GetMapping(path = "/applications/appointment/{preRegistrationId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Fetch Appointment details", description = "Fetch Appointment details", tags = "appointment-controller")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Appointment details fetched Successfully"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<MainResponseDTO<BookingRegistrationDTO>> getAppointments(
			@PathVariable("preRegistrationId") String preRegistrationId) {
		log.info("To fetch appointment details for preRegID: {}", preRegistrationId);
		return ResponseEntity.status(HttpStatus.OK).body(appointmentService.getAppointmentDetails(preRegistrationId));
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getCancelappointmentpreregid())")
	@PutMapping(path = "/applications/appointment/{preRegistrationId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Cancel an booked appointment", description = "Cancel an booked appointment", tags = "appointment-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Appointment cancelled successfully"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<MainResponseDTO<CancelBookingResponseDTO>> cancelBook(
			@PathVariable("preRegistrationId") String preRegistrationId) {
		log.info("Cancel the appointment for preRegId :{} ", preRegistrationId);
		return ResponseEntity.status(HttpStatus.OK).body(appointmentService.cancelAppointment(preRegistrationId));
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getCancelappointmentpreregid())")
	@PutMapping(path = "/internal/applications/appointment/{preRegistrationId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Cancel an booked appointment. Used internally by batch job", description = "Cancel an booked appointment. Used internally by batch job,", tags = "appointment-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Appointment cancelled successfully"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<MainResponseDTO<CancelBookingResponseDTO>> internalCancelBook(
			@PathVariable("preRegistrationId") String preRegistrationId) {
		log.info("Cancel the appointment for preRegId called internally :{} ", preRegistrationId);
		return ResponseEntity.status(HttpStatus.OK).body(appointmentService.cancelAppointment(preRegistrationId));
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getDeleteappointmentpreregid())")
	@DeleteMapping(path = "/applications/appointment", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Discard Booking", description = "CDiscard Booking", tags = "appointment-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Deletion of Booking is successfully"),
			@ApiResponse(responseCode = "204", description = "No Content"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))), })
	public ResponseEntity<MainResponseDTO<DeleteBookingDTO>> discardIndividual(
			@RequestParam(value = "preRegistrationId") String preId) {
		log.info("Delete booking with preId: {}", preId);
		return ResponseEntity.status(HttpStatus.OK)
				.body(appointmentService.deleteBookingAndUpdateApplicationStatus(preId));
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostappointmentmulti())")
	@PostMapping(path = "/applications/appointment", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Booking Appointment", description = "Booking Appointment", tags = "appointment-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Appointment Booked Successfully"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<MainResponseDTO<BookingStatus>> bookMultiAppoinment(
			@Validated @RequestBody(required = true) MainRequestDTO<MultiBookingRequest> bookingRequest,
			@RequestHeader(value = "User-Agent") String userAgent) {
		return ResponseEntity.status(HttpStatus.OK)
				.body(appointmentService.makeMultiAppointment(bookingRequest, userAgent));
	}
}