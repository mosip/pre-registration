package io.mosip.preregistration.batchjob.job;

import java.util.List;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.partition.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import io.mosip.preregistration.batchjob.impl.SlotAvailabilityPartitioner;
import io.mosip.preregistration.batchjob.tasklets.ApplicationsBookingCheckTasklet;
import io.mosip.preregistration.batchjob.tasklets.AvailabilitySyncTasklet;
import io.mosip.preregistration.batchjob.tasklets.ConsumedStatusTasklet;
import io.mosip.preregistration.batchjob.tasklets.ExpiredStatusTasklet;
import io.mosip.preregistration.batchjob.tasklets.IdentityReconciliationTasklet;
import io.mosip.preregistration.batchjob.tasklets.PurgeExpiredRegCentersSlotsTasklet;
import javax.sql.DataSource;

/**
 * @author Aiham Hasan
 * @since 1.2.0
 *
 */
@Configuration
public class PreRegistrationBatchJobConfig {

	@Value("${preregistration.slots.generate.thread.count:20}")
	/**
	 * concurrency limit used by this Pre-Registration type.
	 */
	private int concurrencyLimit;

	@Autowired
	/**
	 * consumed status tasklet used by this Pre-Registration type.
	 */
	private ConsumedStatusTasklet consumedStatusTasklet;

	@Autowired
	/**
	 * purge reg center slots tasklet used by this Pre-Registration type.
	 */
	private PurgeExpiredRegCentersSlotsTasklet purgeRegCenterSlotsTasklet;

	@Autowired
	/**
	 * expired status tasklet used by this Pre-Registration type.
	 */
	private ExpiredStatusTasklet expiredStatusTasklet;

	@Autowired
	/**
	 * application booking check tasklet used by this Pre-Registration type.
	 */
	private ApplicationsBookingCheckTasklet applicationBookingCheckTasklet;

	@Autowired
	/**
	 * identity reconciliation tasklet used by this Pre-Registration type.
	 */
	private IdentityReconciliationTasklet identityReconciliationTasklet;

//  Commeting it as transactionManager is imported from Kernel-Auth-Adapter Jar
//	@Bean
//	public PlatformTransactionManager transactionManager(DataSource dataSource) {
//		return new DataSourceTransactionManager(dataSource);
//	}
	
	@Bean
	/**
	 * Performs consumed status step for MOSIP Pre-Registration.
	 *
	 * @param jobRepository job repository used by consumed status step
	 * @param transactionManager transaction manager used by consumed status step
	 * @return consumed status step result
	 */
	public Step consumedStatusStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		return new StepBuilder("consumedStatusStep", jobRepository).tasklet(consumedStatusTasklet, transactionManager)
				.build();
	}

	@Bean
	/**
	 * Performs purge expired slots step for MOSIP Pre-Registration.
	 *
	 * @param jobRepository job repository used by purge expired slots step
	 * @param transactionManager transaction manager used by purge expired slots step
	 * @return purge expired slots step result
	 */
	public Step purgeExpiredSlotsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		return new StepBuilder("purgeExpiredSlotsStep", jobRepository)
				.tasklet(purgeRegCenterSlotsTasklet, transactionManager).build();
	}

	@Bean
	/**
	 * Performs expired status step for MOSIP Pre-Registration.
	 *
	 * @param jobRepository job repository used by expired status step
	 * @param transactionManager transaction manager used by expired status step
	 * @return expired status step result
	 */
	public Step expiredStatusStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		return new StepBuilder("expiredStatusStep", jobRepository).tasklet(expiredStatusTasklet, transactionManager)
				.build();
	}

	@Bean
	/**
	 * Performs update booking in applications step for MOSIP Pre-Registration.
	 *
	 * @param jobRepository job repository used by update booking in applications step
	 * @param transactionManager transaction manager used by update booking in applications step
	 * @return update booking in applications step result
	 */
	public Step updateBookingInApplicationsStep(JobRepository jobRepository,
			PlatformTransactionManager transactionManager) {
		return new StepBuilder("updateBookingInApplicationsStep", jobRepository)
				.tasklet(applicationBookingCheckTasklet, transactionManager).build();
	}

	@Bean
	/**
	 * Performs purge expired slots job for MOSIP Pre-Registration.
	 *
	 * @param jobRepository job repository used by purge expired slots job
	 * @param purgeExpiredSlotsStep purge expired slots step used by purge expired slots job
	 * @return purge expired slots job result
	 */
	public Job purgeExpiredSlotsJob(JobRepository jobRepository,@Qualifier("purgeExpiredSlotsStep") Step purgeExpiredSlotsStep) {
		return new JobBuilder("purgeExpiredSlotsJob", jobRepository).incrementer(new RunIdIncrementer())
				.start(purgeExpiredSlotsStep).build();
	}
	
	@Bean
	/**
	 * Performs consumed status job for MOSIP Pre-Registration.
	 *
	 * @param jobRepository job repository used by consumed status job
	 * @param consumedStatusStep consumed status step used by consumed status job
	 * @return consumed status job result
	 */
	public Job consumedStatusJob(JobRepository jobRepository,@Qualifier("consumedStatusStep") Step consumedStatusStep) {
		return new JobBuilder("consumedStatusJob", jobRepository).incrementer(new RunIdIncrementer())
				.start(consumedStatusStep).build();
	}

	@Bean
	/**
	 * Performs identity reconciliation step for MOSIP Pre-Registration.
	 *
	 * @param jobRepository job repository used by identity reconciliation step
	 * @param transactionManager transaction manager used by identity reconciliation step
	 * @return identity reconciliation step result
	 */
	public Step identityReconciliationStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		return new StepBuilder("identityReconciliationStep", jobRepository)
				.tasklet(identityReconciliationTasklet, transactionManager).build();
	}

	@Bean
	/**
	 * Performs identity reconciliation job for MOSIP Pre-Registration.
	 *
	 * @param jobRepository job repository used by identity reconciliation job
	 * @param identityReconciliationStep identity reconciliation step used by identity reconciliation job
	 * @return identity reconciliation job result
	 */
	public Job identityReconciliationJob(JobRepository jobRepository,
			@Qualifier("identityReconciliationStep") Step identityReconciliationStep) {
		return new JobBuilder("identityReconciliationJob", jobRepository).incrementer(new RunIdIncrementer())
				.start(identityReconciliationStep).build();
	}

	@Bean
	/**
	 * Performs expired status job for MOSIP Pre-Registration.
	 *
	 * @param jobRepository job repository used by expired status job
	 * @param expiredStatusStep expired status step used by expired status job
	 * @return expired status job result
	 */
	public Job expiredStatusJob(JobRepository jobRepository,@Qualifier("expiredStatusStep")  Step expiredStatusStep) {
		return new JobBuilder("expiredStatusJob", jobRepository).incrementer(new RunIdIncrementer())
				.start(expiredStatusStep).build();
	}

	@Bean
	/**
	 * Performs update application for booking check job for MOSIP Pre-Registration.
	 *
	 * @param jobRepository job repository used by update application for booking check job
	 * @param updateBookingInApplicationsStep update booking in applications step used by update application for booking check job
	 * @return update application for booking check job result
	 */
	public Job updateApplicationForBookingCheckJob(JobRepository jobRepository,@Qualifier("updateBookingInApplicationsStep") Step updateBookingInApplicationsStep) {
		return new JobBuilder("updateApplicationForBookingCheckJob", jobRepository).incrementer(new RunIdIncrementer())
				.start(updateBookingInApplicationsStep).build();
	}

	@Bean
	/**
	 * Performs reg center partitioner job for MOSIP Pre-Registration.
	 *
	 * @param jobRepository job repository used by reg center partitioner job
	 * @param slotGenerationStep slot generation step used by reg center partitioner job
	 * @return reg center partitioner job result
	 */
	public Job regCenterPartitionerJob(JobRepository jobRepository,@Qualifier("slotGenerationStep") Step slotGenerationStep) {
		return new JobBuilder("regCenterPartitionerJob", jobRepository)
				.preventRestart()
				.incrementer(new RunIdIncrementer())
				.start(slotGenerationStep)
				.build();
	}

	@Bean
	/**
	 * Performs slot generation step for MOSIP Pre-Registration.
	 *
	 * @param jobRepository job repository used by slot generation step
	 * @param transactionManager transaction manager used by slot generation step
	 * @return slot generation step result
	 */
	public Step slotGenerationStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		return new StepBuilder("slotGenerationStep", jobRepository)
				.partitioner("regCenterPartitionerMasterStep", partitionerMasterStep())
				.step(slaveSlotGenerationStep(jobRepository, transactionManager))
				.gridSize(concurrencyLimit)
				.taskExecutor(taskExecutor())
				.build();
	}

	@Bean
	/**
	 * Performs partitioner master step for MOSIP Pre-Registration.
	 *
	 * @return partitioner master step result
	 */
	public Partitioner partitionerMasterStep() {
		return new SlotAvailabilityPartitioner();
	}

	@Bean
	/**
	 * Performs slave slot generation step for MOSIP Pre-Registration.
	 *
	 * @param jobRepository job repository used by slave slot generation step
	 * @param transactionManager transaction manager used by slave slot generation step
	 * @return slave slot generation step result
	 */
	public Step slaveSlotGenerationStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		return new StepBuilder("slaveSlotGenerationStep", jobRepository)
				.tasklet(slotGenerateTasklet(null, null), transactionManager)
				.build();
	}

	@SuppressWarnings({ "unchecked" })
	@Bean
	@StepScope
	/**
	 * Performs slot generate tasklet for MOSIP Pre-Registration.
	 *
	 * @return slot generate tasklet result
	 */
	public Tasklet slotGenerateTasklet(@Value("#{stepExecutionContext['name']}") String name,
			@Value("#{stepExecutionContext['regCenterIdsPartList']}") Object regCenterIdsPartListObj) {
		List<String> regCenterPartList = (List<String>) regCenterIdsPartListObj;
		AvailabilitySyncTasklet slotGeneratorTasklet = new AvailabilitySyncTasklet(name, regCenterPartList);
		return slotGeneratorTasklet;
	}

	@Bean
	/**
	 * Performs task executor for MOSIP Pre-Registration.
	 *
	 * @return task executor result
	 */
	public TaskExecutor taskExecutor() {
		SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor("SlotGenerator");
		asyncTaskExecutor.setConcurrencyLimit(concurrencyLimit);
		return asyncTaskExecutor;
	}
}
