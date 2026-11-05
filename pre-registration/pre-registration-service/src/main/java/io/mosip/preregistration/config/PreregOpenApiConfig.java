package io.mosip.preregistration.config;

import static io.mosip.preregistration.common.constant.PreregApiPathConstants.CAPTCHA_PATH_PREFIX;

import org.springdoc.core.models.GroupedOpenApi;
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
import io.swagger.v3.oas.models.servers.Server;

/**
 * OpenAPI 3 / SpringDoc for the consolidated Pre-Registration deployable.
 * <p>
 * Registers an API-key {@code Authorization} header so Swagger UI shows the Authorize
 * button (same pattern as kernel-auth-service and kernel-bio-converter). Groups keep
 * application, datasync, and captcha documents separate under the frozen path prefixes.
 * </p>
 *
 * @see PreregApiPathConfig
 */
@Configuration
public class PreregOpenApiConfig {

	/** Scheme name shown by Swagger UI as Authorize. */
	public static final String AUTHORIZATION_SCHEME = "Authorization";

	/** OpenAPI title/server properties from {@code openapi.*} bootstrap keys. */
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
	 * Shared OpenAPI shell with the Authorize security scheme.
	 *
	 * @return base OpenAPI bean required by SpringDoc
	 */
	@Bean
	public OpenAPI preRegistrationOpenApi() {
		OpenAPI api = new OpenAPI()
				.components(new Components().addSecuritySchemes(AUTHORIZATION_SCHEME, authorizationApiKey()))
				.addSecurityItem(new SecurityRequirement().addList(AUTHORIZATION_SCHEME))
				.info(new Info()
						.title(openApiProperties.getInfo().getTitle())
						.version(openApiProperties.getInfo().getVersion())
						.description(openApiProperties.getInfo().getDescription())
						.license(new License()
								.name(openApiProperties.getInfo().getLicense().getName())
								.url(openApiProperties.getInfo().getLicense().getUrl())));
		if (openApiProperties.getService() != null && openApiProperties.getService().getServers() != null) {
			openApiProperties.getService().getServers()
					.forEach(server -> api.addServersItem(
							new Server().description(server.getDescription()).url(server.getUrl())));
		}
		return api;
	}

	/**
	 * Application APIs (demographic, document, login, booking proxy, notification).
	 *
	 * @return grouped OpenAPI for application controllers
	 */
	@Bean
	public GroupedOpenApi applicationApi() {
		return GroupedOpenApi.builder()
				.group("application")
				.displayName("Pre-Registration Application")
				.packagesToScan("io.mosip.preregistration.application.controller")
				.pathsToExclude("/sync/**", CAPTCHA_PATH_PREFIX + "/**")
				.addOpenApiCustomizer(this::applyAuthorization)
				.build();
	}

	/**
	 * Datasync APIs used by Registration Client and Registration Processor.
	 *
	 * @return grouped OpenAPI for datasync
	 */
	@Bean
	public GroupedOpenApi datasyncApi() {
		return GroupedOpenApi.builder()
				.group("datasync")
				.displayName("Pre-Registration Datasync")
				.packagesToScan("io.mosip.preregistration.datasync.controller")
				.pathsToMatch("/sync/**", "/syncV2/**")
				.addOpenApiCustomizer(this::applyAuthorization)
				.build();
	}

	/**
	 * Captcha validation API at the frozen {@code /captcha} prefix.
	 *
	 * @return grouped OpenAPI for captcha
	 */
	/**
	 * Frozen booking-native APIs ({@code /appointment/**}).
	 *
	 * @return grouped OpenAPI for booking
	 */
	@Bean
	public GroupedOpenApi bookingApi() {
		return GroupedOpenApi.builder()
				.group("booking")
				.displayName("Pre-Registration Booking")
				.packagesToScan("io.mosip.preregistration.booking.controller")
				.pathsToMatch("/appointment/**")
				.addOpenApiCustomizer(this::applyAuthorization)
				.build();
	}

	@Bean
	public GroupedOpenApi captchaApi() {
		return GroupedOpenApi.builder()
				.group("captcha")
				.displayName("Pre-Registration Captcha")
				.packagesToScan("io.mosip.preregistration.captcha.controller")
				.pathsToMatch(CAPTCHA_PATH_PREFIX + "/**")
				.addOpenApiCustomizer(this::applyAuthorization)
				.build();
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

	/**
	 * Ensures every group document carries the Authorize scheme.
	 *
	 * @param openApi group document being customized
	 * @return the same instance
	 */
	private OpenAPI applyAuthorization(OpenAPI openApi) {
		if (openApi.getComponents() == null) {
			openApi.setComponents(new Components());
		}
		openApi.getComponents().addSecuritySchemes(AUTHORIZATION_SCHEME, authorizationApiKey());
		openApi.addSecurityItem(new SecurityRequirement().addList(AUTHORIZATION_SCHEME));
		return openApi;
	}
}
