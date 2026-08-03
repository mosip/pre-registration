/*
 * Copyright
 *
 */
package io.mosip.preregistration.batchjob.tasklets;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.batchjob.impl.ApplicationIdentityReconciliationUpdater;
import io.mosip.preregistration.core.config.LoggerConfiguration;

/**
 * Tasklet that drives the identity reconciliation job — re-runs the PII identity migration for
 * records left holding a raw identifier after a best-effort migration failure.
 */
@Component
public class IdentityReconciliationTasklet implements Tasklet {

	@Autowired
	private ApplicationIdentityReconciliationUpdater reconciliationUpdater;

	private Logger log = LoggerConfiguration.logConfig(IdentityReconciliationTasklet.class);

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext arg1) throws Exception {

		try {
			reconciliationUpdater.reconcileRawIdentityRecords();
		} catch (Exception e) {
			log.error("Identity Reconciliation ", " Tasklet ", " encountered exception ", e.getMessage());
			contribution.setExitStatus(new ExitStatus(e.getMessage()));
		}
		return RepeatStatus.FINISHED;
	}

}
