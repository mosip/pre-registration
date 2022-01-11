/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.batchjob.job;

import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import io.mosip.preregistration.batchjob.impl.SlotAvailabilityPartitioner;
import io.mosip.preregistration.batchjob.tasklets.ApplicationsBookingCheckTasklet;
import io.mosip.preregistration.batchjob.tasklets.AvailabilitySyncTasklet;
import io.mosip.preregistration.batchjob.tasklets.ConsumedStatusTasklet;
import io.mosip.preregistration.batchjob.tasklets.ExpiredStatusTasklet;
import io.mosip.preregistration.batchjob.tasklets.PurgeExpiredRegCentersSlotsTasklet;

/**
 * @author Kishan Rathore
 * @since 1.0.0
 *
 */
@Configuration
@EnableBatchProcessing
public class PreRegistrationBatchJobConfig {

	@Value("${preregistration.slots.generate.thread.count:20}")
	private int concurrencyLimit;

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Autowired
	private ConsumedStatusTasklet consumedStatusTasklet;

	@Autowired
	private PurgeExpiredRegCentersSlotsTasklet purgeRegCenterSlotsTasklet;

	@Autowired
	private ExpiredStatusTasklet expiredStatusTasklet;
	
	@Autowired
	private ApplicationsBookingCheckTasklet applicationBookingCheckTasklet;

	@Bean
	public Step consumedStatusStep() {
		return stepBuilderFactory.get("consumedStatusStep").tasklet(consumedStatusTasklet).build();
	}

	@Bean
	public Step purgeExpiredSlotsStep() {
		return stepBuilderFactory.get("purgeExpiredSlotsStep").tasklet(purgeRegCenterSlotsTasklet).build();
	}

	@Bean
	public Step expiredStatusStep() {
		return stepBuilderFactory.get("expiredStatusStep").tasklet(expiredStatusTasklet).build();
	}

	@Bean
	public Step updateBookingInApplicationsStep() {
		return stepBuilderFactory.get("updateBookingInApplicationsStep").tasklet(applicationBookingCheckTasklet).build();
	}

	@Bean
	public Job purgeExpiredSlotsJob() {
		return this.jobBuilderFactory.get("purgeExpiredSlotsJob").incrementer(new RunIdIncrementer())
				.start(purgeExpiredSlotsStep()).build();
	}

	@Bean
	public Job consumedStatusJob() {
		return this.jobBuilderFactory.get("consumedStatusJob").incrementer(new RunIdIncrementer())
				.start(consumedStatusStep()).build();
	}

	@Bean
	public Job expiredStatusJob() {
		return this.jobBuilderFactory.get("expiredStatusJob").incrementer(new RunIdIncrementer())
				.start(expiredStatusStep()).build();
	}

	@Bean
	public Job updateApplicationForBookingCheckJob() {
		return this.jobBuilderFactory.get("updateApplicationForBookingCheckJob").incrementer(new RunIdIncrementer())
				.start(updateBookingInApplicationsStep()).build();
	}

	@Bean(name="regCenterPartitionerJob")
	public Job regCenterPartitionerJob() {
		return this.jobBuilderFactory.get("regCenterPartitionerJob")
									 .preventRestart()
								     .incrementer(new RunIdIncrementer())
									 .start(slotGenerationStep())
									 .build();
	}

	@Bean
	public Step slotGenerationStep() {
		return stepBuilderFactory.get("slotGenerationStep")
								 .partitioner("regCenterPartitionerMasterStep", partitionerMasterStep())
								 .step(slaveSlotGenerationStep())
								 .gridSize(concurrencyLimit)
								 .taskExecutor(taskExecutor())
								 .build();
	}

	@Bean
	public Partitioner partitionerMasterStep() {
		return new SlotAvailabilityPartitioner();
	}

	@Bean
	public Step slaveSlotGenerationStep() {
		return stepBuilderFactory.get("slaveSlotGenerationStep")
								 .tasklet(slotGenerateTasklet(null, null))
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
	public TaskExecutor taskExecutor(){
		SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor("SlotGenerator");
		asyncTaskExecutor.setConcurrencyLimit(concurrencyLimit);
		return asyncTaskExecutor;
	}
}
