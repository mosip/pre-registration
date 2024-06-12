package io.mosip.preregistration.application.test.service;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.analytics.event.anonymous.util.AnonymousProfileUtil;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.preregistration.application.repository.ApplicationRepostiory;
import io.mosip.preregistration.application.repository.DocumentDAO;
import io.mosip.preregistration.application.service.AppointmentServiceImpl;
import io.mosip.preregistration.application.service.DemographicService;
import io.mosip.preregistration.application.service.DocumentService;
import io.mosip.preregistration.application.service.util.AppointmentUtil;
import io.mosip.preregistration.booking.dto.AvailabilityDto;
import io.mosip.preregistration.booking.dto.DateTimeDto;
import io.mosip.preregistration.core.common.dto.BookingRegistrationDTO;
import io.mosip.preregistration.core.common.dto.CancelBookingResponseDTO;
import io.mosip.preregistration.core.common.dto.DeleteBookingDTO;
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
	private DemographicService demographicService;
	
	@MockBean
	private DocumentService documentService;
	
	@MockBean
	private AnonymousProfileUtil profileUtil;

	@MockBean
	private ApplicationRepostiory applicationRepostiory;

	@Value("${version}")
	private String version;

	@Value("${mosip.preregistration.booking.cancel.id}")
	private String appointmentCancelId;
	
	@Value("${mosip.preregistration.booking.fetch.availability.id}")
	private String availablityFetchId;

	@Value("${mosip.utc-datetime-pattern:yyyy-MM-dd'T'hh:mm:ss.SSS'Z'}")
	private String mosipDateTimeFormat;
	
	@Value("${mosip.preregistration.booking.delete.id}")
	private String appointmentDeletelId;
	
	@MockBean
	private DocumentDAO documentDAO;
	
	@Before
	public void setup() {
		ReflectionTestUtils.setField(appointmentService, "mosipDateTimeFormat", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		
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
	
		AuthUserDetails applicationUser = Mockito.mock(AuthUserDetails.class);
		Authentication authentication = Mockito.mock(Authentication.class);
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(applicationUser);
	
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
	public void cancelAppointmentTest() {

		String prid = "98765432101234";

		MainResponseDTO<CancelBookingResponseDTO> cancelAppointmentResponse = new MainResponseDTO<CancelBookingResponseDTO>();

		CancelBookingResponseDTO cancelStatus = new CancelBookingResponseDTO();
		cancelStatus.setMessage("Appointment Cancelled Succesfully");
		cancelStatus.setTransactionId("1234");
		cancelAppointmentResponse.setResponse(cancelStatus);
		cancelAppointmentResponse.setVersion(version);
		cancelAppointmentResponse.setId(appointmentCancelId);
		Mockito.when(util.cancelAppointment(prid)).thenReturn(cancelStatus);

		ApplicationEntity applicationEntity = new ApplicationEntity();
		applicationEntity.setApplicationId("98765432101234");
		applicationEntity.setBookingDate(null);
		applicationEntity.setRegistrationCenterId(null);
		applicationEntity.setSlotFromTime(null);
		applicationEntity.setSlotToTime(null);
		applicationEntity.setBookingType("NEW_PREREGISTRATION");
		applicationEntity.setBookingStatusCode("Pending_Appointment");

		Mockito.when(applicationRepostiory.save(applicationEntity)).thenReturn(applicationEntity);
		Mockito.when(applicationRepostiory.getOne("98765432101234")).thenReturn(applicationEntity);
		AuthUserDetails applicationUser = Mockito.mock(AuthUserDetails.class);
		Authentication authentication = Mockito.mock(Authentication.class);
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(applicationUser);
	
		ApplicationEntity appEntity2 = applicationRepostiory.save(applicationEntity);

		assertEquals(appEntity2, applicationEntity);

		MainResponseDTO<CancelBookingResponseDTO> bookingStatusRes = appointmentService.cancelAppointment(prid);
		cancelAppointmentResponse.setResponsetime(bookingStatusRes.getResponsetime());
		
		assertEquals(bookingStatusRes.getResponse(), cancelAppointmentResponse.getResponse());

	}

	@Test
	public void deleteAppointmentTest() {

		String prid = "98765432101234";

		MainResponseDTO<DeleteBookingDTO> deleteAppointmentResponse = new MainResponseDTO<DeleteBookingDTO>();
		DeleteBookingDTO deleteStatus = new DeleteBookingDTO();
		deleteStatus.setDeletedBy("test");
		deleteStatus.setPreRegistrationId("98765432101234");
		deleteStatus.setDeletedDateTime(new Date());
		deleteAppointmentResponse.setResponse(deleteStatus);
		deleteAppointmentResponse.setId(appointmentDeletelId);
		deleteAppointmentResponse.setVersion(version);
		Mockito.when(util.deleteBooking(prid)).thenReturn(deleteStatus);

		ApplicationEntity applicationEntity = new ApplicationEntity();
		applicationEntity.setApplicationId("98765432101234");
		applicationEntity.setBookingDate(null);
		applicationEntity.setRegistrationCenterId(null);
		applicationEntity.setSlotFromTime(null);
		applicationEntity.setSlotToTime(null);
		applicationEntity.setBookingType("NEW_PREREGISTRATION");
		applicationEntity.setBookingStatusCode("Pending_Appointment");

		Mockito.when(applicationRepostiory.save(applicationEntity)).thenReturn(applicationEntity);
		Mockito.when(applicationRepostiory.getOne("98765432101234")).thenReturn(applicationEntity);
		
		AuthUserDetails applicationUser = Mockito.mock(AuthUserDetails.class);
		Authentication authentication = Mockito.mock(Authentication.class);
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(applicationUser);
	
		
		ApplicationEntity appEntity2 = applicationRepostiory.save(applicationEntity);

		assertEquals(appEntity2, applicationEntity);

		MainResponseDTO<DeleteBookingDTO> deleteRes = appointmentService.deleteBooking(prid);
		deleteRes.setResponsetime(null);
		assertEquals(deleteRes.getResponse().getPreRegistrationId(), deleteAppointmentResponse.getResponse().getPreRegistrationId());

	}

}
