package io.mosip.preregistration.datasync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * Data sync Application
 * 
 * @author M1046129 - Jagadishwari
 *
 */
@SpringBootApplication
@ComponentScan(basePackages = "io.mosip.*, ${mosip.auth.adapter.impl.basepackage}", excludeFilters = {
		@ComponentScan.Filter(type = FilterType.REGEX, pattern = { "io\\.mosip\\.kernel\\.keymigrate\\..*",
				"io\\.mosip\\.kernel\\.zkcryptoservice\\..*", "io\\.mosip\\.kernel\\.tokenidgenerator\\..*",
				"io\\.mosip\\.kernel\\.signature\\..*", "io\\.mosip\\.kernel\\.partnercertservice\\..*",
				"io\\.mosip\\.kernel\\.lkeymanager\\..*", "io\\.mosip\\.kernel\\.keymanagerservice\\..*",
				"io\\.mosip\\.kernel\\.keymanager\\..*", "io\\.mosip\\.kernel\\.keygenerator\\..*",
				"io\\.mosip\\.kernel\\.cryptomanager\\..*" }) })
public class DataSyncApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataSyncApplication.class, args);
	}
}
