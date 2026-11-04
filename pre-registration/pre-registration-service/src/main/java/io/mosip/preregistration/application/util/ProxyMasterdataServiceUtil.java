package io.mosip.preregistration.application.util;

import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_ID;
import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_IDTYPE;
import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_SESSIONID;

import java.net.URI;
import java.net.URLDecoder;
import java.text.MessageFormat;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.application.exception.MasterDataException;
import io.mosip.preregistration.core.config.LoggerConfiguration;

@Component
/**
 * Helper used by Pre-Registration for proxy masterdata service util.
 */
public class ProxyMasterdataServiceUtil {

	@Value("${mosip.base.url}")
	/**
	 * base url used by this Pre-Registration type.
	 */
	private String baseUrl;

	@Value("${masterdata.service.version}")
	/**
	 * version used by this Pre-Registration type.
	 */
	private String version;

	@Qualifier("selfTokenRestTemplate")
	@Autowired
	/**
	 * rest template used by this Pre-Registration type.
	 */
	private RestTemplate restTemplate;

	/**
	 * Performs log config for MOSIP Pre-Registration.
	 *
	 * @return log config result
	 */
	private Logger log = LoggerConfiguration.logConfig(ProxyMasterdataServiceUtil.class);

	@SuppressWarnings("deprecation")
	/**
	 * Returns url.
	 *
	 * @param request request used by get url
	 * @return get url result
	 */
	public URI getUrl(HttpServletRequest request) {

		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "In getUrl method of proxyMasterDataServiceUtil");

		String query = request.getQueryString();
		String requestUrl = request.getRequestURI();
		requestUrl = URLDecoder.decode(requestUrl);
		String url = null;
		URI uri = null;
		if (query != null) {
			String decodedQuery = URLDecoder.decode(query);
			url = baseUrl + "/" + version
					+ requestUrl.replace(request.getContextPath() + "/proxy", "").strip().toString();
			uri = UriComponentsBuilder.fromUriString(url).query(decodedQuery).build().toUri();
			log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, " Requested Url is: " + uri);
		} else {
			url = baseUrl + "/" + version
					+ requestUrl.replace(request.getContextPath() + "/proxy", "").strip().toString();
			uri = UriComponentsBuilder.fromUriString(url).build().toUri();

			log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, " Requested Url is: " + uri);
		}
		return uri;
	}

	/**
	 * Returns http method type.
	 *
	 * @param request request used by get http method type
	 * @return get http method type result
	 */
	public HttpMethod getHttpMethodType(HttpServletRequest request) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"In getHttpMethodType method of proxyMasterDataServiceUtil");

		HttpMethod httpMethod = null;

		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, " Request Method Type: " + request.getMethod());

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

	@Cacheable(value = "masterdata-cache", key = "'MasterdataCache'+#uri", condition = "!#uri.toString().contains('getApplicantType')")
	/**
	 * Performs master data rest call for MOSIP Pre-Registration.
	 *
	 * @param uri uri used by master data rest call
	 * @param body body used by master data rest call
	 * @param methodType method type used by master data rest call
	 * @return master data rest call result
	 */
	public Object masterDataRestCall(URI uri, String body, HttpMethod methodType) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, MessageFormat.format("In masterDataRestCall method with request url {0} body : {1}", uri, body));

		ResponseEntity<?> response = null;

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<?> entity = new HttpEntity<>(body, headers);
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "httpEntity " + entity);

		try {
			response = restTemplate.exchange(uri, methodType, entity, String.class);
			log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "Proxy MasterData Call response for " + uri + response.getBody());
		} catch (Exception e) {
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "Proxy MasterData Call Exception response for url "+ uri + " Error:" + ExceptionUtils.getStackTrace(e));
			throw new MasterDataException("PRG_MSD_APP_001", "Failed to fetch masterdata information");
		}

		return response.getBody();
	}
}
