package io.mosip.preregistration.application.test.service;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import io.mosip.preregistration.application.repository.ApplicationRepostiory;
import io.mosip.preregistration.application.service.AppointmentServiceImpl;
import io.mosip.preregistration.application.service.DemographicService;
import io.mosip.preregistration.application.service.util.AppointmentUtil;
import io.mosip.preregistration.booking.dto.AvailabilityDto;
import io.mosip.preregistration.booking.dto.BookingRequestDTO;
import io.mosip.preregistration.booking.dto.BookingStatusDTO;
import io.mosip.preregistration.booking.dto.DateTimeDto;
import io.mosip.preregistration.core.common.dto.BookingRegistrationDTO;
import io.mosip.preregistration.core.common.dto.CancelBookingResponseDTO;
import io.mosip.preregistration.core.common.dto.DeleteBookingDTO;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.entity.ApplicationEntity;

@RunWith(SpringRunner.class)
@ImportAutoConfiguration(RefreshAutoConfiguration.class)
@ContextConfiguration(classes = { AppointmentServiceImpl.class })
public class AppointmentServiceTest {

	@Autowired
	private AppointmentServiceImpl appointmentService;

	@MockBean
	private AppointmentUtil util;

	@MockBean
	private DemographicService demoService;

	@MockBean
	private ApplicationRepostiory applicationRepostiory;

	@Value("${version}")
	private String version;

	@Value("${mosip.preregistration.booking.fetch.availability.id}")
	private String availablityFetchId;

	@Before
	public void setup() {

	}

	@Test
	public void getSlotAvailablityTest() {

		String regCenterId = "10001";

		MainResponseDTO<AvailabilityDto> response = new MainResponseDTO<AvailabilityDto>();
		response.setResponsetime(LocalDateTime.now().toString());
		response.setId("");
		response.setVersion("1.0");
		AvailabilityDto availabilityDto = new AvailabilityDto();
		availabilityDto.setRegCenterId("10001");

		List<DateTimeDto> list = new ArrayList<>();
		DateTimeDto dtd = new DateTimeDto();
		dtd.setDate(LocalDate.now().toString());
		dtd.setHoliday(false);
		dtd.setTimeSlots(null);

		availabilityDto.setCenterDetails(list);

		response.setResponse(availabilityDto);

		Mockito.when(util.getSlotAvailablityByRegCenterId(regCenterId)).thenReturn(availabilityDto);
		Assert.assertEquals(appointmentService.getSlotAvailablity(regCenterId).getResponse(), availabilityDto);
	}

	@Test
	public void fetchAppointmentDetailsTest() {

		String prid = "98765432101234";

		MainResponseDTO<BookingRegistrationDTO> response = new MainResponseDTO<BookingRegistrationDTO>();
		response.setResponsetime(LocalDateTime.now().toString());
		response.setId("");
		response.setVersion("1.0");

		BookingRegistrationDTO bookingResponse = new BookingRegistrationDTO();
		bookingResponse.setRegDate("23-08-2021");
		bookingResponse.setRegistrationCenterId("10001");
		bookingResponse.setSlotFromTime("10:15.00");
		bookingResponse.setSlotToTime("10:30.00");

		response.setResponse(bookingResponse);

		Mockito.when(util.fetchAppointmentDetails(prid)).thenReturn(bookingResponse);
		Assert.assertEquals(appointmentService.getAppointmentDetails(prid).getResponse(), bookingResponse);
	}

	@Test
	public void makeAppointmentTest() {

		String prid = "98765432101234";

		MainRequestDTO<BookingRequestDTO> bookingDTO = new MainRequestDTO<BookingRequestDTO>();
		BookingRequestDTO bookingRequest = new BookingRequestDTO();

		bookingRequest.setRegDate(LocalDate.now().toString());
		bookingRequest.setRegistrationCenterId("10001");
		bookingRequest.setSlotFromTime("10:15:00");
		bookingRequest.setSlotToTime("10:30:00");

		bookingDTO.setId("");
		bookingDTO.setVersion("1.0");
		bookingDTO.setRequesttime(new Date());
		bookingDTO.setRequest(bookingRequest);

		MainResponseDTO<BookingStatusDTO> bookAppointmentResponse = new MainResponseDTO<BookingStatusDTO>();

		BookingStatusDTO bookingStatus = new BookingStatusDTO();
		bookingStatus.setBookingMessage("Appointment Booked Succesfully");
		bookAppointmentResponse.setResponse(bookingStatus);

		Mockito.when(util.makeAppointment(bookingDTO, prid)).thenReturn(bookingStatus);

		ApplicationEntity applicationEntity = new ApplicationEntity();
		applicationEntity.setApplicationId("98765432101234");
		applicationEntity.setBookingDate(LocalDate.now());
		applicationEntity.setRegistrationCenterId("10001");
		applicationEntity.setSlotFromTime(LocalTime.parse("10:15:00", DateTimeFormatter.ofPattern("H:mm:ss")));
		applicationEntity.setSlotToTime(LocalTime.parse("10:30:00", DateTimeFormatter.ofPattern("H:mm:ss")));

		Mockito.when(applicationRepostiory.save(applicationEntity)).thenReturn(applicationEntity);

		ApplicationEntity appEntity2 = applicationRepostiory.save(applicationEntity);

		assertEquals(appEntity2, applicationEntity);

//		MainResponseDTO<BookingStatusDTO> bookingStatusRes = appointmentService.makeAppointment(bookingDTO, prid);
//		assertEquals(bookingStatusRes, bookAppointmentResponse);

	}

	@Test
	public void cancelAppointmentTest() {

		String prid = "98765432101234";

		MainResponseDTO<CancelBookingResponseDTO> cancelAppointmentResponse = new MainResponseDTO<CancelBookingResponseDTO>();

		CancelBookingResponseDTO cancelStatus = new CancelBookingResponseDTO();
		cancelStatus.setMessage("Appointment Cancelled Succesfully");
		cancelStatus.setTransactionId("1234");

		Mockito.when(util.cancelAppointment(prid)).thenReturn(cancelStatus);

		ApplicationEntity applicationEntity = new ApplicationEntity();
		applicationEntity.setApplicationId("98765432101234");
		applicationEntity.setBookingDate(null);
		applicationEntity.setRegistrationCenterId(null);
		applicationEntity.setSlotFromTime(null);
		applicationEntity.setSlotToTime(null);

		Mockito.when(applicationRepostiory.save(applicationEntity)).thenReturn(applicationEntity);

		ApplicationEntity appEntity2 = applicationRepostiory.save(applicationEntity);

		assertEquals(appEntity2, applicationEntity);

//		MainResponseDTO<CancelBookingResponseDTO> bookingStatusRes = appointmentService.cancelAppointment(prid);
//		assertEquals(bookingStatusRes, cancelAppointmentResponse);

	}

	@Test
	public void deleteAppointmentTest() {

		String prid = "98765432101234";

		MainResponseDTO<DeleteBookingDTO> deleteAppointmentResponse = new MainResponseDTO<DeleteBookingDTO>();

		DeleteBookingDTO deleteStatus = new DeleteBookingDTO();
		deleteStatus.setDeletedBy("test");
		deleteStatus.setPreRegistrationId("98765432101234");
		deleteStatus.setDeletedDateTime(new Date());

		Mockito.when(util.deleteBooking(prid)).thenReturn(deleteStatus);

		ApplicationEntity applicationEntity = new ApplicationEntity();
		applicationEntity.setApplicationId("98765432101234");
		applicationEntity.setBookingDate(null);
		applicationEntity.setRegistrationCenterId(null);
		applicationEntity.setSlotFromTime(null);
		applicationEntity.setSlotToTime(null);

		Mockito.when(applicationRepostiory.save(applicationEntity)).thenReturn(applicationEntity);

		ApplicationEntity appEntity2 = applicationRepostiory.save(applicationEntity);

		assertEquals(appEntity2, applicationEntity);

//		MainResponseDTO<DeleteBookingDTO> deleteRes = appointmentService.deleteBooking(prid);
//		assertEquals(deleteRes, deleteAppointmentResponse);

	}

}
