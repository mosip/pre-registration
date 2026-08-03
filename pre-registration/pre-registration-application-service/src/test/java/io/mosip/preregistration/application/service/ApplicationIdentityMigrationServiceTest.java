package io.mosip.preregistration.application.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import io.mosip.preregistration.application.service.ApplicationIdentityMigrationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import io.mosip.preregistration.application.repository.ApplicationRepostiory;
import io.mosip.preregistration.application.repository.DemographicRepository;
import io.mosip.preregistration.application.repository.DocumentRepository;
import io.mosip.preregistration.application.repository.RegAppointmentRepository;
import io.mosip.preregistration.core.common.entity.ApplicationEntity;
import io.mosip.preregistration.core.common.entity.DemographicEntity;
import io.mosip.preregistration.core.common.entity.DocumentEntity;
import io.mosip.preregistration.core.common.entity.RegistrationBookingEntity;
import io.mosip.preregistration.core.common.service.UserDetailsService;
import io.mosip.preregistration.core.exception.UserLookupException;

@RunWith(JUnit4.class)
public class ApplicationIdentityMigrationServiceTest {

    @InjectMocks
    private ApplicationIdentityMigrationService applicationIdentityMigrationService;

    @Mock
    private ApplicationRepostiory applicationRepostiory;

    @Mock
    private DemographicRepository demographicRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
	private RegAppointmentRepository regAppointmentRepository;

    @Mock
    private UserDetailsService userDetailsService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void migrateAggregateResolvesEachRawColumnToItsOwnOwnerNotTheCaller() {
        String preRegistrationId = "12345678901234";
        // The caller is deliberately NOT the record owner — e.g. an operator acting for an applicant.
        String callerUserId = UUID.randomUUID().toString();
        String ownerUuid = UUID.randomUUID().toString();

        ApplicationEntity applicationEntity = new ApplicationEntity();
        applicationEntity.setApplicationId(preRegistrationId);
        applicationEntity.setCrBy("raw-user");
        applicationEntity.setUpdBy("raw-user");
        applicationEntity.setContactInfo("raw-user");

        DemographicEntity demographicEntity = new DemographicEntity();
        demographicEntity.setPreRegistrationId(preRegistrationId);
        demographicEntity.setCreatedBy("raw-user");
        demographicEntity.setUpdatedBy("raw-user");
        demographicEntity.setCrAppuserId("raw-user");
        demographicEntity.setCreateDateTime(LocalDateTime.now());

        DocumentEntity documentEntity = new DocumentEntity();
        documentEntity.setDocumentId("doc-1");
        documentEntity.setCrBy("raw-user");
        documentEntity.setUpdBy("raw-user");
        documentEntity.setDemographicEntity(demographicEntity);

        RegistrationBookingEntity registrationBookingEntity = new RegistrationBookingEntity();
        registrationBookingEntity.setId("booking-1");
        registrationBookingEntity.setPreregistrationId(preRegistrationId);
        registrationBookingEntity.setCrBy("raw-user");
        registrationBookingEntity.setUpBy("raw-user");
        registrationBookingEntity.setRegDate(LocalDate.now());

        when(applicationRepostiory.findByApplicationId(preRegistrationId)).thenReturn(applicationEntity);
        when(demographicRepository.findBypreRegistrationId(preRegistrationId)).thenReturn(demographicEntity);
        when(documentRepository.findByDemographicEntityPreRegistrationId(preRegistrationId))
                .thenReturn(List.of(documentEntity));
		when(regAppointmentRepository.getRegistrationAppointmentByPreRegistrationId(preRegistrationId))
				.thenReturn(registrationBookingEntity);
        // The raw value resolves to the owner's canonical id, which is not the caller's.
        when(userDetailsService.getOrCreateInternalUserId("raw-user")).thenReturn(ownerUuid);

        applicationIdentityMigrationService.migrateRawUserToEffectiveUser(preRegistrationId, callerUserId);

        assertEquals(ownerUuid, applicationEntity.getCrBy());
        assertEquals(ownerUuid, applicationEntity.getUpdBy());
        assertEquals(ownerUuid, applicationEntity.getContactInfo());
        assertEquals(ownerUuid, demographicEntity.getCreatedBy());
        assertEquals(ownerUuid, demographicEntity.getUpdatedBy());
        assertEquals(ownerUuid, demographicEntity.getCrAppuserId());
        assertEquals(ownerUuid, documentEntity.getCrBy());
        assertEquals(ownerUuid, documentEntity.getUpdBy());
        assertEquals(ownerUuid, registrationBookingEntity.getCrBy());
        assertEquals(ownerUuid, registrationBookingEntity.getUpBy());

        verify(applicationRepostiory).save(applicationEntity);
        verify(demographicRepository).save(demographicEntity);
        verify(documentRepository).save(documentEntity);
		verify(regAppointmentRepository).save(registrationBookingEntity);
    }

    /**
     * An operator acting on an applicant's behalf must not take over the applicant's ownership. Every
     * column already holds the applicant's canonical UUID, so nothing may be written at all.
     */
    @Test
    public void migrateNeverOverwritesOwnershipAlreadyHeldByAnotherCanonicalUser() {
        String preRegistrationId = "12345678901234";
        String applicantUuid = UUID.randomUUID().toString();
        String operatorUuid = UUID.randomUUID().toString();

        ApplicationEntity applicationEntity = new ApplicationEntity();
        applicationEntity.setApplicationId(preRegistrationId);
        applicationEntity.setCrBy(applicantUuid);
        applicationEntity.setUpdBy(applicantUuid);
        applicationEntity.setContactInfo(applicantUuid);

        DemographicEntity demographicEntity = new DemographicEntity();
        demographicEntity.setPreRegistrationId(preRegistrationId);
        demographicEntity.setCreatedBy(applicantUuid);
        demographicEntity.setUpdatedBy(applicantUuid);
        demographicEntity.setCrAppuserId(applicantUuid);

        DocumentEntity documentEntity = new DocumentEntity();
        documentEntity.setDocumentId("doc-1");
        documentEntity.setCrBy(applicantUuid);
        documentEntity.setUpdBy(applicantUuid);
        documentEntity.setDemographicEntity(demographicEntity);

        RegistrationBookingEntity registrationBookingEntity = new RegistrationBookingEntity();
        registrationBookingEntity.setId("booking-1");
        registrationBookingEntity.setPreregistrationId(preRegistrationId);
        registrationBookingEntity.setCrBy(applicantUuid);
        registrationBookingEntity.setUpBy(applicantUuid);

        when(applicationRepostiory.findByApplicationId(preRegistrationId)).thenReturn(applicationEntity);
        when(demographicRepository.findBypreRegistrationId(preRegistrationId)).thenReturn(demographicEntity);
        when(documentRepository.findByDemographicEntityPreRegistrationId(preRegistrationId))
                .thenReturn(List.of(documentEntity));
        when(regAppointmentRepository.getRegistrationAppointmentByPreRegistrationId(preRegistrationId))
                .thenReturn(registrationBookingEntity);

        applicationIdentityMigrationService.migrateRawUserToEffectiveUser(preRegistrationId, operatorUuid);

        assertEquals(applicantUuid, applicationEntity.getCrBy());
        assertEquals(applicantUuid, applicationEntity.getUpdBy());
        assertEquals(applicantUuid, applicationEntity.getContactInfo());
        assertEquals(applicantUuid, demographicEntity.getCreatedBy());
        assertEquals(applicantUuid, demographicEntity.getCrAppuserId());
        assertEquals(applicantUuid, documentEntity.getCrBy());
        assertEquals(applicantUuid, registrationBookingEntity.getCrBy());

        verify(applicationRepostiory, never()).save(any(ApplicationEntity.class));
        verify(demographicRepository, never()).save(any(DemographicEntity.class));
        verify(documentRepository, never()).save(any(DocumentEntity.class));
        verify(regAppointmentRepository, never()).save(any(RegistrationBookingEntity.class));
    }

    /**
     * The legacy case her fix would not have covered: a raw value must resolve to the identity it
     * already names, not to whoever is making the request.
     */
    @Test
    public void migrateResolvesRawOwnershipToTheApplicantNotTheCallingOperator() {
        String preRegistrationId = "12345678901234";
        String applicantUuid = UUID.randomUUID().toString();
        String operatorUuid = UUID.randomUUID().toString();

        DemographicEntity demographicEntity = new DemographicEntity();
        demographicEntity.setPreRegistrationId(preRegistrationId);
        demographicEntity.setCreatedBy("applicant@example.com");
        demographicEntity.setUpdatedBy("applicant@example.com");
        demographicEntity.setCrAppuserId("applicant@example.com");

        when(applicationRepostiory.findByApplicationId(preRegistrationId)).thenReturn(null);
        when(demographicRepository.findBypreRegistrationId(preRegistrationId)).thenReturn(demographicEntity);
        when(documentRepository.findByDemographicEntityPreRegistrationId(preRegistrationId)).thenReturn(List.of());
        when(regAppointmentRepository.getRegistrationAppointmentByPreRegistrationId(preRegistrationId))
                .thenReturn(null);
        when(userDetailsService.getOrCreateInternalUserId("applicant@example.com")).thenReturn(applicantUuid);

        applicationIdentityMigrationService.migrateRawUserToEffectiveUser(preRegistrationId, operatorUuid);

        assertEquals(applicantUuid, demographicEntity.getCreatedBy());
        assertEquals(applicantUuid, demographicEntity.getUpdatedBy());
        assertEquals(applicantUuid, demographicEntity.getCrAppuserId());
        verify(demographicRepository).save(demographicEntity);
    }

    /** A raw value that cannot be resolved is left as-is for the reconciliation job, not overwritten. */
    @Test
    public void migrateLeavesUnresolvableRawValueUntouched() {
        String preRegistrationId = "12345678901234";
        String operatorUuid = UUID.randomUUID().toString();

        DemographicEntity demographicEntity = new DemographicEntity();
        demographicEntity.setPreRegistrationId(preRegistrationId);
        demographicEntity.setCreatedBy("unresolvable-user");

        when(applicationRepostiory.findByApplicationId(preRegistrationId)).thenReturn(null);
        when(demographicRepository.findBypreRegistrationId(preRegistrationId)).thenReturn(demographicEntity);
        when(documentRepository.findByDemographicEntityPreRegistrationId(preRegistrationId)).thenReturn(List.of());
        when(regAppointmentRepository.getRegistrationAppointmentByPreRegistrationId(preRegistrationId))
                .thenReturn(null);
        when(userDetailsService.getOrCreateInternalUserId("unresolvable-user"))
                .thenThrow(new UserLookupException("PRG_CORE_REQ_001", "lookup failed"));

        applicationIdentityMigrationService.migrateRawUserToEffectiveUser(preRegistrationId, operatorUuid);

        assertEquals("unresolvable-user", demographicEntity.getCreatedBy());
        verify(demographicRepository, never()).save(any(DemographicEntity.class));
    }

    @Test
    public void resolveEffectiveUserIdReturnsUuid() {
        String userId = "user@example.com";
        String canonicalUuid = UUID.randomUUID().toString();
        when(userDetailsService.getOrCreateInternalUserId(userId)).thenReturn(canonicalUuid);

        String resolvedUserId = applicationIdentityMigrationService.resolveEffectiveUserId(userId);

        assertEquals(canonicalUuid, resolvedUserId);
    }

    @Test
    public void resolveEffectiveUserIdThrowsWhenUuidResolutionFails() {
        String userId = "user@example.com";
        when(userDetailsService.getOrCreateInternalUserId(userId)).thenReturn(null);

        assertThrows(IllegalStateException.class,
                () -> applicationIdentityMigrationService.resolveEffectiveUserId(userId));
    }

    @Test
    public void resolveEffectiveUserIdThrowsIllegalStateWhenUserLookupExceptionThrown() {
        String userId = "user@example.com";
        when(userDetailsService.getOrCreateInternalUserId(userId))
                .thenThrow(new UserLookupException("PRG_CORE_REQ_024", "Failed to resolve internal user ID"));

        assertThrows(IllegalStateException.class,
                () -> applicationIdentityMigrationService.resolveEffectiveUserId(userId));
    }

    @Test
    public void migrateIsNoOpWhenValuesAlreadyMatch() {
        String preRegistrationId = "12345678901234";
        String effectiveUserId = UUID.randomUUID().toString();

        ApplicationEntity applicationEntity = new ApplicationEntity();
        applicationEntity.setApplicationId(preRegistrationId);
        applicationEntity.setCrBy(effectiveUserId);
        applicationEntity.setUpdBy(effectiveUserId);
        applicationEntity.setContactInfo(effectiveUserId);

        DemographicEntity demographicEntity = new DemographicEntity();
        demographicEntity.setPreRegistrationId(preRegistrationId);
        demographicEntity.setCreatedBy(effectiveUserId);
        demographicEntity.setUpdatedBy(effectiveUserId);
        demographicEntity.setCrAppuserId(effectiveUserId);

        DocumentEntity documentEntity = new DocumentEntity();
        documentEntity.setDocumentId("doc-1");
        documentEntity.setCrBy(effectiveUserId);
        documentEntity.setUpdBy(effectiveUserId);
        documentEntity.setDemographicEntity(demographicEntity);

        RegistrationBookingEntity registrationBookingEntity = new RegistrationBookingEntity();
        registrationBookingEntity.setId("booking-1");
        registrationBookingEntity.setPreregistrationId(preRegistrationId);
        registrationBookingEntity.setCrBy(effectiveUserId);
        registrationBookingEntity.setUpBy(effectiveUserId);
        registrationBookingEntity.setRegDate(LocalDate.now());

        when(applicationRepostiory.findByApplicationId(preRegistrationId)).thenReturn(applicationEntity);
        when(demographicRepository.findBypreRegistrationId(preRegistrationId)).thenReturn(demographicEntity);
        when(documentRepository.findByDemographicEntityPreRegistrationId(preRegistrationId))
                .thenReturn(List.of(documentEntity));
        when(regAppointmentRepository.getRegistrationAppointmentByPreRegistrationId(preRegistrationId))
                .thenReturn(registrationBookingEntity);

        applicationIdentityMigrationService.migrateRawUserToEffectiveUser(preRegistrationId, effectiveUserId);

        // Every column already holds the canonical UUID — nothing should be re-persisted.
        verify(applicationRepostiory, never()).save(any());
        verify(demographicRepository, never()).save(any());
        verify(documentRepository, never()).save(any());
        verify(regAppointmentRepository, never()).save(any());
    }

    @Test
    public void migrateHandlesMissingLinkedRecords() {
        String preRegistrationId = "12345678901234";
        String callerUserId = UUID.randomUUID().toString();
        String ownerUuid = UUID.randomUUID().toString();

        ApplicationEntity applicationEntity = new ApplicationEntity();
        applicationEntity.setApplicationId(preRegistrationId);
        applicationEntity.setCrBy("raw-user");

        when(applicationRepostiory.findByApplicationId(preRegistrationId)).thenReturn(applicationEntity);
        when(demographicRepository.findBypreRegistrationId(preRegistrationId)).thenReturn(null);
        when(documentRepository.findByDemographicEntityPreRegistrationId(preRegistrationId)).thenReturn(null);
        when(regAppointmentRepository.getRegistrationAppointmentByPreRegistrationId(preRegistrationId))
                .thenReturn(null);
        when(userDetailsService.getOrCreateInternalUserId("raw-user")).thenReturn(ownerUuid);

        applicationIdentityMigrationService.migrateRawUserToEffectiveUser(preRegistrationId, callerUserId);

        // Present record is migrated to its own owner; absent linked records must not NPE or save.
        assertEquals(ownerUuid, applicationEntity.getCrBy());
        verify(applicationRepostiory).save(applicationEntity);
        verify(demographicRepository, never()).save(any());
        verify(documentRepository, never()).save(any());
        verify(regAppointmentRepository, never()).save(any());
    }

    @Test
    public void migrateSkipsEntirelyWhenEffectiveUserIdBlank() {
        applicationIdentityMigrationService.migrateRawUserToEffectiveUser("12345678901234", "   ");

        // Blank effective id short-circuits before any lookup.
        verify(applicationRepostiory, never()).findByApplicationId(any());
        verify(applicationRepostiory, never()).save(any());
    }
}
