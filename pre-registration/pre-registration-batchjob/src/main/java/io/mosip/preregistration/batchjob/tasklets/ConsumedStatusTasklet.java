package io.mosip.preregistration.batchjob.tasklets;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.batchjob.impl.ApplicationConsumedStatusUpdater;
import io.mosip.preregistration.core.config.LoggerConfiguration;

/**
 * This class is a tasklet of batch job to call update status service in batch service.
 * 
 * @author Kishan Rathore
 * @since 1.0.0
 *
 */
@Component
public class ConsumedStatusTasklet implements Tasklet {

	@Autowired
	/**
	 * status updater used by this Pre-Registration type.
	 */
	private ApplicationConsumedStatusUpdater statusUpdater;

	/**
	 * Performs log config for MOSIP Pre-Registration.
	 *
	 * @return log config result
	 */
	private Logger log = LoggerConfiguration.logConfig(ConsumedStatusTasklet.class);

	/* (non-Javadoc)
	 * @see org.springframework.batch.core.step.tasklet.Tasklet#execute(org.springframework.batch.core.StepContribution, 
	 * 			org.springframework.batch.core.scope.context.ChunkContext)
	 */
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext arg1) throws Exception {

		try {
			statusUpdater.updateConsumedStatus();
		} catch (Exception e) {
			log.error("Update Consumed Status ", " Tasklet ", " encountered exception ", e.getMessage());
			contribution.setExitStatus(new ExitStatus(e.getMessage()));
		}
		return RepeatStatus.FINISHED;
	}

}
