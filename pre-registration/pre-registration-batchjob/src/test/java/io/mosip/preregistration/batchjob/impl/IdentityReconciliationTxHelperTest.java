package io.mosip.preregistration.batchjob.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.mosip.preregistration.batchjob.repository.utils.BatchJpaRepositoryImpl;
import io.mosip.preregistration.core.common.entity.ApplicationEntity;
import io.mosip.preregistration.core.common.entity.DemographicEntity;
import io.mosip.preregistration.core.common.entity.DocumentEntity;
import io.mosip.preregistration.core.common.entity.RegistrationBookingEntity;
import io.mosip.preregistration.core.common.service.UserDetailsService;
import io.mosip.preregistration.core.exception.UserLookupException;

@RunWith(JUnit4.class)
public class IdentityReconciliationTxHelperTest {

	@InjectMocks
	private IdentityReconciliationTxHelper txHelper;

	@Mock
	private BatchJpaRepositoryImpl batchJpaRepositoryImpl;

	@Mock
	private UserDetailsService userDetailsService;

	private static final String PRID = "12345678901234";

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void convertsAllRawColumnsAcrossLinkedTables() {
		String raw = "user@example.com";
		String uuid = UUID.randomUUID().toString();

		ApplicationEntity application = new ApplicationEntity();
		application.setApplicationId(PRID);
		application.setCrBy(raw);
		application.setUpdBy(raw);
		application.setContactInfo(raw);

		DemographicEntity demographic = new DemographicEntity();
		demographic.setPreRegistrationId(PRID);
		demographic.setCreatedBy(raw);
		demographic.setUpdatedBy(raw);
		demographic.setCrAppuserId(raw);

		DocumentEntity document = new DocumentEntity();
		document.setDocumentId("doc-1");
		document.setCrBy(raw);
		document.setUpdBy(raw);

		RegistrationBookingEntity booking = new RegistrationBookingEntity();
		booking.setId("booking-1");
		booking.setPreregistrationId(PRID);
		booking.setCrBy(raw);
		booking.setUpBy(raw);

		when(batchJpaRepositoryImpl.getApplicationObject(PRID)).thenReturn(application);
		when(batchJpaRepositoryImpl.getApplicantDemographicObject(PRID)).thenReturn(demographic);
		when(batchJpaRepositoryImpl.getApplicantDocuments(PRID)).thenReturn(List.of(document));
		when(batchJpaRepositoryImpl.getRegistrationAppointmentObject(PRID)).thenReturn(booking);
		when(userDetailsService.getOrCreateInternalUserId(raw)).thenReturn(uuid);

		boolean changed = txHelper.reconcileOne(PRID);

		assertTrue(changed);
		assertEquals(uuid, application.getCrBy());
		assertEquals(uuid, application.getUpdBy());
		assertEquals(uuid, application.getContactInfo());
		assertEquals(uuid, demographic.getCreatedBy());
		assertEquals(uuid, demographic.getUpdatedBy());
		assertEquals(uuid, demographic.getCrAppuserId());
		assertEquals(uuid, document.getCrBy());
		assertEquals(uuid, document.getUpdBy());
		assertEquals(uuid, booking.getCrBy());
		assertEquals(uuid, booking.getUpBy());
		verify(batchJpaRepositoryImpl).updateApplicantEntity(application);
		verify(batchJpaRepositoryImpl).updateApplicantDemographic(demographic);
		verify(batchJpaRepositoryImpl).updateApplicantDocument(document);
		verify(batchJpaRepositoryImpl).updateBooking(booking);
	}

	@Test
	public void leavesAlreadyUuidColumnUntouched() {
		// The key guard: a column that already holds a canonical UUID (e.g. a genuine updater) must not
		// be overwritten with the creator's id resolved from cr_by.
		String raw = "user@example.com";
		String resolvedUuid = UUID.randomUUID().toString();
		String existingUpdByUuid = UUID.randomUUID().toString();

		ApplicationEntity application = new ApplicationEntity();
		application.setApplicationId(PRID);
		application.setCrBy(raw);
		application.setUpdBy(existingUpdByUuid);
		application.setContactInfo(raw);

		when(batchJpaRepositoryImpl.getApplicationObject(PRID)).thenReturn(application);
		when(batchJpaRepositoryImpl.getApplicantDemographicObject(PRID)).thenReturn(null);
		when(batchJpaRepositoryImpl.getApplicantDocuments(PRID)).thenReturn(null);
		when(batchJpaRepositoryImpl.getRegistrationAppointmentObject(PRID)).thenReturn(null);
		when(userDetailsService.getOrCreateInternalUserId(raw)).thenReturn(resolvedUuid);

		boolean changed = txHelper.reconcileOne(PRID);

		assertTrue(changed);
		assertEquals(resolvedUuid, application.getCrBy());
		assertEquals(resolvedUuid, application.getContactInfo());
		assertEquals(existingUpdByUuid, application.getUpdBy());
		verify(userDetailsService, never()).getOrCreateInternalUserId(existingUpdByUuid);
		verify(batchJpaRepositoryImpl).updateApplicantEntity(application);
	}

	@Test
	public void isNoOpWhenEverythingAlreadyCanonical() {
		String uuid = UUID.randomUUID().toString();

		ApplicationEntity application = new ApplicationEntity();
		application.setApplicationId(PRID);
		application.setCrBy(uuid);
		application.setUpdBy(uuid);
		application.setContactInfo(uuid);

		when(batchJpaRepositoryImpl.getApplicationObject(PRID)).thenReturn(application);
		when(batchJpaRepositoryImpl.getApplicantDemographicObject(PRID)).thenReturn(null);
		when(batchJpaRepositoryImpl.getApplicantDocuments(PRID)).thenReturn(null);
		when(batchJpaRepositoryImpl.getRegistrationAppointmentObject(PRID)).thenReturn(null);

		boolean changed = txHelper.reconcileOne(PRID);

		assertFalse(changed);
		verify(batchJpaRepositoryImpl, never()).updateApplicantEntity(any());
		verify(userDetailsService, never()).getOrCreateInternalUserId(any());
	}

	@Test
	public void skipsColumnWhenResolutionFails() {
		String raw = "user@example.com";

		ApplicationEntity application = new ApplicationEntity();
		application.setApplicationId(PRID);
		application.setCrBy(raw);

		when(batchJpaRepositoryImpl.getApplicationObject(PRID)).thenReturn(application);
		when(batchJpaRepositoryImpl.getApplicantDemographicObject(PRID)).thenReturn(null);
		when(batchJpaRepositoryImpl.getApplicantDocuments(PRID)).thenReturn(null);
		when(batchJpaRepositoryImpl.getRegistrationAppointmentObject(PRID)).thenReturn(null);
		when(userDetailsService.getOrCreateInternalUserId(raw))
				.thenThrow(new UserLookupException("PRG_CORE_REQ_024", "resolution failed"));

		boolean changed = txHelper.reconcileOne(PRID);

		assertFalse(changed);
		assertEquals(raw, application.getCrBy());
		verify(batchJpaRepositoryImpl, never()).updateApplicantEntity(any());
	}

	@Test
	public void handlesNullLinkedRecords() {
		String raw = "user@example.com";
		String uuid = UUID.randomUUID().toString();

		ApplicationEntity application = new ApplicationEntity();
		application.setApplicationId(PRID);
		application.setCrBy(raw);

		when(batchJpaRepositoryImpl.getApplicationObject(PRID)).thenReturn(application);
		when(batchJpaRepositoryImpl.getApplicantDemographicObject(PRID)).thenReturn(null);
		when(batchJpaRepositoryImpl.getApplicantDocuments(PRID)).thenReturn(null);
		when(batchJpaRepositoryImpl.getRegistrationAppointmentObject(PRID)).thenReturn(null);
		when(userDetailsService.getOrCreateInternalUserId(raw)).thenReturn(uuid);

		boolean changed = txHelper.reconcileOne(PRID);

		assertTrue(changed);
		assertEquals(uuid, application.getCrBy());
		verify(batchJpaRepositoryImpl).updateApplicantEntity(application);
		verify(batchJpaRepositoryImpl, never()).updateApplicantDemographic(any());
		verify(batchJpaRepositoryImpl, never()).updateApplicantDocument(any());
		verify(batchJpaRepositoryImpl, never()).updateBooking(any());
	}
}
