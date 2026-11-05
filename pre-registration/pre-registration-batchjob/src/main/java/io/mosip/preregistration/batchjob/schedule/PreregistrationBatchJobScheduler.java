package io.mosip.preregistration.batchjob.schedule;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.job.parameters.InvalidJobParametersException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.launch.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.core.config.LoggerConfiguration;

/**
 * This class is a job scheduler of batch in which jobs are getting executed
 * based on cron expressions.
 * 
 * @author Kishan Rathore
 * @since 1.0.0
 *
 */
@RefreshScope
@Component
@EnableScheduling
public class PreregistrationBatchJobScheduler {

	/**
	 * Performs log config for MOSIP Pre-Registration.
	 *
	 * @return log config result
	 */
	private Logger LOGGER = LoggerConfiguration.logConfig(PreregistrationBatchJobScheduler.class);

	/**
	 * logdisplay used by this Pre-Registration type.
	 */
	private static final String LOGDISPLAY = "{} - {} - {}";

	/**
	 * job status used by this Pre-Registration type.
	 */
	private static final String JOB_STATUS = "Job's status";

	@Autowired
	/**
	 * job launcher used by this Pre-Registration type.
	 */
	private JobLauncher jobLauncher;

	@Qualifier("regCenterPartitionerJob")
	@Autowired
	/**
	 * reg center partitioner job used by this Pre-Registration type.
	 */
	private Job regCenterPartitionerJob;

	@Qualifier("consumedStatusJob")
	@Autowired
	/**
	 * consumed status job used by this Pre-Registration type.
	 */
	private Job consumedStatusJob;

	@Qualifier("identityReconciliationJob")
	@Autowired
	/**
	 * identity reconciliation job used by this Pre-Registration type.
	 */
	private Job identityReconciliationJob;

	@Qualifier("expiredStatusJob")
	@Autowired
	/**
	 * expired status job used by this Pre-Registration type.
	 */
	private Job expiredStatusJob;
	
	@Qualifier("updateApplicationForBookingCheckJob")
	@Autowired
	/**
	 * update application for booking check job used by this Pre-Registration type.
	 */
	private Job updateApplicationForBookingCheckJob;

	@Qualifier("purgeExpiredSlotsJob")
	@Autowired
	/**
	 * purge expired slots job used by this Pre-Registration type.
	 */
	private Job purgeExpiredSlotsJob;

	@Scheduled(cron = "${preregistration.job.schedule.cron.consumedStatusJob}")
	/**
	 * Performs consumed status scheduler for MOSIP Pre-Registration.
	 */
	public void consumedStatusScheduler() {

		JobParameters jobParam = new JobParametersBuilder().addLong("updateStatusTime", System.currentTimeMillis())
				.toJobParameters();
		try {
			JobExecution jobExecution = jobLauncher.run(consumedStatusJob, jobParam);

			LOGGER.info(LOGDISPLAY, JOB_STATUS, String.valueOf(jobExecution.getId()), jobExecution.getStatus().toString());

		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				| InvalidJobParametersException e) {

			LOGGER.error(LOGDISPLAY, "Consumed status job failed to read Processed_pre_registration_list", e.getMessage(),null);
		}
	} 

	@Scheduled(cron = "${preregistration.job.schedule.cron.identityReconciliationJob}")
	/**
	 * Performs identity reconciliation scheduler for MOSIP Pre-Registration.
	 */
	public void identityReconciliationScheduler() {

		JobParameters jobParam = new JobParametersBuilder()
				.addLong("identityReconciliationJobTime", System.currentTimeMillis()).toJobParameters();
		try {
			JobExecution jobExecution = jobLauncher.run(identityReconciliationJob, jobParam);

			LOGGER.info(LOGDISPLAY, JOB_STATUS, String.valueOf(jobExecution.getId()), jobExecution.getStatus().toString());

		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				| InvalidJobParametersException e) {

			LOGGER.error(LOGDISPLAY, "Identity reconciliation job failed to reconcile raw identifiers", e.getMessage(),
					null);
		}
	}

	@Scheduled(cron = "${preregistration.job.schedule.cron.slotavailability}")
	/**
	 * Performs availability sync scheduler for MOSIP Pre-Registration.
	 */
	public void availabilitySyncScheduler() {

		JobParameters jobParam = new JobParametersBuilder().addLong("bookingJobTime", System.currentTimeMillis())
				.toJobParameters();
		try {

			JobExecution jobExecution = jobLauncher.run(regCenterPartitionerJob, jobParam);

			LOGGER.info(LOGDISPLAY, JOB_STATUS, String.valueOf(jobExecution.getId()), jobExecution.getStatus().toString());

		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				| InvalidJobParametersException e) {

			LOGGER.error(LOGDISPLAY, "Availability Sync Job failed to read data from master data service", e.getMessage(),null);
		}

	}

	@Scheduled(cron = "${preregistration.job.schedule.cron.expiredStatusJob}")
	/**
	 * Performs expired status scheduler for MOSIP Pre-Registration.
	 */
	public void expiredStatusScheduler() {

		JobParameters jobParam = new JobParametersBuilder().addLong("expiredStatusJobTime", System.currentTimeMillis())
				.toJobParameters();
		try {

			JobExecution jobExecution = jobLauncher.run(expiredStatusJob, jobParam);

			LOGGER.info(LOGDISPLAY, JOB_STATUS, String.valueOf(jobExecution.getId()), jobExecution.getStatus().toString());

		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				| InvalidJobParametersException e) {

			LOGGER.error(LOGDISPLAY, "Expired Status Job failed to read data from service", e.getMessage(),null);
		}

	}
	
	@Scheduled(cron = "${preregistration.job.schedule.cron.updateApplicationsBookingJob}")
	/**
	 * Performs applications booking status scheduler for MOSIP Pre-Registration.
	 */
	public void applicationsBookingStatusScheduler() {

		JobParameters jobParam = new JobParametersBuilder().addLong("updateApplicationsBookingStatusJobTime", System.currentTimeMillis())
				.toJobParameters();
		try {

			JobExecution jobExecution = jobLauncher.run(updateApplicationForBookingCheckJob, jobParam);

			LOGGER.info(LOGDISPLAY, JOB_STATUS, String.valueOf(jobExecution.getId()), jobExecution.getStatus().toString());

		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				| InvalidJobParametersException e) {

			LOGGER.error(LOGDISPLAY, "Applications Booking  Status Job failed to read data from service", e.getMessage(),null);
		}

	}

	@Scheduled(cron = "${preregistration.job.schedule.cron.purgeExpiredRegCenterSlots}")
	/**
	 * Performs purge expired slots scheduler for MOSIP Pre-Registration.
	 */
	public void purgeExpiredSlotsScheduler() {

		JobParameters jobParam = new JobParametersBuilder().addLong("purgeExpiredSlotsJob", System.currentTimeMillis())
				.toJobParameters();
		try {
			JobExecution jobExecution = jobLauncher.run(purgeExpiredSlotsJob, jobParam);
			LOGGER.info(LOGDISPLAY, JOB_STATUS, String.valueOf(jobExecution.getId()), jobExecution.getStatus().toString());
		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				| InvalidJobParametersException e) {
			LOGGER.error(LOGDISPLAY, "Applications Booking  Status Job failed to read data from service", e.getMessage(),null);
		}
	}
}
