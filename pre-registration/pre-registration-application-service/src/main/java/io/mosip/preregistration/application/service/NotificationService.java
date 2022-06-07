package io.mosip.preregistration.application.service;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

import org.json.JSONException;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.application.code.NotificationRequestCodes;
import io.mosip.preregistration.application.dto.NotificationResponseDTO;
import io.mosip.preregistration.application.errorcodes.NotificationErrorCodes;
import io.mosip.preregistration.application.errorcodes.NotificationErrorMessages;
import io.mosip.preregistration.application.exception.BookingDetailsNotFoundException;
import io.mosip.preregistration.application.exception.DemographicDetailsNotFoundException;
import io.mosip.preregistration.application.exception.MandatoryFieldException;
import io.mosip.preregistration.application.exception.RestCallException;
import io.mosip.preregistration.application.exception.util.NotificationExceptionCatcher;
import io.mosip.preregistration.application.service.util.NotificationServiceUtil;
import io.mosip.preregistration.core.code.AuditLogVariables;
import io.mosip.preregistration.core.code.BookingTypeCodes;
import io.mosip.preregistration.core.code.EventId;
import io.mosip.preregistration.core.code.EventName;
import io.mosip.preregistration.core.code.EventType;
import io.mosip.preregistration.core.common.dto.AuditRequestDto;
import io.mosip.preregistration.core.common.dto.BookingRegistrationDTO;
import io.mosip.preregistration.core.common.dto.DemographicResponseDTO;
import io.mosip.preregistration.core.common.dto.KeyValuePairDto;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.dto.NotificationDTO;
import io.mosip.preregistration.core.common.entity.ApplicationEntity;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.util.AuditLogUtil;
import io.mosip.preregistration.core.util.NotificationUtil;
import io.mosip.preregistration.core.util.ValidationUtil;

/**
 * The service class contans all the method for notification.
 * 
 * @author Sanober Noor
 * @author Tapaswini Behera
 * @since 1.0.0
 *
 */
@Service
public class NotificationService {

	/**
	 * The reference to {@link NotificationUtil}.
	 */
	@Autowired
	private NotificationUtil notificationUtil;

	/**
	 * The reference to {@link NotificationServiceUtil}.
	 */
	@Autowired
	private NotificationServiceUtil serviceUtil;

	@Autowired
	private DemographicServiceIntf demographicServiceIntf;
	
	@Autowired
	private ApplicationServiceIntf applicationServiceIntf;
	
	/**
	 * Reference for ${appointmentResourse.url} from property file
	 */
	@Value("${appointmentResourse.url}")
	private String appointmentResourseUrl;

	private Logger log = LoggerConfiguration.logConfig(NotificationService.class);

	Map<String, String> requiredRequestMap = new HashMap<>();

	@Value("${mosip.pre-registration.notification.id}")
	private String Id;

	@Value("${version}")
	private String version;

	/**
	 * 
	 */
	@Value("${demographic.resource.url}")
	private String demographicResourceUrl;
	/**
	 * 
	 */
	@Value("${preregistration.response}")
	private String demographicResponse;

	@Value("${preregistration.demographicDetails}")
	private String demographicDetails;

	@Value("${preregistration.identity}")
	private String identity;

	@Value("${preregistration.identity.email}")
	private String email;

	@Value("${preregistration.identity.name}")
	private String fullName;

	@Value("${preregistration.identity.phone}")
	private String phone;

	@Value("${preregistration.notification.nameFormat}")
	private String nameFormat;

	@Value("#{'${mosip.notificationtype}'.split('\\|')}")
	private List<String> notificationTypeList;

	MainResponseDTO<NotificationResponseDTO> response;

	/**
	 * Autowired reference for {@link #AuditLogUtil}
	 */
	@Autowired
	private AuditLogUtil auditLogUtil;

	@Autowired
	private ValidationUtil validationUtil;

	@PostConstruct
	public void setupBookingService() {
		requiredRequestMap.put("version", version);
	}

	public AuthUserDetails authUserDetails() {
		return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	/**
	 * Method to send notification.
	 * 
	 * @param jsonString the json string.
	 * @param langCode   the language code.
	 * @param file       the file to send.
	 * @return the response dto.
	 */
	public MainResponseDTO<NotificationResponseDTO> sendNotification(String jsonString, String langCode,
			MultipartFile file, boolean isLatest) {

		response = new MainResponseDTO<>();

		NotificationResponseDTO notificationResponse = new NotificationResponseDTO();
		log.info("sessionId", "idType", "id", "In notification service of sendNotification with request  " + jsonString
				+ " and langCode " + langCode);
		requiredRequestMap.put("id", Id);
		response.setId(Id);
		response.setVersion(version);
		String resp = null;
		boolean isSuccess = false;
		try {
			MainRequestDTO<NotificationDTO> notificationReqDTO = serviceUtil.createNotificationDetails(jsonString,
					langCode, isLatest);
			response.setId(notificationReqDTO.getId());
			response.setVersion(notificationReqDTO.getVersion());
			NotificationDTO notificationDto = notificationReqDTO.getRequest();
			if (validationUtil.requestValidator(validationUtil.prepareRequestMap(notificationReqDTO),
					requiredRequestMap)) {
				MainResponseDTO<ApplicationEntity> appEntity = applicationServiceIntf.getApplicationInfo(notificationDto.getPreRegistrationId());
				String bookingType = appEntity.getResponse().getBookingType();
				MainResponseDTO<DemographicResponseDTO> demoDetail = notificationDtoValidation(bookingType, notificationDto);
				if (notificationDto.isAdditionalRecipient()) {
					log.info("sessionId", "idType", "id",
							"In notification service of sendNotification if additionalRecipient is"
									+ notificationDto.isAdditionalRecipient());
					if (notificationDto.getMobNum() != null && !notificationDto.getMobNum().isEmpty()) {
						if (validationUtil.phoneValidator(notificationDto.getMobNum())) {
							notificationUtil.notify(NotificationRequestCodes.SMS.getCode(), notificationDto, file);
						} else {
							throw new MandatoryFieldException(NotificationErrorCodes.PRG_PAM_ACK_007.getCode(),
									NotificationErrorMessages.PHONE_VALIDATION_EXCEPTION.getMessage(), response);
						}
					}
					if (notificationDto.getEmailID() != null && !notificationDto.getEmailID().isEmpty()) {
						if (validationUtil.emailValidator(notificationDto.getEmailID())) {
							notificationUtil.notify(NotificationRequestCodes.EMAIL.getCode(), notificationDto, file);
						} else {
							throw new MandatoryFieldException(NotificationErrorCodes.PRG_PAM_ACK_006.getCode(),
									NotificationErrorMessages.EMAIL_VALIDATION_EXCEPTION.getMessage(), response);

						}
					}
					if ((notificationDto.getEmailID() == null || notificationDto.getEmailID().isEmpty()
							|| !validationUtil.emailValidator(notificationDto.getEmailID()))
							&& (notificationDto.getMobNum() == null || notificationDto.getMobNum().isEmpty()
									|| !validationUtil.phoneValidator(notificationDto.getMobNum()))) {
						throw new MandatoryFieldException(NotificationErrorCodes.PRG_PAM_ACK_001.getCode(),
								NotificationErrorMessages.MOBILE_NUMBER_OR_EMAIL_ADDRESS_NOT_FILLED.getMessage(),
								response);
					}
					notificationResponse.setMessage(NotificationRequestCodes.MESSAGE.getCode());
				} else {
					log.info("sessionId", "idType", "id",
							"In notification service of sendNotification if additionalRecipient is"
									+ notificationDto.isAdditionalRecipient());
					resp = getDemographicDetailsWithPreId(bookingType, demoDetail, appEntity, notificationDto, langCode, file);
					notificationResponse.setMessage(resp);
				}
			}

			response.setResponse(notificationResponse);
			isSuccess = true;
		} catch (RuntimeException | IOException | ParseException
				| io.mosip.kernel.core.util.exception.JsonParseException
				| io.mosip.kernel.core.util.exception.JsonMappingException | io.mosip.kernel.core.exception.IOException
				| JSONException | java.text.ParseException ex) {
			log.error("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id", "In notification service of sendNotification " + ex.getMessage());
			new NotificationExceptionCatcher().handle(ex, response);
		} finally {
			response.setResponsetime(validationUtil.getCurrentResponseTime());
			if (isSuccess) {
				setAuditValues(EventId.PRE_411.toString(), EventName.NOTIFICATION.toString(),
						EventType.SYSTEM.toString(),
						"Pre-Registration data is sucessfully trigger notification to the user",
						AuditLogVariables.NO_ID.toString(), authUserDetails().getUserId(),
						authUserDetails().getUsername());
			} else {
				setAuditValues(EventId.PRE_405.toString(), EventName.EXCEPTION.toString(), EventType.SYSTEM.toString(),
						"Failed to trigger notification to the user", AuditLogVariables.NO_ID.toString(),
						authUserDetails().getUserId(), authUserDetails().getUsername());
			}
		}
		return response;
	}

	/**
	 * This method is calling demographic getApplication service to get the user
	 * emailId and mobile number
	 * 
	 * @param notificationDto
	 * @param langCode
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private String getDemographicDetailsWithPreId(String bookingType, MainResponseDTO<DemographicResponseDTO> responseEntity,
			MainResponseDTO<ApplicationEntity> appEntity, NotificationDTO notificationDto, String langCode, MultipartFile file) throws IOException {
		try {
			List<KeyValuePairDto<String,String>> langaueNamePairs = new ArrayList<KeyValuePairDto<String,String>>();
			if (BookingTypeCodes.NEW_PREREGISTRATION.toString().equals(bookingType)) {
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper = JsonMapper.builder().addModule(new AfterburnerModule()).build();
				objectMapper.registerModule(new JavaTimeModule());

				JsonNode responseNode = objectMapper
						.readTree(responseEntity.getResponse().getDemographicDetails().toJSONString());
	
				responseNode = responseNode.get(identity);
				
				JsonNode arrayNode = responseNode.get(fullName);
				KeyValuePairDto langaueNamePair = null;
				if (arrayNode.isArray()) {
					for (JsonNode jsonNode : arrayNode) {
						langaueNamePair = new KeyValuePairDto();
						langaueNamePair.setKey(jsonNode.get("language").asText().trim());
						langaueNamePair.setValue(jsonNode.get("value").asText().trim());
						langaueNamePairs.add(langaueNamePair);
					}
				}			
				notificationDto.setFullName(langaueNamePairs);
				if (responseNode.get(email) != null) {
					String emailId = responseNode.get(email).asText();
					notificationDto.setEmailID(emailId);
					notificationUtil.notify(NotificationRequestCodes.EMAIL.getCode(), notificationDto, file);
				}
				if (responseNode.get(phone) != null) {
					String phoneNumber = responseNode.get(phone).asText();
					notificationDto.setMobNum(phoneNumber);
					notificationUtil.notify(NotificationRequestCodes.SMS.getCode(), notificationDto, file);
	
				}
				if (responseNode.get(email) == null && responseNode.get(phone) == null) {
					log.info("sessionId", "idType", "id",
							"In notification service of sendNotification failed to send Email and sms request ");
				}
			} else {
				//handle notifications for other booking types like LOST_FORGOTTEN_UIN, MISCELLANEOUS_PURPOSE etc
				if (notificationDto.getName() == null) {
					//in case name is null, set applicationId as name
					String applicationId = appEntity.getResponse().getApplicationId();
					notificationDto.setName(applicationId);
				}
				KeyValuePairDto langaueNamePair = null;
				langaueNamePair = new KeyValuePairDto();
				langaueNamePair.setKey(notificationDto.getLanguageCode());
				langaueNamePair.setValue(notificationDto.getName());
				langaueNamePairs.add(langaueNamePair);
				notificationDto.setFullName(langaueNamePairs);
				if (notificationDto.getEmailID() != null) {
					notificationUtil.notify(NotificationRequestCodes.EMAIL.getCode(), notificationDto, file);
				}
				if (notificationDto.getMobNum() != null) {
					notificationUtil.notify(NotificationRequestCodes.SMS.getCode(), notificationDto, file);
				}
				if (notificationDto.getEmailID() == null && notificationDto.getMobNum() == null ) {
					//in case both email id and mob num are null, send details to contact info 
					String createdById = appEntity.getResponse().getContactInfo();
					if (createdById != null) {
						if (validationUtil.emailValidator(createdById)) {
							notificationDto.setEmailID(createdById);
							notificationUtil.notify(NotificationRequestCodes.EMAIL.getCode(), notificationDto, file);
						}
						else if (validationUtil.phoneValidator(createdById)) {
							notificationDto.setMobNum(createdById);
							notificationUtil.notify(NotificationRequestCodes.SMS.getCode(), notificationDto, file);
						} else {
							log.info("sessionId", "idType", "id",
									"In notification service of sendNotification failed to send Email and sms request ");	
						}
					} else {
						log.info("sessionId", "idType", "id",
								"In notification service of sendNotification failed to send Email and sms request ");	
					}
				}
			}
			return NotificationRequestCodes.MESSAGE.getCode();
		} catch (RestClientException ex) {
			log.error("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id",
					"In getDemographicDetailsWithPreId method of notification service - " + ex.getMessage());
			throw new RestCallException(NotificationErrorCodes.PRG_PAM_ACK_011.getCode(),
					NotificationErrorMessages.DEMOGRAPHIC_CALL_FAILED.getMessage());
		}
	}

	/**
	 * This method is used to audit all the trigger notification events
	 * 
	 * @param eventId
	 * @param eventName
	 * @param eventType
	 * @param description
	 * @param idType
	 */
	public void setAuditValues(String eventId, String eventName, String eventType, String description, String idType,
			String userId, String userName) {
		AuditRequestDto auditRequestDto = new AuditRequestDto();
		auditRequestDto.setEventId(eventId);
		auditRequestDto.setEventName(eventName);
		auditRequestDto.setEventType(eventType);
		auditRequestDto.setDescription(description);
		auditRequestDto.setSessionUserId(userId);
		auditRequestDto.setSessionUserName(userName);
		auditRequestDto.setId(idType);
		auditRequestDto.setModuleId(AuditLogVariables.NOTIFY.toString());
		auditRequestDto.setModuleName(AuditLogVariables.NOTIFICATION_SERVICE.toString());
		auditLogUtil.saveAuditDetails(auditRequestDto);
	}

	public MainResponseDTO<DemographicResponseDTO> notificationDtoValidation(String bookingType, NotificationDTO dto)
			throws IOException, ParseException {
		MainResponseDTO<DemographicResponseDTO> demoDetail = null;
		if (BookingTypeCodes.NEW_PREREGISTRATION.toString().equals(bookingType)) {
			demoDetail = getDemographicDetails(dto);
		} 
		if (!dto.getIsBatch()) {
			BookingRegistrationDTO bookingDTO = getAppointmentDetailsRestService(dto.getPreRegistrationId());
			String registrationCenterId = bookingDTO.getRegistrationCenterId();
			String time = LocalTime.parse(bookingDTO.getSlotFromTime(), DateTimeFormatter.ofPattern("HH:mm"))
					.format(DateTimeFormatter.ofPattern("hh:mm a"));
			log.info("sessionId", "idType", "id", "In notificationDtoValidation with bookingDTO " + bookingDTO);
			if (dto.getAppointmentDate() != null && !dto.getAppointmentDate().trim().equals("")) {
				if (bookingDTO.getRegDate().equals(dto.getAppointmentDate())) {
					if (dto.getAppointmentTime() != null && !dto.getAppointmentTime().trim().equals("")) {

						if (!time.equals(dto.getAppointmentTime())) {
							throw new MandatoryFieldException(NotificationErrorCodes.PRG_PAM_ACK_010.getCode(),
									NotificationErrorMessages.APPOINTMENT_TIME_NOT_CORRECT.getMessage(), response);
						}
					} else {
						throw new MandatoryFieldException(NotificationErrorCodes.PRG_PAM_ACK_002.getCode(),
								NotificationErrorMessages.INCORRECT_MANDATORY_FIELDS.getMessage(), response);
					}
				}

				else {
					throw new MandatoryFieldException(NotificationErrorCodes.PRG_PAM_ACK_009.getCode(),
							NotificationErrorMessages.APPOINTMENT_DATE_NOT_CORRECT.getMessage(), response);
				}

			}

			else {
				throw new MandatoryFieldException(NotificationErrorCodes.PRG_PAM_ACK_002.getCode(),
						NotificationErrorMessages.INCORRECT_MANDATORY_FIELDS.getMessage(), response);
			}
			dto = serviceUtil.modifyCenterNameAndAddress(dto, registrationCenterId, dto.getLanguageCode().split(",")[0]);
		}
		return demoDetail;
	}

	/**
	 * This method is calling demographic getApplication service to validate the
	 * demographic details
	 * 
	 * @param notificationDto
	 * @return DemographicResponseDTO
	 * @throws ParseException
	 */

	public MainResponseDTO<DemographicResponseDTO> getDemographicDetails(NotificationDTO notificationDto)
			throws IOException, ParseException {
		MainResponseDTO<DemographicResponseDTO> responseEntity = demographicServiceIntf
				.getDemographicData(notificationDto.getPreRegistrationId());
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper = JsonMapper.builder().addModule(new AfterburnerModule()).build();
		objectMapper.registerModule(new JavaTimeModule());
	
		if (responseEntity.getErrors() != null) {
			throw new DemographicDetailsNotFoundException(responseEntity.getErrors(), response);
		}
		JsonNode responseNode = objectMapper.readTree(responseEntity.getResponse().getDemographicDetails().toJSONString());
		responseNode = responseNode.get(identity);
		if (!notificationDto.isAdditionalRecipient()) {
			if (notificationDto.getMobNum() != null || notificationDto.getEmailID() != null) {
				log.error("sessionId", "idType", "id",
						"Not considering the requested mobilenumber/email since additional recipient is false ");
			}
		}
		boolean isNameMatchFound = false;
		if (!notificationDto.getIsBatch()) {
			if (nameFormat != null) {
				String[] nameKeys = nameFormat.split(",");
				for (int i = 0; i < nameKeys.length; i++) {
					JsonNode arrayNode = responseNode.get(nameKeys[i]);
					for (JsonNode jsonNode : arrayNode) {
						if (notificationDto.getName().trim().equals(jsonNode.get("value").asText().trim())) {
							isNameMatchFound = true;
							break;
						}
					}
				}

			}
			if (!isNameMatchFound) {
				throw new MandatoryFieldException(NotificationErrorCodes.PRG_PAM_ACK_008.getCode(),
						NotificationErrorMessages.FULL_NAME_VALIDATION_EXCEPTION.getMessage(), response);
			}
		}
		return responseEntity;
	}

	/**
	 * This Method is used to retrieve booking data
	 * 
	 * @param preId
	 * @return BookingRegistrationDTO
	 * 
	 */
	public BookingRegistrationDTO getAppointmentDetailsRestService(String preId) {
		log.info("sessionId", "idType", "id", "In getAppointmentDetailsRestService method of notification service ");

		BookingRegistrationDTO bookingRegistrationDTO = null;
		MainResponseDTO<BookingRegistrationDTO> respEntity = notificationUtil.getAppointmentDetails(preId);
		if (respEntity.getErrors() != null) {
			throw new BookingDetailsNotFoundException(respEntity.getErrors(), response);
		}
		bookingRegistrationDTO = respEntity.getResponse();
		return bookingRegistrationDTO;
	}
}
