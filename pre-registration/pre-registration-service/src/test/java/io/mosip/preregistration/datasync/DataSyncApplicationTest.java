package io.mosip.preregistration.datasync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Marker Spring Boot configuration for datasync slice tests. Scan is empty so
 * {@code @WebMvcTest} / {@code @SpringBootTest(classes = ...)} do not pull the
 * full HTTP application or kernel generators.
 */
@SpringBootApplication(scanBasePackages = {})
public class DataSyncApplicationTest {
	public static void main(String[] args) {
		SpringApplication.run(DataSyncApplicationTest.class, args);
	}
}
