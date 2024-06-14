/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.batchjob;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

import io.mosip.kernel.dataaccess.hibernate.config.HibernateDaoConfig;

/**
 * This class is used to define the start of the Booking application.
 * 
 * @author Aiham Hasan
 * @since 1.2.0
 *
 */

@SpringBootApplication
@ComponentScan(basePackages = "io.mosip.*", excludeFilters = {
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = { "io\\.mosip\\.kernel\\.zkcryptoservice\\..*",
                "io\\.mosip\\.kernel\\.tokenidgenerator\\..*",
                "io\\.mosip\\.kernel\\.signature\\..*",
                "io\\.mosip\\.kernel\\.partnercertservice\\..*",
                "io\\.mosip\\.kernel\\.lkeymanager\\..*",
                "io\\.mosip\\.kernel\\.keymanagerservice\\..*",
                "io\\.mosip\\.kernel\\.keymanager\\..*",
                "io\\.mosip\\.kernel\\.keygenerator\\..*",
                "io\\.mosip\\.kernel\\.cryptomanager\\..*",
                "io\\.mosip\\.kernel\\.crypto\\..*",
                "io\\.mosip\\.kernel\\.clientcrypto\\..*",
                "io.mosip.kernel.dataaccess.hibernate.config.*",
        }),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = { HibernateDaoConfig.class })
})
public class PreRegistrationBatchJob {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(PreRegistrationBatchJob.class, args);
	}
}
