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
import org.springframework.context.annotation.Primary;
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
	/**
	 * max connection per route used by this Pre-Registration type.
	 */
	private int maxConnectionPerRoute;

	@Value("${preregistration.appservice.httpclient.connections.max:100}")
	/**
	 * total max connection used by this Pre-Registration type.
	 */
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
	@Primary
	public Map<String, String> ic() {
		return Collections.unmodifiableMap(id);
	}

	@Bean
	/**
	 * Performs rest template config for MOSIP Pre-Registration.
	 *
	 * @return rest template config result
	 * @throws KeyManagementException when the operation cannot complete
	 * @throws NoSuchAlgorithmException when the operation cannot complete
	 * @throws KeyStoreException when the operation cannot complete
	 */
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

	/**
	 * Plain {@link RestTemplate} for Google reCAPTCHA verify (no MOSIP auth adapter).
	 * {@code CaptchaServiceImpl} injects this as {@code restTemplateBean}.
	 *
	 * @return pooled RestTemplate
	 * @throws KeyManagementException when TLS setup fails
	 * @throws NoSuchAlgorithmException when TLS setup fails
	 * @throws KeyStoreException when TLS setup fails
	 */
	@Bean
	public RestTemplate restTemplateBean()
			throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		return restTemplateConfig();
	}
}
