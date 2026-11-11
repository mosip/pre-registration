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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
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
import jakarta.servlet.http.HttpServletRequest;

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
		for (SwaggerMount mount : SWAGGER_MOUNTS) {
			registry.addResourceHandler(mount.uiBasePath() + "/swagger-ui/**")
					.addResourceLocations(SWAGGER_UI_WEBJAR_LOCATION)
					.resourceChain(true);
		}
	}

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		for (SwaggerMount mount : SWAGGER_MOUNTS) {
			String index = mount.swaggerUiIndexPath();
			registry.addRedirectViewController(mount.uiBasePath() + "/swagger-ui.html", index);
			registry.addRedirectViewController(mount.uiBasePath() + "/swagger-ui", index);
			registry.addRedirectViewController(mount.uiBasePath() + "/swagger-ui/", index);
			registry.addViewController(mount.uiBasePath() + "/v3/api-docs")
					.setViewName("forward:" + mount.springDocGroupPath());
			registry.addViewController(mount.uiBasePath() + "/v3/api-docs/")
					.setViewName("forward:" + mount.springDocGroupPath());
		}
		String applicationIndex = SWAGGER_MOUNTS.get(0).swaggerUiIndexPath();
		registry.addRedirectViewController("/swagger-ui.html", applicationIndex);
		registry.addRedirectViewController("/swagger-ui", applicationIndex);
		registry.addRedirectViewController("/swagger-ui/", applicationIndex);
		registry.addRedirectViewController("/swagger-ui/index.html", applicationIndex);
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
				int marker = url.lastIndexOf("/swagger-ui.css");
				if (marker > 0) {
					String dir = url.substring(0, marker + 1);
					if (dir.startsWith("jar:")) {
						int bang = dir.indexOf("!/");
						if (bang >= 0) {
							return "classpath:" + dir.substring(bang + 1);
						}
					}
					if (dir.contains("META-INF/resources/webjars/swagger-ui/")) {
						return "classpath:/" + dir.substring(dir.indexOf("META-INF/"));
					}
				}
			}
		} catch (IOException ignored) {
			// fall through to default
		}
		return "classpath:/META-INF/resources/webjars/swagger-ui/";
	}

	/**
	 * Dedicated Swagger UI HTML under each legacy catalog prefix. Asset hrefs are relative
	 * so the browser requests {@code {prefix}/swagger-ui/swagger-ui.css}, which Istio already
	 * routes when the prefix is {@code /appointment/**} or {@code /sync/**}.
	 */
	@Controller
	public static class PerServiceSwaggerUiController {

		@GetMapping(path = {
				APPLICATION_SWAGGER_PREFIX + "/swagger-ui/index.html",
				DATASYNC_SWAGGER_PREFIX + "/swagger-ui/index.html",
				CAPTCHA_SWAGGER_PREFIX + "/swagger-ui/index.html",
				BOOKING_SWAGGER_PREFIX + "/swagger-ui/index.html"
		}, produces = MediaType.TEXT_HTML_VALUE)
		@ResponseBody
		public String swaggerUiIndex(HttpServletRequest request) {
			SwaggerMount mount = resolveMount(request.getRequestURI())
					.orElse(SWAGGER_MOUNTS.get(0));
			return renderSwaggerHtml(mount, request.getContextPath());
		}

		private static java.util.Optional<SwaggerMount> resolveMount(String requestUri) {
			String path = requestUri == null ? "" : requestUri;
			return SWAGGER_MOUNTS.stream()
					.sorted((a, b) -> Integer.compare(b.uiBasePath().length(), a.uiBasePath().length()))
					.filter(m -> path.contains(m.uiBasePath() + "/swagger-ui"))
					.findFirst();
		}

		private static String renderSwaggerHtml(SwaggerMount mount, String contextPath) {
			String title = escape(mount.title());
			String urls = swaggerDefinitionUrls(contextPath == null ? "" : contextPath);
			String primaryName = escape(mount.title());
			return """
					<!DOCTYPE html>
					<html lang="en">
					<head>
					  <meta charset="UTF-8">
					  <title>%s</title>
					  <link rel="stylesheet" type="text/css" href="swagger-ui.css">
					  <link rel="icon" type="image/png" href="favicon-32x32.png" sizes="32x32">
					</head>
					<body>
					  <div id="swagger-ui"></div>
					  <script src="swagger-ui-bundle.js"></script>
					  <script src="swagger-ui-standalone-preset.js"></script>
					  <script>
					    window.onload = function() {
					      window.ui = SwaggerUIBundle({
					        urls: %s,
					        'urls.primaryName': '%s',
					        dom_id: '#swagger-ui',
					        deepLinking: true,
					        queryConfigEnabled: true,
					        presets: [SwaggerUIBundle.presets.apis, SwaggerUIStandalonePreset],
					        plugins: [SwaggerUIBundle.plugins.DownloadUrl],
					        layout: 'StandaloneLayout',
					        persistAuthorization: true,
					        validatorUrl: ''
					      });
					    };
					  </script>
					</body>
					</html>
					""".formatted(title, urls, primaryName);
		}

		private static String swaggerDefinitionUrls(String contextPath) {
			StringBuilder urls = new StringBuilder("[");
			for (int i = 0; i < SWAGGER_MOUNTS.size(); i++) {
				SwaggerMount mount = SWAGGER_MOUNTS.get(i);
				if (i > 0) {
					urls.append(',');
				}
				urls.append("{url:'").append(escape(contextPath + mount.apiDocsPath()))
						.append("',name:'").append(escape(mount.title())).append("'}");
			}
			urls.append(']');
			return urls.toString();
		}

		private static String escape(String value) {
			return value.replace("'", "\\'").replace("<", "&lt;").replace(">", "&gt;");
		}
	}
}
