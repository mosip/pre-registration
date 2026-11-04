package io.mosip.preregistration.booking.test.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.preregistration.booking.dto.AvailabilityDto;
import io.mosip.preregistration.booking.dto.BookingDataByRegIdDto;
import io.mosip.preregistration.booking.dto.BookingRequestDTO;
import io.mosip.preregistration.booking.dto.BookingStatus;
import io.mosip.preregistration.booking.dto.BookingStatusDTO;
import io.mosip.preregistration.booking.dto.MultiBookingRequest;
import io.mosip.preregistration.booking.dto.MultiBookingRequestDTO;
import io.mosip.preregistration.booking.dto.RegistrationCenterDto;
import io.mosip.preregistration.booking.entity.AvailibityEntity;
import io.mosip.preregistration.booking.errorcodes.ErrorCodes;
import io.mosip.preregistration.booking.errorcodes.ErrorMessages;
import io.mosip.preregistration.booking.exception.AvailablityNotFoundException;
import io.mosip.preregistration.booking.exception.DemographicGetStatusException;
import io.mosip.preregistration.booking.exception.RecordNotFoundException;
import io.mosip.preregistration.booking.repository.impl.BookingDAO;
import io.mosip.preregistration.booking.service.BookingService;
import io.mosip.preregistration.booking.service.util.BookingLock;
import io.mosip.preregistration.booking.service.util.BookingServiceUtil;
import io.mosip.preregistration.core.code.StatusCodes;
import io.mosip.preregistration.core.common.dto.BookingRegistrationDTO;
import io.mosip.preregistration.core.common.dto.CancelBookingResponseDTO;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.dto.PreRegIdsByRegCenterIdResponseDTO;
import io.mosip.preregistration.core.common.entity.RegistrationBookingEntity;
import io.mosip.preregistration.core.common.service.ApplicationIdentityMigrationService;
import io.mosip.preregistration.core.exception.PreRegistrationException;
import io.mosip.preregistration.core.util.AuditLogUtil;
import io.mosip.preregistration.core.util.ValidationUtil;

@RunWith(MockitoJUnitRunner.Silent.class)
public class BookingServiceCoverageTest {

	private static final String PRE_ID = "98765432101234";
	private static final String CENTER_ID = "10001";
	private static final String REG_DATE = "2026-12-10";
	private static final String FROM = "09:00";
	private static final String TO = "09:15";

	@Mock
	private BookingDAO bookingDAO;

	@Mock
	private BookingServiceUtil serviceUtil;

	@Mock
	private ApplicationIdentityMigrationService applicationIdentityMigrationService;

	@Mock
	private ValidationUtil validationUtil;

	@Mock
	private AuditLogUtil auditLogUtil;

	@Spy
	private BookingLock bookingLock = new BookingLock();

	@InjectMocks
	private BookingService bookingService;

	private BookingRequestDTO bookingRequestDTO;
	private AvailibityEntity availabilityEntity;
	private RegistrationBookingEntity bookingEntity;
	private AuthUserDetails principal;

	@Before
	public void setUp() {
		ReflectionTestUtils.setField(bookingService, "syncDays", 7);
		ReflectionTestUtils.setField(bookingService, "displayDays", 7L);
		ReflectionTestUtils.setField(bookingService, "availabilityOffset", 2);
		ReflectionTestUtils.setField(bookingService, "versionUrl", "1.0");
		ReflectionTestUtils.setField(bookingService, "idUrlSync", "sync");
		ReflectionTestUtils.setField(bookingService, "idUrlBookAppointment", "book");
		ReflectionTestUtils.setField(bookingService, "idUrlFetch", "fetch");
		ReflectionTestUtils.setField(bookingService, "idUrlCancel", "cancel");
		ReflectionTestUtils.setField(bookingService, "idUrlDelete", "delete");
		ReflectionTestUtils.setField(bookingService, "idUrlAvailability", "availability");
		ReflectionTestUtils.setField(bookingService, "idUrlBookingByDate", "booking-by-date");
		ReflectionTestUtils.setField(bookingService, "idUrlIncreaseAvailability", "increase");
		ReflectionTestUtils.setField(bookingService, "idUrlCheckSlotAvailability", "check-slot");
		ReflectionTestUtils.setField(bookingService, "idUrlDeleteOld", "delete-old");

		principal = org.mockito.Mockito.mock(AuthUserDetails.class);
		when(principal.getUserId()).thenReturn("user-1");
		when(principal.getUsername()).thenReturn("user-1");
		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken(principal, null));

		when(serviceUtil.getCurrentResponseTime()).thenReturn("2026-09-29T00:00:00.000Z");

		bookingRequestDTO = new BookingRequestDTO();
		bookingRequestDTO.setRegistrationCenterId(CENTER_ID);
		bookingRequestDTO.setRegDate(REG_DATE);
		bookingRequestDTO.setSlotFromTime(FROM);
		bookingRequestDTO.setSlotToTime(TO);

		availabilityEntity = new AvailibityEntity();
		availabilityEntity.setRegcntrId(CENTER_ID);
		availabilityEntity.setRegDate(LocalDate.parse(REG_DATE));
		availabilityEntity.setFromTime(LocalTime.parse(FROM));
		availabilityEntity.setToTime(LocalTime.parse(TO));
		availabilityEntity.setAvailableKiosks(5);

		bookingEntity = new RegistrationBookingEntity();
		bookingEntity.setCrBy("creator");
		bookingEntity.setPreregistrationId(PRE_ID);
		bookingEntity.setRegistrationCenterId(CENTER_ID);
		bookingEntity.setRegDate(LocalDate.parse(REG_DATE));
		bookingEntity.setSlotFromTime(LocalTime.parse(FROM));
		bookingEntity.setSlotToTime(LocalTime.parse(TO));
	}

	@After
	public void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void setupBookingServiceIsNoOp() {
		bookingService.setupBookingService();
	}

	@Test
	public void authUserDetailsReturnsPrincipal() {
		assertEquals(principal, bookingService.authUserDetails());
	}

	@Test
	public void getAvailabilityReturnsSlots() {
		when(serviceUtil.isValidRegCenter(CENTER_ID)).thenReturn(true);
		when(bookingDAO.findAvailability(eq(CENTER_ID), any(LocalDate.class), any(LocalDate.class)))
				.thenReturn(List.of(availabilityEntity));
		when(serviceUtil.slotSetter(any(), any(), any(), any())).thenReturn(0);

		MainResponseDTO<AvailabilityDto> response = bookingService.getAvailability(CENTER_ID);

		assertEquals(CENTER_ID, response.getResponse().getRegCenterId());
		assertEquals("availability", response.getId());
		verify(auditLogUtil).saveAuditDetails(any());
	}

	@Test
	public void getAvailabilityHolidayLookaheadThenEmpty() {
		when(serviceUtil.isValidRegCenter(CENTER_ID)).thenReturn(true);
		when(bookingDAO.findAvailability(eq(CENTER_ID), any(LocalDate.class), any(LocalDate.class)))
				.thenReturn(List.of(availabilityEntity)).thenReturn(null);
		when(serviceUtil.slotSetter(any(), any(), any(), any())).thenReturn(2);

		MainResponseDTO<AvailabilityDto> response = bookingService.getAvailability(CENTER_ID);

		assertEquals(CENTER_ID, response.getResponse().getRegCenterId());
		verify(bookingDAO, org.mockito.Mockito.times(2)).findAvailability(eq(CENTER_ID), any(LocalDate.class),
				any(LocalDate.class));
	}

	@Test
	public void getAvailabilityHolidayLookaheadFindsMoreSlots() {
		AvailibityEntity nextDay = new AvailibityEntity();
		nextDay.setRegcntrId(CENTER_ID);
		nextDay.setRegDate(LocalDate.parse("2026-12-11"));
		nextDay.setFromTime(LocalTime.parse(FROM));
		nextDay.setToTime(LocalTime.parse(TO));
		nextDay.setAvailableKiosks(3);

		when(serviceUtil.isValidRegCenter(CENTER_ID)).thenReturn(true);
		when(bookingDAO.findAvailability(eq(CENTER_ID), any(LocalDate.class), any(LocalDate.class)))
				.thenReturn(List.of(availabilityEntity)).thenReturn(List.of(nextDay));
		when(serviceUtil.slotSetter(any(), any(), any(), any())).thenReturn(1).thenReturn(0);

		MainResponseDTO<AvailabilityDto> response = bookingService.getAvailability(CENTER_ID);

		assertNotNull(response.getResponse().getCenterDetails());
	}

	@Test(expected = RecordNotFoundException.class)
	public void getAvailabilityThrowsWhenNoSlots() {
		when(serviceUtil.isValidRegCenter(CENTER_ID)).thenReturn(true);
		when(bookingDAO.findAvailability(eq(CENTER_ID), any(LocalDate.class), any(LocalDate.class))).thenReturn(null);

		bookingService.getAvailability(CENTER_ID);
	}

	@Test
	public void bookAppointmentPendingAppointmentCreatesBooking() {
		stubSuccessfulBookPath(StatusCodes.PENDING_APPOINTMENT.getCode());

		MainResponseDTO<BookingStatusDTO> response = bookingService.bookAppointment(mainBookingRequest(), PRE_ID);

		assertEquals("Appointment booked successfully", response.getResponse().getBookingMessage());
		verify(bookingDAO).saveRegistrationEntityForBooking(any());
	}

	@Test
	public void bookAppointmentCancelledCreatesBooking() {
		stubSuccessfulBookPath(StatusCodes.CANCELLED.getCode());

		MainResponseDTO<BookingStatusDTO> response = bookingService.bookAppointment(mainBookingRequest(), PRE_ID);

		assertEquals("Appointment booked successfully", response.getResponse().getBookingMessage());
	}

	@Test
	public void bookAppointmentBookedRebooks() {
		stubSuccessfulBookPath(StatusCodes.BOOKED.getCode());
		when(bookingDAO.findByPreRegistrationId(PRE_ID)).thenReturn(bookingEntity);
		when(bookingDAO.deleteByPreRegistrationId(PRE_ID)).thenReturn(1);
		when(bookingDAO.findByRegDateAndRegcntrIdAndFromTimeAndToTime(any(), anyString(), any(), any()))
				.thenReturn(availabilityEntity);
		when(serviceUtil.timeSpanCheckForRebook(any(), any())).thenReturn(true);

		MainResponseDTO<BookingStatusDTO> response = bookingService.bookAppointment(mainBookingRequest(), PRE_ID);

		assertEquals("Appointment booked successfully", response.getResponse().getBookingMessage());
		verify(bookingDAO, org.mockito.Mockito.atLeastOnce()).deleteByPreRegistrationId(PRE_ID);
	}

	@Test
	public void bookAppointmentExpiredDeletesThenBooks() {
		stubSuccessfulBookPath(StatusCodes.EXPIRED.getCode());
		when(bookingDAO.deleteByPreRegistrationId(PRE_ID)).thenReturn(1);

		MainResponseDTO<BookingStatusDTO> response = bookingService.bookAppointment(mainBookingRequest(), PRE_ID);

		assertEquals("Appointment booked successfully", response.getResponse().getBookingMessage());
	}

	@Test(expected = DemographicGetStatusException.class)
	public void bookAppointmentRejectsIncompleteApplication() {
		when(serviceUtil.validateAppointmentDate(anyMap())).thenReturn(true);
		when(serviceUtil.getApplicationBookingStatus(PRE_ID))
				.thenReturn(StatusCodes.APPLICATION_INCOMPLETE.getCode());

		bookingService.bookAppointment(mainBookingRequest(), PRE_ID);
	}

	@Test(expected = DemographicGetStatusException.class)
	public void bookAppointmentRejectsPrefetchedApplication() {
		when(serviceUtil.validateAppointmentDate(anyMap())).thenReturn(true);
		when(serviceUtil.getApplicationBookingStatus(PRE_ID)).thenReturn(StatusCodes.PREFETCHED.getCode());

		bookingService.bookAppointment(mainBookingRequest(), PRE_ID);
	}

	@Test
	public void bookAppointmentSkipsWhenDateInvalid() {
		when(serviceUtil.validateAppointmentDate(anyMap())).thenReturn(false);

		MainResponseDTO<BookingStatusDTO> response = bookingService.bookAppointment(mainBookingRequest(), PRE_ID);

		verify(bookingDAO, never()).saveRegistrationEntityForBooking(any());
		assertNotNull(response);
	}

	@Test
	public void bookMultiAppointmentPendingAppointment() {
		stubSuccessfulBookPath(StatusCodes.PENDING_APPOINTMENT.getCode());

		MainResponseDTO<BookingStatus> response = bookingService.bookMultiAppointment(multiBookingRequest());

		assertEquals(1, response.getResponse().getBookingStatusResponse().size());
	}

	@Test
	public void bookMultiAppointmentBookedRebooks() {
		stubSuccessfulBookPath(StatusCodes.BOOKED.getCode());
		when(bookingDAO.findByPreRegistrationId(PRE_ID)).thenReturn(bookingEntity);
		when(bookingDAO.deleteByPreRegistrationId(PRE_ID)).thenReturn(1);
		when(bookingDAO.findByRegDateAndRegcntrIdAndFromTimeAndToTime(any(), anyString(), any(), any()))
				.thenReturn(availabilityEntity);
		when(serviceUtil.timeSpanCheckForRebook(any(), any())).thenReturn(true);

		MainResponseDTO<BookingStatus> response = bookingService.bookMultiAppointment(multiBookingRequest());

		assertEquals(1, response.getResponse().getBookingStatusResponse().size());
	}

	@Test
	public void bookMultiAppointmentExpiredDeletesThenBooks() {
		stubSuccessfulBookPath(StatusCodes.EXPIRED.getCode());
		when(bookingDAO.deleteByPreRegistrationId(PRE_ID)).thenReturn(1);

		MainResponseDTO<BookingStatus> response = bookingService.bookMultiAppointment(multiBookingRequest());

		assertEquals(1, response.getResponse().getBookingStatusResponse().size());
	}

	@Test
	public void bookMultiAppointmentCancelledCreatesBooking() {
		stubSuccessfulBookPath(StatusCodes.CANCELLED.getCode());

		MainResponseDTO<BookingStatus> response = bookingService.bookMultiAppointment(multiBookingRequest());

		assertEquals(1, response.getResponse().getBookingStatusResponse().size());
	}

	@Test(expected = DemographicGetStatusException.class)
	public void bookMultiAppointmentRejectsIncomplete() {
		when(serviceUtil.validateAppointmentDate(anyMap())).thenReturn(true);
		when(serviceUtil.getApplicationBookingStatus(PRE_ID))
				.thenReturn(StatusCodes.APPLICATION_INCOMPLETE.getCode());

		bookingService.bookMultiAppointment(multiBookingRequest());
	}

	@Test(expected = DemographicGetStatusException.class)
	public void bookMultiAppointmentRejectsPrefetched() {
		when(serviceUtil.validateAppointmentDate(anyMap())).thenReturn(true);
		when(serviceUtil.getApplicationBookingStatus(PRE_ID)).thenReturn(StatusCodes.PREFETCHED.getCode());

		bookingService.bookMultiAppointment(multiBookingRequest());
	}

	@Test
	public void getAppointmentDetailsReturnsSlot() {
		when(serviceUtil.checkApplicationStatus(PRE_ID)).thenReturn(true);
		when(bookingDAO.findByPreRegistrationId(PRE_ID)).thenReturn(bookingEntity);

		MainResponseDTO<BookingRegistrationDTO> response = bookingService.getAppointmentDetails(PRE_ID);

		assertEquals(CENTER_ID, response.getResponse().getRegistrationCenterId());
		assertEquals(REG_DATE, response.getResponse().getRegDate());
	}

	@Test(expected = PreRegistrationException.class)
	public void getAppointmentDetailsRejectsBlankId() {
		bookingService.getAppointmentDetails("  ");
	}

	@Test(expected = PreRegistrationException.class)
	public void getAppointmentDetailsRejectsNullId() {
		bookingService.getAppointmentDetails(null);
	}

	@Test
	public void cancelAppointmentWrapsCancelBooking() {
		stubCancelSuccess(false);

		MainResponseDTO<CancelBookingResponseDTO> response = bookingService.cancelAppointment(PRE_ID);

		assertTrue(response.getResponse().getMessage().contains("cancelled"));
		assertEquals("cancel", response.getId());
	}

	@Test
	public void cancelAppointmentBatchSkipsTimeSpanCheck() {
		stubCancelSuccess(true);

		MainResponseDTO<CancelBookingResponseDTO> response = bookingService.cancelAppointmentBatch(PRE_ID);

		assertTrue(response.getResponse().getMessage().contains("cancelled"));
		verify(serviceUtil, never()).timeSpanCheckForCancle(any());
	}

	@Test
	public void checkSlotAvailabilitySucceedsWhenKioskOpen() {
		RegistrationCenterDto center = new RegistrationCenterDto();
		center.setId(CENTER_ID);
		when(serviceUtil.getRegCenterMasterData(CENTER_ID)).thenReturn(List.of(center));
		when(bookingDAO.findRegistrationCenterId(CENTER_ID)).thenReturn(true);
		when(bookingDAO.findByRegDateAndRegcntrIdAndFromTimeAndToTime(any(), anyString(), any(), any()))
				.thenReturn(availabilityEntity);

		bookingService.checkSlotAvailability(bookingRequestDTO);

		verify(bookingDAO).findRegistrationCenterId(CENTER_ID);
	}

	@Test(expected = RecordNotFoundException.class)
	public void checkSlotAvailabilityRejectsUnknownCenter() {
		RegistrationCenterDto other = new RegistrationCenterDto();
		other.setId("99999");
		when(serviceUtil.getRegCenterMasterData(CENTER_ID)).thenReturn(List.of(other));

		bookingService.checkSlotAvailability(bookingRequestDTO);
	}

	@Test(expected = AvailablityNotFoundException.class)
	public void checkSlotAvailabilityRejectsEmptySlot() {
		RegistrationCenterDto center = new RegistrationCenterDto();
		center.setId(CENTER_ID);
		availabilityEntity.setAvailableKiosks(0);
		when(serviceUtil.getRegCenterMasterData(CENTER_ID)).thenReturn(List.of(center));
		when(bookingDAO.findRegistrationCenterId(CENTER_ID)).thenReturn(true);
		when(bookingDAO.findByRegDateAndRegcntrIdAndFromTimeAndToTime(any(), anyString(), any(), any()))
				.thenReturn(availabilityEntity);

		bookingService.checkSlotAvailability(bookingRequestDTO);
	}

	@Test
	public void deleteOldBookingReturnsTrueWhenRowsDeleted() {
		when(bookingDAO.deleteByPreRegistrationId(PRE_ID)).thenReturn(2);

		assertTrue(bookingService.deleteOldBooking(PRE_ID));
	}

	@Test
	public void deleteOldBookingReturnsFalseWhenNothingDeleted() {
		when(bookingDAO.deleteByPreRegistrationId(PRE_ID)).thenReturn(0);

		assertFalse(bookingService.deleteOldBooking(PRE_ID));
	}

	@Test(expected = RecordNotFoundException.class)
	public void deleteOldBookingRethrowsMappedException() {
		when(bookingDAO.deleteByPreRegistrationId(PRE_ID))
				.thenThrow(new RecordNotFoundException(ErrorCodes.PRG_BOOK_RCI_015.getCode(),
						ErrorMessages.NO_TIME_SLOTS_ASSIGNED_TO_THAT_REG_CENTER.getMessage()));

		bookingService.deleteOldBooking(PRE_ID);
	}

	@Test
	public void increaseAvailabilityIncrementsKioskCount() {
		when(bookingDAO.findByRegDateAndRegcntrIdAndFromTimeAndToTime(any(), anyString(), any(), any()))
				.thenReturn(availabilityEntity);
		when(bookingDAO.updateAvailibityEntity(any())).thenReturn(availabilityEntity);

		assertTrue(bookingService.increaseAvailability(bookingRequestDTO));
		assertEquals(6, availabilityEntity.getAvailableKiosks());
	}

	@Test(expected = RecordNotFoundException.class)
	public void increaseAvailabilityRethrowsMappedException() {
		when(bookingDAO.findByRegDateAndRegcntrIdAndFromTimeAndToTime(any(), anyString(), any(), any()))
				.thenThrow(new RecordNotFoundException(ErrorCodes.PRG_BOOK_RCI_015.getCode(),
						ErrorMessages.NO_TIME_SLOTS_ASSIGNED_TO_THAT_REG_CENTER.getMessage()));

		bookingService.increaseAvailability(bookingRequestDTO);
	}

	@Test
	public void setAuditValuesPersistsAudit() {
		bookingService.setAuditValues("PRE_401", "RETRIEVE", "BUSINESS", "ok", "id-type", "user-1", "user-1",
				CENTER_ID);

		verify(auditLogUtil).saveAuditDetails(any());
	}

	@Test
	public void getBookedPreRegistrationByDateReturnsIds() {
		when(serviceUtil.validateFromDateAndToDate(anyString(), anyString(), anyString())).thenReturn(true);
		when(bookingDAO.findByBookingDateBetweenAndRegCenterId(any(), any(), eq(CENTER_ID), any()))
				.thenReturn(List.of(PRE_ID));

		MainResponseDTO<PreRegIdsByRegCenterIdResponseDTO> response = bookingService
				.getBookedPreRegistrationByDate("2026-12-01", "2026-12-10", CENTER_ID);

		assertEquals(List.of(PRE_ID), response.getResponse().getPreRegistrationIds());
		assertEquals(CENTER_ID, response.getResponse().getRegistrationCenterId());
	}

	@Test
	public void getBookedPreRegistrationByDateDefaultsEmptyToDate() {
		when(serviceUtil.validateFromDateAndToDate(eq("2026-12-01"), eq("2026-12-01"), anyString())).thenReturn(true);
		when(bookingDAO.findByBookingDateBetweenAndRegCenterId(any(), any(), eq(CENTER_ID), any()))
				.thenReturn(new ArrayList<>());

		MainResponseDTO<PreRegIdsByRegCenterIdResponseDTO> response = bookingService
				.getBookedPreRegistrationByDate("2026-12-01", "", CENTER_ID);

		assertNotNull(response.getResponse());
	}

	@Test(expected = RecordNotFoundException.class)
	public void getBookedPreRegistrationByDateRethrowsMappedException() {
		when(serviceUtil.validateFromDateAndToDate(anyString(), anyString(), anyString()))
				.thenThrow(new RecordNotFoundException(ErrorCodes.PRG_BOOK_RCI_015.getCode(),
						ErrorMessages.NO_TIME_SLOTS_ASSIGNED_TO_THAT_REG_CENTER.getMessage()));

		bookingService.getBookedPreRegistrationByDate("2026-12-01", "2026-12-10", CENTER_ID);
	}

	@Test
	public void getBookedPreRegistrationsReturnsSlotsById() {
		when(serviceUtil.validateFromDateAndToDate(anyString(), anyString(), anyString())).thenReturn(true);
		when(bookingDAO.findByBookingDateBetweenAndRegCenterId(any(), any(), eq(CENTER_ID), any()))
				.thenReturn(List.of(PRE_ID));

		MainResponseDTO<BookingDataByRegIdDto> response = bookingService.getBookedPreRegistrations("2026-12-01",
				"2026-12-10", CENTER_ID);

		assertEquals(CENTER_ID, response.getResponse().getRegistrationCenterId());
		assertNotNull(response.getResponse().getIdsWithAppointmentDate());
	}

	@Test
	public void getBookedPreRegistrationsDefaultsNullToDate() {
		when(serviceUtil.validateFromDateAndToDate(eq("2026-12-01"), eq("2026-12-01"), anyString())).thenReturn(true);
		when(bookingDAO.findByBookingDateBetweenAndRegCenterId(any(), any(), eq(CENTER_ID), any()))
				.thenReturn(Collections.emptyList());

		MainResponseDTO<BookingDataByRegIdDto> response = bookingService.getBookedPreRegistrations("2026-12-01", null,
				CENTER_ID);

		assertEquals(CENTER_ID, response.getResponse().getRegistrationCenterId());
	}

	@Test(expected = RecordNotFoundException.class)
	public void getBookedPreRegistrationsRethrowsMappedException() {
		when(serviceUtil.validateFromDateAndToDate(anyString(), anyString(), anyString()))
				.thenThrow(new RecordNotFoundException(ErrorCodes.PRG_BOOK_RCI_015.getCode(),
						ErrorMessages.NO_TIME_SLOTS_ASSIGNED_TO_THAT_REG_CENTER.getMessage()));

		bookingService.getBookedPreRegistrations("2026-12-01", "2026-12-10", CENTER_ID);
	}

	private void stubSuccessfulBookPath(String statusCode) {
		when(serviceUtil.validateAppointmentDate(anyMap())).thenReturn(true);
		when(serviceUtil.getApplicationBookingStatus(PRE_ID)).thenReturn(statusCode);
		when(serviceUtil.mandatoryParameterCheck(eq(PRE_ID), any())).thenReturn(true);
		when(serviceUtil.slotTimeValidCheck(eq(PRE_ID), any())).thenReturn(true);
		RegistrationCenterDto center = new RegistrationCenterDto();
		center.setId(CENTER_ID);
		when(serviceUtil.getRegCenterMasterData(CENTER_ID)).thenReturn(List.of(center));
		when(bookingDAO.findRegistrationCenterId(CENTER_ID)).thenReturn(true);
		when(bookingDAO.findByRegDateAndRegcntrIdAndFromTimeAndToTime(any(), anyString(), any(), any()))
				.thenReturn(availabilityEntity);
		when(bookingDAO.findFirstByRegDateAndRegcntrIdAndFromTimeAndToTime(any(), anyString(), any(), any()))
				.thenReturn(availabilityEntity);
		when(serviceUtil.isKiosksAvailable(any())).thenReturn(true);
		when(serviceUtil.bookingEntitySetter(anyString(), any())).thenReturn(bookingEntity);
		when(bookingDAO.saveRegistrationEntityForBooking(any())).thenReturn(bookingEntity);
		when(bookingDAO.updateAvailibityEntity(any())).thenReturn(availabilityEntity);
	}

	private void stubCancelSuccess(boolean batch) {
		when(serviceUtil.mandatoryParameterCheckforCancel(PRE_ID)).thenReturn(true);
		when(serviceUtil.getDemographicStatusForCancel(PRE_ID)).thenReturn(true);
		when(bookingDAO.findByPreRegistrationId(PRE_ID)).thenReturn(bookingEntity);
		when(bookingDAO.findFirstByRegDateAndRegcntrIdAndFromTimeAndToTime(any(), anyString(), any(), any()))
				.thenReturn(availabilityEntity);
		when(bookingDAO.updateAvailibityEntity(any())).thenReturn(availabilityEntity);
		if (!batch) {
			when(serviceUtil.timeSpanCheckForCancle(any())).thenReturn(true);
		}
	}

	private MainRequestDTO<BookingRequestDTO> mainBookingRequest() {
		MainRequestDTO<BookingRequestDTO> request = new MainRequestDTO<>();
		request.setRequest(bookingRequestDTO);
		request.setRequesttime(new Date());
		return request;
	}

	private MainRequestDTO<MultiBookingRequest> multiBookingRequest() {
		MultiBookingRequestDTO row = new MultiBookingRequestDTO();
		row.setPreRegistrationId(PRE_ID);
		row.setRegistrationCenterId(CENTER_ID);
		row.setRegDate(REG_DATE);
		row.setSlotFromTime(FROM);
		row.setSlotToTime(TO);
		MultiBookingRequest multi = new MultiBookingRequest();
		multi.setBookingRequest(List.of(row));
		MainRequestDTO<MultiBookingRequest> request = new MainRequestDTO<>();
		request.setRequest(multi);
		request.setRequesttime(new Date());
		return request;
	}
}
