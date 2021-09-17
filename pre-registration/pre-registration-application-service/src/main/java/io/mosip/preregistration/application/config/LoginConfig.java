package io.mosip.preregistration.application.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class LoginConfig {

	@Autowired
	BuildProperties buildProperties;

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI().info(new Info().title(buildProperties.getName()).version(buildProperties.getVersion())
				.description("Maven Spring Boot Project of MOSIP Pre-Registration Service"));

	}

}
