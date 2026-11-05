package io.mosip.preregistration.application.test.config;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class TestConfig {

	@Bean
	public RestTemplate restTemplateConfig()
			throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {

		TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

		SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy)
				.build();

		SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

		HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
				.setSSLSocketFactory(csf).build();
		CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(connectionManager).build();
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

		requestFactory.setHttpClient(httpClient);
		return new RestTemplate(requestFactory);
	}

	@Bean
	public RestTemplate selfTokenRestTemplate()
			throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {

		TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

		SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy)
				.build();

		SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

		HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
				.setSSLSocketFactory(csf).build();
		CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(connectionManager).build();
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

		requestFactory.setHttpClient(httpClient);
		return new RestTemplate(requestFactory);
	}

	@Bean
	public RestTemplate restTemplate() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {

		TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

		SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy)
				.build();

		SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

		HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
				.setSSLSocketFactory(csf).build();
		CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(connectionManager).build();
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

		requestFactory.setHttpClient(httpClient);
		return new RestTemplate(requestFactory);
	}

	@Bean
	public RestTemplate plainRestTemplate() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

		SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy)
				.build();

		SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

		HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
				.setSSLSocketFactory(csf).build();
		CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(connectionManager).build();
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

		requestFactory.setHttpClient(httpClient);
		return new RestTemplate(requestFactory);
	}
}