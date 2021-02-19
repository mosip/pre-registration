package io.mosip.preregistration.application.util;

import java.net.URI;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import io.mosip.kernel.core.logger.spi.Logger;
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
		String requestUrl = request.getRequestURI();
		requestUrl = URLDecoder.decode(requestUrl);
		String url = null;
		URI uri = null;
		if (query != null) {
                        String decodedQuery = URLDecoder.decode(query);
			url = baseUrl + "/" + version
					+ requestUrl.replace(request.getContextPath() + "/proxy", "").strip().toString();
			uri = UriComponentsBuilder.fromHttpUrl(url).query(decodedQuery).build().toUri();
			log.info("sessionId", "idType", "id", " Requested Url is: " + uri);
		} else {
			url = baseUrl + "/" + version
					+ requestUrl.replace(request.getContextPath() + "/proxy", "").strip().toString();
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

}

