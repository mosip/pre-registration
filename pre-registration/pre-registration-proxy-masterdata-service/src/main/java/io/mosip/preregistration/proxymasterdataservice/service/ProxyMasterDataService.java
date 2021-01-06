package io.mosip.preregistration.proxymasterdataservice.service;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.proxymasterdataservice.config.LoggerConfiguration;
import io.mosip.preregistration.proxymasterdataservice.service.util.ProxyMasterdataServiceUtil;

@Service
public class ProxyMasterDataService {

	@Autowired
	private ProxyMasterdataServiceUtil util;

	private Logger log = LoggerConfiguration.logConfig(ProxyMasterDataService.class);

	public Object getMasterDataResponse(String body, HttpServletRequest request) {

		log.info("sessionId", "idType", "id",
				"In getMasterDataResponse method with request url" + request.getRequestURI() + body);

		ResponseEntity<?> response = null;

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Cookie", util.getAuthToken());

		HttpEntity<?> entity = new HttpEntity<>(body, headers);

		log.info("sessionId", "idType", "id", "httpEntity " + entity);

		try {

			RestTemplate restTemplate = util.getRestTemplate();

			response = restTemplate.exchange(util.getUrl(request), util.getHttpMethodType(request), entity,
					String.class);

			log.info("sessionId", "idType", "id",
					"Proxy MasterData Call response for " + util.getUrl(request) + response.getBody());

		} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException | RestClientException e) {

			log.info("sessionId", "idType", "id", "Proxy MasterData Call Exception response for " + util.getUrl(request)
					+ e.getMessage() + ExceptionUtils.getStackTrace(e));
			throw new RestClientException(e.getMessage());
		}

		return response.getBody();
	}

}
