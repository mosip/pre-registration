package io.mosip.preregistration.captcha.config;

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
@ConfigurationProperties("mosip.preregistration.captcha")
public class SwaggerConfig {

	/** The id. */
	private Map<String, String> id;

	/**
	 * Sets the id.
	 *
	 * @param id the id
	 */
	public void setId(Map<String, String> id) {
		this.id = id;
	}

	/**
	 * Id.
	 *
	 * @return the map
	 */
	@Bean
	public Map<String, String> ic() {
		return Collections.unmodifiableMap(id);
	}

	/**
	 * Application Title
	 */
	private static final String TITLE = "Captcha Service";
	/**
	 * Captcha Service
	 */
	private static final String DESCRIPTION = " Captcha Service for Generating and validating captcha";

	/**
	 * Produces {@link ApiInfo}
	 * 
	 * @return {@link ApiInfo}
	 */
	@Autowired
	BuildProperties buildProperties;

	/**
	 * Produces {@link ApiInfo}
	 * 
	 * @return {@link ApiInfo}
	 */
	private ApiInfo apiInfo() {
		return new ApiInfoBuilder().title(TITLE).description(DESCRIPTION).version(buildProperties.getVersion()).build();
	}

	/**
	 *
	 * 
	 * @return Docket bean
	 */
	@Bean
	public Docket api() {

		Docket docket = new Docket(DocumentationType.SWAGGER_2).select()
				.apis(RequestHandlerSelectors.basePackage("io.mosip.preregistration.captcha.controller"))
				.paths(PathSelectors.regex("(?!/(error).*).*")).build().apiInfo(apiInfo());
		return docket;
	}

	@Bean
	public RestTemplate restTemplateBean() {
		return new RestTemplate();
	}
}
