package io.mosip.preregistration.captcha;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@ComponentScan(basePackages = { "io.mosip.preregistration.captcha.*",
		"io.mosip.preregistration.core.*" }, excludeFilters = @ComponentScan.Filter(type = FilterType.ASPECTJ, pattern = {
				"io.mosip.preregistration.core.config.RestInterceptor",
				"io.mosip.preregistration.core.config.SSLConfig", "io.mosip.preregistration.core.util.AuditLogUtil" }))
public class PreRegistrationCaptchaServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PreRegistrationCaptchaServiceApplication.class, args);
	}

}
