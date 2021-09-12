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
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

@RestController
public class AppointmentController {

	@Autowired
	private AppointmentService appointmentService;

	private Logger log = LoggerConfiguration.logConfig(AppointmentController.class);

	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetslotsavailablity())")
	@GetMapping(path = "/applications/appointment/slots/availability/{registrationCenterId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Fetch availability Data")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Availablity details fetched successfully") })
	public ResponseEntity<MainResponseDTO<AvailabilityDto>> getAvailability(
			@PathVariable("registrationCenterId") String registrationCenterId) {
		log.info("fetch availablity for regID: {}", registrationCenterId);
		return ResponseEntity.status(HttpStatus.OK).body(appointmentService.getSlotAvailablity(registrationCenterId));
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostappointmentpregid())")
	@PostMapping(path = "/applications/appointment/{preRegistrationId}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Booking Appointment")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Appointment Booked Successfully") })
	public ResponseEntity<MainResponseDTO<BookingStatusDTO>> bookAppoinment(
			@PathVariable("preRegistrationId") String preRegistrationId,
			@Validated @RequestBody(required = true) MainRequestDTO<BookingRequestDTO> bookingDTO,
			@ApiIgnore Errors errors,
			@RequestHeader(value = "User-Agent") String userAgent) {
		log.info("Book an appointment for preRegId: {}", preRegistrationId);
		return ResponseEntity.status(HttpStatus.OK)
				.body(appointmentService.makeAppointment(bookingDTO, preRegistrationId, userAgent));
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetappointmentdetailspreregid())")
	@GetMapping(path = "/applications/appointment/{preRegistrationId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Fetch Appointment details")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Appointment details fetched Successfully") })
	public ResponseEntity<MainResponseDTO<BookingRegistrationDTO>> getAppointments(
			@PathVariable("preRegistrationId") String preRegistrationId) {
		log.info("To fetch appointment details for preRegID: {}", preRegistrationId);
		return ResponseEntity.status(HttpStatus.OK).body(appointmentService.getAppointmentDetails(preRegistrationId));

	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getCancelappointmentpreregid())")
	@PutMapping(path = "/applications/appointment/{preRegistrationId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Cancel an booked appointment")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Appointment canceled successfully") })
	public ResponseEntity<MainResponseDTO<CancelBookingResponseDTO>> cancelBook(
			@PathVariable("preRegistrationId") String preRegistrationId) {
		log.info("Cancel the appointment for preRegId:{} ", preRegistrationId);
		return ResponseEntity.status(HttpStatus.OK).body(appointmentService.cancelAppointment(preRegistrationId));
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getDeleteappointmentpreregid())")
	@DeleteMapping(path = "/applications/appointment", produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Discard Booking")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Deletion of Booking is successfully") })
	public ResponseEntity<MainResponseDTO<DeleteBookingDTO>> discardIndividual(
			@RequestParam(value = "preRegistrationId") String preId) {
		log.info("Delete booking with preId: {}", preId);
		return ResponseEntity.status(HttpStatus.OK).body(appointmentService.deleteBooking(preId));
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostappointmentmulti())")
	@PostMapping(path = "/applications/appointment", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Booking Appointment")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Appointment Booked Successfully") })
	public ResponseEntity<MainResponseDTO<BookingStatus>> bookMultiAppoinment(
			@Validated @RequestBody(required = true) MainRequestDTO<MultiBookingRequest> bookingRequest,
			@RequestHeader(value = "User-Agent") String userAgent) {
		return ResponseEntity.status(HttpStatus.OK).body(appointmentService.makeMultiAppointment(bookingRequest, userAgent));
	}

}
