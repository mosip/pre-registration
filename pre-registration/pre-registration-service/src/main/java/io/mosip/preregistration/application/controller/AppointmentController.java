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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
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
import io.mosip.preregistration.core.util.DataValidationUtil;
import io.mosip.preregistration.core.util.RequestValidator;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_SESSIONID;
import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_IDTYPE;
import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_ID;

@RestController
@Tag(name = "appointment-controller", description = "Appointment Controller")
/**
 * REST controller for appointment APIs in MOSIP Pre-Registration.
 */
public class AppointmentController {

	@Autowired
	/**
	 * appointment service used by this Pre-Registration type.
	 */
	private AppointmentService appointmentService;

	@Autowired
	/**
	 * Shared MOSIP request-envelope validator (id, version, requesttime). Same bean
	 * {@code BookingController} uses; {@code mosip.id.book} is on the {@code @Primary}
	 * {@code ic} map.
	 */
	private RequestValidator requestValidator;

	/**
	 * {@code RequestValidator#validateId} key for booking POSTs. Resolves
	 * {@code mosip.id.book} → {@code mosip.pre-registration.booking.book}.
	 */
	private static final String BOOKING = "book";

	/**
	 * Registers {@link RequestValidator} so {@code @Validated} booking POST bodies run
	 * version / requesttime / request-body checks before the handler. {@code validateId}
	 * is still called in the handler because the id key is operation-specific.
	 *
	 * @param binder Spring data binder for this controller
	 */
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.addValidators(requestValidator);
	}

	/**
	 * Performs log config for MOSIP Pre-Registration.
	 *
	 * @return log config result
	 */
	private Logger log = LoggerConfiguration.logConfig(AppointmentController.class);

	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetslotsavailablity())")
	@GetMapping(path = "/applications/appointment/slots/availability/{registrationCenterId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Fetch availability Data", description = "Fetch availability Data", tags = "appointment-controller")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Availablity details fetched successfully"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	/**
	 * Returns availability.
	 *
	 * @param registrationCenterId registration center id used by get availability
	 * @return get availability result
	 */
	public ResponseEntity<MainResponseDTO<AvailabilityDto>> getAvailability(
			@PathVariable("registrationCenterId") String registrationCenterId) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "fetch availablity for regID: " + registrationCenterId);
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
	/**
	 * Performs book appoinment for MOSIP Pre-Registration.
	 *
	 * @param preRegistrationId pre registration id used by book appoinment
	 * @param bookingDTO booking dto used by book appoinment
	 * @param errors errors used by book appoinment
	 * @param userAgent user agent used by book appoinment
	 * @return book appoinment result
	 */
	public ResponseEntity<MainResponseDTO<BookingStatusDTO>> bookAppoinment(
			@PathVariable("preRegistrationId") String preRegistrationId,
			@Validated @RequestBody(required = true) MainRequestDTO<BookingRequestDTO> bookingDTO,
			@ApiParam(hidden = true) Errors errors, @RequestHeader(value = "User-Agent") String userAgent) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "Book an appointment for preRegId: " + preRegistrationId);
		requestValidator.validateId(BOOKING, bookingDTO.getId(), errors);
		DataValidationUtil.validate(errors, BOOKING);
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
	/**
	 * Returns appointments.
	 *
	 * @param preRegistrationId pre registration id used by get appointments
	 * @return get appointments result
	 */
	public ResponseEntity<MainResponseDTO<BookingRegistrationDTO>> getAppointments(
			@PathVariable("preRegistrationId") String preRegistrationId) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"To fetch appointment details for preRegID: " + preRegistrationId);
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
	/**
	 * Performs cancel book for MOSIP Pre-Registration.
	 *
	 * @param preRegistrationId pre registration id used by cancel book
	 * @return cancel book result
	 */
	public ResponseEntity<MainResponseDTO<CancelBookingResponseDTO>> cancelBook(
			@PathVariable("preRegistrationId") String preRegistrationId) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"Cancel the appointment for preRegId : " + preRegistrationId);
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
	/**
	 * Performs internal cancel book for MOSIP Pre-Registration.
	 *
	 * @param preRegistrationId pre registration id used by internal cancel book
	 * @return internal cancel book result
	 */
	public ResponseEntity<MainResponseDTO<CancelBookingResponseDTO>> internalCancelBook(
			@PathVariable("preRegistrationId") String preRegistrationId) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"Cancel the appointment for preRegId called internally :" + preRegistrationId);
		return ResponseEntity.status(HttpStatus.OK).body(appointmentService.cancelAppointment(preRegistrationId));
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getDeleteappointmentpreregid())")
	@DeleteMapping(path = "/applications/appointment", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Discard Booking", description = "CDiscard Booking", tags = "appointment-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Deletion of Booking is successfully"),
			@ApiResponse(responseCode = "204", description = "No Content"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))), })
	/**
	 * Performs discard individual for MOSIP Pre-Registration.
	 *
	 * @param preId pre id used by discard individual
	 * @return discard individual result
	 */
	public ResponseEntity<MainResponseDTO<DeleteBookingDTO>> discardIndividual(
			@RequestParam(value = "preRegistrationId") String preId) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "Delete booking with preId: " + preId);
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
	/**
	 * Performs book multi appoinment for MOSIP Pre-Registration.
	 *
	 * @param bookingRequest booking request used by book multi appoinment
	 * @param userAgent user agent used by book multi appoinment
	 * @return book multi appoinment result
	 */
	public ResponseEntity<MainResponseDTO<BookingStatus>> bookMultiAppoinment(
			@Validated @RequestBody(required = true) MainRequestDTO<MultiBookingRequest> bookingRequest,
			@ApiParam(hidden = true) Errors errors, @RequestHeader(value = "User-Agent") String userAgent) {
		requestValidator.validateId(BOOKING, bookingRequest.getId(), errors);
		DataValidationUtil.validate(errors, BOOKING);
		return ResponseEntity.status(HttpStatus.OK)
				.body(appointmentService.makeMultiAppointment(bookingRequest, userAgent));
	}
}
