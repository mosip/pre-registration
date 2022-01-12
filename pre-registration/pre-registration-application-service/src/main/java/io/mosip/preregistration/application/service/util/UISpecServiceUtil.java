package io.mosip.preregistration.application.service.util;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.application.dto.PageDTO;
import io.mosip.preregistration.application.dto.UISpecResponseDTO;
import io.mosip.preregistration.application.errorcodes.ApplicationErrorCodes;
import io.mosip.preregistration.application.errorcodes.ApplicationErrorMessages;
import io.mosip.preregistration.application.exception.UISpecException;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.exception.RestCallException;

@Component
public class UISpecServiceUtil {

	@Qualifier("selfTokenRestTemplate")
	@Autowired
	private RestTemplate restTemplate;

	@Value("${masterdata.resource.url}")
	private String materdataResourceUrl;

	/**
	 * Logger instance
	 */
	private Logger log = LoggerConfiguration.logConfig(UISpecServiceUtil.class);

	private final String domain = "pre-registration";

	public List<UISpecResponseDTO> getUISchema(Double version, Double idSchemaVersion) {
		log.info("In  UISpec serviceutil getUIschema method");
		List<UISpecResponseDTO> response = null;
		ResponseEntity<ResponseWrapper<List<UISpecResponseDTO>>> responseEntity = null;
		ResponseWrapper<List<UISpecResponseDTO>> body = null;
		try {
			log.info("Calling masterdata service to get ui spec");
			UriComponentsBuilder regbuilder = UriComponentsBuilder
					.fromHttpUrl(materdataResourceUrl + "/uispec/" + domain + "/latest");

			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("version", version.toString());
			params.add("identitySchemaVersion", idSchemaVersion.toString());

			String uriBuilder = regbuilder.queryParams(params).build().encode().toUriString();

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<?> entity = new HttpEntity<>(headers);

			log.info("masterdata service to get ui spec uri {} ", uriBuilder);

			responseEntity = restTemplate.exchange(uriBuilder, HttpMethod.GET, entity,
					new ParameterizedTypeReference<ResponseWrapper<List<UISpecResponseDTO>>>() {
					});

			body = responseEntity.getBody();
			if (body != null) {
				if (body.getErrors() != null && !body.getErrors().isEmpty()) {
					log.info("error while fetching uispec {}", body.getErrors());
					throw new RestCallException(body.getErrors().get(0).getErrorCode(),
							body.getErrors().get(0).getMessage());
				}
				response = body.getResponse();
			}
			
			log.info("ui spec response {}", response);
			if (Objects.isNull(response)) {
				throw new UISpecException(ApplicationErrorCodes.PRG_APP_003.getCode(),
						ApplicationErrorMessages.UNABLE_TO_FETCH_THE_UI_SPEC.getMessage());
			}

		} catch (RestClientException ex) {
			log.error("error while fetching uispec {}", ex);
			throw new UISpecException(ApplicationErrorCodes.PRG_APP_003.getCode(),
					ApplicationErrorMessages.UNABLE_TO_FETCH_THE_UI_SPEC.getMessage());
		} catch (Exception ex) {
			log.error("error while fetching uispec {}", ex);
			if (body != null && body.getErrors() != null && !body.getErrors().isEmpty()) {
				throw new UISpecException(body.getErrors().get(0).getErrorCode(),
						body.getErrors().get(0).getMessage());
			} else {
				throw new UISpecException(ApplicationErrorCodes.PRG_APP_003.getCode(),
						ApplicationErrorMessages.UNABLE_TO_FETCH_THE_UI_SPEC.getMessage());
			}
		}
		return response;

	}

	public PageDTO<UISpecResponseDTO> getAllUISchema(int pageNumber, int pageSize) {
		log.info("In  UISpec serviceutil getAllUISchema method");
		PageDTO<UISpecResponseDTO> response = null;
		ResponseEntity<ResponseWrapper<PageDTO<UISpecResponseDTO>>> responseEntity = null;
		ResponseWrapper<PageDTO<UISpecResponseDTO>> body = null;
		try {
			log.info("Calling masterdata service to get ui spec");
			UriComponentsBuilder regbuilder = UriComponentsBuilder.fromHttpUrl(materdataResourceUrl + "/uispec/all");

			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("pageNumber", String.valueOf(pageNumber));
			params.add("pageSize", String.valueOf(pageSize));

			String uriBuilder = regbuilder.queryParams(params).build().encode().toUriString();

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<?> entity = new HttpEntity<>(headers);

			log.info("masterdata service to get ui spec uri {} ", uriBuilder);

			responseEntity = restTemplate.exchange(uriBuilder, HttpMethod.GET, entity,
					new ParameterizedTypeReference<ResponseWrapper<PageDTO<UISpecResponseDTO>>>() {
					});

			body = responseEntity.getBody();
			if (body != null) {
				if (body.getErrors() != null && !body.getErrors().isEmpty()) {
					log.info("error while fetching uispec {}", body.getErrors());
					throw new RestCallException(body.getErrors().get(0).getErrorCode(),
							body.getErrors().get(0).getMessage());
				}
				response = body.getResponse();
			}

			log.info("uispec resposne {}", response);
			if (Objects.isNull(response)) {
				throw new UISpecException(ApplicationErrorCodes.PRG_APP_003.getCode(),
						ApplicationErrorMessages.UNABLE_TO_FETCH_THE_UI_SPEC.getMessage());
			}

		} catch (RestClientException ex) {
			log.error("error while fetching uispec {}", ex);
			throw new UISpecException(ApplicationErrorCodes.PRG_APP_003.getCode(),
					ApplicationErrorMessages.UNABLE_TO_FETCH_THE_UI_SPEC.getMessage());
		} catch (Exception ex) {
			log.error("error while fetching uispec {}", ex);
			if (body != null && body.getErrors() != null && !body.getErrors().isEmpty()) {
				throw new UISpecException(body.getErrors().get(0).getErrorCode(),
						body.getErrors().get(0).getMessage());
			} else {
				throw new UISpecException(ApplicationErrorCodes.PRG_APP_003.getCode(),
						ApplicationErrorMessages.UNABLE_TO_FETCH_THE_UI_SPEC.getMessage());
			}
		}
		return response;

	}

}
