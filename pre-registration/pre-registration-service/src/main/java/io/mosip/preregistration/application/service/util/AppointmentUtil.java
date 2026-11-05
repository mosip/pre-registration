package io.mosip.preregistration.application.service.util;

import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_ID;
import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_IDTYPE;
import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_SESSIONID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.application.errorcodes.AppointmentErrorCodes;
import io.mosip.preregistration.application.exception.AppointmentExecption;
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
import io.mosip.preregistration.core.exception.RestCallException;
import io.mosip.preregistration.pipeline.InProcessBookingClient;

/**
 * Application appointment APIs call booking through {@link InProcessBookingClient}.
 * Booking runs in this JVM; do not HTTP-loopback to {@code /appointment/**}.
 */
@Component
public class AppointmentUtil {

	@Autowired
	@Lazy
	private InProcessBookingClient bookingClient;

	private Logger log = LoggerConfiguration.logConfig(AppointmentUtil.class);

	/**
	 * @param regCenterId registration centre id
	 * @return availability for that centre
	 */
	public AvailabilityDto getSlotAvailablityByRegCenterId(String regCenterId) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"Fetching slots availability in-process for regCenterId: " + regCenterId);
		return unwrap(bookingClient.getAvailability(regCenterId.trim()),
				AppointmentErrorCodes.FAILED_TO_FETCH_AVAILABLITY);
	}

	/**
	 * @param bookingDTO booking request
	 * @param preRegistrationId pre-registration id
	 * @return booking status
	 */
	public BookingStatusDTO makeAppointment(MainRequestDTO<BookingRequestDTO> bookingDTO, String preRegistrationId) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"Booking appointment in-process for preRegistrationId: " + preRegistrationId);
		return unwrap(bookingClient.bookAppointment(bookingDTO, preRegistrationId.trim()),
				AppointmentErrorCodes.BOOKING_FAILED);
	}

	/**
	 * @param preRegistrationId pre-registration id
	 * @return appointment details
	 */
	public BookingRegistrationDTO fetchAppointmentDetails(String preRegistrationId) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"Fetching appointment details in-process for preRegistrationId: " + preRegistrationId);
		return unwrap(bookingClient.getAppointmentDetails(preRegistrationId.trim()),
				AppointmentErrorCodes.FAILED_TO_FETCH_APPOINTMENT_DETAILS);
	}

	/**
	 * @param preRegId pre-registration id
	 * @return delete result
	 */
	public DeleteBookingDTO deleteBooking(String preRegId) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"Deleting appointment in-process for preRegistrationId: " + preRegId);
		MainResponseDTO<DeleteBookingDTO> body = bookingClient.deleteBooking(preRegId.trim());
		if (body != null && body.getErrors() != null && !body.getErrors().isEmpty()) {
			throw new RestCallException(body.getErrors().get(0).getErrorCode(),
					body.getErrors().get(0).getMessage());
		}
		if (body == null || body.getResponse() == null) {
			throw new AppointmentExecption(AppointmentErrorCodes.FAILED_TO_DELETE_APPOINTMENT.getCode(),
					AppointmentErrorCodes.FAILED_TO_DELETE_APPOINTMENT.getMessage());
		}
		return body.getResponse();
	}

	/**
	 * @param preRegistrationId pre-registration id
	 * @return cancel result
	 */
	public CancelBookingResponseDTO cancelAppointment(String preRegistrationId) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"Cancelling appointment in-process for preRegistrationId: " + preRegistrationId);
		return unwrap(bookingClient.cancelAppointment(preRegistrationId.trim()),
				AppointmentErrorCodes.CANCEL_APPOINTMENT_FAILED);
	}

	/**
	 * @param bookingRequest multi-book request
	 * @return multi-book status
	 */
	public BookingStatus multiAppointmentBooking(MainRequestDTO<MultiBookingRequest> bookingRequest) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "Multi booking appointment in-process");
		return unwrap(bookingClient.bookMultiAppointment(bookingRequest),
				AppointmentErrorCodes.MULTI_BOOKING_FAILED);
	}

	private <T> T unwrap(MainResponseDTO<T> body, AppointmentErrorCodes fallback) {
		if (body != null && body.getErrors() != null && !body.getErrors().isEmpty()) {
			throw new AppointmentExecption(body.getErrors().get(0).getErrorCode(),
					body.getErrors().get(0).getMessage());
		}
		if (body == null || body.getResponse() == null) {
			throw new AppointmentExecption(fallback.getCode(), fallback.getMessage());
		}
		return body.getResponse();
	}
}
