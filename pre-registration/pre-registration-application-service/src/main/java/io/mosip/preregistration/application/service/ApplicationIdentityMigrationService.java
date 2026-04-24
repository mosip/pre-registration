package io.mosip.preregistration.application.service;

import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_ID;
import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_IDTYPE;
import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_SESSIONID;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.application.repository.ApplicationRepostiory;
import io.mosip.preregistration.application.repository.DemographicRepository;
import io.mosip.preregistration.application.repository.DocumentRepository;
import io.mosip.preregistration.application.repository.RegAppointmentRepository;
import io.mosip.preregistration.core.common.entity.ApplicationEntity;
import io.mosip.preregistration.core.common.entity.DemographicEntity;
import io.mosip.preregistration.core.common.entity.DocumentEntity;
import io.mosip.preregistration.core.common.entity.RegistrationBookingEntity;
import io.mosip.preregistration.core.common.service.UserDetailsService;
import io.mosip.preregistration.core.config.LoggerConfiguration;

@Service
public class ApplicationIdentityMigrationService {

    @Value("${mosip.prereg.pii.backward.compatibility}")
    private boolean piiBackwardCompatibility;

    @Autowired
    private ApplicationRepostiory applicationRepostiory;

    @Autowired
    private DemographicRepository demographicRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
	private RegAppointmentRepository regAppointmentRepository;

    @Autowired
    private UserDetailsService userDetailsService;

    private final Logger log = LoggerConfiguration.logConfig(ApplicationIdentityMigrationService.class);

    public String resolveEffectiveUserId(String userId) {
        if (piiBackwardCompatibility) {
            return userDetailsService.resolveUserUuidOrIdentifier(userId);
        }
        return userDetailsService.resolveUserUuid(userId)
                .orElseThrow(() -> new IllegalStateException("Failed to resolve canonical user UUID"));
    }

    @Transactional
    public void migrateRawUserToEffectiveUser(String preRegistrationId, String effectiveUserId) {

        String trimmedPreRegistrationId = preRegistrationId.trim();
        String trimmedEffectiveUserId = effectiveUserId.trim();
        int migratedRecords = 0;

        ApplicationEntity applicationEntity = applicationRepostiory.findByApplicationId(trimmedPreRegistrationId);
        if (applicationEntity != null) {
            boolean changed = false;
            changed |= replaceIfDifferent(applicationEntity.getCrBy(), trimmedEffectiveUserId,
                    applicationEntity::setCrBy);
            changed |= replaceIfDifferent(applicationEntity.getUpdBy(), trimmedEffectiveUserId,
                    applicationEntity::setUpdBy);
            changed |= replaceIfDifferent(applicationEntity.getContactInfo(), trimmedEffectiveUserId,
                    applicationEntity::setContactInfo);
            if (changed) {
                applicationRepostiory.save(applicationEntity);
                migratedRecords++;
            }
        }

        DemographicEntity demographicEntity = demographicRepository.findBypreRegistrationId(trimmedPreRegistrationId);
        if (demographicEntity != null) {
            boolean changed = false;
            changed |= replaceIfDifferent(demographicEntity.getCreatedBy(), trimmedEffectiveUserId,
                    demographicEntity::setCreatedBy);
            changed |= replaceIfDifferent(demographicEntity.getUpdatedBy(), trimmedEffectiveUserId,
                    demographicEntity::setUpdatedBy);
            changed |= replaceIfDifferent(demographicEntity.getCrAppuserId(), trimmedEffectiveUserId,
                    demographicEntity::setCrAppuserId);
            if (changed) {
                demographicRepository.save(demographicEntity);
                migratedRecords++;
            }
        }

        List<DocumentEntity> documentEntities = documentRepository
                .findByDemographicEntityPreRegistrationId(trimmedPreRegistrationId);
        if (documentEntities != null) {
            for (DocumentEntity documentEntity : documentEntities) {
                boolean changed = false;
                changed |= replaceIfDifferent(documentEntity.getCrBy(), trimmedEffectiveUserId, documentEntity::setCrBy);
                changed |= replaceIfDifferent(documentEntity.getUpdBy(), trimmedEffectiveUserId, documentEntity::setUpdBy);
                if (changed) {
                    documentRepository.save(documentEntity);
                    migratedRecords++;
                }
            }
        }

		RegistrationBookingEntity registrationBookingEntity = regAppointmentRepository
				.getRegistrationAppointmentByPreRegistrationId(trimmedPreRegistrationId);
        if (registrationBookingEntity != null) {
            boolean changed = false;
            changed |= replaceIfDifferent(registrationBookingEntity.getCrBy(), trimmedEffectiveUserId,
                    registrationBookingEntity::setCrBy);
            changed |= replaceIfDifferent(registrationBookingEntity.getUpBy(), trimmedEffectiveUserId,
                    registrationBookingEntity::setUpBy);
            if (changed) {
				regAppointmentRepository.save(registrationBookingEntity);
                migratedRecords++;
            }
        }

        log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
                "Completed aggregate identity migration for preRegistrationId " + trimmedPreRegistrationId
                        + " with migrated record count " + migratedRecords);
    }

    private boolean replaceIfDifferent(String currentValue, String newValue, java.util.function.Consumer<String> setter) {
        String current = currentValue == null ? "" : currentValue.trim();
        if (current.equals(newValue)) {
            return false;
        }
        setter.accept(newValue);
        return true;
    }
}
