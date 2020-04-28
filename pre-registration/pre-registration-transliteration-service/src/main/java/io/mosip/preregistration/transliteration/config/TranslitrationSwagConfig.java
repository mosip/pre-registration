/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.transliteration.config;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * This class is used for Swagger configuration, also to configure Host and
 * Port.
 * 
 * @author Kishan Rathore
 * @since 1.0.0
 *
 */
@Configuration
@EnableSwagger2
@ConfigurationProperties("mosip.pre-registration.transliteration")
public class TranslitrationSwagConfig {
	
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
	private Logger log = LoggerConfiguration.logConfig(TranslitrationSwagConfig.class);

	/**
	 * Reference for ${application.env.local:false} from property file.
	 */
	@Value("${application.env.local:false}")
	private Boolean localEnv;

	/**
	 * Reference for ${swagger.base-url:#{null}} from property file.
	 */
	@Value("${swagger.base-url:#{null}}")
	private String swaggerBaseUrl;

	/**
	 * Reference for ${server.port:9098} from property file.
	 */
	@Value("${server.port:9098}")
	private int serverPort;

	/**
	 * To define Protocol
	 */
	String proto = "http";

	/**
	 * To define Host
	 */
	String host = "localhost";

	/**
	 * To define port
	 */
	int port = -1;

	/**
	 * To define host along with the port
	 */
	String hostWithPort = "localhost:9098";

	/**
	 * To configure Host and port along with docket.
	 * 
	 * @return Docket docket
	 */
	@Bean
	public Docket api() {
		boolean swaggerBaseUrlSet = false;
		if (!localEnv && swaggerBaseUrl != null && !swaggerBaseUrl.isEmpty()) {
			try {
				proto = new URL(swaggerBaseUrl).getProtocol();
				host = new URL(swaggerBaseUrl).getHost();
				port = new URL(swaggerBaseUrl).getPort();
				if (port == -1) {
					hostWithPort = host;
				} else {
					hostWithPort = host + ":" + port;
				} 
				swaggerBaseUrlSet = true;
			} catch (MalformedURLException e) {
				log.error("sessionId", "idType", "id", "In registrationStatusBean method of Transliteration Service- " + e.getMessage());
				System.err.println("SwaggerUrlException: " + e);
			}
		}

		Docket docket = new Docket(DocumentationType.SWAGGER_2).groupName("Pre-Registration-translitrator").select()
				.apis(RequestHandlerSelectors.any()).paths(PathSelectors.regex("(?!/(error).*).*")).build();
		
		if (swaggerBaseUrlSet) {
			docket.protocols(protocols()).host(hostWithPort);
		}
		return docket;
	}
	
	/**
	 * @return set or protocols
	 */
	private Set<String> protocols() {
		Set<String> protocols = new HashSet<>();
		protocols.add(proto);
		return protocols;
	}
}
