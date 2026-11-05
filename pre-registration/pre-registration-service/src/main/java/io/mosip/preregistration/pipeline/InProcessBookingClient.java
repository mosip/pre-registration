package io.mosip.preregistration.pipeline;

import java.util.List;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.exception.BaseUncheckedException;
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
import io.mosip.preregistration.core.common.dto.ExceptionJSONInfoDTO;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;

/**
 * SDK adapter for booking within the consolidated Pre-Registration JVM.
 * <p>
 * Delegates directly to {@link BookingServiceIntf} (no internal HTTP to
 * {@code /appointment/**}). Same pattern as id-repository
 * {@code InProcessCredentialClient}.
 * </p>
 * <p>
 * {@link io.mosip.preregistration.booking.exception.util.BookingExceptionCatcher}
 * rethrows typed exceptions the HTTP {@code BookingExceptionHandler} used to
 * turn into {@code MainResponseDTO.errors}. Callers on the application /
 * datasync path never hit that handler, so this adapter maps those MOSIP
 * exceptions back into the same error envelope HTTP used to return.
 * </p>
 *
 * @see InProcessApplicationClient
 * @see BookingServiceIntf
 */
@Component
public class InProcessBookingClient {

	@Autowired
	@Lazy
	/**
	 * Booking service used by this in-process adapter. {@code @Lazy} breaks the
	 * AppointmentService → BookingService → ApplicationService cycle.
	 */
	private BookingServiceIntf bookingService;

	/**
	 * Fetches slot availability for a registration centre.
	 *
	 * @param regCenterId registration centre id
	 * @return availability envelope; booking MOSIP errors are in {@code errors}, not thrown
	 */
	public MainResponseDTO<AvailabilityDto> getAvailability(String regCenterId) {
		return invoke(() -> bookingService.getAvailability(regCenterId));
	}

	/**
	 * Books one appointment for the given pre-registration id.
	 *
	 * @param bookingDTO booking request envelope ({@code id}, {@code version}, slot fields)
	 * @param preRegistrationId pre-registration id
	 * @return booking status envelope; booking MOSIP errors are in {@code errors}, not thrown
	 */
	public MainResponseDTO<BookingStatusDTO> bookAppointment(MainRequestDTO<BookingRequestDTO> bookingDTO,
			String preRegistrationId) {
		return invoke(() -> bookingService.bookAppointment(bookingDTO, preRegistrationId));
	}

	/**
	 * Books appointments for several pre-registration ids in one request.
	 *
	 * @param bookingRequest multi-book request envelope
	 * @return multi-book status envelope; booking MOSIP errors are in {@code errors}, not thrown
	 */
	public MainResponseDTO<BookingStatus> bookMultiAppointment(MainRequestDTO<MultiBookingRequest> bookingRequest) {
		return invoke(() -> bookingService.bookMultiAppointment(bookingRequest));
	}

	/**
	 * Fetches the booked slot for a pre-registration id.
	 *
	 * @param preRegistrationId pre-registration id
	 * @return appointment details envelope; booking MOSIP errors are in {@code errors}, not thrown
	 */
	public MainResponseDTO<BookingRegistrationDTO> getAppointmentDetails(String preRegistrationId) {
		return invoke(() -> bookingService.getAppointmentDetails(preRegistrationId));
	}

	/**
	 * Cancels the booked appointment for a pre-registration id.
	 *
	 * @param preRegistrationId pre-registration id
	 * @return cancel result envelope; booking MOSIP errors are in {@code errors}, not thrown
	 */
	public MainResponseDTO<CancelBookingResponseDTO> cancelAppointment(String preRegistrationId) {
		return invoke(() -> bookingService.cancelAppointment(preRegistrationId));
	}

	/**
	 * Deletes the booking row for a pre-registration id.
	 *
	 * @param preRegistrationId pre-registration id
	 * @return delete result envelope; booking MOSIP errors are in {@code errors}, not thrown
	 */
	public MainResponseDTO<DeleteBookingDTO> deleteBooking(String preRegistrationId) {
		return invoke(() -> bookingService.deleteBooking(preRegistrationId));
	}

	/**
	 * Lists booked pre-registration ids for a centre in a date range.
	 *
	 * @param fromDate from date ({@code yyyy-MM-dd})
	 * @param toDate to date ({@code yyyy-MM-dd}); empty uses {@code fromDate}
	 * @param regCenterId registration centre id
	 * @return booked pre-ids envelope; booking MOSIP errors are in {@code errors}, not thrown
	 */
	public MainResponseDTO<BookingDataByRegIdDto> getBookedPreRegistrations(String fromDate, String toDate,
			String regCenterId) {
		return invoke(() -> bookingService.getBookedPreRegistrations(fromDate, toDate, regCenterId));
	}

	/**
	 * Runs a booking-service call and maps MOSIP exceptions to the HTTP error envelope.
	 * <p>
	 * {@code BookingService} + {@code BookingExceptionCatcher} rethrow
	 * {@link BaseUncheckedException} / {@link BaseCheckedException}. On the native
	 * {@code /appointment/**} path, {@code BookingExceptionHandler} turns those into
	 * {@link MainResponseDTO#getErrors()}. Application and datasync callers never hit
	 * that handler, so this method copies that mapping: return the success body, or a
	 * body whose {@code errors} list holds {@code errorCode} / {@code message}.
	 * Unexpected (non-MOSIP) exceptions are rethrown.
	 * </p>
	 *
	 * @param <T> response payload type inside {@link MainResponseDTO}
	 * @param call booking-service invocation
	 * @return the service envelope, or an envelope with {@code errors} populated from the
	 *         MOSIP exception
	 */
	private <T> MainResponseDTO<T> invoke(Supplier<MainResponseDTO<T>> call) {
		try {
			return call.get();
		} catch (BaseUncheckedException e) {
			return errorEnvelope(e.getErrorCode(), e.getErrorText());
		} catch (Exception e) {
			if (e instanceof BaseCheckedException checked) {
				return errorEnvelope(checked.getErrorCode(), checked.getErrorText());
			}
			throw e;
		}
	}

	/**
	 * Builds a {@link MainResponseDTO} with a single {@link ExceptionJSONInfoDTO} and no
	 * success payload. Callers such as
	 * {@link io.mosip.preregistration.application.service.util.AppointmentUtil} read
	 * {@code errors[0]} and rethrow as {@code AppointmentExecption} with the original
	 * booking code ({@code PRG_BOOK_RCI_*}, {@code KER-MSD-*}).
	 *
	 * @param <T> unused payload type of the envelope
	 * @param code MOSIP error code from the thrown exception
	 * @param message MOSIP error text from the thrown exception
	 * @return envelope whose {@code errors} list contains {@code code} and {@code message}
	 */
	private static <T> MainResponseDTO<T> errorEnvelope(String code, String message) {
		MainResponseDTO<T> dto = new MainResponseDTO<>();
		dto.setErrors(List.of(new ExceptionJSONInfoDTO(code, message)));
		return dto;
	}
}
