package io.mosip.preregistration.application.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
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
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void migrateAggregateToEffectiveUserUpdatesAllLinkedTables() {
        String preRegistrationId = "12345678901234";
        String effectiveUserId = UUID.randomUUID().toString();

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

        applicationIdentityMigrationService.migrateRawUserToEffectiveUser(preRegistrationId, effectiveUserId);

        assertEquals(effectiveUserId, applicationEntity.getCrBy());
        assertEquals(effectiveUserId, applicationEntity.getUpdBy());
        assertEquals(effectiveUserId, applicationEntity.getContactInfo());
        assertEquals(effectiveUserId, demographicEntity.getCreatedBy());
        assertEquals(effectiveUserId, demographicEntity.getUpdatedBy());
        assertEquals(effectiveUserId, demographicEntity.getCrAppuserId());
        assertEquals(effectiveUserId, documentEntity.getCrBy());
        assertEquals(effectiveUserId, documentEntity.getUpdBy());
        assertEquals(effectiveUserId, registrationBookingEntity.getCrBy());
        assertEquals(effectiveUserId, registrationBookingEntity.getUpBy());

        verify(applicationRepostiory).save(applicationEntity);
        verify(demographicRepository).save(demographicEntity);
        verify(documentRepository).save(documentEntity);
		verify(regAppointmentRepository).save(registrationBookingEntity);
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
}
