package io.mosip.preregistration.booking.test.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;

import io.mosip.preregistration.booking.controller.BookingController;
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
import io.mosip.preregistration.core.util.RequestValidator;

@RunWith(MockitoJUnitRunner.class)
public class BookingControllerTest {

	private static final String PRE_ID = "98765432101234";
	private static final String CENTER_ID = "10001";
	private static final String FROM = "2026-12-01";
	private static final String TO = "2026-12-10";

	@Mock
	private BookingServiceIntf bookingService;

	@Mock
	private RequestValidator requestValidator;

	@InjectMocks
	private BookingController controller;

	private Errors errors;

	@Before
	public void setUp() {
		errors = new BeanPropertyBindingResult(new MainRequestDTO<>(), "request");
	}

	@Test
	public void initBinderRegistersRequestValidator() {
		WebDataBinder binder = org.mockito.Mockito.mock(WebDataBinder.class);
		controller.initBinder(binder);
		verify(binder).addValidators(requestValidator);
	}

	@Test
	public void getAvailabilityDelegatesToService() {
		MainResponseDTO<AvailabilityDto> body = new MainResponseDTO<>();
		when(bookingService.getAvailability(CENTER_ID)).thenReturn(body);

		ResponseEntity<MainResponseDTO<AvailabilityDto>> response = controller.getAvailability(CENTER_ID);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertSame(body, response.getBody());
	}

	@Test
	public void bookAppointmentValidatesIdThenDelegates() {
		MainRequestDTO<BookingRequestDTO> request = new MainRequestDTO<>();
		request.setId("book");
		MainResponseDTO<BookingStatusDTO> body = new MainResponseDTO<>();
		when(bookingService.bookAppointment(request, PRE_ID)).thenReturn(body);

		ResponseEntity<MainResponseDTO<BookingStatusDTO>> response = controller.bookAppoinment(PRE_ID, request, errors);

		verify(requestValidator).validateId(eq("book"), eq("book"), any(Errors.class));
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertSame(body, response.getBody());
	}

	@Test
	public void bookMultiAppointmentValidatesIdThenDelegates() {
		MainRequestDTO<MultiBookingRequest> request = new MainRequestDTO<>();
		request.setId("book");
		MainResponseDTO<BookingStatus> body = new MainResponseDTO<>();
		when(bookingService.bookMultiAppointment(request)).thenReturn(body);

		ResponseEntity<MainResponseDTO<BookingStatus>> response = controller.bookMultiAppoinment(request, errors);

		verify(requestValidator).validateId(eq("book"), eq("book"), any(Errors.class));
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertSame(body, response.getBody());
	}

	@Test
	public void getAppointmentsDelegatesToService() {
		MainResponseDTO<BookingRegistrationDTO> body = new MainResponseDTO<>();
		when(bookingService.getAppointmentDetails(PRE_ID)).thenReturn(body);

		ResponseEntity<MainResponseDTO<BookingRegistrationDTO>> response = controller.getAppointments(PRE_ID);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertSame(body, response.getBody());
	}

	@Test
	public void cancelBookDelegatesToService() {
		MainResponseDTO<CancelBookingResponseDTO> body = new MainResponseDTO<>();
		when(bookingService.cancelAppointment(PRE_ID)).thenReturn(body);

		ResponseEntity<MainResponseDTO<CancelBookingResponseDTO>> response = controller.cancelBook(PRE_ID);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertSame(body, response.getBody());
	}

	@Test
	public void cancelAppointmentBatchDelegatesToService() {
		MainResponseDTO<CancelBookingResponseDTO> body = new MainResponseDTO<>();
		when(bookingService.cancelAppointmentBatch(PRE_ID)).thenReturn(body);

		ResponseEntity<MainResponseDTO<CancelBookingResponseDTO>> response = controller.cancelAppointmentBatch(PRE_ID);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertSame(body, response.getBody());
	}

	@Test
	public void discardIndividualDelegatesToService() {
		MainResponseDTO<DeleteBookingDTO> body = new MainResponseDTO<>();
		when(bookingService.deleteBooking(PRE_ID)).thenReturn(body);

		ResponseEntity<MainResponseDTO<DeleteBookingDTO>> response = controller.discardIndividual(PRE_ID);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertSame(body, response.getBody());
	}

	@Test
	public void getBookedDataByDateDelegatesToService() {
		MainResponseDTO<PreRegIdsByRegCenterIdResponseDTO> body = new MainResponseDTO<>();
		when(bookingService.getBookedPreRegistrationByDate(FROM, TO, CENTER_ID)).thenReturn(body);

		ResponseEntity<MainResponseDTO<PreRegIdsByRegCenterIdResponseDTO>> response = controller.getBookedDataByDate(
				FROM, TO, CENTER_ID);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertSame(body, response.getBody());
	}

	@Test
	public void getBookedDataByRegIdDelegatesToService() {
		MainResponseDTO<BookingDataByRegIdDto> body = new MainResponseDTO<>();
		when(bookingService.getBookedPreRegistrations(FROM, TO, CENTER_ID)).thenReturn(body);

		ResponseEntity<MainResponseDTO<BookingDataByRegIdDto>> response = controller.getBookedDataByRegId(FROM, TO,
				CENTER_ID);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertSame(body, response.getBody());
	}
}
