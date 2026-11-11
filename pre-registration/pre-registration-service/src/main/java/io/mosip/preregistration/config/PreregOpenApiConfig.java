package io.mosip.preregistration.config;

import static io.mosip.preregistration.common.constant.PreregApiPathConstants.APPLICATION_SWAGGER_PREFIX;
import static io.mosip.preregistration.common.constant.PreregApiPathConstants.BOOKING_SWAGGER_PREFIX;
import static io.mosip.preregistration.common.constant.PreregApiPathConstants.CAPTCHA_SWAGGER_PREFIX;
import static io.mosip.preregistration.common.constant.PreregApiPathConstants.DATASYNC_SWAGGER_PREFIX;

import java.io.IOException;
import java.util.List;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
 * Same pattern as id-repository {@code IdRepoOpenApiConfig}: one Swagger UI under each
 * former service's catalog path. SpringDoc's single {@code springdoc.swagger-ui.path}
 * cannot hold four values; custom HTML + webjar assets under each prefix avoid the
 * {@code /swagger-ui/swagger-ui/index.html} relative redirect.
 * </p>
 *
 * <h2>Per-service Swagger UI (relative to {@code /preregistration/v1})</h2>
 * <table>
 *   <caption>Legacy catalog URLs</caption>
 *   <tr><th>Service</th><th>Swagger UI</th><th>OpenAPI JSON</th></tr>
 *   <tr><td>Application</td><td>{@code /application-service/swagger-ui.html}</td>
 *       <td>{@code /application-service/v3/api-docs}</td></tr>
 *   <tr><td>Datasync</td><td>{@code /sync/datasync-service/swagger-ui.html}</td>
 *       <td>{@code /sync/datasync-service/v3/api-docs}</td></tr>
 *   <tr><td>Captcha</td><td>{@code /captcha-service/swagger-ui.html}</td>
 *       <td>{@code /captcha-service/v3/api-docs}</td></tr>
 *   <tr><td>Booking</td><td>{@code /appointment/booking-service/swagger-ui.html}</td>
 *       <td>{@code /appointment/booking-service/v3/api-docs}</td></tr>
 * </table>
 *
 * @see PreregApiPathConfig
 */
@Configuration
public class PreregOpenApiConfig implements WebMvcConfigurer {

	/** Mount points: UI base path (no trailing slash), SpringDoc group id, page title. */
	public static final List<SwaggerMount> SWAGGER_MOUNTS = List.of(
			new SwaggerMount(APPLICATION_SWAGGER_PREFIX, "application", "MOSIP Pre-Registration Application"),
			new SwaggerMount(DATASYNC_SWAGGER_PREFIX, "datasync", "MOSIP Pre-Registration Datasync"),
			new SwaggerMount(CAPTCHA_SWAGGER_PREFIX, "captcha", "MOSIP Pre-Registration Captcha"),
			new SwaggerMount(BOOKING_SWAGGER_PREFIX, "booking", "MOSIP Pre-Registration Booking"));

	/** Classpath location of versioned swagger-ui webjar assets (trailing slash). */
	private static final String SWAGGER_UI_WEBJAR_LOCATION = resolveSwaggerUiWebJarLocation();

	/** Scheme name shown by Swagger UI as Authorize. */
	public static final String AUTHORIZATION_SCHEME = "Authorization";

	/** OpenAPI title/info properties from {@code openapi.*} bootstrap keys. */
	private final OpenApiProperties openApiProperties;

	/**
	 * Binds {@code openapi.*} properties into the OpenAPI document.
	 *
	 * @param openApiProperties typed {@code openapi.*} properties
	 */
	public PreregOpenApiConfig(OpenApiProperties openApiProperties) {
		this.openApiProperties = openApiProperties;
	}

	/**
	 * @param uiBasePath service public prefix (e.g. {@code /application-service})
	 * @param groupId    SpringDoc {@link GroupedOpenApi} group name
	 * @param title      HTML / OpenAPI title
	 */
	public record SwaggerMount(String uiBasePath, String groupId, String title) {

		/** @return {@code {uiBasePath}/swagger-ui/index.html} */
		public String swaggerUiIndexPath() {
			return uiBasePath + "/swagger-ui/index.html";
		}

		/**
		 * Public OpenAPI JSON path (legacy per-service URL).
		 *
		 * @return {@code {uiBasePath}/v3/api-docs}
		 */
		public String apiDocsPath() {
			return uiBasePath + "/v3/api-docs";
		}

		/** Internal SpringDoc group document forwarded from {@link #apiDocsPath()}. */
		public String springDocGroupPath() {
			return "/v3/api-docs/" + groupId;
		}
	}

	/**
	 * Serves swagger-ui CSS/JS under each catalog prefix so Istio does not need a root
	 * {@code /swagger-ui} route for assets.
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/swagger-ui/**")
				.addResourceLocations(SWAGGER_UI_WEBJAR_LOCATION)
				.resourceChain(false);
		for (SwaggerMount mount : SWAGGER_MOUNTS) {
			registry.addResourceHandler(mount.uiBasePath() + "/swagger-ui/**")
					.addResourceLocations(SWAGGER_UI_WEBJAR_LOCATION)
					.resourceChain(false);
		}
	}

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		for (SwaggerMount mount : SWAGGER_MOUNTS) {
			registry.addViewController(mount.uiBasePath() + "/v3/api-docs")
					.setViewName("forward:" + mount.springDocGroupPath());
			registry.addViewController(mount.uiBasePath() + "/v3/api-docs/")
					.setViewName("forward:" + mount.springDocGroupPath());
		}
	}

	/**
	 * Shared OpenAPI shell; each {@link GroupedOpenApi} overrides {@link Info} via customizer.
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

	@Bean
	public GroupedOpenApi applicationApi() {
		return GroupedOpenApi.builder()
				.group("application")
				.displayName("Pre-Registration Application")
				.packagesToScan("io.mosip.preregistration.application.controller")
				.addOpenApiCustomizer(openApi -> applyAuthorization(openApi)
						.info(serviceInfo("MOSIP Pre-Registration Application",
								"Demographic, document, login, notification, and application-appointment APIs")))
				.build();
	}

	@Bean
	public GroupedOpenApi datasyncApi() {
		return GroupedOpenApi.builder()
				.group("datasync")
				.displayName("Pre-Registration Datasync")
				.packagesToScan("io.mosip.preregistration.datasync.controller")
				.addOpenApiCustomizer(openApi -> applyAuthorization(openApi)
						.info(serviceInfo("MOSIP Pre-Registration Datasync",
								"Forward and reverse sync APIs")))
				.build();
	}

	@Bean
	public GroupedOpenApi captchaApi() {
		return GroupedOpenApi.builder()
				.group("captcha")
				.displayName("Pre-Registration Captcha")
				.packagesToScan("io.mosip.preregistration.captcha.controller")
				.addOpenApiCustomizer(openApi -> applyAuthorization(openApi)
						.info(serviceInfo("MOSIP Pre-Registration Captcha",
								"Captcha validation API")))
				.build();
	}

	@Bean
	public GroupedOpenApi bookingApi() {
		return GroupedOpenApi.builder()
				.group("booking")
				.displayName("Pre-Registration Booking")
				.packagesToScan("io.mosip.preregistration.booking.controller")
				.addOpenApiCustomizer(openApi -> applyAuthorization(openApi)
						.info(serviceInfo("MOSIP Pre-Registration Booking",
								"Native /appointment/** booking APIs")))
				.build();
	}

	private static SecurityScheme authorizationApiKey() {
		return new SecurityScheme()
				.type(SecurityScheme.Type.APIKEY)
				.in(SecurityScheme.In.HEADER)
				.name(AUTHORIZATION_SCHEME);
	}

	private static OpenAPI applyAuthorization(OpenAPI api) {
		if (api.getComponents() == null) {
			api.setComponents(new Components());
		}
		api.getComponents().addSecuritySchemes(AUTHORIZATION_SCHEME, authorizationApiKey());
		api.addSecurityItem(new SecurityRequirement().addList(AUTHORIZATION_SCHEME));
		return api;
	}

	private Info serviceInfo(String title, String description) {
		return new Info()
				.title(title)
				.description(description)
				.version(openApiProperties.getInfo().getVersion())
				.license(new License()
						.name(openApiProperties.getInfo().getLicense().getName())
						.url(openApiProperties.getInfo().getLicense().getUrl()));
	}

	private static String resolveSwaggerUiWebJarLocation() {
		try {
			Resource[] resources = new PathMatchingResourcePatternResolver()
					.getResources("classpath*:/META-INF/resources/webjars/swagger-ui/*/swagger-ui.css");
			if (resources.length > 0) {
				String url = resources[0].getURL().toExternalForm();
				int meta = url.indexOf("META-INF/resources/webjars/swagger-ui/");
				int css = url.lastIndexOf("/swagger-ui.css");
				if (meta >= 0 && css > meta) {
					return "classpath:/" + url.substring(meta, css + 1);
				}
			}
		} catch (IOException ignored) {
			// fall through to default
		}
		return "classpath:/META-INF/resources/webjars/swagger-ui/";
	}
}
