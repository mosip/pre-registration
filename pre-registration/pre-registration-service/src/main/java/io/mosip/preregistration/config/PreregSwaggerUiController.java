package io.mosip.preregistration.config;

import static io.mosip.preregistration.common.constant.PreregApiPathConstants.APPLICATION_SWAGGER_PREFIX;
import static io.mosip.preregistration.common.constant.PreregApiPathConstants.BOOKING_SWAGGER_PREFIX;
import static io.mosip.preregistration.common.constant.PreregApiPathConstants.CAPTCHA_SWAGGER_PREFIX;
import static io.mosip.preregistration.common.constant.PreregApiPathConstants.DATASYNC_SWAGGER_PREFIX;
import static io.mosip.preregistration.config.PreregOpenApiConfig.SWAGGER_MOUNTS;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import io.mosip.preregistration.config.PreregOpenApiConfig.SwaggerMount;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Literal swagger catalog mappings so they win over DataSync
 * {@code GET /sync/{preRegistrationId}/{machineId}} and stay on Istio prefixes that
 * already exist ({@code /swagger-ui}, {@code /sync}, {@code /appointment}, {@code /captcha}).
 */
@Controller
public class PreregSwaggerUiController {

	/**
	 * Redirects the root catalog landing pages onto {@code /swagger-ui/index.html}
	 * so they stay on the existing Istio {@code /swagger-ui} prefix.
	 *
	 * @return Spring MVC redirect view name
	 */
	@GetMapping(path = { "/swagger-ui.html", "/swagger-ui", "/swagger-ui/" })
	public String redirectRootSwaggerUi() {
		return "redirect:/swagger-ui/index.html";
	}

	/**
	 * Redirects a per-service {@code swagger-ui.html} landing URL to that catalog's
	 * {@code index.html}. Literal mappings beat DataSync {@code /sync/{id}/{machineId}}.
	 *
	 * @param request inbound catalog request (URI selects the mount)
	 * @return Spring MVC redirect view name
	 */
	@GetMapping(path = {
			APPLICATION_SWAGGER_PREFIX + "/swagger-ui.html",
			APPLICATION_SWAGGER_PREFIX + "/swagger-ui",
			APPLICATION_SWAGGER_PREFIX + "/swagger-ui/",
			DATASYNC_SWAGGER_PREFIX + "/swagger-ui.html",
			DATASYNC_SWAGGER_PREFIX + "/swagger-ui",
			DATASYNC_SWAGGER_PREFIX + "/swagger-ui/",
			CAPTCHA_SWAGGER_PREFIX + "/swagger-ui.html",
			CAPTCHA_SWAGGER_PREFIX + "/swagger-ui",
			CAPTCHA_SWAGGER_PREFIX + "/swagger-ui/",
			BOOKING_SWAGGER_PREFIX + "/swagger-ui.html",
			BOOKING_SWAGGER_PREFIX + "/swagger-ui",
			BOOKING_SWAGGER_PREFIX + "/swagger-ui/"
	})
	public String redirectCatalogSwaggerUi(HttpServletRequest request) {
		SwaggerMount mount = resolveMount(request.getRequestURI()).orElse(SWAGGER_MOUNTS.get(0));
		return "redirect:" + mount.swaggerUiIndexPath();
	}

	/**
	 * Serves the catalog Swagger UI HTML (relative webjar assets + grouped OpenAPI URLs).
	 *
	 * @param request inbound index request (URI selects the mount)
	 * @return HTML document for Swagger UI
	 */
	@GetMapping(path = {
			"/swagger-ui/index.html",
			APPLICATION_SWAGGER_PREFIX + "/swagger-ui/index.html",
			DATASYNC_SWAGGER_PREFIX + "/swagger-ui/index.html",
			CAPTCHA_SWAGGER_PREFIX + "/swagger-ui/index.html",
			BOOKING_SWAGGER_PREFIX + "/swagger-ui/index.html"
	}, produces = MediaType.TEXT_HTML_VALUE)
	@ResponseBody
	public String swaggerUiIndex(HttpServletRequest request) {
		SwaggerMount mount = resolveMount(request.getRequestURI()).orElse(SWAGGER_MOUNTS.get(0));
		return renderSwaggerHtml(mount, request.getContextPath());
	}

	/**
	 * Picks the longest catalog prefix contained in {@code requestUri}.
	 *
	 * @param requestUri servlet URI, possibly {@code null}
	 * @return matching mount, or empty when the URI is not a catalog swagger path
	 */
	static java.util.Optional<SwaggerMount> resolveMount(String requestUri) {
		String path = requestUri == null ? "" : requestUri;
		return SWAGGER_MOUNTS.stream()
				.sorted((a, b) -> Integer.compare(b.uiBasePath().length(), a.uiBasePath().length()))
				.filter(m -> path.contains(m.uiBasePath() + "/swagger-ui"))
				.findFirst();
	}

	/**
	 * Builds the catalog HTML. {@code queryConfigEnabled} is false so a crafted
	 * {@code ?url=} cannot replace the OpenAPI document (and leak Authorize).
	 *
	 * @param mount       catalog whose title and docs URL are primary
	 * @param contextPath servlet context, possibly {@code null}
	 * @return HTML for Swagger UI
	 */
	static String renderSwaggerHtml(SwaggerMount mount, String contextPath) {
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
				        queryConfigEnabled: false,
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

	/**
	 * JSON array of {@code {url,name}} entries for the four catalog OpenAPI documents.
	 *
	 * @param contextPath servlet context, never {@code null} (use {@code ""} )
	 * @return JavaScript array literal
	 */
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
