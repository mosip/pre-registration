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

	/** Server list advertised in the OpenAPI document. */
	private Service service;

	/** Legacy single-group settings (still bound; grouped APIs are defined in Java). */
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

	/** Wrapper for the servers list. */
	@Data
	public static class Service {
		/** Advertised OpenAPI servers. */
		private List<Server> servers;
	}

	/** One OpenAPI server entry. */
	@Data
	public static class Server {
		/** Server description. */
		private String description;
		/** Server URL (usually includes the servlet context). */
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
