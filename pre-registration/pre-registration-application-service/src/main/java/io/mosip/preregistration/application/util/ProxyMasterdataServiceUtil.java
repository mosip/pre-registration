package io.mosip.preregistration.application.util;

import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Collections;

import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.application.config.RestInterceptor;
import io.mosip.preregistration.core.config.LoggerConfiguration;

@Component
public class ProxyMasterdataServiceUtil {


	@Value("${mosip.base.url}")
	private String baseUrl;
	
	@Value("${masterdata.service.version}")
	private String version;

	private Logger log = LoggerConfiguration.logConfig(ProxyMasterdataServiceUtil.class);

	public URI getUrl(HttpServletRequest request) {

		log.info("sessionId", "idType", "id", "In getUrl method of proxyMasterDataServiceUtil");

		String query = request.getQueryString();
		String url = null;
		URI uri = null;
		if (query != null) {

			url = baseUrl + "/" + version
					+ request.getRequestURI().replace(request.getContextPath() + "/proxy", "").strip().toString();
			uri = UriComponentsBuilder.fromHttpUrl(url).query(query).build().toUri();
			log.info("sessionId", "idType", "id", " Requested Url is: " + uri);
		} else {
			url = baseUrl + "/" + version
					+ request.getRequestURI().replace(request.getContextPath() + "/proxy", "").strip().toString();
			uri = UriComponentsBuilder.fromHttpUrl(url).build().toUri();

			log.info("sessionId", "idType", "id", " Requested Url is: " + uri);
		}
		return uri;
	}

	public HttpMethod getHttpMethodType(HttpServletRequest request) {

		log.info("sessionId", "idType", "id", "In getHttpMethodType method of proxyMasterDataServiceUtil");

		HttpMethod httpMethod = null;

		log.info("sessionId", "idType", "id", " Request Method Type: " + request.getMethod());

		switch (request.getMethod()) {
		case "GET":
			httpMethod = HttpMethod.GET;
			break;

		case "POST":
			httpMethod = HttpMethod.POST;
			break;

		case "DELETE":
			httpMethod = HttpMethod.DELETE;
			break;

		case "PUT":
			httpMethod = HttpMethod.PUT;
			break;
		}
		return httpMethod;
	}

	
	public RestTemplate getRestTemplate() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {

		TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

		SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy)
				.build();

		SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

		CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

		requestFactory.setHttpClient(httpClient);
		
		 RestTemplate restTemplate = new RestTemplate(requestFactory);
		 restTemplate.setInterceptors(Collections.singletonList(new RestInterceptor()));
		 
		return restTemplate;
	}
}