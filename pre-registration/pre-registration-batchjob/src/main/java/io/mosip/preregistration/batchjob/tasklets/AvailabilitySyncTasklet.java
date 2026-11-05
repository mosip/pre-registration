package io.mosip.preregistration.batchjob.tasklets;

import java.util.List;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
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
	/**
	 * availability generator used by this Pre-Registration type.
	 */
	private SlotAvailabilityGenerator availabilityGenerator;
	
	/**
	 * Performs log config for MOSIP Pre-Registration.
	 *
	 * @return log config result
	 */
	private Logger log = LoggerConfiguration.logConfig(AvailabilitySyncTasklet.class);

	/**
	 * part reg centers list used by this Pre-Registration type.
	 */
	private List<String> partRegCentersList;

	/**
	 * name used by this Pre-Registration type.
	 */
	private String name;

	/**
	 * Creates a availability sync tasklet.
	 *
	 * @param name name supplied to this constructor
	 * @param partRegCentersList part reg centers list supplied to this constructor
	 */
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
