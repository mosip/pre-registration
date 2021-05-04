package io.mosip.preregistration.application.service.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
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
import io.mosip.preregistration.application.dto.UISpecPublishRequestDTO;
import io.mosip.preregistration.application.dto.UISpecResponseDTO;
import io.mosip.preregistration.application.dto.UISpecficationRequestDTO;
import io.mosip.preregistration.application.errorcodes.ApplicationErrorCodes;
import io.mosip.preregistration.application.errorcodes.ApplicationErrorMessages;
import io.mosip.preregistration.application.exception.UISpecException;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.exception.RestCallException;

@Component
public class UISpecServiceUtil {

	@Autowired
	private RestTemplate restTemplate;

	@Value("${masterdata.resource.url}")
	private String materdataResourceUrl;

	/**
	 * Logger instance
	 */
	private Logger log = LoggerConfiguration.logConfig(UISpecServiceUtil.class);

	private final String domain = "pre-registration";

	public UISpecResponseDTO saveUISchema(UISpecficationRequestDTO uiSpecRequest) {
		log.info("In  UISpec serviceutil saveUIschema method");
		UISpecResponseDTO response = null;
		ResponseEntity<ResponseWrapper<UISpecResponseDTO>> responseEntity = null;
		try {
			log.info("Calling masterdata service to save ui spec");
			UriComponentsBuilder regbuilder = UriComponentsBuilder.fromHttpUrl(materdataResourceUrl + "/uispec");
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

			MainRequestDTO<UISpecficationRequestDTO> uiSpecPostRequest = new MainRequestDTO<>();
			uiSpecPostRequest.setRequest(uiSpecRequest);
			uiSpecPostRequest.setRequesttime(new Date());

			HttpEntity<MainRequestDTO<UISpecficationRequestDTO>> entity = new HttpEntity<MainRequestDTO<UISpecficationRequestDTO>>(
					uiSpecPostRequest, headers);
			String uriBuilder = regbuilder.build().encode().toUriString();

			log.info("masterdata service to save ui spec uri {} and request {}", uriBuilder, uiSpecPostRequest);

			responseEntity = restTemplate.exchange(uriBuilder, HttpMethod.POST, entity,
					new ParameterizedTypeReference<ResponseWrapper<UISpecResponseDTO>>() {
					});

			if (responseEntity.getBody().getErrors() != null && !responseEntity.getBody().getErrors().isEmpty()) {
				log.info("error while saving uispec {}", responseEntity.getBody().getErrors());
				throw new RestCallException(responseEntity.getBody().getErrors().get(0).getErrorCode(),
						responseEntity.getBody().getErrors().get(0).getMessage());
			}

			response = responseEntity.getBody().getResponse();
			log.info("Saved uispec resposne {}", response);
			if (Objects.isNull(response)) {
				throw new UISpecException(ApplicationErrorCodes.PRG_APP_001.getCode(),
						ApplicationErrorMessages.UNABLE_TO_CREATE_THE_UI_SPEC.getMessage());
			}

		} catch (RestClientException ex) {
			log.error("error while saving uispec {}", ex);
			throw new UISpecException(ApplicationErrorCodes.PRG_APP_001.getCode(),
					ApplicationErrorMessages.UNABLE_TO_CREATE_THE_UI_SPEC.getMessage());
		} catch (Exception ex) {
			log.error("error while saving uispec {}", ex);
			throw new UISpecException(responseEntity.getBody().getErrors().get(0).getErrorCode(),
					responseEntity.getBody().getErrors().get(0).getMessage());
		}
		return response;

	}

	public UISpecResponseDTO updateUISchema(UISpecficationRequestDTO uiSpecRequest, String id) {
		log.info("In  UISpec serviceutil updateUIschema method");
		UISpecResponseDTO response = null;
		ResponseEntity<ResponseWrapper<UISpecResponseDTO>> responseEntity = null;
		try {
			log.info("Calling masterdata service to update ui spec");
			UriComponentsBuilder regbuilder = UriComponentsBuilder.fromHttpUrl(materdataResourceUrl + "/uispec");
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("id", id);

			MainRequestDTO<UISpecficationRequestDTO> uiSpecPostRequest = new MainRequestDTO<>();
			uiSpecPostRequest.setRequest(uiSpecRequest);
			uiSpecPostRequest.setRequesttime(new Date());

			HttpEntity<MainRequestDTO<UISpecficationRequestDTO>> entity = new HttpEntity<MainRequestDTO<UISpecficationRequestDTO>>(
					uiSpecPostRequest, headers);
			String uriBuilder = regbuilder.queryParams(params).build().encode().toUriString();

			log.info("masterdata service to update ui spec uri {} and request {}", uriBuilder, uiSpecPostRequest);

			responseEntity = restTemplate.exchange(uriBuilder, HttpMethod.PUT, entity,
					new ParameterizedTypeReference<ResponseWrapper<UISpecResponseDTO>>() {
					});

			if (responseEntity.getBody().getErrors() != null && !responseEntity.getBody().getErrors().isEmpty()) {
				log.info("error while updating uispec {}", responseEntity.getBody().getErrors());
				throw new RestCallException(responseEntity.getBody().getErrors().get(0).getErrorCode(),
						responseEntity.getBody().getErrors().get(0).getMessage());
			}

			response = responseEntity.getBody().getResponse();
			log.info("updated uispec resposne {}", response);
			if (Objects.isNull(response)) {
				throw new UISpecException(ApplicationErrorCodes.PRG_APP_002.getCode(),
						ApplicationErrorMessages.UNABLE_TO_UPDATE_THE_UI_SPEC.getMessage());
			}

		} catch (RestClientException ex) {
			log.error("error while updating uispec {}", ex.getCause());
			throw new UISpecException(ApplicationErrorCodes.PRG_APP_002.getCode(),
					ApplicationErrorMessages.UNABLE_TO_UPDATE_THE_UI_SPEC.getMessage());
		} catch (Exception ex) {
			log.error("error while updating uispec {}", ex);
			throw new UISpecException(responseEntity.getBody().getErrors().get(0).getErrorCode(),
					responseEntity.getBody().getErrors().get(0).getMessage());
		}
		return response;

	}

	public String publishUISchema(String id) {
		log.info("In  UISpec serviceutil publishUIschema method");
		String response = null;
		ResponseEntity<String> responseEntity = null;
		try {
			log.info("Calling masterdata service to publish ui spec");
			UriComponentsBuilder regbuilder = UriComponentsBuilder
					.fromHttpUrl(materdataResourceUrl + "/uispec/publish");
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

			UISpecPublishRequestDTO publishRequest = new UISpecPublishRequestDTO();
			publishRequest.setId(id);
			publishRequest.setEffectiveFrom(LocalDateTime.now(ZoneId.of("UTC")).plusMinutes(5));

			MainRequestDTO<UISpecPublishRequestDTO> uiSpecPublishRequest = new MainRequestDTO<>();
			uiSpecPublishRequest.setRequest(publishRequest);
			uiSpecPublishRequest.setRequesttime(new Date());

			HttpEntity<MainRequestDTO<UISpecPublishRequestDTO>> entity = new HttpEntity<MainRequestDTO<UISpecPublishRequestDTO>>(
					uiSpecPublishRequest, headers);

			String uriBuilder = regbuilder.build().encode().toUriString();

			log.info("masterdata service to publish ui spec uri {} and request {}", uriBuilder, uiSpecPublishRequest);

			responseEntity = restTemplate.exchange(uriBuilder, HttpMethod.PUT, entity, String.class);

			response = responseEntity.getBody();

			/*if (responseEntity.getBody().getErrors() != null && !responseEntity.getBody().getErrors().isEmpty()) {
				log.info("error while updating uispec {}", responseEntity.getBody().getErrors());
				throw new RestCallException(responseEntity.getBody().getErrors().get(0).getErrorCode(),
						responseEntity.getBody().getErrors().get(0).getMessage());
			}

			response = responseEntity.getBody().getResponse();
			log.info("updated uispec resposne {}", response);
			if (Objects.isNull(response)) {
				throw new UISpecException(ApplicationErrorCodes.PRG_APP_005.getCode(),
						ApplicationErrorMessages.FAILED_TO_PUBLISH_THE_UI_SPEC.getMessage());
			}*/

		} catch (RestClientException ex) {
			log.error("error while updating uispec {}", ex);
			throw new UISpecException(ApplicationErrorCodes.PRG_APP_005.getCode(),
					ApplicationErrorMessages.FAILED_TO_PUBLISH_THE_UI_SPEC.getMessage());
		} catch (Exception ex) {
			log.error("error while updating uispec {}", ex);
			/*throw new UISpecException(responseEntity.getBody().getErrors().get(0).getErrorCode(),
					responseEntity.getBody().getErrors().get(0).getMessage());*/
		}
		return response;

	}

	public List<UISpecResponseDTO> getUISchema(Double version, Double idSchemaVersion) {
		log.info("In  UISpec serviceutil getUIschema method");
		List<UISpecResponseDTO> response = null;
		ResponseEntity<ResponseWrapper<List<UISpecResponseDTO>>> responseEntity = null;
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

			if (responseEntity.getBody().getErrors() != null && !responseEntity.getBody().getErrors().isEmpty()) {
				log.info("error while fetching uispec {}", responseEntity.getBody().getErrors());
				throw new RestCallException(responseEntity.getBody().getErrors().get(0).getErrorCode(),
						responseEntity.getBody().getErrors().get(0).getMessage());
			}

			response = responseEntity.getBody().getResponse();
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
			throw new UISpecException(responseEntity.getBody().getErrors().get(0).getErrorCode(),
					responseEntity.getBody().getErrors().get(0).getMessage());
		}
		return response;

	}

	public String deleteUISchema(String id) {
		log.info("In  UISpec serviceutil deleteUIschema method");
		String response = null;
		ResponseEntity<ResponseWrapper<String>> responseEntity = null;
		try {
			log.info("Calling masterdata service to delete ui spec");
			UriComponentsBuilder regbuilder = UriComponentsBuilder.fromHttpUrl(materdataResourceUrl + "/uispec");

			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("id", id);

			String uriBuilder = regbuilder.queryParams(params).build().encode().toUriString();

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<?> entity = new HttpEntity<>(headers);

			log.info("masterdata service to delete ui spec uri {} ", uriBuilder);

			responseEntity = restTemplate.exchange(uriBuilder, HttpMethod.DELETE, entity,
					new ParameterizedTypeReference<ResponseWrapper<String>>() {
					});
			if (responseEntity.getBody().getErrors() != null && !responseEntity.getBody().getErrors().isEmpty()) {
				log.info("error while deleting uispec {}", responseEntity.getBody().getErrors());
				throw new RestCallException(responseEntity.getBody().getErrors().get(0).getErrorCode(),
						responseEntity.getBody().getErrors().get(0).getMessage());
			}

			response = responseEntity.getBody().getResponse();
			log.info("uispec resposne {}", response);
			if (Objects.isNull(response)) {
				throw new UISpecException(ApplicationErrorCodes.PRG_APP_004.getCode(),
						ApplicationErrorMessages.FAILED_TO_DELETE_THE_UI_SPEC.getMessage());
			}

		} catch (RestClientException ex) {
			log.error("error while deleting uispec {}", ex);
			throw new UISpecException(ApplicationErrorCodes.PRG_APP_004.getCode(),
					ApplicationErrorMessages.FAILED_TO_DELETE_THE_UI_SPEC.getMessage());
		} catch (Exception ex) {
			log.error("error while fetching uispec {}", ex);
			throw new UISpecException(responseEntity.getBody().getErrors().get(0).getErrorCode(),
					responseEntity.getBody().getErrors().get(0).getMessage());
		}
		return response;

	}

	public PageDTO<UISpecResponseDTO> getAllUISchema(int pageNumber, int pageSize) {
		log.info("In  UISpec serviceutil getAllUISchema method");
		PageDTO<UISpecResponseDTO> response = null;
		ResponseEntity<ResponseWrapper<PageDTO<UISpecResponseDTO>>> responseEntity = null;
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

			if (responseEntity.getBody().getErrors() != null && !responseEntity.getBody().getErrors().isEmpty()) {
				log.info("error while fetching uispec {}", responseEntity.getBody().getErrors());
				throw new RestCallException(responseEntity.getBody().getErrors().get(0).getErrorCode(),
						responseEntity.getBody().getErrors().get(0).getMessage());
			}

			response = responseEntity.getBody().getResponse();
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
			throw new UISpecException(responseEntity.getBody().getErrors().get(0).getErrorCode(),
					responseEntity.getBody().getErrors().get(0).getMessage());
		}
		return response;

	}

}
