/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.batchjob.job;

import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.support.Partitioner;
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
	private int concurrencyLimit;

	@Autowired
	private ConsumedStatusTasklet consumedStatusTasklet;

	@Autowired
	private PurgeExpiredRegCentersSlotsTasklet purgeRegCenterSlotsTasklet;

	@Autowired
	private ExpiredStatusTasklet expiredStatusTasklet;

	@Autowired
	private ApplicationsBookingCheckTasklet applicationBookingCheckTasklet;

//  Commeting it as transactionManager is imported from Kernel-Auth-Adapter Jar
//	@Bean
//	public PlatformTransactionManager transactionManager(DataSource dataSource) {
//		return new DataSourceTransactionManager(dataSource);
//	}
	
	@Bean
	public Step consumedStatusStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		return new StepBuilder("consumedStatusStep", jobRepository).tasklet(consumedStatusTasklet, transactionManager)
				.build();
	}

	@Bean
	public Step purgeExpiredSlotsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		return new StepBuilder("purgeExpiredSlotsStep", jobRepository)
				.tasklet(purgeRegCenterSlotsTasklet, transactionManager).build();
	}

	@Bean
	public Step expiredStatusStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		return new StepBuilder("expiredStatusStep", jobRepository).tasklet(expiredStatusTasklet, transactionManager)
				.build();
	}

	@Bean
	public Step updateBookingInApplicationsStep(JobRepository jobRepository,
			PlatformTransactionManager transactionManager) {
		return new StepBuilder("updateBookingInApplicationsStep", jobRepository)
				.tasklet(applicationBookingCheckTasklet, transactionManager).build();
	}

	@Bean
	public Job purgeExpiredSlotsJob(JobRepository jobRepository,@Qualifier("purgeExpiredSlotsStep") Step purgeExpiredSlotsStep) {
		return new JobBuilder("purgeExpiredSlotsJob", jobRepository).incrementer(new RunIdIncrementer())
				.start(purgeExpiredSlotsStep).build();
	}
	
	@Bean
	public Job consumedStatusJob(JobRepository jobRepository,@Qualifier("consumedStatusStep") Step consumedStatusStep) {
		return new JobBuilder("consumedStatusJob", jobRepository).incrementer(new RunIdIncrementer())
				.start(consumedStatusStep).build();
	}

	@Bean
	public Job expiredStatusJob(JobRepository jobRepository,@Qualifier("expiredStatusStep")  Step expiredStatusStep) {
		return new JobBuilder("expiredStatusJob", jobRepository).incrementer(new RunIdIncrementer())
				.start(expiredStatusStep).build();
	}

	@Bean
	public Job updateApplicationForBookingCheckJob(JobRepository jobRepository,@Qualifier("updateBookingInApplicationsStep") Step updateBookingInApplicationsStep) {
		return new JobBuilder("updateApplicationForBookingCheckJob", jobRepository).incrementer(new RunIdIncrementer())
				.start(updateBookingInApplicationsStep).build();
	}

	@Bean
	public Job regCenterPartitionerJob(JobRepository jobRepository,@Qualifier("slotGenerationStep") Step slotGenerationStep) {
		return new JobBuilder("regCenterPartitionerJob", jobRepository)
				.preventRestart()
				.incrementer(new RunIdIncrementer())
				.start(slotGenerationStep)
				.build();
	}

	@Bean
	public Step slotGenerationStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		return new StepBuilder("slotGenerationStep", jobRepository)
				.partitioner("regCenterPartitionerMasterStep", partitionerMasterStep())
				.step(slaveSlotGenerationStep(jobRepository, transactionManager))
				.gridSize(concurrencyLimit)
				.taskExecutor(taskExecutor())
				.build();
	}

	@Bean
	public Partitioner partitionerMasterStep() {
		return new SlotAvailabilityPartitioner();
	}

	@Bean
	public Step slaveSlotGenerationStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		return new StepBuilder("slaveSlotGenerationStep", jobRepository)
				.tasklet(slotGenerateTasklet(null, null), transactionManager)
				.build();
	}

	@SuppressWarnings({ "unchecked" })
	@Bean
	@StepScope
	public Tasklet slotGenerateTasklet(@Value("#{stepExecutionContext['name']}") String name,
			@Value("#{stepExecutionContext['regCenterIdsPartList']}") Object regCenterIdsPartListObj) {
		List<String> regCenterPartList = (List<String>) regCenterIdsPartListObj;
		AvailabilitySyncTasklet slotGeneratorTasklet = new AvailabilitySyncTasklet(name, regCenterPartList);
		return slotGeneratorTasklet;
	}

	@Bean
	public TaskExecutor taskExecutor() {
		SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor("SlotGenerator");
		asyncTaskExecutor.setConcurrencyLimit(concurrencyLimit);
		return asyncTaskExecutor;
	}
}
