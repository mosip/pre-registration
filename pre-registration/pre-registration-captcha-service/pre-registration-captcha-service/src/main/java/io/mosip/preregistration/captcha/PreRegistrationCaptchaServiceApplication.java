package io.mosip.preregistration.captcha;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages="io.mosip.*")
public class PreRegistrationCaptchaServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PreRegistrationCaptchaServiceApplication.class, args);
	}

}
