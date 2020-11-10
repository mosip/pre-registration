/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.application.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * This class is used to define the start of the demographic service
 * 
 * @author Rajath KR
 * @since 1.0.0
 */
@SpringBootApplication
@ComponentScan(basePackages = "io.mosip.*", excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "io.mosip.kernel.idobjectvalidator.*"))
public class PreRegistrationApplicationTest {
	/**
	 * 
	 * @param args Unused
	 */
	public static void main(String[] args) {
		SpringApplication.run(PreRegistrationApplicationTest.class, args);
	}
}
