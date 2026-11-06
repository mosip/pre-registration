package io.mosip.preregistration.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.mosip.preregistration.application.config.OpenApiProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * OpenAPI 3 / SpringDoc for the consolidated Pre-Registration deployable.
 * <p>
 * One document at {@code /v3/api-docs} so Swagger UI is a single page (no
 * "Select a definition" dropdown). The server URL is left unset so SpringDoc
 * fills it from the request, matching MOSIP ID-Repository.
 * </p>
 *
 * @see PreregApiPathConfig
 */
@Configuration
public class PreregOpenApiConfig {

	/** Scheme name shown by Swagger UI as Authorize. */
	public static final String AUTHORIZATION_SCHEME = "Authorization";

	/** OpenAPI title/info properties from {@code openapi.*} bootstrap keys. */
	private final OpenApiProperties openApiProperties;

	/**
	 * Binds {@code openapi.*} properties into the OpenAPI document.
	 *
	 * @param openApiProperties typed {@code openapi.*} properties
	 */
	@Autowired
	public PreregOpenApiConfig(OpenApiProperties openApiProperties) {
		this.openApiProperties = openApiProperties;
	}

	/**
	 * Single OpenAPI document with the Authorize security scheme.
	 *
	 * @return base OpenAPI bean required by SpringDoc
	 */
	@Bean
	public OpenAPI preRegistrationOpenApi() {
		return new OpenAPI()
				.components(new Components().addSecuritySchemes(AUTHORIZATION_SCHEME, authorizationApiKey()))
				.addSecurityItem(new SecurityRequirement().addList(AUTHORIZATION_SCHEME))
				.info(new Info()
						.title(openApiProperties.getInfo().getTitle())
						.version(openApiProperties.getInfo().getVersion())
						.description(openApiProperties.getInfo().getDescription())
						.license(new License()
								.name(openApiProperties.getInfo().getLicense().getName())
								.url(openApiProperties.getInfo().getLicense().getUrl())));
	}

	/**
	 * Header API key named {@code Authorization} (cookie or Bearer token pasted by the tester).
	 *
	 * @return security scheme for Swagger UI Authorize
	 */
	private static SecurityScheme authorizationApiKey() {
		return new SecurityScheme()
				.type(SecurityScheme.Type.APIKEY)
				.in(SecurityScheme.In.HEADER)
				.name(AUTHORIZATION_SCHEME);
	}
}
