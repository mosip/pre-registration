package io.mosip.preregistration.application.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.mosip.preregistration.application.errorcodes.ApplicationErrorCodes;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.analytics.event.anonymous.util.AnonymousProfileUtil;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.preregistration.application.repository.ApplicationRepostiory;
import io.mosip.preregistration.application.repository.DocumentDAO;
import io.mosip.preregistration.application.service.util.AppointmentUtil;
import io.mosip.preregistration.booking.dto.AvailabilityDto;
import io.mosip.preregistration.booking.dto.BookingRequestDTO;
import io.mosip.preregistration.booking.dto.BookingStatus;
import io.mosip.preregistration.booking.dto.BookingStatusDTO;
import io.mosip.preregistration.booking.dto.DateTimeDto;
import io.mosip.preregistration.booking.dto.MultiBookingRequest;
import io.mosip.preregistration.booking.dto.MultiBookingRequestDTO;
import io.mosip.preregistration.core.code.BookingTypeCodes;
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
	
	@Mock
	private DocumentDAO documentDAO;

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

		when(appointmentUtils.getSlotAvailablityByRegCenterId(Mockito.any())).thenReturn(availabilityDto);
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

		when(appointmentUtils.getSlotAvailablityByRegCenterId(Mockito.any()))
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

		when(appointmentUtils.fetchAppointmentDetails(prid)).thenReturn(bookingResponse);
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

		when(appointmentUtils.fetchAppointmentDetails(prid))
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
		when(appointmentUtils.cancelAppointment(prid)).thenReturn(cancelStatus);

		ApplicationEntity applicationEntity = new ApplicationEntity();
		applicationEntity.setApplicationId("98765432101234");
		applicationEntity.setBookingDate(null);
		applicationEntity.setRegistrationCenterId(null);
		applicationEntity.setSlotFromTime(null);
		applicationEntity.setSlotToTime(null);
		applicationEntity.setBookingType("NEW_PREREGISTRATION");
		applicationEntity.setBookingStatusCode("Pending_Appointment");

		when(applicationRepostiory.save(applicationEntity)).thenReturn(applicationEntity);
		when(applicationRepostiory.getOne("98765432101234")).thenReturn(applicationEntity);
		AuthUserDetails applicationUser = mock(AuthUserDetails.class);
		Authentication authentication = mock(Authentication.class);
		SecurityContext securityContext = mock(SecurityContext.class);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(applicationUser);

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
		when(appointmentUtils.cancelAppointment(prid))
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

		when(applicationRepostiory.save(applicationEntity)).thenReturn(applicationEntity);
		when(applicationRepostiory.getOne("98765432101234")).thenReturn(applicationEntity);
		AuthUserDetails applicationUser = mock(AuthUserDetails.class);
		Authentication authentication = mock(Authentication.class);
		SecurityContext securityContext = mock(SecurityContext.class);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(applicationUser);
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
		when(appointmentUtils.deleteBooking(prid)).thenReturn(deleteStatus);

		ApplicationEntity applicationEntity = new ApplicationEntity();
		applicationEntity.setApplicationId("98765432101234");
		applicationEntity.setBookingDate(null);
		applicationEntity.setRegistrationCenterId(null);
		applicationEntity.setSlotFromTime(null);
		applicationEntity.setSlotToTime(null);
		applicationEntity.setBookingType("NEW_PREREGISTRATION");
		applicationEntity.setBookingStatusCode("Pending_Appointment");

		when(applicationRepostiory.save(applicationEntity)).thenReturn(applicationEntity);
		when(applicationRepostiory.getOne("98765432101234")).thenReturn(applicationEntity);

		AuthUserDetails applicationUser = mock(AuthUserDetails.class);
		Authentication authentication = mock(Authentication.class);
		SecurityContext securityContext = mock(SecurityContext.class);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(applicationUser);
		ApplicationEntity appEntity2 = applicationRepostiory.save(applicationEntity);
		assertEquals(appEntity2, applicationEntity);
		MainResponseDTO<DeleteBookingDTO> deleteRes = appointmentServiceImpl.deleteBooking(prid);
		deleteRes.setResponsetime(null);
		assertEquals(deleteRes.getResponse().getPreRegistrationId(),
				deleteAppointmentResponse.getResponse().getPreRegistrationId());

	}

	@Test
	public void deleteBookingAndUpdateApplicationStatus() {
		String prid = "98765432101234";
		MainResponseDTO<DeleteBookingDTO> deleteAppointmentResponse = new MainResponseDTO<DeleteBookingDTO>();
		DeleteBookingDTO deleteStatus = new DeleteBookingDTO();
		deleteStatus.setDeletedBy("test");
		deleteStatus.setPreRegistrationId("98765432101234");
		deleteStatus.setDeletedDateTime(new Date());
		deleteAppointmentResponse.setResponse(deleteStatus);
		deleteAppointmentResponse.setId(appointmentDeletelId);
		deleteAppointmentResponse.setVersion(version);
		when(appointmentUtils.deleteBooking(prid)).thenReturn(deleteStatus);
		ApplicationEntity applicationEntity = new ApplicationEntity();
		applicationEntity.setApplicationId("98765432101234");
		applicationEntity.setBookingDate(null);
		applicationEntity.setRegistrationCenterId(null);
		applicationEntity.setSlotFromTime(null);
		applicationEntity.setSlotToTime(null);
		applicationEntity.setBookingType("NEW_PREREGISTRATION");
		applicationEntity.setBookingStatusCode("Pending_Appointment");

		when(applicationRepostiory.save(applicationEntity)).thenReturn(applicationEntity);
		when(applicationRepostiory.getOne("98765432101234")).thenReturn(applicationEntity);

		AuthUserDetails applicationUser = mock(AuthUserDetails.class);
		Authentication authentication = mock(Authentication.class);
		SecurityContext securityContext = mock(SecurityContext.class);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(applicationUser);
		ApplicationEntity appEntity2 = applicationRepostiory.save(applicationEntity);
		assertEquals(appEntity2, applicationEntity);
		MainResponseDTO<DeleteBookingDTO> deleteRes = appointmentServiceImpl
				.deleteBookingAndUpdateApplicationStatus(prid);
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
		when(appointmentUtils.deleteBooking(prid))
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

		when(applicationRepostiory.save(applicationEntity)).thenReturn(applicationEntity);
		when(applicationRepostiory.getOne("98765432101234")).thenReturn(applicationEntity);

		AuthUserDetails applicationUser = mock(AuthUserDetails.class);
		Authentication authentication = mock(Authentication.class);
		SecurityContext securityContext = mock(SecurityContext.class);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(applicationUser);

		ApplicationEntity appEntity2 = applicationRepostiory.save(applicationEntity);

		assertEquals(appEntity2, applicationEntity);

		MainResponseDTO<DeleteBookingDTO> deleteRes = appointmentServiceImpl.deleteBooking(prid);
		deleteRes.setResponsetime(null);
		assertEquals(deleteRes.getErrors().get(0).getErrorCode(),
				AppointmentErrorCodes.FAILED_TO_UPDATE_APPLICATIONS.getCode());
	}

	@Test
	public void deleteBookingAndUpdateApplicationExecptionTest() {

		String prid = "98765432101234";

		MainResponseDTO<DeleteBookingDTO> deleteAppointmentResponse = new MainResponseDTO<DeleteBookingDTO>();
		DeleteBookingDTO deleteStatus = new DeleteBookingDTO();
		deleteStatus.setDeletedBy("test");
		deleteStatus.setPreRegistrationId("98765432101234");
		deleteStatus.setDeletedDateTime(new Date());
		deleteAppointmentResponse.setResponse(deleteStatus);
		deleteAppointmentResponse.setId(appointmentDeletelId);
		deleteAppointmentResponse.setVersion(version);
		when(appointmentUtils.deleteBooking(prid))
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

		when(applicationRepostiory.save(applicationEntity)).thenReturn(applicationEntity);
		when(applicationRepostiory.getOne("98765432101234")).thenReturn(applicationEntity);

		AuthUserDetails applicationUser = mock(AuthUserDetails.class);
		Authentication authentication = mock(Authentication.class);
		SecurityContext securityContext = mock(SecurityContext.class);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(applicationUser);

		ApplicationEntity appEntity2 = applicationRepostiory.save(applicationEntity);

		assertEquals(appEntity2, applicationEntity);

		MainResponseDTO<DeleteBookingDTO> deleteRes = appointmentServiceImpl
				.deleteBookingAndUpdateApplicationStatus(prid);
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
		when(appointmentUtils.makeAppointment(bookingDTO, preRegistrationId)).thenReturn(booked);
		Boolean documentExists = true;
		when(documentDAO.existsByPreregId(preRegistrationId)).thenReturn(documentExists);
		ApplicationEntity applicationEntity = new ApplicationEntity();
		applicationEntity.setApplicationId("98765432");
		applicationEntity.setAppointmentDate(LocalDate.now());
		applicationEntity.setBookingDate(LocalDate.now());
		applicationEntity.setBookingType("NEW_PREREGISTRATION");
		applicationEntity.setBookingStatusCode("PENDING_APPOINTMENT");
		when(applicationRepostiory.save(applicationEntity)).thenReturn(applicationEntity);

		BookingStatusDTO bookingResponse = appointmentUtils.makeAppointment(bookingDTO, preRegistrationId);
		bookingResponse.setBookingMessage("Booked");
		AuthUserDetails applicationUser = mock(AuthUserDetails.class);
		Authentication authentication = mock(Authentication.class);
		SecurityContext securityContext = mock(SecurityContext.class);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(applicationUser);
		when(applicationRepostiory.getOne(preRegistrationId)).thenReturn(applicationEntity);
		DemographicResponseDTO demographicresponseData = new DemographicResponseDTO();
		demographicresponseData.setPreRegistrationId(preRegistrationId);
		demographicresponseData.setStatusCode("PENDING_APPOINTMENT");
		demographicData.setResponse(demographicresponseData);
		when(demographicService.getDemographicData(Mockito.any())).thenReturn(demographicData);
		DocumentsMetaData documentsMetaData = new DocumentsMetaData();
		documentsData.setResponse(documentsMetaData);
		when(documentService.getAllDocumentForPreId(preRegistrationId)).thenReturn(documentsData);
		assertNotNull(appointmentServiceImpl.makeAppointment(bookingDTO, preRegistrationId, userAgent));
	}

	@Test
	public void makeMultiAppointmentTest() {
		MainRequestDTO<MultiBookingRequest> bookingRequest = new MainRequestDTO<MultiBookingRequest>();
		MainResponseDTO<BookingStatus> multiBookingResponse = new MainResponseDTO<BookingStatus>();
		List<BookingStatusDTO> bookingStatusResponse = new ArrayList<BookingStatusDTO>();
		BookingStatusDTO bookingStatusDto = new BookingStatusDTO();
		bookingStatusResponse.add(bookingStatusDto);
		MultiBookingRequest req = new MultiBookingRequest();
		List<MultiBookingRequestDTO> list = new ArrayList<>();
		MultiBookingRequestDTO item = new MultiBookingRequestDTO();
		item.setPreRegistrationId("98765432");
		item.setRegistrationCenterId("10001");
		item.setRegDate(LocalDate.now().toString());
		item.setSlotFromTime("10:00:00");
		item.setSlotToTime("10:15:00");
		list.add(item);
		req.setBookingRequest(list);
		String id = "123";
		String userAgent = "demo";
		AuthUserDetails applicationUser = mock(AuthUserDetails.class);
		Authentication authentication = mock(Authentication.class);
		SecurityContext securityContext = mock(SecurityContext.class);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(applicationUser);

		ApplicationEntity applicationEntity = new ApplicationEntity();
		applicationEntity.setApplicationId("98765432");
		applicationEntity.setAppointmentDate(LocalDate.now());
		applicationEntity.setBookingDate(LocalDate.now());
		applicationEntity.setBookingType(BookingTypeCodes.UPDATE_REGISTRATION.toString());
		applicationEntity.setBookingStatusCode("PENDING_APPOINTMENT");
		when(applicationRepostiory.getOne(Mockito.any())).thenReturn(applicationEntity);
		when(applicationRepostiory.save(applicationEntity)).thenReturn(applicationEntity);

		multiBookingResponse.setResponsetime(LocalDateTime.now().toString());
		multiBookingResponse.setVersion(version);
		bookingRequest.setId(id);
		bookingRequest.setVersion(version);
		bookingRequest.setRequesttime(Date.from(Instant.now()));
		bookingRequest.setRequest(req);
		bookingRequest.setRequest(req);
		BookingStatus bookingStatus = new BookingStatus();
		bookingStatus.setBookingStatusResponse(bookingStatusResponse);
		when(appointmentUtils.multiAppointmentBooking(bookingRequest)).thenReturn(bookingStatus);
		assertNotNull(appointmentServiceImpl.makeMultiAppointment(bookingRequest, userAgent));
	}

	@Test
	public void test_null_application_id_throws_appointment_exception() {
		AppointmentServiceImpl appointmentService = new AppointmentServiceImpl();
		String applicationId = null;

		AppointmentExecption exception = assertThrows(AppointmentExecption.class,
				() -> ReflectionTestUtils.invokeMethod(appointmentService, "userValidation", applicationId));

		assertEquals(ApplicationErrorCodes.PRG_APP_013.getCode(), exception.getErrorCode());
		assertEquals("preRegistrationId cannot be empty.", exception.getErrorMessage());
	}

	@Test
	public void test_convert_granted_authorities_to_string_list() {
		AppointmentServiceImpl appointmentService = new AppointmentServiceImpl();
		List<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
		authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

		List<String> result = ReflectionTestUtils.invokeMethod(appointmentService, "listAuth", authorities);

		assertEquals(2, result.size());
		assertEquals("ROLE_ADMIN", result.get(0));
		assertEquals("ROLE_USER", result.get(1));
	}

	@Test
	public void test_handle_null_authority_strings() {
		AppointmentServiceImpl appointmentService = new AppointmentServiceImpl();
		List<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
		authorities.add(new GrantedAuthority() {
			@Override
			public String getAuthority() {
				return null;
			}
		});
		authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

		List<String> result = ReflectionTestUtils.invokeMethod(appointmentService, "listAuth", authorities);

		assertEquals(3, result.size());
		assertEquals("ROLE_ADMIN", result.get(0));
		assertNull(result.get(1));
		assertEquals("ROLE_USER", result.get(2));
	}

}
