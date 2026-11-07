package io.mosip.preregistration.pipeline;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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
import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.preregistration.core.exception.InvalidRequestParameterException;

/**
 * Unit tests for {@link InProcessBookingClient}.
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class InProcessBookingClientTest {

	private static final String PRE_ID = "98765432101234";
	private static final String CENTER_ID = "10001";

	@InjectMocks
	private InProcessBookingClient client;

	@Mock
	private BookingServiceIntf bookingService;

	@Test
	public void getAvailabilityDelegatesToBookingService() {
		MainResponseDTO<AvailabilityDto> expected = new MainResponseDTO<>();
		when(bookingService.getAvailability(CENTER_ID)).thenReturn(expected);

		assertSame(expected, client.getAvailability(CENTER_ID));
		verify(bookingService).getAvailability(CENTER_ID);
	}

	@Test
	public void bookAppointmentMapsUncheckedExceptionToErrorEnvelope() {
		MainRequestDTO<BookingRequestDTO> request = new MainRequestDTO<>();
		when(bookingService.bookAppointment(request, PRE_ID)).thenThrow(
				new InvalidRequestParameterException("PRG_BOOK_RCI_007", "center missing", new MainResponseDTO<>()));

		MainResponseDTO<BookingStatusDTO> actual = client.bookAppointment(request, PRE_ID);

		assertEquals("PRG_BOOK_RCI_007", actual.getErrors().get(0).getErrorCode());
		assertEquals("center missing", actual.getErrors().get(0).getMessage());
	}

	@Test
	public void bookAppointmentMapsCheckedExceptionToErrorEnvelope() {
		MainRequestDTO<BookingRequestDTO> request = new MainRequestDTO<>();
		when(bookingService.bookAppointment(request, PRE_ID)).thenAnswer(invocation -> {
			throw new BaseCheckedException("PRG_BOOK_RCI_032", "no appointment");
		});

		MainResponseDTO<BookingStatusDTO> actual = client.bookAppointment(request, PRE_ID);

		assertEquals("PRG_BOOK_RCI_032", actual.getErrors().get(0).getErrorCode());
		assertEquals("no appointment", actual.getErrors().get(0).getMessage());
	}

	@Test
	public void bookAppointmentDelegatesToBookingService() {
		MainRequestDTO<BookingRequestDTO> request = new MainRequestDTO<>();
		MainResponseDTO<BookingStatusDTO> expected = new MainResponseDTO<>();
		when(bookingService.bookAppointment(request, PRE_ID)).thenReturn(expected);

		assertSame(expected, client.bookAppointment(request, PRE_ID));
		verify(bookingService).bookAppointment(request, PRE_ID);
	}

	@Test
	public void bookMultiAppointmentDelegatesToBookingService() {
		MainRequestDTO<MultiBookingRequest> request = new MainRequestDTO<>();
		MainResponseDTO<BookingStatus> expected = new MainResponseDTO<>();
		when(bookingService.bookMultiAppointment(request)).thenReturn(expected);

		assertSame(expected, client.bookMultiAppointment(request));
		verify(bookingService).bookMultiAppointment(request);
	}

	@Test
	public void getAppointmentDetailsDelegatesToBookingService() {
		MainResponseDTO<BookingRegistrationDTO> expected = new MainResponseDTO<>();
		when(bookingService.getAppointmentDetails(PRE_ID)).thenReturn(expected);

		assertSame(expected, client.getAppointmentDetails(PRE_ID));
		verify(bookingService).getAppointmentDetails(PRE_ID);
	}

	@Test
	public void cancelAppointmentDelegatesToBookingService() {
		MainResponseDTO<CancelBookingResponseDTO> expected = new MainResponseDTO<>();
		when(bookingService.cancelAppointment(PRE_ID)).thenReturn(expected);

		assertSame(expected, client.cancelAppointment(PRE_ID));
		verify(bookingService).cancelAppointment(PRE_ID);
	}

	@Test
	public void deleteBookingDelegatesToBookingService() {
		MainResponseDTO<DeleteBookingDTO> expected = new MainResponseDTO<>();
		when(bookingService.deleteBooking(PRE_ID)).thenReturn(expected);

		assertSame(expected, client.deleteBooking(PRE_ID));
		verify(bookingService).deleteBooking(PRE_ID);
	}

	@Test
	public void getBookedPreRegistrationsDelegatesToBookingService() {
		MainResponseDTO<BookingDataByRegIdDto> expected = new MainResponseDTO<>();
		when(bookingService.getBookedPreRegistrations("2026-12-01", "2026-12-10", CENTER_ID)).thenReturn(expected);

		assertSame(expected, client.getBookedPreRegistrations("2026-12-01", "2026-12-10", CENTER_ID));
		verify(bookingService).getBookedPreRegistrations("2026-12-01", "2026-12-10", CENTER_ID);
	}
}
