package io.mosip.preregistration.application.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * Binds {@code openapi.*} keys from bootstrap/config-server into typed beans used by
 * {@link io.mosip.preregistration.config.PreregOpenApiConfig}.
 */
@Configuration
@ConfigurationProperties(prefix = "openapi")
@Data
public class OpenApiProperties {

	/** Info block shown in Swagger UI. */
	private InfoProperty info;

	/** Legacy group keys still bound from bootstrap; unused after the single-document OpenAPI change. */
	private Group group;

	/** OpenAPI info section. */
	@Data
	public static class InfoProperty {
		/** API title. */
		private String title;
		/** API description. */
		private String description;
		/** API version string. */
		private String version;
		/** License block. */
		private LicenseProperty license;
	}

	/** License name and URL. */
	@Data
	public static class LicenseProperty {
		/** License display name. */
		private String name;
		/** License URL. */
		private String url;
	}

	/** Legacy group binding. */
	@Data
	public static class Group {
		/** Group name. */
		private String name;
		/** Path globs. */
		private List<String> paths;
	}
}
