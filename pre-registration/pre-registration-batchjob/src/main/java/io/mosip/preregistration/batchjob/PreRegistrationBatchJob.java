package io.mosip.preregistration.batchjob;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

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
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = { "io\\.mosip\\.kernel\\.core\\.logger\\.config\\..*",
                "io\\.mosip\\.kernel\\.zkcryptoservice\\..*",
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
                "io\\.mosip\\.kernel\\.datamapper\\..*",
                "io\\.mosip\\.kernel\\.idgenerator\\.mispid\\..*",
                "io.mosip.kernel.dataaccess.hibernate.config.*",
        }),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = { HibernateDaoConfig.class })
})
/**
 * Class supporting pre registration batch job in MOSIP Pre-Registration (io.mosip.preregistration.batchjob).
 */
public class PreRegistrationBatchJob {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(PreRegistrationBatchJob.class, args);
	}
}
