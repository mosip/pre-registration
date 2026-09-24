package io.mosip.preregistration.batchjob.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.batchjob.code.PreRegBatchContants;
import io.mosip.preregistration.batchjob.repository.utils.BatchJpaRepositoryImpl;
import io.mosip.preregistration.core.config.LoggerConfiguration;

/**
 * Reconciliation safety-net for the PII identity migration.
 *
 * <p>Identity migration into the canonical {@code user_details} registry runs best-effort on the
 * pre-registration create/update paths — if the aggregate backfill throws, the ownership columns of
 * the linked rows (applications/applicant_demographic/applicant_document/reg_appointment) are left
 * holding the raw identifier while the request still succeeds. Active users self-heal on their next
 * create/update/booking; this job is the backstop for records whose users never return, so no raw
 * identifier lingers indefinitely. It detects candidates on {@code applications.cr_by}.
 *
 * <p>Records are processed in <b>bounded batches</b> and each record is reconciled in its <b>own
 * transaction</b> (see {@link IdentityReconciliationTxHelper}), so a single failure or a large
 * legacy backlog cannot exhaust memory or discard the whole run's progress. It is idempotent — a
 * record already holding canonical UUIDs is skipped.
 *
 * @since 1.3.1
 */
@Component
public class ApplicationIdentityReconciliationUpdater {

	/** Bounded so a large legacy backlog is not loaded/mutated all at once (no config key needed). */
	private static final int RECONCILIATION_BATCH_SIZE = 500;

	private Logger log = LoggerConfiguration.logConfig(ApplicationIdentityReconciliationUpdater.class);

	@Autowired
	private BatchJpaRepositoryImpl batchJpaRepositoryImpl;

	@Autowired
	private IdentityReconciliationTxHelper reconciliationTxHelper;

	public void reconcileRawIdentityRecords() {

		log.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH,
				PreRegBatchContants.IDENTITY_RECONCILIATION_JOB, "Identity reconciliation started");

		int reconciled = 0;
		int failed = 0;
		// Records that cannot be resolved stay candidates and fail again on every nightly run, so the
		// same trace would repeat nightly per record. Keep the first occurrence of each distinct
		// cause — latching on "any failure" would hide a second, different root cause behind the
		// first one logged — and report the rest as a count in the completion line.
		Set<String> tracedFailureCauses = new HashSet<>();

		// Reconciled rows drop out of the raw-identifier predicate, so re-reading the first bounded
		// batch each iteration advances the window without offset paging. Stop when a full batch makes
		// no progress (all remaining candidates are currently un-resolvable) — guarantees termination.
		while (true) {
			List<String> preRegistrationIds = batchJpaRepositoryImpl
					.getPreRegIdsWithRawIdentifier(RECONCILIATION_BATCH_SIZE);
			if (Objects.isNull(preRegistrationIds) || preRegistrationIds.isEmpty()) {
				break;
			}

			int reconciledThisBatch = 0;
			for (String preRegistrationId : preRegistrationIds) {
				try {
					if (reconciliationTxHelper.reconcileOne(preRegistrationId)) {
						reconciled++;
						reconciledThisBatch++;
					}
				} catch (Exception ex) {
					failed++;
					log.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH,
							PreRegBatchContants.IDENTITY_RECONCILIATION_JOB,
							"Identity reconciliation failed for preRegistrationId: " + preRegistrationId);
					if (tracedFailureCauses.add(ex.getClass().getName())) {
						log.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH,
								PreRegBatchContants.IDENTITY_RECONCILIATION_JOB, ExceptionUtils.getStackTrace(ex));
					}
				}
			}

			if (reconciledThisBatch == 0) {
				break;
			}
		}

		log.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH,
				PreRegBatchContants.IDENTITY_RECONCILIATION_JOB,
				"Identity reconciliation completed. reconciled=" + reconciled + ", failed=" + failed);
	}
}
