package io.mosip.preregistration.proxymasterdataservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "io.mosip.*")
public class ProxyMasterdataServiceApplicationTests {
	/**
	 * 
	 * @param args Unused
	 */
	public static void main(String[] args) {
		SpringApplication.run(ProxyMasterdataServiceApplicationTests.class, args);
	}
}
