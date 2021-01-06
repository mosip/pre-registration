package io.mosip.preregistration.proxymasterdataservice.service.util;

import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import io.mosip.kernel.core.authmanager.model.AuthNResponseDto;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.proxymasterdataservice.config.LoggerConfiguration;
import io.mosip.preregistration.proxymasterdataservice.dto.ClientSecretDTO;

@Component
public class ProxyMasterdataServiceUtil {

	@Value("${appId}")
	private String appId;

	@Value("${clientId}")
	private String clientId;

	@Value("${secretKey}")
	private String secretKey;

	@Value("${sendOtp.resource.url}")
	private String sendOtpResourceUrl;

	@Value("${mosip.base.url}")
	private String baseUrl;

	@Value("${version}")
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

	public String getAuthToken() {
		String tokenUrl = sendOtpResourceUrl + "/authenticate/clientidsecretkey";
		ClientSecretDTO clientSecretDto = new ClientSecretDTO(clientId, secretKey, appId);
		io.mosip.kernel.core.http.RequestWrapper<ClientSecretDTO> requestKernel = new RequestWrapper<>();
		requestKernel.setRequest(clientSecretDto);
		requestKernel.setRequesttime(LocalDateTime.now());
		ResponseEntity<ResponseWrapper<AuthNResponseDto>> response = (ResponseEntity<ResponseWrapper<AuthNResponseDto>>) callAuthService(
				tokenUrl, HttpMethod.POST, MediaType.APPLICATION_JSON, requestKernel, null, ResponseWrapper.class);
		if (!(response.getBody().getErrors() == null || response.getBody().getErrors().isEmpty())) {
			throw new RestClientException("rest call failed");
		}
		return response.getHeaders().get("Set-Cookie").get(0);
	}

	public ResponseEntity<?> callAuthService(String url, HttpMethod httpMethodType, MediaType mediaType, Object body,
			Map<String, String> headersMap, Class<?> responseClass) {
		ResponseEntity<?> response = null;
		try {
			log.info("sessionId", "idType", "id", "In callAuthService method of proxyMasterDataServiceUtil");
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(mediaType);
			HttpEntity<?> request = null;
			if (headersMap != null) {
				headersMap.forEach((k, v) -> headers.add(k, v));
			}
			if (body != null) {
				request = new HttpEntity<>(body, headers);
			} else {
				request = new HttpEntity<>(headers);
			}
			log.info("sessionId", "idType", "id", "calling kernel rest service :" + url);
			response = getRestTemplate().exchange(url, httpMethodType, request, responseClass);
		} catch (RestClientException | KeyManagementException | NoSuchAlgorithmException | KeyStoreException ex) {
			log.debug("sessionId", "idType", "id", "Kernel rest call exception " + ExceptionUtils.getStackTrace(ex));
			throw new RestClientException("rest call failed");
		}
		return response;

	}

	public RestTemplate getRestTemplate() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {

		TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

		SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy)
				.build();

		SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

		CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

		requestFactory.setHttpClient(httpClient);
		return new RestTemplate(requestFactory);
	}
}