package io.mosip.preregistration.application.service.util;

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
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
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
import io.mosip.preregistration.core.common.dto.KeyValuePairDto;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.NotificationDTO;
import io.mosip.preregistration.core.common.dto.SMSRequestDTO;
import io.mosip.preregistration.core.config.LoggerConfiguration;

/**
 * The util class.
 * 
 * @author Sanober Noor
 * @since 1.0.0
 *
 */
@Component
public class NotificationServiceUtil {

	@Value("${mosip.utc-datetime-pattern}")
	private String utcDateTimePattern;

	private Logger log = LoggerConfiguration.logConfig(NotificationServiceUtil.class);

	@Autowired
	private Environment environment;

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

	/**
	 * Autowired reference for {@link #RestTemplateBuilder}
	 */

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
		log.info("sessionId", "idType", "id",
				"In createUploadDto method of notification service util with body " + jsonString);
		MainRequestDTO<NotificationDTO> notificationReqDto = new MainRequestDTO<>();
		JSONObject notificationData = new JSONObject(jsonString);
		JSONObject notificationDtoData = (JSONObject) notificationData.get("request");
		NotificationDTO notificationDto = null;
		List<KeyValuePairDto> langaueNamePairs = new ArrayList<KeyValuePairDto>();
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
			notificationDto.setFullName(langaueNamePairs);
		}
		if (!isLatest) {
			notificationDto = (NotificationDTO) JsonUtils.jsonStringToJavaObject(NotificationDTO.class,
					notificationDtoData.toString());
			KeyValuePairDto langaueNamePair = new KeyValuePairDto();
			langaueNamePair.setKey(langauageCode);
			langaueNamePair.setValue(notificationDto.getName());
			langaueNamePairs.add(langaueNamePair);
			notificationDto.setFullName(langaueNamePairs);
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

	public void invokeSmsNotification(Map values, String userId, String token, MainRequestDTO<OtpRequestDTO> requestDTO,
			String langCode) throws PreRegLoginException, IOException {
		log.info("sessionId", "idType", "id", "In invokeSmsNotification method of notification service util");
		String otpSmsTemplate = environment.getProperty(PreRegLoginConstant.OTP_SMS_TEMPLATE);
		String smsTemplate = applyTemplate(values, otpSmsTemplate, token, langCode);
		sendSmsNotification(userId, smsTemplate, token, requestDTO);
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
	public void invokeEmailNotification(Map values, String userId, String token,
			MainRequestDTO<OtpRequestDTO> requestDTO, String langCode) throws PreRegLoginException, IOException {
		log.info("sessionId", "idType", "id", "In invokeEmailNotification method of notification service util");
		String otpContentTemaplate = environment.getProperty(PreRegLoginConstant.OTP_CONTENT_TEMPLATE);
		String otpSubjectTemplate = environment.getProperty(PreRegLoginConstant.OTP_SUBJECT_TEMPLATE);
		String mailSubject = applyTemplate(values, otpSubjectTemplate, token, langCode);
		String mailContent = applyTemplate(values, otpContentTemaplate, token, langCode);
		sendEmailNotification(userId, mailSubject, mailContent, token, requestDTO);
	}

	/**
	 * Send sms notification.
	 *
	 * @param notificationMobileNo the notification mobile no
	 * @param message              the message
	 * @throws PreRegLoginException
	 */
	public void sendSmsNotification(String notificationMobileNo, String message, String token,
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
			headers.set("Cookie", token.substring(0, token.indexOf(";")));

			HttpEntity<PreRegSmsRequestDto> entity1 = new HttpEntity<PreRegSmsRequestDto>(preRegSmsRequestDto, headers);

			PreRegSmsResponseDto response = restTemplate.exchange(environment.getProperty("sms-notification.rest.uri"),
					HttpMethod.POST, entity1, PreRegSmsResponseDto.class).getBody();
			if (!response.getResponse().getStatus().equalsIgnoreCase(PreRegLoginConstant.SUCCESS))
				throw new PreRegLoginException(PreRegLoginErrorConstants.DATA_VALIDATION_FAILED.getErrorCode(),
						PreRegLoginErrorConstants.DATA_VALIDATION_FAILED.getErrorMessage());

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
	public void sendEmailNotification(String emailId, String mailSubject, String mailContent, String token,
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

			headers1.set("Cookie", token.substring(0, token.indexOf(";")));
			HttpEntity<LinkedMultiValueMap<String, Object>> entity1 = new HttpEntity<LinkedMultiValueMap<String, Object>>(
					map, headers1);

			PreRegSmsResponseDto response = restTemplate.exchange(environment.getProperty("mail-notification.rest.uri"),
					HttpMethod.POST, entity1, PreRegSmsResponseDto.class).getBody();

			if (!response.getResponse().getStatus().equalsIgnoreCase(PreRegLoginConstant.SUCCESS))
				throw new PreRegLoginException(PreRegLoginErrorConstants.DATA_VALIDATION_FAILED.getErrorCode(),
						PreRegLoginErrorConstants.DATA_VALIDATION_FAILED.getErrorMessage());

		} catch (PreRegLoginException e) {

			log.error(PreRegLoginConstant.SESSION_ID, "Inside Mail Notification >>>>>", e.getErrorCode(),
					e.getErrorText());
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

	public String applyTemplate(Map mp, String templateName, String token, String langCode)

			throws PreRegLoginException, IOException {
		Objects.requireNonNull(templateName);
		Objects.requireNonNull(mp);
		StringWriter writer = new StringWriter();
		InputStream templateValue;
		String fetchedTemplate = fetchTemplate(templateName, token, langCode);
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
	public String fetchTemplate(String templateName, String token, String langCode) throws PreRegLoginException {

		Map<String, String> params = new HashMap<>();
		params.put(LANG_CODE, langCode);
		params.put(TEMPLATE_TYPE_CODE, templateName);

		HttpHeaders headers1 = new HttpHeaders();

		headers1.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

		headers1.set("Cookie", token.substring(0, token.indexOf(";")));
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

}
