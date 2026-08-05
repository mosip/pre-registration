package io.mosip.preregistration.core.common.service;

import static io.mosip.preregistration.core.constant.PreRegCoreConstant.LOGGER_ID;
import static io.mosip.preregistration.core.constant.PreRegCoreConstant.LOGGER_IDTYPE;
import static io.mosip.preregistration.core.constant.PreRegCoreConstant.LOGGER_SESSIONID;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.core.common.repository.ApplicationRepostiory;
import io.mosip.preregistration.core.common.repository.DemographicRepository;
import io.mosip.preregistration.core.common.repository.DocumentRepository;
import io.mosip.preregistration.core.common.repository.RegAppointmentRepository;
import io.mosip.preregistration.core.common.entity.ApplicationEntity;
import io.mosip.preregistration.core.common.entity.DemographicEntity;
import io.mosip.preregistration.core.common.entity.DocumentEntity;
import io.mosip.preregistration.core.common.entity.RegistrationBookingEntity;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.exception.UserLookupException;
import io.mosip.preregistration.core.util.GenericUtil;

@Service
public class ApplicationIdentityMigrationService {

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
        try {
            String uuid = userDetailsService.getOrCreateInternalUserId(userId);
            if (uuid == null) {
                throw new IllegalStateException("Failed to resolve UUID for user during migration, maskedUserId="
                        + GenericUtil.maskIdentifier(userId));
            }
            return uuid;
        } catch (UserLookupException ex) {
            throw new IllegalStateException("Failed to resolve UUID for user during migration, maskedUserId="
                    + GenericUtil.maskIdentifier(userId), ex);
        }
    }

    /**
     * Aggregate backfill that rewrites raw creator/updater identifiers to canonical surrogate UUIDs
     * across the pre-registration's application, demographic, document and booking rows.
     *
     * <p><b>Each column is resolved from its own current value</b>, never from the caller. A column
     * already holding a canonical UUID is left alone, so a request made on an applicant's behalf — an
     * operator booking an appointment, say — can no longer overwrite the applicant's ownership. See
     * {@link #convertIfRaw}. Consequently {@code effectiveUserId} no longer determines what is
     * written; it survives only as a guard that skips the pass when the caller has no resolvable
     * identity, and could be dropped from the signature in a follow-up.
     *
     * <p><b>Failure policy — best-effort by design.</b> Every caller invokes this inside a try/catch
     * that logs and swallows failures rather than failing the user's request, and this is applied
     * consistently across all flows (create/update/booking). It is intentional and safe because:
     * <ul>
     *   <li>The primary record's own identifier is already resolved to a canonical UUID on the
     *       non-best-effort create/update path <em>before</em> this backfill runs, so authentication
     *       and ownership checks are never broken by a backfill failure.</li>
     *   <li>This method is idempotent (only still-raw columns are converted) and re-runs on the user's
     *       next create, update or booking, so a transiently-missed record self-heals without any
     *       operator action.</li>
     *   <li>Failures are logged with masked identifiers for operational follow-up.</li>
     * </ul>
     * A record belonging to a user who never returns is swept up by the nightly identity
     * reconciliation batch job in {@code pre-registration-batchjob} (detects candidates across every
     * ownership column it converts, so partially migrated rows are picked up too), so no raw
     * identifier lingers indefinitely.
     *
     * <p><b>Why {@code noRollbackFor}.</b> A caller's try/catch alone is not enough to make this
     * best-effort: this method joins the caller's transaction, so without this flag Spring's
     * interceptor would mark that transaction rollback-only on the way out and the caller's commit
     * would fail with {@code UnexpectedRollbackException} — the swallowed exception would still sink
     * the user's request. {@code noRollbackFor} suppresses that marking while keeping the backfill in
     * the caller's transaction, which it must stay in: several callers invoke it immediately after
     * inserting the row being backfilled, and a {@code REQUIRES_NEW} transaction could not see that
     * uncommitted row. Any partial writes left by a mid-way failure are flushed with the caller's
     * commit and cleaned up by the reconciliation job. Residual limitation: a failure originating in
     * the database itself (rather than in user lookup) aborts the underlying transaction regardless
     * of this flag.
     */
    @Transactional(noRollbackFor = Exception.class)
    public void migrateRawUserToEffectiveUser(String preRegistrationId, String effectiveUserId) {

        if (effectiveUserId == null || effectiveUserId.isBlank()) {
            log.warn(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
                    "migrateRawUserToEffectiveUser skipped: effectiveUserId is blank for preRegistrationId=" + preRegistrationId);
            return;
        }
        String trimmedPreRegistrationId = preRegistrationId.trim();
        int migratedRecords = 0;

        ApplicationEntity applicationEntity = applicationRepostiory.findByApplicationId(trimmedPreRegistrationId);
        if (applicationEntity != null) {
            boolean changed = false;
            changed |= convertIfRaw(applicationEntity.getCrBy(), applicationEntity::setCrBy);
            changed |= convertIfRaw(applicationEntity.getUpdBy(), applicationEntity::setUpdBy);
            changed |= convertIfRaw(applicationEntity.getContactInfo(), applicationEntity::setContactInfo);
            if (changed) {
                applicationRepostiory.save(applicationEntity);
                migratedRecords++;
            }
        }

        DemographicEntity demographicEntity = demographicRepository.findBypreRegistrationId(trimmedPreRegistrationId);
        if (demographicEntity != null) {
            boolean changed = false;
            changed |= convertIfRaw(demographicEntity.getCreatedBy(), demographicEntity::setCreatedBy);
            changed |= convertIfRaw(demographicEntity.getUpdatedBy(), demographicEntity::setUpdatedBy);
            changed |= convertIfRaw(demographicEntity.getCrAppuserId(), demographicEntity::setCrAppuserId);
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
                changed |= convertIfRaw(documentEntity.getCrBy(), documentEntity::setCrBy);
                changed |= convertIfRaw(documentEntity.getUpdBy(), documentEntity::setUpdBy);
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
            changed |= convertIfRaw(registrationBookingEntity.getCrBy(), registrationBookingEntity::setCrBy);
            changed |= convertIfRaw(registrationBookingEntity.getUpBy(), registrationBookingEntity::setUpBy);
            if (changed) {
				regAppointmentRepository.save(registrationBookingEntity);
                migratedRecords++;
            }
        }

        log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
                "Completed aggregate identity migration for preRegistrationId " + trimmedPreRegistrationId
                        + " with migrated record count " + migratedRecords);
    }

    /**
     * Converts a single ownership column only if it currently holds a raw (non-UUID) identifier, and
     * resolves that column's <em>own</em> value to <em>its own</em> canonical UUID.
     *
     * <p>This is a migration, not an attribution. The column must end up holding the canonical id of
     * whoever it already referred to — never the id of whoever happens to be making the request. The
     * previous implementation wrote a single caller-supplied id across every column, which on the
     * appointment path is the operator rather than the applicant: it overwrote an applicant's correct
     * ownership (and, because {@code matchesUser} compares against these columns, locked them out of
     * their own pre-registration). Blanks, nulls and already-canonical values are therefore left
     * untouched, and a value that cannot be resolved is left raw for the nightly reconciliation job.
     *
     * <p>Mirrors {@code IdentityReconciliationTxHelper#convertIfRaw} in pre-registration-batchjob so
     * the online and batch paths cannot diverge again.
     */
    private boolean convertIfRaw(String currentValue, java.util.function.Consumer<String> setter) {
        if (currentValue == null) {
            return false;
        }
        String trimmed = currentValue.trim();
        if (trimmed.isEmpty() || GenericUtil.isUuid(trimmed)) {
            return false;
        }
        String resolved;
        try {
            resolved = userDetailsService.getOrCreateInternalUserId(trimmed);
        } catch (UserLookupException ex) {
            log.warn(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
                    "Leaving ownership column raw; could not resolve maskedUserId="
                            + GenericUtil.maskIdentifier(trimmed));
            return false;
        }
        if (resolved == null || !GenericUtil.isUuid(resolved.trim())) {
            return false;
        }
        setter.accept(resolved.trim());
        return true;
    }
}
