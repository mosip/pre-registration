/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.application.service.util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.assertj.core.util.Arrays;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.preregistration.application.code.DemographicRequestCodes;
import io.mosip.preregistration.application.dto.ApplicantTypeKeyValueDTO;
import io.mosip.preregistration.application.dto.ApplicantTypeRequestDTO;
import io.mosip.preregistration.application.dto.ApplicantTypeResponseDTO;
import io.mosip.preregistration.application.dto.ApplicantValidDocumentDto;
import io.mosip.preregistration.application.dto.DemographicCreateResponseDTO;
import io.mosip.preregistration.application.dto.DemographicRequestDTO;
import io.mosip.preregistration.application.dto.DemographicUpdateResponseDTO;
import io.mosip.preregistration.application.dto.IdSchemaDto;
import io.mosip.preregistration.application.dto.LanguageValueDto;
import io.mosip.preregistration.application.dto.PridFetchResponseDto;
import io.mosip.preregistration.application.dto.UISpecMetaDataDTO;
import io.mosip.preregistration.application.errorcodes.ApplicationErrorCodes;
import io.mosip.preregistration.application.errorcodes.ApplicationErrorMessages;
import io.mosip.preregistration.application.errorcodes.DemographicErrorCodes;
import io.mosip.preregistration.application.errorcodes.DemographicErrorMessages;
import io.mosip.preregistration.application.exception.MasterDataException;
import io.mosip.preregistration.application.exception.OperationNotAllowedException;
import io.mosip.preregistration.application.exception.RecordFailedToUpdateException;
import io.mosip.preregistration.application.exception.RecordNotFoundException;
import io.mosip.preregistration.application.repository.ApplicationRepostiory;
import io.mosip.preregistration.application.service.AppointmentService;
import io.mosip.preregistration.application.service.UISpecService;
import io.mosip.preregistration.booking.dto.RegistrationCenterResponseDto;
import io.mosip.preregistration.core.code.ApplicationStatusCode;
import io.mosip.preregistration.core.code.BookingTypeCodes;
import io.mosip.preregistration.core.code.StatusCodes;
import io.mosip.preregistration.core.common.dto.DeleteBookingDTO;
import io.mosip.preregistration.core.common.dto.DemographicResponseDTO;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.dto.RequestWrapper;
import io.mosip.preregistration.core.common.entity.ApplicationEntity;
import io.mosip.preregistration.core.common.entity.DemographicEntity;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.exception.DatabaseOperationException;
import io.mosip.preregistration.core.exception.EncryptionFailedException;
import io.mosip.preregistration.core.exception.RecordFailedToDeleteException;
import io.mosip.preregistration.core.exception.RestCallException;
import io.mosip.preregistration.core.util.CryptoUtil;
import io.mosip.preregistration.core.util.HashUtill;
import io.mosip.preregistration.core.util.ValidationUtil;
import io.mosip.preregistration.demographic.exception.system.DateParseException;
import io.mosip.preregistration.demographic.exception.system.JsonParseException;
import io.mosip.preregistration.demographic.exception.system.SystemFileIOException;
import io.mosip.preregistration.demographic.exception.system.SystemIllegalArgumentException;
import jakarta.annotation.PostConstruct;

import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_SESSIONID;
import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_IDTYPE;
import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_ID;

/**
 * This class provides the utility methods for DemographicService
 * 
 * @author Ravi C Balaji
 * @author Sanober Noor
 * @since 1.0.0
 */
@Component
@RefreshScope
public class DemographicServiceUtil {

	@Value("${mosip.utc-datetime-pattern}")
	private String utcDateTimePattern;

	/**
	 * Environment instance
	 */
	@Autowired
	private Environment env;

	@Autowired
	private RestTemplate restTemplate;

	@Qualifier("selfTokenRestTemplate")
	@Autowired
	RestTemplate selfTokenrestTemplate;

	@Value("${mosip.io.prid.url}")
	private String pridURl;

	@Value("${mosip.preregistration.id-schema}")
	private String idSchemaConfig;

	@Value("${masterdata.resource.url}")
	private String masterdataResourseUrl;

	@Autowired
	private AppointmentService appointmentService;

	@Autowired
	private UISpecService uiSpecService;

	@Autowired
	private ApplicationRepostiory applicationRepostiory;
	/**
	 * Logger instance
	 */
	private Logger log = LoggerConfiguration.logConfig(DemographicServiceUtil.class);

	@Autowired
	CryptoUtil cryptoUtil;

	/**
	 * ObjectMapper global object creation
	 */
	private ObjectMapper mapper;

	@PostConstruct
	public void init() {
		mapper = JsonMapper.builder().addModule(new AfterburnerModule()).build();
		mapper.registerModule(new JavaTimeModule());
	}

	/**
	 * This setter method is used to assign the initial demographic entity values to
	 * the createDTO
	 * 
	 * @param demographicEntity pass the demographicEntity
	 * @return createDTO with the values
	 */
	public DemographicResponseDTO setterForCreateDTO(DemographicEntity demographicEntity) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"In setterForCreateDTO method of pre-registration service util");
		JSONParser jsonParser = new JSONParser();
		DemographicResponseDTO createDto = new DemographicResponseDTO();
		try {
			createDto.setPreRegistrationId(demographicEntity.getPreRegistrationId());
			createDto.setDemographicDetails((JSONObject) jsonParser.parse(new String(cryptoUtil
					.decrypt(demographicEntity.getApplicantDetailJson(), demographicEntity.getEncryptedDateTime()))));
			createDto.setStatusCode(demographicEntity.getStatusCode());
			createDto.setLangCode(demographicEntity.getLangCode());
			createDto.setCreatedBy(demographicEntity.getCreatedBy());
			createDto.setCreatedDateTime(getLocalDateString(demographicEntity.getCreateDateTime()));
			createDto.setUpdatedBy(demographicEntity.getUpdatedBy());
			createDto.setUpdatedDateTime(getLocalDateString(demographicEntity.getUpdateDateTime()));
		} catch (ParseException ex) {
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, ExceptionUtils.getStackTrace(ex));
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"In setterForCreateDTO method of pre-registration service- " + ex.getMessage());
			throw new JsonParseException(DemographicErrorCodes.PRG_PAM_APP_007.getCode(),
					DemographicErrorMessages.JSON_PARSING_FAILED.getMessage(), ex.getCause());
		} catch (EncryptionFailedException ex) {
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, ExceptionUtils.getStackTrace(ex));
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"In setterForCreateDTO method of pre-registration service- " + ex.getMessage());
			throw ex;
		}
		return createDto;
	}

	/**
	 * This setter method is used to assign the initial demographic entity values to
	 * the createDTO
	 * 
	 * @param demographicEntity pass the demographicEntity
	 * @return createDTO with the values
	 */
	public DemographicCreateResponseDTO setterForCreatePreRegistration(DemographicEntity demographicEntity,
			JSONObject requestJson) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"In setterForCreateDTO method of pre-registration service util");
		DemographicCreateResponseDTO createDto = new DemographicCreateResponseDTO();
		try {
			createDto.setPreRegistrationId(demographicEntity.getPreRegistrationId());
			createDto.setDemographicDetails(requestJson);
			createDto.setStatusCode(demographicEntity.getStatusCode());
			createDto.setLangCode(demographicEntity.getLangCode());
			createDto.setCreatedDateTime(getLocalDateString(demographicEntity.getCreateDateTime()));
		} catch (EncryptionFailedException ex) {
			log.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, ExceptionUtils.getStackTrace(ex));
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"In setterForCreateDTO method of pre-registration service- " + ex.getMessage());
			throw ex;
		}
		return createDto;
	}

	/**
	 * This setter method is used to assign the initial demographic entity values to
	 * the createDTO
	 * 
	 * @param demographicEntity pass the demographicEntity
	 * @return createDTO with the values
	 */
	public DemographicUpdateResponseDTO setterForUpdatePreRegistration(DemographicEntity demographicEntity) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"In setterForCreateDTO method of pre-registration service util");
		JSONParser jsonParser = new JSONParser();
		DemographicUpdateResponseDTO createDto = new DemographicUpdateResponseDTO();
		try {
			createDto.setPreRegistrationId(demographicEntity.getPreRegistrationId());
			createDto.setDemographicDetails((JSONObject) jsonParser.parse(new String(cryptoUtil
					.decrypt(demographicEntity.getApplicantDetailJson(), demographicEntity.getEncryptedDateTime()))));
			createDto.setStatusCode(demographicEntity.getStatusCode());
			createDto.setLangCode(demographicEntity.getLangCode());
			createDto.setUpdatedDateTime(getLocalDateString(demographicEntity.getCreateDateTime()));
		} catch (ParseException ex) {
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, ExceptionUtils.getStackTrace(ex));
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"In setterForCreateDTO method of pre-registration service- " + ex.getMessage());
			throw new JsonParseException(DemographicErrorCodes.PRG_PAM_APP_007.getCode(),
					DemographicErrorMessages.JSON_PARSING_FAILED.getMessage(), ex.getCause());
		} catch (EncryptionFailedException ex) {
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, ExceptionUtils.getStackTrace(ex));
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"In setterForCreateDTO method of pre-registration service- " + ex.getMessage());
			throw ex;
		}
		return createDto;
	}

	/**
	 * This method is used to set the values from the request to the
	 * demographicEntity entity fields.
	 * 
	 * @param demographicRequest pass demographicRequest
	 * @param requestId          pass requestId
	 * @param entityType         pass entityType
	 * @return demographic entity with values
	 */

	public DemographicEntity prepareDemographicEntityForCreate(DemographicRequestDTO demographicRequest,
			String statuscode, String userId, String preRegistrationId) {

		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"In prepareDemographicEntity method of pre-registration service util");
		DemographicEntity demographicEntity = new DemographicEntity();
		saveAndUpdateApplicationEntity(preRegistrationId, BookingTypeCodes.NEW_PREREGISTRATION.getBookingTypeCode(),
				ApplicationStatusCode.DRAFT.getApplicationStatusCode(), StatusCodes.APPLICATION_INCOMPLETE.getCode(),
				userId);
		demographicEntity.setPreRegistrationId(preRegistrationId);
		LocalDateTime encryptionDateTime = DateUtils.getUTCCurrentDateTime();
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"Encryption start time : " + DateUtils.getUTCCurrentDateTimeString());
		byte[] encryptedDemographicDetails = cryptoUtil
				.encrypt(demographicRequest.getDemographicDetails().toJSONString().getBytes(), encryptionDateTime);
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"Encryption end time : " + DateUtils.getUTCCurrentDateTimeString());
		demographicEntity.setApplicantDetailJson(encryptedDemographicDetails);
		demographicEntity.setLangCode(demographicRequest.getLangCode());
		demographicEntity.setCrAppuserId(userId);
		demographicEntity.setCreatedBy(userId);
		demographicEntity.setCreateDateTime(LocalDateTime.now(ZoneId.of("UTC")));
		demographicEntity.setStatusCode(statuscode);
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"Hashing start time : " + DateUtils.getUTCCurrentDateTimeString());
		demographicEntity.setDemogDetailHash(HashUtill.hashUtill(demographicEntity.getApplicantDetailJson()));
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"Hashing end time : " + DateUtils.getUTCCurrentDateTimeString());
		demographicEntity.setUpdatedBy(userId);
		demographicEntity.setUpdateDateTime(LocalDateTime.now(ZoneId.of("UTC")));
		demographicEntity.setEncryptedDateTime(encryptionDateTime);
		return demographicEntity;
	}

	/**
	 * This method is used to set the values from the request to the
	 * demographicEntity entity fields.
	 * 
	 * @param demographicRequest pass demographicRequest
	 * @param requestId          pass requestId
	 * @param entityType         pass entityType
	 * @return demographic entity with values
	 */
	public DemographicEntity prepareDemographicEntityForUpdate(DemographicEntity demographicEntity,
			DemographicRequestDTO demographicRequest, String statuscode, String userId, String preRegistrationId)
			throws EncryptionFailedException {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"In prepareDemographicEntity method of pre-registration service util");
		ApplicationEntity applicationEntity = findApplicationById(preRegistrationId);
		saveAndUpdateApplicationEntity(preRegistrationId, applicationEntity.getBookingType(),
				applicationEntity.getApplicationStatusCode(), applicationEntity.getBookingStatusCode(),
				applicationEntity.getCrBy());
		demographicEntity.setPreRegistrationId(preRegistrationId);
		LocalDateTime encryptionDateTime = DateUtils.getUTCCurrentDateTime();
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"Encryption start time : " + DateUtils.getUTCCurrentDateTimeString());
		byte[] encryptedDemographicDetails = cryptoUtil
				.encrypt(demographicRequest.getDemographicDetails().toJSONString().getBytes(), encryptionDateTime);
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"Encryption end time : " + DateUtils.getUTCCurrentDateTimeString());
		demographicEntity.setApplicantDetailJson(encryptedDemographicDetails);
		demographicEntity.setLangCode(demographicRequest.getLangCode());
		demographicEntity.setStatusCode(statuscode);
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"Hashing start time : " + DateUtils.getUTCCurrentDateTimeString());
		demographicEntity.setDemogDetailHash(HashUtill.hashUtill(demographicEntity.getApplicantDetailJson()));
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"Hashing end time : " + DateUtils.getUTCCurrentDateTimeString());
		demographicEntity.setUpdateDateTime(LocalDateTime.now(ZoneId.of("UTC")));
		demographicEntity.setEncryptedDateTime(encryptionDateTime);
		return demographicEntity;
	}

	/**
	 * This method is used to add the initial request values into a map for input
	 * validations.
	 *
	 * @param demographicRequestDTO pass demographicRequestDTO
	 * @return a map for request input validation
	 */

	public Map<String, String> prepareRequestMap(MainRequestDTO<?> requestDto) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "In prepareRequestMap method of Login Service Util");
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

	/**
	 * This method is used to set the JSON values to RequestCodes constants.
	 * 
	 * @param demographicData pass demographicData
	 * @param identityKey     pass identityKey
	 * @return values from JSON based on key
	 * 
	 * @throws ParseException                        On json Parsing Failed
	 * @throws org.json.simple.parser.ParseException
	 * 
	 */
	public JSONArray getValueFromIdentity(byte[] demographicData, String identityKey)
			throws ParseException, org.json.simple.parser.ParseException {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"In getValueFromIdentity method of pre-registration service util ");
		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObj = (JSONObject) jsonParser.parse(new String(demographicData));
		JSONObject identityObj = (JSONObject) jsonObj.get(DemographicRequestCodes.IDENTITY.getCode());
		return (JSONArray) identityObj.get(identityKey);
	}

	/**
	 * This method is used to set the JSON values to RequestCodes constants.
	 * 
	 * @param demographicData pass demographicData
	 * @param identityKey     pass postalcode
	 * @return values from JSON
	 * 
	 * @throws ParseException                        On json Parsing Failed
	 * @throws org.json.simple.parser.ParseException
	 * 
	 */

	public String getIdJSONValue(String demographicData, String value) throws ParseException {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"In getValueFromIdentity method of pe-registration service util to get getIdJSONValue ");

		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObj = (JSONObject) jsonParser.parse(demographicData);
		JSONObject identityObj = (JSONObject) jsonObj.get(DemographicRequestCodes.IDENTITY.getCode());
		if (identityObj.get(value) != null)
			return identityObj.get(value).toString();
		return "";

	}

	/**
	 * This method is used as Null checker for different input keys.
	 *
	 * @param key pass the key
	 * @return true if key not null and return false if key is null.
	 */
	public boolean isNull(Object key) {
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

	/**
	 * This method is used to validate Pending_Appointment and Booked status codes.
	 * 
	 * @param statusCode pass statusCode
	 * @return true or false
	 */
	public boolean checkStatusForDeletion(String statusCode) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"In checkStatusForDeletion method of pre-registration service util ");

		if (!statusCode.equals(StatusCodes.EXPIRED.getCode())) {
			return true;
		} else {
			throw new OperationNotAllowedException(DemographicErrorCodes.PRG_PAM_APP_003.getCode(),
					DemographicErrorMessages.DELETE_OPERATION_NOT_ALLOWED.getMessage());
		}
	}

	public String getCurrentResponseTime() {
		return DateUtils.formatDate(new Date(System.currentTimeMillis()), utcDateTimePattern);
	}

	public Date getDateFromString(String date) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"In getDateFromString method of pre-registration service util ");
		try {
			return new SimpleDateFormat(utcDateTimePattern).parse(date);
		} catch (java.text.ParseException ex) {
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, ExceptionUtils.getStackTrace(ex));
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"In getDateFromString method of pre-registration service- " + ex.getCause());
			throw new DateParseException(DemographicErrorCodes.PRG_PAM_APP_011.getCode(),
					DemographicErrorMessages.UNSUPPORTED_DATE_FORMAT.getMessage(), ex.getCause());
		}
	}

	public String getLocalDateString(LocalDateTime date) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(utcDateTimePattern);
		return date.format(dateTimeFormatter);
	}

	public boolean isStatusValid(String status) {
		for (StatusCodes choice : StatusCodes.values())
			if (choice.getCode().equals(status))
				return true;
		return false;
	}

	/**
	 * This method will return the MainResponseDTO with id and version
	 * 
	 * @param mainRequestDto
	 * @return MainResponseDTO<?>
	 */
	public MainResponseDTO<?> getMainResponseDto(MainRequestDTO<?> mainRequestDto) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "In getMainResponseDTO method of Login Common Util");
		MainResponseDTO<?> response = new MainResponseDTO<>();
		response.setId(mainRequestDto.getId());
		response.setVersion(mainRequestDto.getVersion());

		return response;
	}

	public static Integer parsePageIndex(String text) {
		try {
			return Integer.parseInt(text);
		} catch (NumberFormatException e) {
			throw new SystemIllegalArgumentException(DemographicErrorCodes.PRG_PAM_APP_019.getCode(),
					DemographicErrorMessages.INVALID_PAGE_INDEX_VALUE.getMessage());
		}
	}

	public static Integer parsePageSize(String text) {
		try {
			return Integer.parseInt(text);
		} catch (IllegalArgumentException e) {
			throw new SystemIllegalArgumentException(DemographicErrorCodes.PRG_PAM_APP_015.getCode(),
					DemographicErrorMessages.PAGE_SIZE_MUST_BE_GREATER_THAN_ZERO.getMessage());
		}
	}

	/**
	 * This method is used for config rest call
	 * 
	 * @param filname
	 * @return
	 */
	public String getJson(String filename) {
		try {
			String configServerUri = env.getProperty("spring.cloud.config.uri");
			String configLabel = env.getProperty("spring.cloud.config.label");
			String configProfile = env.getProperty("spring.profiles.active");
			String configAppName = env.getProperty("spring.cloud.config.name");
			StringBuilder uriBuilder = new StringBuilder();
			uriBuilder.append(configServerUri + "/").append(configAppName + "/").append(configProfile + "/")
					.append(configLabel + "/").append(filename);
			// uriBuilder.append(
			// "http://104.211.212.28:51000/preregistration/dev/master/PreRegistrationIdentitiyMapping.json");
			log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					" URL in demographic service util of getJson " + uriBuilder);
			return restTemplate.getForObject(uriBuilder.toString(), String.class);
		} catch (Exception ex) {
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, ExceptionUtils.getStackTrace(ex));
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"In pre-registration service util of getPreregistrationIdentityJson- " + ex.getMessage());
			throw new SystemFileIOException(DemographicErrorCodes.PRG_PAM_APP_018.getCode(),
					DemographicErrorMessages.UBALE_TO_READ_IDENTITY_JSON.getMessage(), null);
		}
	}

	public String generateId() {
		String prid = null;
		try {
			UriComponentsBuilder regbuilder = UriComponentsBuilder.fromHttpUrl(pridURl);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<RequestWrapper<RegistrationCenterResponseDto>> entity = new HttpEntity<>(headers);
			String uriBuilder = regbuilder.build().encode().toUriString();
			log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"In callRegCenterDateRestService method of Booking Service URL- " + uriBuilder);
			ResponseEntity<ResponseWrapper<PridFetchResponseDto>> responseEntity = selfTokenrestTemplate.exchange(
					uriBuilder, HttpMethod.GET, entity,
					new ParameterizedTypeReference<ResponseWrapper<PridFetchResponseDto>>() {
					});
			ResponseWrapper<PridFetchResponseDto> body = responseEntity.getBody();
			if (body != null) {
				if (body.getErrors() != null && !body.getErrors().isEmpty()) {
					throw new RestCallException(body.getErrors().get(0).getErrorCode(),
							body.getErrors().get(0).getMessage());
				}
				if (body.getResponse() != null) {
					prid = body.getResponse().getPrid();
				}
			}
			if (prid == null || prid.isEmpty()) {
				throw new RestCallException(DemographicErrorCodes.PRG_PAM_APP_020.getCode(),
						DemographicErrorMessages.PRID_RESTCALL_FAIL.getMessage());
			}

		} catch (RestClientException ex) {
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, ExceptionUtils.getStackTrace(ex));
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"In callRegCenterDateRestService method of Booking Service Util for HttpClientErrorException- "
							+ ex.getMessage());
			throw new RestCallException(DemographicErrorCodes.PRG_PAM_APP_020.getCode(),
					DemographicErrorMessages.PRID_RESTCALL_FAIL.getMessage());
		}
		return prid;

	}

	public IdSchemaDto getSchema() {
		IdSchemaDto response = null;
		try {
			UriComponentsBuilder regbuilder = UriComponentsBuilder.fromHttpUrl(idSchemaConfig);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<RequestWrapper<RegistrationCenterResponseDto>> entity = new HttpEntity<>(headers);
			String uriBuilder = regbuilder.build().encode().toUriString();

			ResponseEntity<ResponseWrapper<IdSchemaDto>> responseEntity = selfTokenrestTemplate.exchange(uriBuilder,
					HttpMethod.GET, entity, new ParameterizedTypeReference<ResponseWrapper<IdSchemaDto>>() {
					});
			ResponseWrapper<IdSchemaDto> body = responseEntity.getBody();
			if (body != null) {
				if (body.getErrors() != null && !body.getErrors().isEmpty()) {
					throw new RestCallException(body.getErrors().get(0).getErrorCode(),
							body.getErrors().get(0).getMessage());
				}
				response = body.getResponse();
			}

			if (response == null) {
				throw new RestCallException(DemographicErrorCodes.PRG_PAM_APP_020.getCode(),
						DemographicErrorMessages.ID_SCHEMA_FETCH_FAILED.getMessage());
			}

		} catch (RestClientException ex) {

			throw new RestCallException(DemographicErrorCodes.PRG_PAM_APP_020.getCode(),
					DemographicErrorMessages.ID_SCHEMA_FETCH_FAILED.getMessage());
		}
		return response;

	}

	public ResponseEntity<?> callAuthService(String url, HttpMethod httpMethodType, MediaType mediaType, Object body,
			Map<String, String> headersMap, Class<?> responseClass) {
		ResponseEntity<?> response = null;
		try {
			log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "In getResponseEntity method of Login Common Util");
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
			log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "In call to kernel rest service :" + url);
			response = restTemplate.exchange(url, httpMethodType, request, responseClass);
		} catch (RestClientException ex) {
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"Kernel rest call exception " + ExceptionUtils.getStackTrace(ex));
			throw new RestClientException("rest call failed");
		}
		return response;

	}

	public MainResponseDTO<DeleteBookingDTO> deleteBooking(String preRegId) throws Exception {
		return appointmentService.deleteBooking(preRegId);
	}

	public boolean isDemographicBookedOrExpired(DemographicEntity demographicEntity, ValidationUtil validationUtil) {
		return validationUtil.isStatusBookedOrExpired(demographicEntity.getStatusCode());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public JSONObject constructNewDemographicRequest(List<String> identityKeys, JSONObject demographicDetails)
			throws ParseException {

		List<Object> demographicKeys = Arrays.asList(
				((HashMap) demographicDetails.get(DemographicRequestCodes.IDENTITY.getCode())).keySet().toArray());

		log.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "IdentitySchemakeys: {" + identityKeys.toString()
				+ "} and PreRegIdentitykeys: {" + demographicKeys.toString() + "}");

		JSONObject demographicJson = new JSONObject();

		for (String key : identityKeys) {
			if (demographicKeys.contains(key)) {
				demographicJson.put(key,
						((HashMap) demographicDetails.get(DemographicRequestCodes.IDENTITY.getCode())).get(key));
			}
		}

		JSONObject constructedJson = new JSONObject();
		constructedJson.put(DemographicRequestCodes.IDENTITY.getCode(), demographicJson);

		return constructedJson;
	}

	public ApplicationEntity saveAndUpdateApplicationEntity(String preId, String bookingTypeCode,
			String applicationStatusCode, String bookingStatusCode, String userId) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"Creating/Updating an applications in applications table with ID: " + preId);
		ApplicationEntity applicationEntity = new ApplicationEntity();
		applicationEntity.setApplicationId(preId);
		applicationEntity.setApplicationStatusCode(applicationStatusCode);
		applicationEntity.setBookingType(bookingTypeCode);
		applicationEntity.setBookingStatusCode(bookingStatusCode);
		applicationEntity.setCrBy(userId);
		applicationEntity.setCrDtime(LocalDateTime.now(ZoneId.of("UTC")));
		applicationEntity.setUpdBy(userId);
		applicationEntity.setUpdDtime(LocalDateTime.now(ZoneId.of("UTC")));
		applicationEntity.setContactInfo(userId);
		try {
			applicationEntity = applicationRepostiory.save(applicationEntity);
		} catch (Exception ex) {
			log.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, ExceptionUtils.getStackTrace(ex));
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"Error while persisting applications entity -" + ex.getMessage());
			throw new RecordFailedToUpdateException(ApplicationErrorCodes.PRG_APP_009.getCode(),
					ApplicationErrorMessages.FAILED_TO_UPDATE_APPLICATIONS.getMessage());
		}
		return applicationEntity;
	}

	public void updateApplicationStatus(String applicationId, String status, String userId) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"Updating applications status in applications table with statuscode: {" + status
						+ "} for applicationId: {" + applicationId + "}");
		try {
			ApplicationEntity applicationEntity = findApplicationById(applicationId);
			applicationEntity.setBookingStatusCode(status);
			applicationEntity.setUpdBy(userId);
			applicationEntity.setUpdDtime(LocalDateTime.now());
			if (status.toLowerCase().equals(StatusCodes.PENDING_APPOINTMENT.getCode().toLowerCase())) {
				applicationEntity.setApplicationStatusCode(ApplicationStatusCode.SUBMITTED.getApplicationStatusCode());
			}
			applicationRepostiory.update(applicationEntity);
		} catch (Exception ex) {
			log.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, ExceptionUtils.getStackTrace(ex));
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"Error while updating status for applications -" + ex.getMessage());
			throw new RecordFailedToUpdateException(ApplicationErrorCodes.PRG_APP_010.getCode(),
					ApplicationErrorMessages.STATUS_UPDATE_FOR_APPLICATIONS_FAILED.getMessage());
		}
	}

	public ApplicationEntity findApplicationById(String applicationId) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,"Fetching applications entry for applicationID: "+ applicationId);
		ApplicationEntity applicationEntity = null;
		try {
			applicationEntity = applicationRepostiory.findByApplicationId(applicationId);
			if (Objects.isNull(applicationEntity)) {
				throw new RecordNotFoundException(DemographicErrorCodes.PRG_PAM_APP_005.getCode(),
						DemographicErrorMessages.NO_RECORD_FOUND_FOR_USER_ID.getMessage());
			}
		} catch (DataAccessException ex) {
			throw new RecordNotFoundException(DemographicErrorCodes.PRG_PAM_APP_005.getCode(),
					DemographicErrorMessages.NO_RECORD_FOUND_FOR_USER_ID.getMessage());
		}
		return applicationEntity;
	}

	public void deleteApplicationFromApplications(String applicationId) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,"Deleting applications entry for applicationID: "+ applicationId);
		try {
			applicationRepostiory.deleteById(applicationId);
		} catch (DatabaseOperationException e) {
			throw new RecordFailedToDeleteException(ApplicationErrorCodes.PRG_APP_011.getCode(),
					ApplicationErrorMessages.DELETE_FAILED_FOR_APPLICATION.getMessage());
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Set<String> getDataCaptureLaanguage(JSONObject jsonObject) {

		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "In getDataCaputureLanguage method");

		Set<String> dataCaptureLang = null;

		List<Object> demographicKeys = Arrays
				.asList(((HashMap) jsonObject.get(DemographicRequestCodes.IDENTITY.getCode())).keySet().toArray());

		for (Object key : demographicKeys) {
			log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,"key--->"+ key);

			Object demograhicObject = ((HashMap) jsonObject.get(DemographicRequestCodes.IDENTITY.getCode())).get(key);

			if (demograhicObject instanceof List) {
				JSONArray arr = ((JSONArray) demograhicObject);

				dataCaptureLang = (Set<String>) arr.stream().map(data -> {
					String language = null;
					try {
						language = mapper.readValue(data.toString(), LanguageValueDto.class).getLanguage();
					} catch (IOException e) {
						throw new JsonParseException(DemographicErrorCodes.PRG_PAM_APP_007.getCode(),
								DemographicErrorMessages.JSON_IO_EXCEPTION.getMessage());
					}
					return language;
				}).collect(Collectors.toSet());

				break;
			}
		}
		return dataCaptureLang;
	}

	@SuppressWarnings("rawtypes")
	public ApplicantTypeRequestDTO createApplicantTypeRequest(DemographicEntity demographicEntity)
			throws ParseException {
		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject = (JSONObject) jsonParser.parse(new String(cryptoUtil
				.decrypt(demographicEntity.getApplicantDetailJson(), demographicEntity.getEncryptedDateTime())));

		List<Object> demographicKeys = Arrays
				.asList(((HashMap) jsonObject.get(DemographicRequestCodes.IDENTITY.getCode())).keySet().toArray());

		ApplicantTypeRequestDTO attributes = new ApplicantTypeRequestDTO();
		List<ApplicantTypeKeyValueDTO<String, Object>> attributeList = new ArrayList<>();

		for (Object key : demographicKeys) {
			ApplicantTypeKeyValueDTO<String, Object> attribute = new ApplicantTypeKeyValueDTO<>();
			attribute.setAttribute((String) key);
			attribute.setValue(((HashMap) jsonObject.get(DemographicRequestCodes.IDENTITY.getCode())).get(key));
			attributeList.add(attribute);
		}

		attributes.setAttributes(attributeList);

		return attributes;

	}

	public String getApplicantypeCode(ApplicantTypeRequestDTO applicantTypeRequest) {

		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,"In getApplicantypeCode method ");
		String applicantTypeCode = null;
		try {
			UriComponentsBuilder regbuilder = UriComponentsBuilder
					.fromHttpUrl(masterdataResourseUrl + "/getApplicantType");
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			RequestWrapper<ApplicantTypeRequestDTO> request = new RequestWrapper<>();
			request.setRequest(applicantTypeRequest);
			HttpEntity<RequestWrapper<ApplicantTypeRequestDTO>> entity = new HttpEntity<>(request, headers);
			String uriBuilder = regbuilder.build().encode().toUriString();

			ResponseEntity<ResponseWrapper<ApplicantTypeResponseDTO>> responseEntity = selfTokenrestTemplate.exchange(
					uriBuilder, HttpMethod.POST, entity,
					new ParameterizedTypeReference<ResponseWrapper<ApplicantTypeResponseDTO>>() {
					});

			ResponseWrapper<ApplicantTypeResponseDTO> body = responseEntity.getBody();
			if (body != null) {
				if (body.getErrors() != null && !body.getErrors().isEmpty()) {
					throw new MasterDataException(body.getErrors().get(0).getErrorCode(),
							body.getErrors().get(0).getMessage());
				}
				ApplicantTypeResponseDTO applicantTypeResponseDTO = body.getResponse();
				if (applicantTypeResponseDTO != null && applicantTypeResponseDTO.getApplicantType() != null) {
					applicantTypeCode = applicantTypeResponseDTO.getApplicantType().getApplicantTypeCode();
				}
			}

		} catch (RestClientException ex) {
			log.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, ExceptionUtils.getStackTrace(ex));
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"restcall failed to get applicanttype -" + ex.getMessage());
			throw new MasterDataException(DemographicErrorCodes.PRG_PAM_APP_020.getCode(),
					DemographicErrorMessages.MASTERDATA_RESTCALL_FAIL.getMessage());
		}
		return applicantTypeCode;
	}

	@SuppressWarnings({ "rawtypes" })
	public ApplicantValidDocumentDto getDocCatAndTypeForApplicantCode(String applicantTypeCode, String langCode) {
		ResponseWrapper<ApplicantValidDocumentDto> response = new ResponseWrapper<>();
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "In getDocCatAndTypeForApplicantCode method ");
		ResponseEntity<ResponseWrapper<ApplicantValidDocumentDto>> responseEntity = null;
		try {
			UriComponentsBuilder regbuilder = UriComponentsBuilder.fromHttpUrl(
					masterdataResourseUrl + "/applicanttype/" + applicantTypeCode + "/languages?languages=" + langCode);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity entity = new HttpEntity<>(headers);

			String uriBuilder = regbuilder.build().encode().toUriString();

			responseEntity = selfTokenrestTemplate.exchange(uriBuilder, HttpMethod.GET, entity,
					new ParameterizedTypeReference<ResponseWrapper<ApplicantValidDocumentDto>>() {
					});

			ResponseWrapper<ApplicantValidDocumentDto> body = responseEntity.getBody();
			if (body != null) {
				if (body.getErrors() != null && !body.getErrors().isEmpty()) {
					throw new MasterDataException(body.getErrors().get(0).getErrorCode(),
							body.getErrors().get(0).getMessage());
				}
				response.setResponse(body.getResponse());
			}

		} catch (RestClientException ex) {
			log.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, ExceptionUtils.getStackTrace(ex));
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"restcall failed to get documentcategories for applicanttype code -" + ex.getMessage());
			throw new MasterDataException(DemographicErrorCodes.PRG_PAM_APP_020.getCode(),
					DemographicErrorMessages.MASTERDATA_RESTCALL_FAIL.getMessage());
		}
		return response.getResponse();
	}

	public Set<String> getMandatoryDocCatogery() {
		log.info("In mandatoryDocsCategory method ");
		Set<String> mandatoryDocs = new HashSet<>();
		MainResponseDTO<UISpecMetaDataDTO> uiSpec = uiSpecService.getLatestUISpec(0.0, 0.0);
		uiSpec.getResponse().getJsonSpec().get("identity").get("identity").forEach(field -> {
			if (field.get("controlType").asText().equals("fileupload")
					&& Boolean.valueOf(field.get("required").asText())) {
				mandatoryDocs.add(field.get("subType").asText());
			}
		});

		return mandatoryDocs;
	}
}