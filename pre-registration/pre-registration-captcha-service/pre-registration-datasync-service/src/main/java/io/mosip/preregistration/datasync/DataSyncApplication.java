package io.mosip.preregistration.datasync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Data sync Application
 * 
 * @author M1046129 - Jagadishwari
 *
 */
@SpringBootApplication
@ComponentScan(basePackages = "io.mosip.*")
public class DataSyncApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataSyncApplication.class, args);
	}
}
