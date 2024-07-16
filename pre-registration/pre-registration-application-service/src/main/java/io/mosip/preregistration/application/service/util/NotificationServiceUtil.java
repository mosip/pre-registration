package io.mosip.preregistration.application.service.util;

import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_ID;
import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_IDTYPE;
import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_SESSIONID;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.templatemanager.spi.TemplateManager;
import io.mosip.kernel.core.util.JsonUtils;
import io.mosip.kernel.core.util.exception.JsonMappingException;
import io.mosip.kernel.core.util.exception.JsonParseException;
import io.mosip.preregistration.application.constant.PreRegLoginConstant;
import io.mosip.preregistration.application.constant.PreRegLoginErrorConstants;
import io.mosip.preregistration.application.dto.OtpRequestDTO;
import io.mosip.preregistration.application.dto.PreRegMailRequestDto;
import io.mosip.preregistration.application.dto.PreRegSmsRequestDto;
import io.mosip.preregistration.application.dto.PreRegSmsResponseDto;
import io.mosip.preregistration.application.exception.PreRegLoginException;
import io.mosip.preregistration.booking.dto.RegistrationCenterDto;
import io.mosip.preregistration.booking.dto.RegistrationCenterResponseDto;
import io.mosip.preregistration.core.common.dto.KeyValuePairDto;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.NotificationDTO;
import io.mosip.preregistration.core.common.dto.ResponseWrapper;
import io.mosip.preregistration.core.common.dto.SMSRequestDTO;
import io.mosip.preregistration.core.config.LoggerConfiguration;

/**
 * The util class.
 * 
 * @author Sanober Noor
 * @since 1.0.0
 * @author Aiham Hasan
 * @since 1.2.0
 *
 */
@Component
public class NotificationServiceUtil {

	@Value("${mosip.utc-datetime-pattern}")
	private String utcDateTimePattern;

	private Logger log = LoggerConfiguration.logConfig(NotificationServiceUtil.class);

	@Autowired
	private Environment environment;

	@Qualifier("selfTokenRestTemplate")
	@Autowired
	RestTemplate restTemplate;

	@Autowired
	TemplateManager templateManager;

	/** The Constant LANG_CODE. */

	private static final String LANG_CODE = "langcode";

	private static final String IS_ACTIVE = "isActive";

	/** The Constant TEMPLATE_TYPE_CODE. */

	private static final String TEMPLATE_TYPE_CODE = "templatetypecode";

	@Autowired
	private ObjectMapper objectMapper;

	@Value("${registrationcenter.centerdetail.rest.uri}")
	private String centerDetailUri;

	/**
	 * 
	 * @param jsonString
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws io.mosip.kernel.core.exception.IOException
	 * @throws JSONException
	 * @throws ParseException
	 * @throws com.fasterxml.jackson.core.JsonParseException
	 * @throws com.fasterxml.jackson.databind.JsonMappingException
	 */

	@SuppressWarnings("unchecked")
	public MainRequestDTO<NotificationDTO> createNotificationDetails(String jsonString, String langauageCode,
			boolean isLatest)
			throws JsonParseException, JsonMappingException, io.mosip.kernel.core.exception.IOException, JSONException,
			ParseException, com.fasterxml.jackson.core.JsonParseException,
			com.fasterxml.jackson.databind.JsonMappingException, IOException {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"In createUploadDto method of notification service util with body " + jsonString);
		MainRequestDTO<NotificationDTO> notificationReqDto = new MainRequestDTO<>();
		JSONObject notificationData = new JSONObject(jsonString);
		JSONObject notificationDtoData = (JSONObject) notificationData.get("request");
		NotificationDTO notificationDto = null;
		List<KeyValuePairDto<String, String>> langaueNamePairs = new ArrayList<KeyValuePairDto<String, String>>();
		if (isLatest) {
			HashMap<String, String> result = objectMapper.readValue(notificationDtoData.toString(), HashMap.class);
			KeyValuePairDto langaueNamePair = null;
			for (Map.Entry<String, String> set : result.entrySet()) {
				langaueNamePair = new KeyValuePairDto();
				notificationDto = objectMapper.convertValue(set.getValue(), NotificationDTO.class);
				langaueNamePair.setKey(set.getKey());
				langaueNamePair.setValue(notificationDto.getName());
				langaueNamePairs.add(langaueNamePair);
			}
			if (notificationDto != null) {
				notificationDto.setFullName(langaueNamePairs);
				notificationDto.setLanguageCode(langauageCode);
			}
		}
		if (!isLatest) {
			notificationDto = (NotificationDTO) JsonUtils.jsonStringToJavaObject(NotificationDTO.class,
					notificationDtoData.toString());
			KeyValuePairDto langaueNamePair = new KeyValuePairDto();
			langaueNamePair.setKey(langauageCode);
			langaueNamePair.setValue(notificationDto.getName());
			langaueNamePairs.add(langaueNamePair);
			notificationDto.setFullName(langaueNamePairs);
			notificationDto.setLanguageCode(langauageCode);
		}

		notificationReqDto.setId(notificationData.get("id").toString());
		notificationReqDto.setVersion(notificationData.get("version").toString());
		if (!(notificationData.get("requesttime") == null
				|| notificationData.get("requesttime").toString().isEmpty())) {
			notificationReqDto.setRequesttime(
					new SimpleDateFormat(utcDateTimePattern).parse(notificationData.get("requesttime").toString()));
		} else {
			notificationReqDto.setRequesttime(null);
		}
		notificationReqDto.setRequest(notificationDto);
		return notificationReqDto;
	}

	/**
	 * Sms notification.
	 *
	 * @param values               the values
	 * @param sender               the sender
	 * @param contentTemplate      the content template
	 * @param notificationMobileNo the notification mobile no
	 * @throws IOException
	 * @throws PreRegLoginException
	 * @throws IdAuthenticationBusinessException the id authentication business
	 *                                           exception
	 */

	public void invokeSmsNotification(Map values, String userId, MainRequestDTO<OtpRequestDTO> requestDTO,
			String langCode) throws PreRegLoginException, IOException {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"In invokeSmsNotification method of notification service util");
		String otpSmsTemplate = environment.getProperty(PreRegLoginConstant.OTP_SMS_TEMPLATE);
		String smsTemplate = applyTemplate(values, otpSmsTemplate, langCode);
		sendSmsNotification(userId, smsTemplate, requestDTO);
	}

	/**
	 * Email notification.
	 *
	 * @param values          the values
	 * @param emailId         the email id
	 * @param sender          the sender
	 * @param contentTemplate the content template
	 * @param subjectTemplate the subject template
	 * @throws IOException
	 * @throws PreRegLoginException
	 * @throws IdAuthenticationBusinessException the id authentication business
	 *                                           exception
	 */
	public void invokeEmailNotification(Map values, String userId, MainRequestDTO<OtpRequestDTO> requestDTO,
			String langCode) throws PreRegLoginException, IOException {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"In invokeEmailNotification method of notification service util");
		String otpContentTemaplate = environment.getProperty(PreRegLoginConstant.OTP_CONTENT_TEMPLATE);
		String otpSubjectTemplate = environment.getProperty(PreRegLoginConstant.OTP_SUBJECT_TEMPLATE);
		String mailSubject = applyTemplate(values, otpSubjectTemplate, langCode);
		String mailContent = applyTemplate(values, otpContentTemaplate, langCode);
		sendEmailNotification(userId, mailSubject, mailContent, requestDTO);
	}

	/**
	 * Send sms notification.
	 *
	 * @param notificationMobileNo the notification mobile no
	 * @param message              the message
	 * @throws PreRegLoginException
	 */
	public void sendSmsNotification(String notificationMobileNo, String message,
			MainRequestDTO<OtpRequestDTO> requestDTO) throws PreRegLoginException {
		try {
			PreRegSmsRequestDto preRegSmsRequestDto = new PreRegSmsRequestDto();
			SMSRequestDTO smsRequestDto = new SMSRequestDTO();
			smsRequestDto.setMessage(message);
			smsRequestDto.setNumber(notificationMobileNo);
			preRegSmsRequestDto.setRequest(smsRequestDto);
			preRegSmsRequestDto.setId(requestDTO.getId());
			preRegSmsRequestDto.setRequesttime(LocalDateTime.now());
			preRegSmsRequestDto.setVersion(requestDTO.getVersion());

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

			HttpEntity<PreRegSmsRequestDto> entity1 = new HttpEntity<PreRegSmsRequestDto>(preRegSmsRequestDto, headers);

			PreRegSmsResponseDto response = restTemplate.exchange(environment.getProperty("sms-notification.rest.uri"),
					HttpMethod.POST, entity1, PreRegSmsResponseDto.class).getBody();
			if (response != null && response.getResponse() != null) {
				if (!response.getResponse().getStatus().equalsIgnoreCase(PreRegLoginConstant.SUCCESS))
					throw new PreRegLoginException(PreRegLoginErrorConstants.DATA_VALIDATION_FAILED.getErrorCode(),
							PreRegLoginErrorConstants.DATA_VALIDATION_FAILED.getErrorMessage());
			}

		} catch (PreRegLoginException e) {
			log.error(PreRegLoginConstant.SESSION_ID, "Inside SMS Notification >>>>>", e.getErrorCode(),
					e.getErrorText());
			throw new PreRegLoginException(PreRegLoginErrorConstants.DATA_VALIDATION_FAILED.getErrorCode(),
					PreRegLoginErrorConstants.DATA_VALIDATION_FAILED.getErrorMessage());
		}
	}

	/**
	 * Send email notification.
	 *
	 * @param emailId     the email id
	 * @param mailSubject the mail subject
	 * @param mailContent the mail content
	 * @throws PreRegLoginException
	 */
	public void sendEmailNotification(String emailId, String mailSubject, String mailContent,
			MainRequestDTO<OtpRequestDTO> requestDTO) throws PreRegLoginException {
		try {
			PreRegMailRequestDto mailRequestDto = new PreRegMailRequestDto();
			mailRequestDto.setMailSubject(mailSubject);
			mailRequestDto.setMailContent(mailContent);

			mailRequestDto.setMailTo(new String[] { emailId });

			LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
			map.add("mailContent", mailContent);
			map.add("mailSubject", mailSubject);
			map.add("mailTo", emailId);

			HttpHeaders headers1 = new HttpHeaders();

			headers1.setContentType(MediaType.MULTIPART_FORM_DATA);

			HttpEntity<LinkedMultiValueMap<String, Object>> entity1 = new HttpEntity<LinkedMultiValueMap<String, Object>>(
					map, headers1);

			PreRegSmsResponseDto response = restTemplate.exchange(environment.getProperty("mail-notification.rest.uri"),
					HttpMethod.POST, entity1, PreRegSmsResponseDto.class).getBody();

			if (response != null && response.getResponse() != null) {
				if (!response.getResponse().getStatus().equalsIgnoreCase(PreRegLoginConstant.SUCCESS))
					throw new PreRegLoginException(PreRegLoginErrorConstants.DATA_VALIDATION_FAILED.getErrorCode(),
							PreRegLoginErrorConstants.DATA_VALIDATION_FAILED.getErrorMessage());
			}
		} catch (PreRegLoginException e) {
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"Inside Mail Notification >>>>>" + e.getErrorCode() + e.getErrorText());
			throw new PreRegLoginException(PreRegLoginErrorConstants.DATA_VALIDATION_FAILED.getErrorCode(),
					PreRegLoginErrorConstants.DATA_VALIDATION_FAILED.getErrorMessage());
		}
	}

	/**
	 * To apply Template for PDF Generation.
	 *
	 * @param templateName - template name for pdf format
	 * @param values       - list of contents
	 * @return the string
	 * @throws IdAuthenticationBusinessException the id authentication business
	 *                                           exception
	 * @throws IOException                       Signals that an I/O exception has
	 *                                           occurred.
	 */

	public String applyTemplate(Map mp, String templateName, String langCode) throws PreRegLoginException, IOException {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"In applyTemplate of NotificationServiceUtil for templateName {" + templateName + "} and values {" + mp
						+ "}");
		Objects.requireNonNull(templateName);
		Objects.requireNonNull(mp);
		StringWriter writer = new StringWriter();
		InputStream templateValue;
		String fetchedTemplate = fetchTemplate(templateName, langCode);
		templateValue = templateManager
				.merge(new ByteArrayInputStream(fetchedTemplate.getBytes(StandardCharsets.UTF_8)), mp);
		if (templateValue == null) {
			throw new PreRegLoginException(PreRegLoginErrorConstants.MISSING_INPUT_PARAMETER.getErrorCode(),
					String.format(PreRegLoginErrorConstants.MISSING_INPUT_PARAMETER.getErrorMessage(), "TEMPLATE"));
		}
		IOUtils.copy(templateValue, writer, StandardCharsets.UTF_8);
		return writer.toString();
	}

	/**
	 * Fetch Templates for e-KYC based on Template name.
	 *
	 * @param templateName the template name
	 * @return the string
	 * @throws IdAuthenticationBusinessException the id authentication business
	 *                                           exception
	 */
	public String fetchTemplate(String templateName, String langCode) throws PreRegLoginException {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"In fetchTemplate of NotificationServiceUtil for templateName : " + templateName);
		Map<String, String> params = new HashMap<>();
		params.put(LANG_CODE, langCode);
		params.put(TEMPLATE_TYPE_CODE, templateName);

		HttpHeaders headers1 = new HttpHeaders();

		headers1.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

		HttpEntity entity1 = new HttpEntity<>(headers1);

		String url = UriComponentsBuilder
				.fromUriString(environment.getProperty("id-masterdata-template-service-multilang.rest.uri"))
				.buildAndExpand(params).toString();

		Map<String, Object> response = restTemplate.exchange(url, HttpMethod.GET, entity1, Map.class, templateName)
				.getBody();

		Map<String, List<Map<String, Object>>> fetchResponse;
		if (response instanceof Map) {
			fetchResponse = (Map<String, List<Map<String, Object>>>) response.get("response");
		} else {
			fetchResponse = Collections.emptyMap();
		}

		List<Map<String, Object>> masterDataList = fetchResponse.get("templates");
		Map<String, Map<String, String>> masterDataMap = new HashMap<>();
		for (Map<String, Object> map : masterDataList) {
			String lang = String.valueOf(map.get("langCode"));
			if (!params.containsKey("langCode")
					|| (params.containsKey("langCode") && lang.contentEquals(params.get("langCode")))) {
				String key = String.valueOf(map.get("templateTypeCode"));
				String value = String.valueOf(map.get("fileText"));
				Object isActiveObj = map.get(IS_ACTIVE);
				if (isActiveObj instanceof Boolean && (Boolean) isActiveObj) {
					Map<String, String> valueMap = masterDataMap.computeIfAbsent(lang,
							k -> new LinkedHashMap<String, String>());
					valueMap.put(key, value);
				}
			}
		}

		return Optional.ofNullable(masterDataMap.get(params.get(LANG_CODE))).map(map -> map.get(templateName))
				.orElse("");
	}

	@SuppressWarnings("null")
	public NotificationDTO modifyCenterNameAndAddress(NotificationDTO notificationDto, String registrationCenterId,
			String langCode) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"In modifyCenterNameAndAddress of NotificationServiceUtil for registrationCenterId "
						+ registrationCenterId);
		if (notificationDto != null) {
			List<KeyValuePairDto<String, String>> centerName = notificationDto.getRegistrationCenterName();
			List<KeyValuePairDto<String, String>> address = notificationDto.getAddress();
			if (centerName == null && address == null) {
				centerName = new ArrayList<>();
				address = new ArrayList<>();
				String firstRegCenterLangCode = null;
				Map<String, RegistrationCenterDto> regCentersMap = new HashMap<String, RegistrationCenterDto>();
				for (KeyValuePairDto<String, String> key : notificationDto.getFullName()) {
					String regCenterLangCode = (String) key.getKey();
					RegistrationCenterDto centerDto = getNotificationCenterAddressDTO(registrationCenterId,
							regCenterLangCode);
					if (centerDto != null) {
						if (firstRegCenterLangCode == null) {
							firstRegCenterLangCode = regCenterLangCode;
						}
						regCentersMap.put(regCenterLangCode, centerDto);
					} else {
						centerDto = getNotificationCenterAddressDTO(registrationCenterId, "all");
						if (firstRegCenterLangCode == null) {
							firstRegCenterLangCode = regCenterLangCode;
						}
						regCentersMap.put(regCenterLangCode, centerDto);
					}
				}
				RegistrationCenterDto defaultCenterDto = null;
				if (regCentersMap.size() > 0) {
					defaultCenterDto = regCentersMap.get(firstRegCenterLangCode);
				}
				for (KeyValuePairDto<String, String> key : notificationDto.getFullName()) {
					String regCenterLangCode = (String) key.getKey();
					if (!regCentersMap.containsKey(regCenterLangCode)) {
						regCentersMap.put(regCenterLangCode, defaultCenterDto);
					}
					RegistrationCenterDto centerDto = regCentersMap.get(regCenterLangCode);
					KeyValuePairDto<String, String> regCenterDetailsName = new KeyValuePairDto<>();
					regCenterDetailsName.setKey((String) key.getKey());
					regCenterDetailsName.setValue(centerDto.getName());
					centerName.add(regCenterDetailsName);
					KeyValuePairDto<String, String> regCenterDetailsAddress = new KeyValuePairDto<>();
					regCenterDetailsAddress.setKey((String) key.getKey());
					StringBuilder sb = new StringBuilder(centerDto.getAddressLine1());
					sb.append(" ").append(centerDto.getAddressLine2()).append(" ").append(centerDto.getAddressLine3());
					regCenterDetailsAddress.setValue(sb.toString());
					address.add(regCenterDetailsAddress);
				}

//				for (KeyValuePairDto<String, String> key : notificationDto.getFullName()) {
//					RegistrationCenterDto centerDto = getNotificationCenterAddressDTO(registrationCenterId,
//							(String) key.getKey());
//					System.out.println("NotificationCenterDTO: " + centerDto);
//					if (centerDto != null) {
//						KeyValuePairDto<String, String> regCenterDetailsName = new KeyValuePairDto<>();
//						regCenterDetailsName.setKey((String) key.getKey());
//						regCenterDetailsName.setValue(centerDto.getName());
//						centerName.add(regCenterDetailsName);
//						KeyValuePairDto<String, String> regCenterDetailsAddress = new KeyValuePairDto<>();
//						regCenterDetailsAddress.setKey((String) key.getKey());
//						StringBuilder sb = new StringBuilder(centerDto.getAddressLine1());
//						sb.append(" ").append(centerDto.getAddressLine2()).append(" ").append(centerDto.getAddressLine3());
//						regCenterDetailsAddress.setValue(sb.toString());
//						address.add(regCenterDetailsAddress);
//					}
//				}

			}
			notificationDto.setRegistrationCenterName(centerName);
			notificationDto.setAddress(address);
		}
		// System.out.println("NotificationDto: " + notificationDto);
		return notificationDto;
	}

	public ResponseWrapper<RegistrationCenterResponseDto> getRegistrationCenter(String registrationCenterId,
			String langCode) {
		ResponseWrapper<RegistrationCenterResponseDto> response = new ResponseWrapper<>();
		ResponseEntity<ResponseWrapper<RegistrationCenterResponseDto>> responseEntity = null;
//		String url = getAppointmentResourseUrl + "/appointment/" + preRegId;
//		String url = "https://dev.mosip.net/v1/masterdata/registrationcenters/10001/eng"
		String url = centerDetailUri + "/" + registrationCenterId + "/" + langCode;
		try {
			log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "In Registration method of RegistrationCenterController" + url);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<?> entity = new HttpEntity<>(headers);
			log.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, entity.toString());
			responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity,
					new ParameterizedTypeReference<ResponseWrapper<RegistrationCenterResponseDto>>() {
					});
			log.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, responseEntity.toString());
			ResponseWrapper<RegistrationCenterResponseDto> body = responseEntity.getBody();
			if (body != null) {
				if (body.getErrors() != null && !body.getErrors().isEmpty()) {
					log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, body.getErrors().toString());
					response.setErrors(body.getErrors());
				} else {
					response.setResponse(body.getResponse());
				}
				
			}
			log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "In call to registrationcenter rest service :" + url);
		} catch (Exception ex) {
			log.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "Registration Center Details" + ExceptionUtils.getStackTrace(ex));
			throw new RestClientException("Rest call failed");
		}
		return response;
	}

	public RegistrationCenterDto getNotificationCenterAddressDTO(String registrationCenterId, String langCode) {
		RegistrationCenterDto centerDto = null;
		ResponseWrapper<RegistrationCenterResponseDto> getRegistrationCenter = getRegistrationCenter(
				registrationCenterId, langCode);
		RegistrationCenterResponseDto registrationCenterResponseDto = getRegistrationCenter.getResponse();
		if (registrationCenterResponseDto != null) {
			if (registrationCenterResponseDto.getRegistrationCenters() != null
					&& !registrationCenterResponseDto.getRegistrationCenters().isEmpty()) {
				centerDto = registrationCenterResponseDto.getRegistrationCenters().get(0);
			}
		}
		return centerDto;
	}
}