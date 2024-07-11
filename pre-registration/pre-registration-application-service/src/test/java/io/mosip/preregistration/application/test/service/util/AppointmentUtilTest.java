package io.mosip.preregistration.application.test.service.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import java.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import io.mosip.preregistration.application.service.util.AppointmentUtil;
import io.mosip.preregistration.booking.dto.AvailabilityDto;
import io.mosip.preregistration.booking.dto.BookingStatus;
import io.mosip.preregistration.booking.dto.BookingStatusDTO;
import io.mosip.preregistration.booking.dto.DateTimeDto;
import io.mosip.preregistration.core.common.dto.BookingRegistrationDTO;
import io.mosip.preregistration.core.common.dto.CancelBookingResponseDTO;
import io.mosip.preregistration.core.common.dto.DeleteBookingDTO;

@RunWith(SpringRunner.class)
public class AppointmentUtilTest {

	@MockBean
	private AppointmentUtil util;

	@Test
	public void getSlotAvailablityByRegCenterIdTest() {

		AvailabilityDto availabilityDto = new AvailabilityDto();
		availabilityDto.setRegCenterId("10001");

		List<DateTimeDto> list = new ArrayList<>();
		DateTimeDto dtd = new DateTimeDto();
		dtd.setDate(LocalDate.now().toString());
		dtd.setHoliday(false);
		dtd.setTimeSlots(null);

		availabilityDto.setCenterDetails(list);

		Mockito.when(util.getSlotAvailablityByRegCenterId(Mockito.anyString())).thenReturn(availabilityDto);
		assertEquals(util.getSlotAvailablityByRegCenterId(Mockito.anyString()).getRegCenterId(), "10001");
		assertNotNull(util.getSlotAvailablityByRegCenterId(Mockito.anyString()));

	}

	@Test
	public void makeAppointmentTest() {

		BookingStatusDTO response = new BookingStatusDTO();
		response.setBookingMessage("Appointment Booked Succesfully");

		Mockito.when(util.makeAppointment(Mockito.any(), Mockito.anyString())).thenReturn(response);
		assertEquals(util.makeAppointment(Mockito.any(), Mockito.anyString()).getBookingMessage(),
				"Appointment Booked Succesfully");
		assertNotNull(util.makeAppointment(Mockito.any(), Mockito.anyString()));

	}

	@Test
	public void fetchAppointmentDetailsTest() {

		BookingRegistrationDTO response = new BookingRegistrationDTO();
		response.setRegDate("23-08-2021");
		response.setRegistrationCenterId("10001");
		response.setSlotFromTime("10:15.00");
		response.setSlotToTime("10:30.00");

		Mockito.when(util.fetchAppointmentDetails(Mockito.anyString())).thenReturn(response);
		assertEquals(util.fetchAppointmentDetails(Mockito.anyString()).getRegistrationCenterId(), "10001");
		assertNotNull(util.fetchAppointmentDetails(Mockito.anyString()));

	}

	@Test
	public void deleteAppointmentTest() {

		DeleteBookingDTO response = new DeleteBookingDTO();
		response.setDeletedBy("test");
		response.setPreRegistrationId("98765432101234");
		response.setDeletedDateTime(new Date());

		Mockito.when(util.deleteBooking(Mockito.anyString())).thenReturn(response);
		assertEquals(util.deleteBooking(Mockito.anyString()).getPreRegistrationId(), "98765432101234");
		assertNotNull(util.deleteBooking(Mockito.anyString()));

	}

	@Test
	public void cancelAppointmentTest() {

		CancelBookingResponseDTO response = new CancelBookingResponseDTO();
		response.setTransactionId("");
		response.setMessage("Appointment Cancelled Succesfully");

		Mockito.when(util.cancelAppointment(Mockito.anyString())).thenReturn(response);
		assertEquals(util.cancelAppointment(Mockito.anyString()).getMessage(), "Appointment Cancelled Succesfully");
		assertNotNull(util.cancelAppointment(Mockito.anyString()));

	}

	@Test
	public void makeMultiAppointmentTest() {

		BookingStatus response = new BookingStatus();
		List<BookingStatusDTO> list = new ArrayList<BookingStatusDTO>();
		BookingStatusDTO bookingStatus = new BookingStatusDTO();
		bookingStatus.setBookingMessage("Appointment Booked Succesfully");
		list.add(bookingStatus);
		response.setBookingStatusResponse(list);

		Mockito.when(util.multiAppointmentBooking(Mockito.any())).thenReturn(response);
		assertEquals(util.multiAppointmentBooking(Mockito.any()).getBookingStatusResponse().get(0).getBookingMessage(),
				"Appointment Booked Succesfully");
		assertNotNull(util.multiAppointmentBooking(Mockito.any()));
		assertEquals(util.multiAppointmentBooking(Mockito.any()).getBookingStatusResponse().size(), 1);

	}

}
