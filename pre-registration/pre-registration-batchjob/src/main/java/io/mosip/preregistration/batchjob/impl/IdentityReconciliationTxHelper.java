package io.mosip.preregistration.batchjob.impl;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.batchjob.code.PreRegBatchContants;
import io.mosip.preregistration.batchjob.repository.utils.BatchJpaRepositoryImpl;
import io.mosip.preregistration.core.common.entity.ApplicationEntity;
import io.mosip.preregistration.core.common.entity.DemographicEntity;
import io.mosip.preregistration.core.common.entity.DocumentEntity;
import io.mosip.preregistration.core.common.entity.RegistrationBookingEntity;
import io.mosip.preregistration.core.common.service.UserDetailsService;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.exception.UserLookupException;
import io.mosip.preregistration.core.util.GenericUtil;

/**
 * Applies the identity reconciliation for a single pre-registration id in its own transaction.
 *
 * <p>Runs {@link Propagation#REQUIRES_NEW} so each record commits (or rolls back) independently: one
 * record's failure cannot mark a shared transaction rollback-only and discard the progress of the
 * others. Kept in a separate bean from {@link ApplicationIdentityReconciliationUpdater} so the
 * transactional proxy actually applies (self-invocation would bypass it).
 *
 * <p>Only columns that are <b>themselves</b> raw (non-UUID) are converted, and each is resolved from
 * its own value — an ownership column that already holds a canonical UUID is left untouched, so a
 * correctly-migrated {@code upd_by}/{@code contact_info} is never overwritten with a different user's
 * id.
 */
@Component
public class IdentityReconciliationTxHelper {

	private Logger log = LoggerConfiguration.logConfig(IdentityReconciliationTxHelper.class);

	@Autowired
	private BatchJpaRepositoryImpl batchJpaRepositoryImpl;

	@Autowired
	private UserDetailsService userDetailsService;

	/**
	 * Reconciles the application row and its linked demographic/document/booking rows for one
	 * pre-registration id.
	 *
	 * @param preRegistrationId the id to reconcile
	 * @return {@code true} if any column was converted, {@code false} if the record was already
	 *         canonical or nothing could be resolved.
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean reconcileOne(String preRegistrationId) {
		boolean changed = false;

		ApplicationEntity application = batchJpaRepositoryImpl.getApplicationObject(preRegistrationId);
		if (Objects.nonNull(application)) {
			boolean rowChanged = false;
			rowChanged |= convertIfRaw(application.getCrBy(), application::setCrBy);
			rowChanged |= convertIfRaw(application.getUpdBy(), application::setUpdBy);
			rowChanged |= convertIfRaw(application.getContactInfo(), application::setContactInfo);
			if (rowChanged) {
				batchJpaRepositoryImpl.updateApplicantEntity(application);
				changed = true;
			}
		}

		DemographicEntity demographic = batchJpaRepositoryImpl.getApplicantDemographicObject(preRegistrationId);
		if (Objects.nonNull(demographic)) {
			boolean rowChanged = false;
			rowChanged |= convertIfRaw(demographic.getCreatedBy(), demographic::setCreatedBy);
			rowChanged |= convertIfRaw(demographic.getUpdatedBy(), demographic::setUpdatedBy);
			rowChanged |= convertIfRaw(demographic.getCrAppuserId(), demographic::setCrAppuserId);
			if (rowChanged) {
				batchJpaRepositoryImpl.updateApplicantDemographic(demographic);
				changed = true;
			}
		}

		List<DocumentEntity> documents = batchJpaRepositoryImpl.getApplicantDocuments(preRegistrationId);
		if (Objects.nonNull(documents)) {
			for (DocumentEntity document : documents) {
				boolean rowChanged = false;
				rowChanged |= convertIfRaw(document.getCrBy(), document::setCrBy);
				rowChanged |= convertIfRaw(document.getUpdBy(), document::setUpdBy);
				if (rowChanged) {
					batchJpaRepositoryImpl.updateApplicantDocument(document);
					changed = true;
				}
			}
		}

		RegistrationBookingEntity booking = batchJpaRepositoryImpl.getRegistrationAppointmentObject(preRegistrationId);
		if (Objects.nonNull(booking)) {
			boolean rowChanged = false;
			rowChanged |= convertIfRaw(booking.getCrBy(), booking::setCrBy);
			rowChanged |= convertIfRaw(booking.getUpBy(), booking::setUpBy);
			if (rowChanged) {
				batchJpaRepositoryImpl.updateBooking(booking);
				changed = true;
			}
		}

		if (changed) {
			log.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH,
					PreRegBatchContants.IDENTITY_RECONCILIATION_JOB,
					"Reconciled identity for preRegistrationId: " + preRegistrationId);
		}
		return changed;
	}

	/**
	 * Converts a single ownership column only if its current value is a raw (non-UUID) identifier,
	 * resolving that column's own value to its canonical UUID. Already-canonical values, blanks and
	 * nulls are left untouched; if resolution fails the value is left raw for a later run.
	 */
	private boolean convertIfRaw(String currentValue, Consumer<String> setter) {
		if (currentValue == null) {
			return false;
		}
		String trimmed = currentValue.trim();
		if (trimmed.isEmpty() || GenericUtil.isUuid(trimmed)) {
			return false;
		}
		String resolved = resolveUserUuid(trimmed);
		if (resolved == null || !GenericUtil.isUuid(resolved.trim())) {
			return false;
		}
		setter.accept(resolved.trim());
		return true;
	}

	private String resolveUserUuid(String userId) {
		try {
			return userDetailsService.getOrCreateInternalUserId(userId);
		} catch (UserLookupException ex) {
			log.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH,
					PreRegBatchContants.IDENTITY_RECONCILIATION_JOB, "Failed to resolve UUID for masked user "
							+ GenericUtil.maskIdentifier(userId) + "; leaving field raw for a later run.");
			return userId;
		}
	}
}
