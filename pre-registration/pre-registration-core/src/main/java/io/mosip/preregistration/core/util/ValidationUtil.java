package io.mosip.preregistration.core.util;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;

import javax.persistence.LockModeType;

import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.core.code.RequestCodes;
import io.mosip.preregistration.core.code.StatusCodes;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.PageDTO;
import io.mosip.preregistration.core.common.dto.ValidDocumentsResponseDTO;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.errorcodes.ErrorCodes;
import io.mosip.preregistration.core.errorcodes.ErrorMessages;
import io.mosip.preregistration.core.exception.InvalidRequestException;
import io.mosip.preregistration.core.exception.MasterDataNotAvailableException;

@Component
public class ValidationUtil {

	@Value("${mosip.id.validation.identity.email}")
	private String emailRegex;

	@Value("${mosip.id.validation.identity.phone}")
	private String phoneRegex;

	@Value("${mosip.mandatory-languages}")
	private String mandatoryLangCodes;

	@Value("${mosip.optional-languages}")
	private String optionalLangCodes;

	@Value("${mosip.kernel.idobjectvalidator.masterdata.documenttypes.rest.uri}")
	private String documentTypeUri;

	@Value("${mosip.kernel.masterdata.validdoc.rest.uri}")
	private String masterdataUri;

	private static Logger log = LoggerConfiguration.logConfig(ValidationUtil.class);

	public boolean emailValidator(String email) {
		return email.matches(emailRegex);
	}

	public boolean phoneValidator(String phone) {
		return phone.matches(phoneRegex);
	}

	public boolean idValidation(String value, String regex) {
		if (!isNull(value)) {
			return value.matches(regex);
		}
		return false;
	}

	/** The validDocsMap. */
	private static MultiValueMap validDocsMap = new MultiValueMap();;

	@Qualifier("selfTokenRestTemplate")
	@Autowired
	RestTemplate restTemplate;

	private static final String DOCUMENTS = "documents";

	private static final String IS_ACTIVE = "isActive";

	private static final String CODE = "code";

	private static final String NAME = "name";

	public boolean requestValidator(MainRequestDTO<?> mainRequest) {
		log.info("sessionId", "idType", "id",
				"In requestValidator method of pre-registration core with mainRequest " + mainRequest);
		if (mainRequest.getId() == null) {
			throw new InvalidRequestException(ErrorCodes.PRG_CORE_REQ_001.getCode(),
					ErrorMessages.INVALID_REQUEST_ID.getMessage(), null);
		} else if (mainRequest.getRequest() == null) {
			throw new InvalidRequestException(ErrorCodes.PRG_CORE_REQ_004.getCode(),
					ErrorMessages.INVALID_REQUEST_BODY.getMessage(), null);
		} else if (mainRequest.getRequesttime() == null) {
			throw new InvalidRequestException(ErrorCodes.PRG_CORE_REQ_003.getCode(),
					ErrorMessages.INVALID_REQUEST_DATETIME.getMessage(), null);
		} else if (mainRequest.getVersion() == null) {
			throw new InvalidRequestException(ErrorCodes.PRG_CORE_REQ_002.getCode(),
					ErrorMessages.INVALID_REQUEST_VERSION.getMessage(), null);
		}
		return true;
	}

	public boolean requestValidator(Map<String, String> requestMap, Map<String, String> requiredRequestMap) {

		log.debug("sessionId", "idType", "id", "In requestValidator");
		log.info("sessionId", "idType", "id", "In requestValidator method of pre-registration core with requestMap "
				+ requestMap + " againt requiredRequestMap " + requiredRequestMap);
		for (String key : requestMap.keySet()) {
			if (key.equals(RequestCodes.ID) && (requestMap.get(RequestCodes.ID) == null
					|| !requestMap.get(RequestCodes.ID).equals(requiredRequestMap.get(RequestCodes.ID)))) {
				throw new InvalidRequestException(ErrorCodes.PRG_CORE_REQ_001.getCode(),
						ErrorMessages.INVALID_REQUEST_ID.getMessage(), null);
			} else if (key.equals(RequestCodes.VER) && (requestMap.get(RequestCodes.VER) == null
					|| !requestMap.get(RequestCodes.VER).equals(requiredRequestMap.get(RequestCodes.VER)))) {
				throw new InvalidRequestException(ErrorCodes.PRG_CORE_REQ_002.getCode(),
						ErrorMessages.INVALID_REQUEST_VERSION.getMessage(), null);
			} else if (key.equals(RequestCodes.REQ_TIME) && requestMap.get(RequestCodes.REQ_TIME) == null) {
				throw new InvalidRequestException(ErrorCodes.PRG_CORE_REQ_003.getCode(),
						ErrorMessages.INVALID_REQUEST_DATETIME.getMessage(), null);

			} else if (key.equals(RequestCodes.REQ_TIME) && requestMap.get(RequestCodes.REQ_TIME) != null) {
				try {
					LocalDate localDate = LocalDate.parse(requestMap.get(RequestCodes.REQ_TIME));
					LocalDate serverDate = new Date().toInstant().atZone(ZoneId.of("UTC")).toLocalDate();
					if (localDate.isBefore(serverDate) || localDate.isAfter(serverDate)) {
						throw new InvalidRequestException(ErrorCodes.PRG_CORE_REQ_013.getCode(),
								ErrorMessages.INVALID_REQUEST_DATETIME_NOT_CURRENT_DATE.getMessage(), null);
					}

				} catch (Exception ex) {
					throw new InvalidRequestException(ErrorCodes.PRG_CORE_REQ_013.getCode(),
							ErrorMessages.INVALID_REQUEST_DATETIME_NOT_CURRENT_DATE.getMessage(), null);
				}

			} else if (key.equals(RequestCodes.REQUEST) && (requestMap.get(RequestCodes.REQUEST) == null
					|| requestMap.get(RequestCodes.REQUEST).equals(""))) {
				throw new InvalidRequestException(ErrorCodes.PRG_CORE_REQ_004.getCode(),
						ErrorMessages.INVALID_REQUEST_BODY.getMessage(), null);
			}
		}
		return true;

	}

	public boolean requstParamValidator(Map<String, String> requestMap) {
		log.info("sessionId", "idType", "id",
				"In requstParamValidator method of pre-registration core with requestMap " + requestMap);
		for (String key : requestMap.keySet()) {
			if (key.equals(RequestCodes.USER_ID) && (requestMap.get(RequestCodes.USER_ID) == null
					|| requestMap.get(RequestCodes.USER_ID).equals(""))) {
				throw new InvalidRequestException(ErrorCodes.PRG_CORE_REQ_001.getCode(),
						ErrorMessages.MISSING_REQUEST_PARAMETER.getMessage(), null);
			} else if (key.equals(RequestCodes.PRE_REGISTRATION_ID)
					&& (requestMap.get(RequestCodes.PRE_REGISTRATION_ID) == null
							|| requestMap.get(RequestCodes.PRE_REGISTRATION_ID).equals(""))) {
				throw new InvalidRequestException(ErrorCodes.PRG_CORE_REQ_001.getCode(),
						ErrorMessages.MISSING_REQUEST_PARAMETER.getMessage(), null);
			} else if (key.equals(RequestCodes.STATUS_CODE) && (requestMap.get(RequestCodes.STATUS_CODE) == null
					|| requestMap.get(RequestCodes.STATUS_CODE).equals(""))) {
				throw new InvalidRequestException(ErrorCodes.PRG_CORE_REQ_001.getCode(),
						ErrorMessages.INVALID_STATUS_CODE.getMessage(), null);
			} else if (key.equals(RequestCodes.FROM_DATE) && (requestMap.get(RequestCodes.FROM_DATE) == null
					|| requestMap.get(RequestCodes.FROM_DATE).equals(""))) {
				throw new InvalidRequestException(ErrorCodes.PRG_CORE_REQ_001.getCode(),
						ErrorMessages.INVALID_DATE.getMessage(), null);
			} else if (key.equals(RequestCodes.FROM_DATE) && requestMap.get(RequestCodes.FROM_DATE) != null) {
				try {
					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(requestMap.get(RequestCodes.FROM_DATE));
				} catch (Exception ex) {
					throw new InvalidRequestException(ErrorCodes.PRG_CORE_REQ_003.getCode(),
							ErrorMessages.INVALID_REQUEST_DATETIME.getMessage() + "_FORMAT --> yyyy-MM-dd HH:mm:ss",
							null);
				}
			} else if (key.equals(RequestCodes.TO_DATE) && (requestMap.get(RequestCodes.TO_DATE) == null
					|| requestMap.get(RequestCodes.TO_DATE).equals(""))) {
				throw new InvalidRequestException(ErrorCodes.PRG_CORE_REQ_001.getCode(),
						ErrorMessages.INVALID_DATE.getMessage(), null);
			} else if (key.equals(RequestCodes.TO_DATE) && requestMap.get(RequestCodes.TO_DATE) != null) {
				try {
					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(requestMap.get(RequestCodes.TO_DATE));
				} catch (Exception ex) {
					throw new InvalidRequestException(ErrorCodes.PRG_CORE_REQ_003.getCode(),
							ErrorMessages.INVALID_REQUEST_DATETIME.getMessage() + "_FORMAT --> yyyy-MM-dd HH:mm:ss",
							null);
				}
			} else if (key.equals(RequestCodes.APPLICATION_ID)
					&& (requestMap.get(RequestCodes.APPLICATION_ID) == null
							|| requestMap.get(RequestCodes.APPLICATION_ID).equals(""))) {
				throw new InvalidRequestException(ErrorCodes.PRG_CORE_REQ_001.getCode(),
						ErrorMessages.MISSING_REQUEST_PARAMETER.getMessage(), null);
			}

		}
		return true;
	}

	/**
	 * This method is used as Null checker for different input keys.
	 *
	 * @param key pass the key
	 * @return true if key not null and return false if key is null.
	 */
	public static boolean isNull(Object key) {
		if (key instanceof String) {
			if (key.equals(""))
				return true;
		} else if (key instanceof List<?>) {
			if (((List<?>) key).isEmpty())
				return true;
		} else {
			if (key == null)
				return true;
		}
		return false;

	}

	public boolean langvalidation(String langCode) {
		Set<String> reqParams = new HashSet<>();
		for (String optionalLang : optionalLangCodes.split(",")) {
			reqParams.add(optionalLang);
		}
		for (String manLang : mandatoryLangCodes.split(",")) {
			reqParams.add(manLang);
		}

		if (reqParams.contains(langCode)) {
			return true;
		} else {
			throw new InvalidRequestException(ErrorCodes.PRG_CORE_REQ_014.getCode(),
					ErrorMessages.INVALID_LANG_CODE.getMessage(), null);
		}
	}

	@SuppressWarnings("unchecked")
	@Lock(LockModeType.READ)
	public boolean validateDocuments(String langCode, String catCode, String typeCode, String preRegistrationId) {
		log.debug("In validateDocuments method with docCatMap:{} preRegistrationId: {} ", validDocsMap,
				preRegistrationId);
		log.debug("In validateDocuments method with typeCode: {} catCode: {} preRegistrationId: {}", typeCode, catCode,
				preRegistrationId);
		if (validDocsMap.containsKey(catCode)) {
			List<String> docTypes = (List<String>) validDocsMap.get(catCode);
			if (docTypes.contains(typeCode)) {
				log.debug("sessionId", "idType", "id",
						"inside validateDocuments inside second if preRegistrationId " + preRegistrationId);
				return true;
			} else {
				log.debug("sessionId", "idType", "id",
						"inside validateDocuments inside else preRegistrationId " + preRegistrationId);
				throw new InvalidRequestException(ErrorCodes.PRG_CORE_REQ_017.toString(),
						ErrorMessages.INVALID_DOC_TYPE_CODE.getMessage() + "   " + validDocsMap + "  catcode " + catCode
								+ " typeCode  ",
						null);
			}
		} else {
			log.debug("sessionId", "idType", "id",
					"inside validateDocuments inside second else  preRegistrationId " + preRegistrationId);
			throw new InvalidRequestException(ErrorCodes.PRG_CORE_REQ_018.toString(),
					ErrorMessages.INVALID_DOC_CAT_CODE.getMessage() + "   " + validDocsMap + "  langCode " + langCode,
					null);
		}

	}

	public Map<String, String> getDocumentTypeNameByTypeCode(String langCode, String catCode) {
		Map<String, String> documentTypeMap = new HashMap<>();
		String uri = UriComponentsBuilder.fromUriString(documentTypeUri).buildAndExpand(catCode, langCode)
				.toUriString();
		@SuppressWarnings("unchecked")
		ResponseWrapper<LinkedHashMap<String, ArrayList<LinkedHashMap<String, Object>>>> responseBody = restTemplate
				.getForObject(uri, ResponseWrapper.class);
		if (responseBody != null) {
			if (Objects.isNull(responseBody.getErrors()) || responseBody.getErrors().isEmpty()) {
				ArrayList<LinkedHashMap<String, Object>> response = responseBody.getResponse().get(DOCUMENTS);
				IntStream.range(0, response.size()).filter(index -> (Boolean) response.get(index).get(IS_ACTIVE))
						.forEach(index -> {
							documentTypeMap.put(String.valueOf(response.get(index).get(CODE)),
									String.valueOf(response.get(index).get(NAME)));
						});
			}
		}
		return documentTypeMap;
	}

	public static boolean parseDate(String reqDate, String format) {
		log.info("sessionId", "idType", "id", "In parseDate method of core validation util");
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			sdf.setLenient(false);
			sdf.parse(reqDate);
			LocalDate.parse(reqDate);
		} catch (Exception e) {
			log.debug("sessionId", "idType", "id", ExceptionUtils.getStackTrace(e));
			log.error("sessionId", "idType", "id", "In parseDate method of core validation util - " + e.getMessage());
			return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public void getAllDocCategoriesAndTypes() {
		try {
			log.debug("In getAllDocCategoriesAndTypes");
			String uri = UriComponentsBuilder.fromUriString(masterdataUri).toUriString();
			HttpHeaders headers = new HttpHeaders();
			HttpEntity<HttpHeaders> entity = new HttpEntity<>(headers);
			log.info("getAllDocCategoriesAndTypes url: {} ", uri);

			int pageNo = 0;
			int totalPage = 0;
			do {
				ResponseEntity<ResponseWrapper<PageDTO<ValidDocumentsResponseDTO>>> response = restTemplate.exchange(
						uri + "&pageNumber=" + pageNo, HttpMethod.GET, entity,
						new ParameterizedTypeReference<ResponseWrapper<PageDTO<ValidDocumentsResponseDTO>>>() {
						});
				ResponseWrapper<PageDTO<ValidDocumentsResponseDTO>> body = response.getBody();
				if (body != null) {
					if (body.getErrors() != null && !body.getErrors().isEmpty()) {
						log.debug("sessionId", "idType", "id",
								"inside getAllDocCategories inside else  preRegistrationId ");
						log.debug("sessionId", "idType", "id", " cat code" + body.getErrors().toString());
						throw new MasterDataNotAvailableException(body.getErrors().get(0).getErrorCode(),
								body.getErrors().get(0).getMessage());
					}
					PageDTO<ValidDocumentsResponseDTO> resp = body.getResponse();
					if (resp  != null) {
						totalPage = resp.getTotalPages();
						resp.getData().stream().filter(docs -> docs.getIsActive()).forEach(activeDocs -> validDocsMap
								.put(activeDocs.getDocCategoryCode(), activeDocs.getDocTypeCode()));	
					}
				}
				pageNo++;
			} while (pageNo != totalPage);
			log.info("validDocsMap {}", validDocsMap);
		} catch (RestClientException e) {
			log.debug("sessionId", "idType", "id", "inside getAllDocCategories inside catch preRegistrationId ");
			log.debug("sessionId", "idType", "id", "---- " + ExceptionUtils.getStackTrace(e));
			log.error("sessionId", "idType", "id", "---- docCatMap " + validDocsMap + ExceptionUtils.getStackTrace(e));
			throw new MasterDataNotAvailableException(ErrorCodes.PRG_CORE_REQ_022.toString(),
					ErrorMessages.MASTERDATA_SERVICE_CALL_FAIL.toString(), e.getCause());
		}
	}

	public Map<String, String> prepareRequestMap(MainRequestDTO<?> requestDto) {
		log.info("sessionId", "idType", "id", "In prepareRequestMap method of Login Service Util");
		Map<String, String> requestMap = new HashMap<>();
		requestMap.put("id", requestDto.getId());
		requestMap.put("version", requestDto.getVersion());
		if (!(requestDto.getRequesttime() == null || requestDto.getRequesttime().toString().isEmpty())) {
			LocalDate date = requestDto.getRequesttime().toInstant().atZone(ZoneId.of("UTC")).toLocalDate();
			requestMap.put("requesttime", date.toString());
		} else {
			requestMap.put("requesttime", null);
		}
		requestMap.put("request", requestDto.getRequest().toString());
		return requestMap;
	}

	public String getCurrentResponseTime() {
		return LocalDateTime.now(ZoneId.of("UTC")).toString();
	}

	public boolean isStatusBookedOrExpired(String status) {
		boolean isStatusBookedOrExpired = false;
		if (StatusCodes.BOOKED.getCode().equals(status)) {
			isStatusBookedOrExpired = !isStatusBookedOrExpired;
		} else if (StatusCodes.EXPIRED.getCode().equals(status)) {
			isStatusBookedOrExpired = !isStatusBookedOrExpired;
		} else if (StatusCodes.CANCELLED.getCode().equals(status)) {
			isStatusBookedOrExpired = !isStatusBookedOrExpired;
		} else if (StatusCodes.PREFETCHED.getCode().equals(status)) {
			isStatusBookedOrExpired = !isStatusBookedOrExpired;
		}
		return isStatusBookedOrExpired;
	}
}