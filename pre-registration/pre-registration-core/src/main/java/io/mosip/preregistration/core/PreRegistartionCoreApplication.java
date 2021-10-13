package io.mosip.preregistration.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/*
 * (non-Javadoc)
 * 
 *
 */

@SpringBootApplication
@ComponentScan(basePackages = "io.mosip.*", excludeFilters = {
		@ComponentScan.Filter(type = FilterType.REGEX,
				pattern = {"io\\.mosip\\.kernel\\.keymigrate\\..*",
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
				}) })
public class PreRegistartionCoreApplication {
	public static void main(String[] args) {
		System.out.println("PreRegistartionCoreApplication started....");
		SpringApplication.run(PreRegistartionCoreApplication.class, args);
	}
}
