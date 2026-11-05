package io.mosip.preregistration.booking.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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

import io.mosip.kernel.core.exception.ParseException;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.booking.dto.AvailabilityDto;
import io.mosip.preregistration.booking.dto.BookingDataByRegIdDto;
import io.mosip.preregistration.booking.dto.BookingRequestDTO;
import io.mosip.preregistration.booking.dto.BookingStatus;
import io.mosip.preregistration.booking.dto.BookingStatusDTO;
import io.mosip.preregistration.booking.dto.MultiBookingRequest;
import io.mosip.preregistration.booking.service.BookingServiceIntf;
import io.mosip.preregistration.core.common.dto.BookingRegistrationDTO;
import io.mosip.preregistration.core.common.dto.CancelBookingResponseDTO;
import io.mosip.preregistration.core.common.dto.DeleteBookingDTO;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.dto.PreRegIdsByRegCenterIdResponseDTO;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.util.DataValidationUtil;
import io.mosip.preregistration.core.util.RequestValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * This class provides different API's to perform operations on Booking
 * Application
 * 
 * @author Kishan Rathore
 * @author Jagadishwari
 * @author Ravi C. Balaji
 * @since 1.0.0
 *
 */
@RestController
@RequestMapping("/")
@Tag(name = "booking-controller", description = "Booking Controller")
@SecurityRequirement(name = "Authorization")
public class BookingController {

	/** Autowired reference for {@link #bookingService}. */
	@Autowired
	private BookingServiceIntf bookingService;

	/** Validates MOSIP request ids on POST/PUT bodies. */
	@Autowired
	private RequestValidator requestValidator;

	/** MOSIP request-id constant for booking APIs. */
	private static final String BOOKING = "book";
	
	
	/**
	 * Inits the binder.
	 *
	 * @param binder the binder
	 */
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.addValidators(requestValidator);
	}


	private Logger log = LoggerConfiguration.logConfig(BookingController.class);


	/**
	 * Get API to get availability details.
	 * 
	 * @param registration_center_id
	 * @return MainResponseDTO
	 */
	//@PreAuthorize("hasAnyRole('INDIVIDUAL')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetappointmentavailability())")
	@GetMapping(path = "/appointment/availability/{registrationCenterId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Fetch availability Data", description = "Fetch availability Data", tags = "booking-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Availability details fetched successfully"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<MainResponseDTO<AvailabilityDto>> getAvailability(
			@PathVariable("registrationCenterId") String registrationCenterId) {
		log.info("sessionId", "idType", "id",
				"In getAvailability method of Booking controller to fetch the availability for regID: "
						+ registrationCenterId);
		return ResponseEntity.status(HttpStatus.OK).body(bookingService.getAvailability(registrationCenterId));
	}

	/**
	 * Post API to book the appointment.
	 * 
	 * @param MainListRequestDTO
	 * @return MainResponseDTO
	 * @throws ParseException
	 * @throws java.text.ParseException
	 */
	//@PreAuthorize("hasAnyRole('INDIVIDUAL')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostappointmentpreregistrationid())")
	@PostMapping(path = "/appointment/{preRegistrationId}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Booking Appointment", description = "Booking Appointment", tags = "booking-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Appointment Booked Successfully"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<MainResponseDTO<BookingStatusDTO>> bookAppoinment(
			@PathVariable("preRegistrationId") String preRegistrationId,
			@Validated @RequestBody(required = true) MainRequestDTO<BookingRequestDTO> bookingDTO,  @Parameter(hidden = true) Errors errors ) {
		log.info("sessionId", "idType", "id",
				"In bookAppoinment method of Booking controller to book an appointment for object: " + bookingDTO);
		requestValidator.validateId(BOOKING, bookingDTO.getId(), errors);
		DataValidationUtil.validate(errors,BOOKING);
		return ResponseEntity.status(HttpStatus.OK).body(bookingService.bookAppointment(bookingDTO, preRegistrationId));
	}
	
	/**
	 * Post API to book the appointment.
	 * 
	 * @param MainListRequestDTO
	 * @return MainResponseDTO
	 * @throws ParseException
	 * @throws java.text.ParseException
	 */
	//@PreAuthorize("hasAnyRole('INDIVIDUAL')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostappointment())")
	@PostMapping(path = "/appointment", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Booking Appointment", description = "Booking Appointment", tags = "booking-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Appointment Booked Successfully"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<MainResponseDTO<BookingStatus>> bookMultiAppoinment(
			@Validated @RequestBody(required = true) MainRequestDTO<MultiBookingRequest> bookingRequest, @Parameter(hidden = true) Errors errors) {
		log.info("sessionId", "idType", "id",
				"In bookAppoinment method of Booking controller to book an appointment for object: " + bookingRequest);
		requestValidator.validateId(BOOKING, bookingRequest.getId(), errors);
		DataValidationUtil.validate(errors,BOOKING);
		return ResponseEntity.status(HttpStatus.OK).body(bookingService.bookMultiAppointment(bookingRequest));
	}

	/**
	 * Get API to get the booked appointment details.
	 * 
	 * @param MainListRequestDTO
	 * @return MainResponseDTO
	 * @throws ParseException
	 * @throws java.text.ParseException
	 */

	//@PreAuthorize("hasAnyRole('INDIVIDUAL','REGISTRATION_OFFICER','REGISTRATION_SUPERVISOR','REGISTRATION_ ADMIN','PRE_REGISTRATION_ADMIN')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetappointmentpreregistrationid())")
	@GetMapping(path = "/appointment/{preRegistrationId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Fetch Appointment details", description = "Fetch Appointment details", tags = "booking-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Appointment details fetched Successfully"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<MainResponseDTO<BookingRegistrationDTO>> getAppointments(
			@PathVariable("preRegistrationId") String preRegistrationId) {
		log.info("sessionId", "idType", "id",
				"In appointmentDetails method of Booking controller to fetch appointment details for preRegID: "
						+ preRegistrationId);
		return ResponseEntity.status(HttpStatus.OK).body(bookingService.getAppointmentDetails(preRegistrationId));

	}

	/**
	 * Put API to cancel the appointment.
	 * 
	 * @param MainListRequestDTO
	 * @return MainResponseDTO
	 * @throws ParseException
	 * @throws java.text.ParseException
	 */
	//@PreAuthorize("hasAnyRole('INDIVIDUAL')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPutappointmentpreregistrationid())")
	@PutMapping(path = "/appointment/{preRegistrationId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Cancel an booked appointment", description = "Cancel an booked appointment", tags = "booking-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Appointment canceled successfully"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<MainResponseDTO<CancelBookingResponseDTO>> cancelBook(
			@PathVariable("preRegistrationId") String preRegistrationId) {
		log.info("sessionId", "idType", "id",
				"In cancelBook method of Booking controller to cancel the appointment for object: " + preRegistrationId);
		return ResponseEntity.status(HttpStatus.OK)
				.body(bookingService.cancelAppointment(preRegistrationId));
	}
	
	/**
	 * Put API to cancel the appointment for Pre-registration batch job.
	 * 
	 * @param MainListRequestDTO
	 * @return MainResponseDTO
	 * @throws ParseException
	 * @throws java.text.ParseException
	 */
	//@PreAuthorize("hasAnyRole('PRE_REGISTRATION_ADMIN','REGISTRATION_SUPERVISOR')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPutbatchappointmentpreregistrationid())")
	@PutMapping(path = "/batch/appointment/{preRegistrationId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Cancel an booked appointment", description = "Cancel an booked appointment", tags = "booking-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Appointment canceled successfully"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<MainResponseDTO<CancelBookingResponseDTO>> cancelAppointmentBatch(
			@PathVariable("preRegistrationId") String preRegistrationId) {
		log.info("sessionId", "idType", "id",
				"In cancelAppointmentBatch method of Booking controller to cancel the appointment for object: " + preRegistrationId+" triggered by batch job");
		return ResponseEntity.status(HttpStatus.OK)
				.body(bookingService.cancelAppointmentBatch(preRegistrationId));
	}

	/**
	 * Delete API to delete the Individual booking associated with the PreId.
	 *
	 * @param preId
	 *            the pre id
	 * @return the deletion status of booking for a pre-id
	 */
	//@PreAuthorize("hasAnyRole('INDIVIDUAL')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getDeleteappointment())")
	@DeleteMapping(path = "/appointment", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Discard Booking", description = "Discard Booking", tags = "booking-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Deletion of Booking is successfully"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "204", description = "No Content"),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
	})
	public ResponseEntity<MainResponseDTO<DeleteBookingDTO>> discardIndividual(
			@RequestParam(value = "preRegistrationId") String preId) {
		log.info("sessionId", "idType", "id", "In Booking controller for deletion of booking with preId " + preId);

		return ResponseEntity.status(HttpStatus.OK).body(bookingService.deleteBooking(preId));
	}

	/**
	 * Get API to fetch all the booked pre-ids within from-date and to-date range.
	 *
	 * @param fromDate
	 *            the from date
	 * @param toDate
	 *            the to date
	 * @return the booked pre-ids for date range
	 */	
	//@PreAuthorize("hasAnyRole('INDIVIDUAL','REGISTRATION_OFFICER','REGISTRATION_SUPERVISOR','REGISTRATION_ ADMIN')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetappointmentpreregistrationidregistrationcenterid())")
	@GetMapping(path = "/appointment/preRegistrationId/{registrationCenterId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Get Pre-Registartion ids By Booked Date Time And Registration center id",
			description = "Get Pre-Registartion ids By Booked Date Time And Registration center id", tags = "booking-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Booked data successfully retrieved"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<MainResponseDTO<PreRegIdsByRegCenterIdResponseDTO>> getBookedDataByDate(
			@RequestParam(value = "from_date", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") String fromDate,
			@RequestParam(value = "to_date") @DateTimeFormat(pattern = "yyyy-MM-dd") String toDate,
			@PathVariable("registrationCenterId") String regCenterId) {
		log.info("sessionId", "idType", "id",
				"In booking controller for fetching all booked preids " + fromDate + " to " + toDate);
		return ResponseEntity.status(HttpStatus.OK)
				.body(bookingService.getBookedPreRegistrationByDate(fromDate, toDate, regCenterId));
	}
	 

	/**
	 * Get API to fetch all the booked pre-ids within from-date and to-date range.
	 *
	 * @param fromDate
	 *            the from date
	 * @param toDate
	 *            the to date
	 * @return the booked pre-ids for date range
	 */
	//@PreAuthorize("hasAnyRole('INDIVIDUAL','REGISTRATION_OFFICER','REGISTRATION_SUPERVISOR','REGISTRATION_ ADMIN')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetappointmentregistrationcenterid())")
	@GetMapping(path = "/appointment/registrationCenterId/{registrationCenterId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Get Pre-Registartion ids By Booked Date Time And Registration center id",
			description = "Get Pre-Registartion ids By Booked Date Time And Registration center id", tags = "booking-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Booked data successfully retrieved"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<MainResponseDTO<BookingDataByRegIdDto>> getBookedDataByRegId(
			@RequestParam(value = "from_date", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") String fromDate,
			@RequestParam(value = "to_date") @DateTimeFormat(pattern = "yyyy-MM-dd") String toDate,
			@PathVariable("registrationCenterId") String regCenterId) {
		log.info("sessionId", "idType", "id",
				"In booking controller for fetching all booked preids " + fromDate + " to " + toDate);
		return ResponseEntity.status(HttpStatus.OK)
				.body(bookingService.getBookedPreRegistrations(fromDate, toDate, regCenterId));
	}
}
