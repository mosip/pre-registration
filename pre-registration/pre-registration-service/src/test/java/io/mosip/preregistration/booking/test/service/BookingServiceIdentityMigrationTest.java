package io.mosip.preregistration.booking.test.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.preregistration.core.common.service.ApplicationIdentityMigrationService;
import io.mosip.preregistration.booking.dto.BookingRequestDTO;
import io.mosip.preregistration.booking.dto.BookingStatusDTO;
import io.mosip.preregistration.booking.entity.AvailibityEntity;
import io.mosip.preregistration.booking.repository.impl.BookingDAO;
import io.mosip.preregistration.booking.service.BookingService;
import io.mosip.preregistration.booking.service.util.BookingServiceUtil;
import io.mosip.preregistration.core.common.dto.CancelBookingResponseDTO;
import io.mosip.preregistration.core.common.dto.DeleteBookingDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.entity.RegistrationBookingEntity;
import io.mosip.preregistration.core.util.AuditLogUtil;
import io.mosip.preregistration.core.util.ValidationUtil;

/**
 * Covers the best-effort contract of the canonical-identity backfill invoked
 * from {@link BookingService}.
 *
 * <p>The backfill rewrites legacy raw identifiers to canonical UUIDs and is
 * documented as best-effort: it must never fail the user's request, because the
 * booking's own {@code crBy} is already canonical by the time it runs, and any
 * record it misses is swept up by the nightly identity reconciliation job. The
 * call sites therefore swallow failures. Without that guard the exception
 * reaches {@code BookingExceptionCatcher}, which rethrows, and the whole booking
 * is rolled back - which is exactly what these tests pin down.
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class BookingServiceIdentityMigrationTest {

	private static final String PRE_REG_ID = "98765432101234";

	private static final String CANONICAL_CR_BY = "3f2a9c14-7b6d-4e18-9c53-1d8ef04a25b7";

	private static final String BOOKING_SUCCESS_MESSAGE = "Appointment booked successfully";

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

	@InjectMocks
	private BookingService bookingService;

	private BookingRequestDTO bookingRequestDTO;

	private AvailibityEntity availabilityEntity;

	private RegistrationBookingEntity bookingEntity;

	@Before
	public void setUp() {
		bookingRequestDTO = new BookingRequestDTO();
		bookingRequestDTO.setRegistrationCenterId("10001");
		bookingRequestDTO.setRegDate("2026-12-10");
		bookingRequestDTO.setSlotFromTime("09:00");
		bookingRequestDTO.setSlotToTime("09:15");

		availabilityEntity = new AvailibityEntity();
		availabilityEntity.setRegcntrId("10001");
		availabilityEntity.setRegDate(LocalDate.parse("2026-12-10"));
		availabilityEntity.setFromTime(LocalTime.parse("09:00"));
		availabilityEntity.setToTime(LocalTime.parse("09:15"));
		availabilityEntity.setAvailableKiosks(5);

		bookingEntity = new RegistrationBookingEntity();
		bookingEntity.setCrBy(CANONICAL_CR_BY);
		bookingEntity.setPreregistrationId(PRE_REG_ID);
		bookingEntity.setRegistrationCenterId("10001");
		bookingEntity.setRegDate(LocalDate.parse("2026-12-10"));
		bookingEntity.setSlotFromTime(LocalTime.parse("09:00"));
		bookingEntity.setSlotToTime(LocalTime.parse("09:15"));

		Mockito.when(bookingDAO.findFirstByRegDateAndRegcntrIdAndFromTimeAndToTime(any(), anyString(), any(), any()))
				.thenReturn(availabilityEntity);
		Mockito.when(serviceUtil.isKiosksAvailable(any())).thenReturn(true);
		Mockito.when(serviceUtil.bookingEntitySetter(anyString(), any())).thenReturn(bookingEntity);
		Mockito.when(bookingDAO.saveRegistrationEntityForBooking(any())).thenReturn(bookingEntity);
		Mockito.when(bookingDAO.updateAvailibityEntity(any())).thenReturn(availabilityEntity);
	}

	private void givenIdentityMigrationFails() {
		Mockito.doThrow(new RuntimeException("identity migration unavailable"))
				.when(applicationIdentityMigrationService).migrateRawUserToEffectiveUser(anyString(), any());
	}

	/**
	 * A failing backfill must not surface to the applicant as a failed booking.
	 */
	@Test
	public void book_identityMigrationFails_bookingSucceeds() {
		givenIdentityMigrationFails();

		BookingStatusDTO result = bookingService.book(PRE_REG_ID, bookingRequestDTO);

		assertNotNull(result);
		assertEquals(BOOKING_SUCCESS_MESSAGE, result.getBookingMessage());
	}

	/**
	 * The steps after the backfill must still run. Guarding only the exception
	 * without letting the flow continue would leave the slot reserved but never
	 * decremented, so this asserts the side effect rather than just the response.
	 */
	@Test
	public void book_identityMigrationFails_availabilityStillDecremented() {
		givenIdentityMigrationFails();

		bookingService.book(PRE_REG_ID, bookingRequestDTO);

		assertEquals(4, availabilityEntity.getAvailableKiosks());
		Mockito.verify(bookingDAO).updateAvailibityEntity(availabilityEntity);
	}

	/**
	 * Guard must not change the happy path: the backfill is still invoked, and
	 * still with the booking's own canonical creator rather than the caller.
	 */
	@Test
	public void book_identityMigrationSucceeds_migratesWithBookingCreator() {
		BookingStatusDTO result = bookingService.book(PRE_REG_ID, bookingRequestDTO);

		assertEquals(BOOKING_SUCCESS_MESSAGE, result.getBookingMessage());
		Mockito.verify(applicationIdentityMigrationService).migrateRawUserToEffectiveUser(PRE_REG_ID,
				CANONICAL_CR_BY);
	}

	// ---------------------------------------------------------------------
	// deleteBooking
	//
	// resolveEffectiveUserId throws a plain IllegalStateException, which
	// BookingExceptionCatcher.handle() does not map - its final else rethrows
	// only BaseUncheckedException / BaseCheckedException. An unguarded failure
	// is therefore swallowed and the method returns a success-shaped response
	// having never deleted anything. These tests pin the guard that prevents it.
	// ---------------------------------------------------------------------

	private void givenDeleteBookingIsReachable() {
		AuthUserDetails principal = Mockito.mock(AuthUserDetails.class);
		Mockito.when(principal.getUserId()).thenReturn("test-user");
		Mockito.when(principal.getUsername()).thenReturn("test-user");
		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken(principal, null));

		Mockito.when(validationUtil.requstParamValidator(anyMap())).thenReturn(true);
		Mockito.when(serviceUtil.checkApplicationStatus(PRE_REG_ID)).thenReturn(true);
		Mockito.when(bookingDAO.findByPreRegistrationId(PRE_REG_ID)).thenReturn(bookingEntity);
	}

	private void givenIdentityResolutionFails() {
		Mockito.when(applicationIdentityMigrationService.resolveEffectiveUserId(anyString()))
				.thenThrow(new IllegalStateException("Failed to resolve UUID for user during migration"));
	}

	@After
	public void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	/**
	 * The deletion is the caller's actual intent and must not be blocked by an
	 * unresolvable legacy identifier.
	 */
	@Test
	public void deleteBooking_identityResolutionFails_bookingStillDeleted() {
		givenDeleteBookingIsReachable();
		givenIdentityResolutionFails();

		MainResponseDTO<DeleteBookingDTO> response = bookingService.deleteBooking(PRE_REG_ID);

		Mockito.verify(bookingDAO).deleteByPreRegistrationId(PRE_REG_ID);
		assertNotNull(response.getResponse());
		assertEquals(PRE_REG_ID, response.getResponse().getPreRegistrationId());
	}

	/**
	 * Attribution degrades to absent. It must never fall back to the raw
	 * identifier, which would put plaintext PII back on the wire.
	 *
	 * <p>The sibling assertions matter: asserting only that {@code deletedBy} is
	 * null would also hold when the method aborts before setting anything, so the
	 * test would pass against the very defect it exists to catch. Requiring the
	 * rest of the DTO to be populated proves the flow ran to completion with just
	 * this one field omitted.
	 */
	@Test
	public void deleteBooking_identityResolutionFails_deletedByOmitted() {
		givenDeleteBookingIsReachable();
		givenIdentityResolutionFails();

		MainResponseDTO<DeleteBookingDTO> response = bookingService.deleteBooking(PRE_REG_ID);

		assertNull(response.getResponse().getDeletedBy());
		assertEquals(PRE_REG_ID, response.getResponse().getPreRegistrationId());
		assertNotNull(response.getResponse().getDeletedDateTime());
	}

	/**
	 * Guard must not change the resolvable path.
	 */
	@Test
	public void deleteBooking_identityResolutionSucceeds_deletedByPopulated() {
		givenDeleteBookingIsReachable();
		Mockito.when(applicationIdentityMigrationService.resolveEffectiveUserId(CANONICAL_CR_BY))
				.thenReturn(CANONICAL_CR_BY);

		MainResponseDTO<DeleteBookingDTO> response = bookingService.deleteBooking(PRE_REG_ID);

		Mockito.verify(bookingDAO).deleteByPreRegistrationId(PRE_REG_ID);
		assertEquals(CANONICAL_CR_BY, response.getResponse().getDeletedBy());
	}

	// ---------------------------------------------------------------------
	// cancelBooking
	//
	// Here the resolve and the backfill are both inside the guard, because the
	// resolved id is not used beyond the backfill call. A failure must leave the
	// cancellation itself untouched.
	// ---------------------------------------------------------------------

	private static final String CANCEL_SUCCESS_MESSAGE =
			"Appointment for the selected application has been successfully cancelled";

	private void givenCancelBookingIsReachable() {
		AuthUserDetails principal = Mockito.mock(AuthUserDetails.class);
		Mockito.when(principal.getUserId()).thenReturn("test-user");
		Mockito.when(principal.getUsername()).thenReturn("test-user");
		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken(principal, null));

		Mockito.when(serviceUtil.mandatoryParameterCheckforCancel(PRE_REG_ID)).thenReturn(true);
		Mockito.when(serviceUtil.getDemographicStatusForCancel(PRE_REG_ID)).thenReturn(true);
		Mockito.when(bookingDAO.findByPreRegistrationId(PRE_REG_ID)).thenReturn(bookingEntity);
	}

	/**
	 * The cancellation is the caller's intent; a failing backfill must not stop it.
	 */
	@Test
	public void cancelBooking_identityMigrationFails_cancellationSucceeds() {
		givenCancelBookingIsReachable();
		givenIdentityResolutionFails();

		CancelBookingResponseDTO result = bookingService.cancelBooking(PRE_REG_ID, false);

		Mockito.verify(bookingDAO).deleteByPreRegistrationId(PRE_REG_ID);
		assertEquals(CANCEL_SUCCESS_MESSAGE, result.getMessage());
	}

	/**
	 * Cancelling frees the slot again. Asserting the side effect catches a guard
	 * that swallows the exception but lets the flow skip the steps after it.
	 */
	@Test
	public void cancelBooking_identityMigrationFails_availabilityStillRestored() {
		givenCancelBookingIsReachable();
		givenIdentityResolutionFails();

		bookingService.cancelBooking(PRE_REG_ID, false);

		assertEquals(6, availabilityEntity.getAvailableKiosks());
		Mockito.verify(bookingDAO).updateAvailibityEntity(availabilityEntity);
	}

	/**
	 * Guard must not change the resolvable path.
	 */
	@Test
	public void cancelBooking_identityResolutionSucceeds_migratesWithResolvedId() {
		givenCancelBookingIsReachable();
		Mockito.when(applicationIdentityMigrationService.resolveEffectiveUserId(CANONICAL_CR_BY))
				.thenReturn(CANONICAL_CR_BY);

		CancelBookingResponseDTO result = bookingService.cancelBooking(PRE_REG_ID, false);

		assertEquals(CANCEL_SUCCESS_MESSAGE, result.getMessage());
		Mockito.verify(applicationIdentityMigrationService).migrateRawUserToEffectiveUser(PRE_REG_ID,
				CANONICAL_CR_BY);
	}
}
