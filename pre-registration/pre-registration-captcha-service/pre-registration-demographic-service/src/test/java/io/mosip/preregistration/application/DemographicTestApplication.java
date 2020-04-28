/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This class is used to define the start of the demographic service
 * 
 * @author Rajath KR
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages= {"io.mosip.preregistration.core.*,io.mosip.preregistration.document.*,io.mosip.preregistration.application.*,io.mosip.kernel.emailnotifier.*,io.mosip.kernel.smsnotifier.*,io.mosip.kernel.cryotomanager.*,io.mosip.kernel.auditmanger.*,io.mosip.kernel.idgenerator.*,io.mosip.kernel.jsonvalidator.*"})
//@ComponentScan(basePackages = "io.mosip.*")
public class DemographicTestApplication {
	/**
	 * 
	 * @param args Unused
	 */
	public static void main(String[] args) {
		SpringApplication.run(DemographicTestApplication.class, args);
	}
}
