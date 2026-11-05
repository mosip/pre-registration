package io.mosip.preregistration.application.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This class is used to define the start of the demographic service
 * 
 * @author Rajath KR
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = { "io.mosip.preregistration.core.*",
		"io.mosip.preregistration.application.controller.*", "io.mosip.preregistration.application.service.*",
		"io.mosip.preregistration.application.entity.*", "io.mosip.preregistration.application.repository.*",
		"io.mosip.kernel.core.*", "io.mosip.kernel.idobjectvalidator.*", "io.mosip.commons.*",
		"io.mosip.preregistration.application.security.*" })
public class PreRegistrationApplicationTest {
	/**
	 * 
	 * @param args Unused
	 */
	public static void main(String[] args) {
		SpringApplication.run(PreRegistrationApplicationTest.class, args);
	}
}