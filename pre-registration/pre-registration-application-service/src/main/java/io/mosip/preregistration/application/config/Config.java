package io.mosip.preregistration.application.config;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Map;

import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @author Raj Jha
 * 
 * @since 1.0.0
 *
 */
@Configuration
@ConfigurationProperties("mosip")
public class Config {

	@Value("${preregistration.appservice.httpclient.connections.max.per.host:20}")
	private int maxConnectionPerRoute;

	@Value("${preregistration.appservice.httpclient.connections.max:100}")
	private int totalMaxConnection;
	
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

	@Bean
	public RestTemplate restTemplateConfig()
			throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
				var connnectionManagerBuilder = PoolingHttpClientConnectionManagerBuilder.create()
			     .setMaxConnPerRoute(maxConnectionPerRoute)
			     .setMaxConnTotal(totalMaxConnection);
			var connectionManager = connnectionManagerBuilder.build();
			HttpClientBuilder httpClientBuilder = HttpClients.custom()
					.setConnectionManager(connectionManager)
					.disableCookieManagement();
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setHttpClient(httpClientBuilder.build());
		return new RestTemplate(requestFactory);
	}

}
