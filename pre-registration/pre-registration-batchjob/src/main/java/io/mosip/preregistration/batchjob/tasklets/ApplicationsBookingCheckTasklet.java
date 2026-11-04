package io.mosip.preregistration.batchjob.tasklets;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.batchjob.impl.ApplicationMismatchDataUpdater;
import io.mosip.preregistration.core.config.LoggerConfiguration;

@Component
/**
 * Class supporting applications booking check tasklet in MOSIP Pre-Registration (io.mosip.preregistration.batchjob.tasklets).
 */
public class ApplicationsBookingCheckTasklet implements Tasklet {

	@Autowired
	/**
	 * mismatch data updater used by this Pre-Registration type.
	 */
	private ApplicationMismatchDataUpdater mismatchDataUpdater;

	/**
	 * Performs log config for MOSIP Pre-Registration.
	 *
	 * @return log config result
	 */
	private Logger log = LoggerConfiguration.logConfig(ApplicationsBookingCheckTasklet.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.core.step.tasklet.Tasklet#execute(org.
	 * springframework.batch.core.StepContribution,
	 * org.springframework.batch.core.scope.context.ChunkContext)
	 */
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

		try {
			mismatchDataUpdater.updateMismatchData();
		} catch (Exception e) {
			log.error("Sync master ", " Tasklet ", " encountered exception ", e.getMessage());
			contribution.setExitStatus(new ExitStatus(e.getMessage()));
		}
		return RepeatStatus.FINISHED;
	}

}
