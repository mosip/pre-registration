package io.mosip.preregistration.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@EnableCaching
@SpringBootApplication
@ComponentScan(basePackages = { "io.mosip.*", "${mosip.lang.traslate.adapter.impl.basepackage}" }, excludeFilters = {
		@ComponentScan.Filter(type = FilterType.REGEX, pattern = {
				"io\\.mosip\\.kernel\\.keymigrate\\.service\\.spi\\.KeyMigratorService",
				"io\\.mosip\\.kernel\\.keymigrate\\.service\\.impl\\.KeyMigratorServiceImpl",
				"io\\.mosip\\.kernel\\.keymigrate\\..*", "io\\.mosip\\.kernel\\.zkcryptoservice\\..*",
				"io\\.mosip\\.kernel\\.tokenidgenerator\\..*", "io\\.mosip\\.kernel\\.signature\\..*",
				"io\\.mosip\\.kernel\\.partnercertservice\\..*", "io\\.mosip\\.kernel\\.lkeymanager\\..*",
				"io\\.mosip\\.kernel\\.keymanagerservice\\..*", "io\\.mosip\\.kernel\\.keymanager\\..*",
				"io\\.mosip\\.kernel\\.keygenerator\\..*", "io\\.mosip\\.kernel\\.cryptomanager\\..*",
				"io\\.mosip\\.kernel\\.crypto\\..*", "io\\.mosip\\.kernel\\.clientcrypto\\..*" }) })
public class PreRegistrationApplication {
	public static void main(String[] args) {
		SpringApplication.run(PreRegistrationApplication.class, args);
	}
}