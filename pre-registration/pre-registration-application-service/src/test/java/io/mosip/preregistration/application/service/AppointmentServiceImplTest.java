package io.mosip.preregistration.application.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.analytics.event.anonymous.util.AnonymousProfileUtil;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.preregistration.application.repository.ApplicationRepostiory;
import io.mosip.preregistration.application.service.util.AppointmentUtil;
import io.mosip.preregistration.booking.dto.AvailabilityDto;
import io.mosip.preregistration.booking.dto.BookingRequestDTO;
import io.mosip.preregistration.booking.dto.BookingStatus;
import io.mosip.preregistration.booking.dto.BookingStatusDTO;
import io.mosip.preregistration.booking.dto.DateTimeDto;
import io.mosip.preregistration.booking.dto.MultiBookingRequest;
import io.mosip.preregistration.core.common.dto.BookingRegistrationDTO;
import io.mosip.preregistration.core.common.dto.CancelBookingResponseDTO;
import io.mosip.preregistration.core.common.dto.DeleteBookingDTO;
import io.mosip.preregistration.core.common.dto.DemographicResponseDTO;
import io.mosip.preregistration.core.common.dto.DocumentsMetaData;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.entity.ApplicationEntity;
import io.mosip.preregistration.application.errorcodes.AppointmentErrorCodes;
import io.mosip.preregistration.application.exception.AppointmentExecption;

@RunWith(JUnit4.class)
@ImportAutoConfiguration(RefreshAutoConfiguration.class)
@ContextConfiguration(classes = { AppointmentServiceImpl.class })
public class AppointmentServiceImplTest {

	@InjectMocks
	AppointmentServiceImpl appointmentServiceImpl;

	@Mock
	private AppointmentUtil appointmentUtils;

	@Mock
	private DemographicService demographicService;

	@Mock
	private DocumentService documentService;

	/**
	 * Autowired reference for {@link #AnonymousProfileUtil}
	 */
	@Mock
	AnonymousProfileUtil anonymousProfileUtil;

	@Value("${version}")
	private String version;

	@Value("${mosip.utc-datetime-pattern:yyyy-MM-dd'T'hh:mm:ss.SSS'Z'}")
	private String mosipDateTimeFormat;

	@Value("${mosip.preregistration.booking.fetch.availability.id}")
	private String availablityFetchId;

	@Value("${mosip.preregistration.booking.fetch.booking.id}")
	private String appointmentDetailsFetchId;

	@Value("${mosip.preregistration.booking.book.id}")
	private String appointmentBookId;

	@Value("${mosip.preregistration.booking.cancel.id}")
	private String appointmentCancelId;

	@Value("${mosip.preregistration.booking.delete.id}")
	private String appointmentDeletelId;

	@Mock
	private ApplicationRepostiory applicationRepostiory;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		ReflectionTestUtils.setField(appointmentServiceImpl, "mosipDateTimeFormat", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

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

		Mockito.when(appointmentUtils.getSlotAvailablityByRegCenterId(Mockito.any())).thenReturn(availabilityDto);
		MainResponseDTO<AvailabilityDto> obj = appointmentServiceImpl.getSlotAvailablity(regCenterId);
		Assert.assertEquals(obj.getResponse(), availabilityDto);
	}

	@Test
	public void getSlotAvailablityAppointmentExecptionTest() {

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

		Mockito.when(appointmentUtils.getSlotAvailablityByRegCenterId(Mockito.any()))
				.thenThrow(new AppointmentExecption(AppointmentErrorCodes.FAILED_TO_UPDATE_APPLICATIONS.getCode(),
						String.format(AppointmentErrorCodes.FAILED_TO_UPDATE_APPLICATIONS.getMessage(), "")));
		Assert.assertEquals(appointmentServiceImpl.getSlotAvailablity(regCenterId).getErrors().get(0).getErrorCode(),
				AppointmentErrorCodes.FAILED_TO_UPDATE_APPLICATIONS.getCode());
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

		Mockito.when(appointmentUtils.fetchAppointmentDetails(prid)).thenReturn(bookingResponse);
		Assert.assertEquals(appointmentServiceImpl.getAppointmentDetails(prid).getResponse(), bookingResponse);
	}

	@Test
	public void fetchAppointmentDetailsAppointmentExecptionTest() {

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

		Mockito.when(appointmentUtils.fetchAppointmentDetails(prid))
				.thenThrow(new AppointmentExecption(AppointmentErrorCodes.FAILED_TO_UPDATE_APPLICATIONS.getCode(),
						String.format(AppointmentErrorCodes.FAILED_TO_UPDATE_APPLICATIONS.getMessage(), "")));
		Assert.assertEquals(appointmentServiceImpl.getAppointmentDetails(prid).getErrors().get(0).getErrorCode(),
				AppointmentErrorCodes.FAILED_TO_UPDATE_APPLICATIONS.getCode());
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
		Mockito.when(appointmentUtils.cancelAppointment(prid)).thenReturn(cancelStatus);

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

		MainResponseDTO<CancelBookingResponseDTO> bookingStatusRes = appointmentServiceImpl.cancelAppointment(prid);
		cancelAppointmentResponse.setResponsetime(bookingStatusRes.getResponsetime());

		assertEquals(bookingStatusRes.getResponse(), cancelAppointmentResponse.getResponse());

	}

	@Test
	public void cancelAppointmentAppointmentExecptionTest() {

		String prid = "98765432101234";

		MainResponseDTO<CancelBookingResponseDTO> cancelAppointmentResponse = new MainResponseDTO<CancelBookingResponseDTO>();

		CancelBookingResponseDTO cancelStatus = new CancelBookingResponseDTO();
		cancelStatus.setMessage("Appointment Cancelled Succesfully");
		cancelStatus.setTransactionId("1234");
		cancelAppointmentResponse.setResponse(cancelStatus);
		cancelAppointmentResponse.setVersion(version);
		cancelAppointmentResponse.setId(appointmentCancelId);
		Mockito.when(appointmentUtils.cancelAppointment(prid))
				.thenThrow(new AppointmentExecption(AppointmentErrorCodes.FAILED_TO_UPDATE_APPLICATIONS.getCode(),
						String.format(AppointmentErrorCodes.FAILED_TO_UPDATE_APPLICATIONS.getMessage(), "")));
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
		MainResponseDTO<CancelBookingResponseDTO> bookingStatusRes = appointmentServiceImpl.cancelAppointment(prid);
		cancelAppointmentResponse.setResponsetime(bookingStatusRes.getResponsetime());
		assertEquals(bookingStatusRes.getErrors().get(0).getErrorCode(),
				AppointmentErrorCodes.FAILED_TO_UPDATE_APPLICATIONS.getCode());
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
		Mockito.when(appointmentUtils.deleteBooking(prid)).thenReturn(deleteStatus);

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
		MainResponseDTO<DeleteBookingDTO> deleteRes = appointmentServiceImpl.deleteBooking(prid);
		deleteRes.setResponsetime(null);
		assertEquals(deleteRes.getResponse().getPreRegistrationId(),
				deleteAppointmentResponse.getResponse().getPreRegistrationId());

	}

	@Test
	public void deleteAppointmentAppointmentExecptionTest() {

		String prid = "98765432101234";

		MainResponseDTO<DeleteBookingDTO> deleteAppointmentResponse = new MainResponseDTO<DeleteBookingDTO>();
		DeleteBookingDTO deleteStatus = new DeleteBookingDTO();
		deleteStatus.setDeletedBy("test");
		deleteStatus.setPreRegistrationId("98765432101234");
		deleteStatus.setDeletedDateTime(new Date());
		deleteAppointmentResponse.setResponse(deleteStatus);
		deleteAppointmentResponse.setId(appointmentDeletelId);
		deleteAppointmentResponse.setVersion(version);
		Mockito.when(appointmentUtils.deleteBooking(prid))
				.thenThrow(new AppointmentExecption(AppointmentErrorCodes.FAILED_TO_UPDATE_APPLICATIONS.getCode(),
						String.format(AppointmentErrorCodes.FAILED_TO_UPDATE_APPLICATIONS.getMessage(), "")));

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

		MainResponseDTO<DeleteBookingDTO> deleteRes = appointmentServiceImpl.deleteBooking(prid);
		deleteRes.setResponsetime(null);
		assertEquals(deleteRes.getErrors().get(0).getErrorCode(),
				AppointmentErrorCodes.FAILED_TO_UPDATE_APPLICATIONS.getCode());
	}

	@Test
	public void makeAppointmentTest() {
		MainResponseDTO<DemographicResponseDTO> demographicData = new MainResponseDTO<DemographicResponseDTO>();
		MainRequestDTO<BookingRequestDTO> bookingDTO = new MainRequestDTO<BookingRequestDTO>();
		MainResponseDTO<DocumentsMetaData> documentsData = new MainResponseDTO<DocumentsMetaData>();
		String preRegistrationId = "98765432";
		String userAgent = "demo";
		BookingRequestDTO bookreq = new BookingRequestDTO();
		bookreq.setRegistrationCenterId(preRegistrationId);
		bookreq.setRegDate(LocalDate.now().toString());
		bookreq.setSlotFromTime("10:00:00");
		bookreq.setSlotToTime("10:15:00");
		bookingDTO.setRequest(bookreq);
		BookingStatusDTO booked = new BookingStatusDTO();
		Mockito.when(appointmentUtils.makeAppointment(bookingDTO, preRegistrationId)).thenReturn(booked);

		ApplicationEntity applicationEntity = new ApplicationEntity();
		applicationEntity.setApplicationId("98765432");
		applicationEntity.setAppointmentDate(LocalDate.now());
		applicationEntity.setBookingDate(LocalDate.now());
		applicationEntity.setBookingType("NEW_PREREGISTRATION");
		applicationEntity.setBookingStatusCode("PENDING_APPOINTMENT");
		Mockito.when(applicationRepostiory.save(applicationEntity)).thenReturn(applicationEntity);

		BookingStatusDTO bookingResponse = appointmentUtils.makeAppointment(bookingDTO, preRegistrationId);
		bookingResponse.setBookingMessage("Booked");
		AuthUserDetails applicationUser = Mockito.mock(AuthUserDetails.class);
		Authentication authentication = Mockito.mock(Authentication.class);
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(applicationUser);
		Mockito.when(applicationRepostiory.getOne(preRegistrationId)).thenReturn(applicationEntity);
		DemographicResponseDTO demographicresponseData = new DemographicResponseDTO();
		demographicresponseData.setPreRegistrationId(preRegistrationId);
		demographicresponseData.setStatusCode("PENDING_APPOINTMENT");
		demographicData.setResponse(demographicresponseData);
		Mockito.when(demographicService.getDemographicData(Mockito.any(), Mockito.any())).thenReturn(demographicData);
		DocumentsMetaData documentsMetaData = new DocumentsMetaData();
		documentsData.setResponse(documentsMetaData);
		Mockito.when(documentService.getAllDocumentForPreId(preRegistrationId)).thenReturn(documentsData);
		assertNotNull(appointmentServiceImpl.makeAppointment(bookingDTO, preRegistrationId, userAgent));
	}

	@Test
	public void makeMultiAppointmentTest() {
		MainRequestDTO<MultiBookingRequest> bookingRequest = new MainRequestDTO<MultiBookingRequest>();
		MainResponseDTO<BookingStatus> multiBookingResponse = new MainResponseDTO<BookingStatus>();
		List<BookingStatusDTO> bookingStatusResponse = new ArrayList<BookingStatusDTO>();
		MultiBookingRequest req = new MultiBookingRequest();
		String id = "123";
		String userAgent = "demo";
		multiBookingResponse.setId(appointmentBookId);
		multiBookingResponse.setResponsetime(LocalDateTime.now().toString());
		multiBookingResponse.setVersion(version);
		bookingRequest.setId(id);
		bookingRequest.setVersion(version);
		bookingRequest.setRequesttime(Date.from(Instant.now()));
		bookingRequest.setRequest(req);
		BookingStatus bookingStatus = new BookingStatus();
		bookingStatus.setBookingStatusResponse(bookingStatusResponse);
		Mockito.when(appointmentUtils.multiAppointmentBooking(bookingRequest)).thenReturn(bookingStatus);
		assertNotNull(appointmentServiceImpl.makeMultiAppointment(bookingRequest, userAgent));
	}

}
