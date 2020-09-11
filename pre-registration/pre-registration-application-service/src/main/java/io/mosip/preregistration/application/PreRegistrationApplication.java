package io.mosip.preregistration.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import io.mosip.preregistration.application.PreRegistrationApplication;

@SpringBootApplication
@ComponentScan(basePackages = "io.mosip.*")
public class PreRegistrationApplication {
	public static void main(String[] args) {
		SpringApplication.run(PreRegistrationApplication.class, args);
	}
}
