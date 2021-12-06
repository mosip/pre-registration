
/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.batchjob.tasklets;

import java.util.List;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.batchjob.impl.SlotAvailabilityGenerator;
import io.mosip.preregistration.core.config.LoggerConfiguration;

/**
 * This class is a tasklet of batch job to call master data sync API in batch service.
 * 
 * @author Kishan Rathore
 * @since 1.0.0
 *
 */
public class AvailabilitySyncTasklet implements Tasklet {

	@Autowired
	private SlotAvailabilityGenerator availabilityGenerator;
	
	private Logger log = LoggerConfiguration.logConfig(AvailabilitySyncTasklet.class);

	private List<String> partRegCentersList;

	private String name;

	public AvailabilitySyncTasklet(String name, List<String> partRegCentersList) {
		this.name = name;
		this.partRegCentersList = partRegCentersList;
	}

	/* (non-Javadoc)
	 * @see org.springframework.batch.core.step.tasklet.Tasklet#execute(org.springframework.batch.core.StepContribution, org.springframework.batch.core.scope.context.ChunkContext)
	 */
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		
		try {
			availabilityGenerator.generateRegistrationAvailabilitySlots(name, partRegCentersList);
		} catch (Exception e) {
			log.error("Sync master ", " Tasklet ", " encountered exception ", e.getMessage());
			contribution.setExitStatus(new ExitStatus(e.getMessage()));
		}

		return RepeatStatus.FINISHED;
	}

}
