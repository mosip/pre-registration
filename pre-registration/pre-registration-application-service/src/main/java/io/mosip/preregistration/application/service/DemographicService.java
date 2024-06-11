package io.mosip.preregistration.application.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.idobjectvalidator.spi.IdObjectValidator;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.JsonUtils;
import io.mosip.kernel.core.util.exception.JsonProcessingException;
import io.mosip.preregistration.application.code.DemographicRequestCodes;
import io.mosip.preregistration.application.dto.ApplicantTypeRequestDTO;
import io.mosip.preregistration.application.dto.ApplicantValidDocumentDto;
import io.mosip.preregistration.application.dto.ApplicationInfoMetadataDTO;
import io.mosip.preregistration.application.dto.DeletePreRegistartionDTO;
import io.mosip.preregistration.application.dto.DemographicCreateResponseDTO;
import io.mosip.preregistration.application.dto.DemographicMetadataDTO;
import io.mosip.preregistration.application.dto.DemographicRequestDTO;
import io.mosip.preregistration.application.dto.DemographicUpdateResponseDTO;
import io.mosip.preregistration.application.dto.DemographicViewDTO;
import io.mosip.preregistration.application.dto.IdSchemaDto;
import io.mosip.preregistration.application.dto.SchemaResponseDto;
import io.mosip.preregistration.application.errorcodes.ApplicationErrorCodes;
import io.mosip.preregistration.application.errorcodes.ApplicationErrorMessages;
import io.mosip.preregistration.application.errorcodes.DemographicErrorCodes;
import io.mosip.preregistration.application.errorcodes.DemographicErrorMessages;
import io.mosip.preregistration.application.exception.BookingDeletionFailedException;
import io.mosip.preregistration.application.exception.DemographicServiceException;
import io.mosip.preregistration.application.exception.DocumentNotFoundException;
import io.mosip.preregistration.application.exception.RecordFailedToUpdateException;
import io.mosip.preregistration.application.exception.RecordNotFoundException;
import io.mosip.preregistration.application.exception.RecordNotFoundForPreIdsException;
import io.mosip.preregistration.application.exception.util.DemographicExceptionCatcher;
import io.mosip.preregistration.application.repository.DemographicRepository;
import io.mosip.preregistration.application.service.util.CommonServiceUtil;
import io.mosip.preregistration.application.service.util.DemographicServiceUtil;
import io.mosip.preregistration.core.code.AuditLogVariables;
import io.mosip.preregistration.core.code.BookingTypeCodes;
import io.mosip.preregistration.core.code.EventId;
import io.mosip.preregistration.core.code.EventName;
import io.mosip.preregistration.core.code.EventType;
import io.mosip.preregistration.core.code.StatusCodes;
import io.mosip.preregistration.core.common.dto.AuditRequestDto;
import io.mosip.preregistration.core.common.dto.BookingRegistrationDTO;
import io.mosip.preregistration.core.common.dto.DeleteBookingDTO;
import io.mosip.preregistration.core.common.dto.DemographicResponseDTO;
import io.mosip.preregistration.core.common.dto.DocumentMultipartResponseDTO;
import io.mosip.preregistration.core.common.dto.DocumentsMetaData;
import io.mosip.preregistration.core.common.dto.ExceptionJSONInfoDTO;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.dto.PreRegIdsByRegCenterIdDTO;
import io.mosip.preregistration.core.common.dto.PreRegistartionStatusDTO;
import io.mosip.preregistration.core.common.dto.identity.DemographicIdentityRequestDTO;
import io.mosip.preregistration.core.common.entity.ApplicationEntity;
import io.mosip.preregistration.core.common.entity.DemographicEntity;
import io.mosip.preregistration.core.common.entity.DocumentEntity;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.exception.EncryptionFailedException;
import io.mosip.preregistration.core.exception.HashingException;
import io.mosip.preregistration.core.exception.InvalidPreRegistrationIdException;
import io.mosip.preregistration.core.exception.InvalidRequestParameterException;
import io.mosip.preregistration.core.exception.PreIdInvalidForUserIdException;
import io.mosip.preregistration.core.exception.PreRegistrationException;
import io.mosip.preregistration.core.exception.RecordFailedToDeleteException;
import io.mosip.preregistration.core.util.AuditLogUtil;
import io.mosip.preregistration.core.util.CryptoUtil;
import io.mosip.preregistration.core.util.HashUtill;
import io.mosip.preregistration.core.util.ValidationUtil;
import io.mosip.preregistration.demographic.exception.system.SystemFileIOException;

/**
 * This class provides the service implementation for Demographic
 * 
 * @author Rajath KR
 * @author Sanober Noor
 * @author Tapaswini Behera
 * @author Jagadishwari S
 * @author Ravi C Balaji
 * @since 1.0.0
 */

@Service
public class DemographicService implements DemographicServiceIntf {

	/**
	 * logger instance
	 */
	private Logger log = LoggerConfiguration.logConfig(DemographicService.class);

	/**
	 * Autowired reference for {@link #RegistrationRepositary}
	 */
	@Autowired
	private DemographicRepository demographicRepository;
	
	/**
	 * Autowired reference for {@link #DemographicServiceUtil}
	 */
	@Autowired
	private DemographicServiceUtil serviceUtil;

	/**
	 * Autowired reference for {@link #JsonValidatorImpl}
	 */
	@Autowired
	private IdObjectValidator jsonValidator;

	@Autowired
	private DocumentServiceIntf documentServiceImpl;

	@Autowired
	private CommonServiceUtil commonServiceUtil;
	
	/**
	 * Autowired reference for {@link #AuditLogUtil}
	 */
	@Autowired
	AuditLogUtil auditLogUtil;

	@Autowired
	ValidationUtil validationUtil;

	/**
	 * Reference for ${document.resource.url} from property file
	 */
	@Value("${document.resource.url}")
	private String docResourceUrl;

	/**
	 * Reference for ${createId} from property file
	 */
	@Value("${mosip.preregistration.demographic.id.create}")
	private String createId;

	/**
	 * Reference for ${updateId} from property file
	 */
	@Value("${mosip.preregistration.demographic.id.update}")
	private String updateId;

	/**
	 * Reference for ${retrieveId} from property file
	 */
	@Value("${mosip.preregistration.demographic.retrieve.basic.id}")
	private String retrieveId;
	/**
	 * Reference for ${retrieveDetailsId} from property file
	 */
	@Value("${mosip.preregistration.demographic.retrieve.details.id}")
	private String retrieveDetailsId;

	/**
	 * Reference for ${retrieveStatusId} from property file
	 */
	@Value("${mosip.preregistration.demographic.retrieve.status.id}")
	private String retrieveStatusId;

	/**
	 * Reference for ${deleteId} from property file
	 */
	@Value("${mosip.preregistration.demographic.delete.id}")
	private String deleteId;

	/**
	 * Reference for ${updateStatusId} from property file
	 */
	@Value("${mosip.preregistration.demographic.update.status.id}")
	private String updateStatusId;

	/**
	 * Reference for ${dateId} from property file
	 */
	@Value("${mosip.preregistration.demographic.id.retrieve.date}")
	private String dateId;

	/**
	 * Reference for ${ver} from property file
	 */
	@Value("${version}")
	private String version;

	Map<String, String> idValidationFields = new HashMap<>();
	/**
	 * Reference for ${appointmentResourse.url} from property file
	 */
	@Value("${appointmentResourse.url}")
	private String appointmentResourseUrl;

	@Value("${booking.resource.url}")
	private String deleteAppointmentResourseUrl;

	@Value("${mosip.pregistration.pagesize}")
	private String pageSize;

	/**
	 * Reference for ${mosip.utc-datetime-pattern} from property file
	 */
	@Value("${mosip.utc-datetime-pattern}")
	private String dateFormat;

	@Value("${preregistration.config.identityjson}")
	private String preregistrationIdJson;
	/**
	 * Response status
	 */
	protected String trueStatus = "true";

	private String getIdentityJsonString = "";

	@Autowired
	private ObjectMapper objectMapper;

	private static final String INDENTITY = "identity";

	private static final String PROPERTIES = "properties";

	@Autowired
	CryptoUtil cryptoUtil;
	
	@Value("${mosip.utc-datetime-pattern}")
	private String mosipDateTimeFormat;


	/**
	 * This method acts as a post constructor to initialize the required request
	 * parameters.
	 */

	public void setup() {
		getIdentityJsonString = serviceUtil.getJson(preregistrationIdJson);
		log.info("Fetched the identity json from config server" + getIdentityJsonString);
		objectMapper = JsonMapper.builder().addModule(new AfterburnerModule()).build();
		objectMapper.registerModule(new JavaTimeModule());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.preregistration.demographic.service.DemographicServiceIntf#
	 * authUserDetails()
	 */
	@Override
	public AuthUserDetails authUserDetails() {
		return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	/*
	 * This method is used to create the demographic data by generating the unique
	 * PreId
	 * 
	 * @see
	 * io.mosip.registration.service.RegistrationService#addPreRegistration(java.
	 * lang.Object, java.lang.String)
	 * 
	 * @param demographicRequest pass demographic request
	 * 
	 * @return responseDTO
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.preregistration.demographic.service.DemographicServiceIntf#
	 * addPreRegistration(io.mosip.preregistration.core.common.dto.MainRequestDTO)
	 */
	@SuppressWarnings({ "unchecked" })
	@Override
	public MainResponseDTO<DemographicCreateResponseDTO> addPreRegistration(
			MainRequestDTO<DemographicRequestDTO> request) {
		log.info("sessionId", "idType", "id", "In addPreRegistration method of pre-registration service ");
		log.info("sessionId", "idType", "id",
				"Pre Registration start time : " + DateUtils.getUTCCurrentDateTimeString());
		MainResponseDTO<DemographicCreateResponseDTO> mainResponseDTO = null;
		boolean isSuccess = false;
		try {
			log.info("sessionId", "idType", "id", "Get Schema from syncdata called");
			IdSchemaDto idSchema = serviceUtil.getSchema();

			log.info("sessionId", "idType", "id", "Get Schema from syncdata successful");

			mainResponseDTO = (MainResponseDTO<DemographicCreateResponseDTO>) serviceUtil.getMainResponseDto(request);
			DemographicRequestDTO demographicRequest = request.getRequest();
			validationUtil.langvalidation(demographicRequest.getLangCode());
			log.info("sessionId", "idType", "id",
					"JSON validator start time : " + DateUtils.getUTCCurrentDateTimeString());

			List<String> identityKeys = convertSchemaJsonToArray(idSchema.getSchemaJson());

			log.info("IDENTITY KEYS: {}", identityKeys);

			List<String> requiredFields = demographicRequest.getRequiredFields().stream()
					.filter(field -> identityKeys.contains(field)).collect(Collectors.toList());

			JSONObject constructedObject = serviceUtil.constructNewDemographicRequest(identityKeys,
					demographicRequest.getDemographicDetails());

			log.info("Constructed Object {}", constructedObject);

			jsonValidator.validateIdObject(idSchema.getSchemaJson(), constructedObject, requiredFields);

			log.info("sessionId", "idType", "id",
					"JSON validator end time : " + DateUtils.getUTCCurrentDateTimeString());
			log.info("sessionId", "idType", "id",
					"Pre ID generation start time : " + DateUtils.getUTCCurrentDateTimeString());
			String preId = serviceUtil.generateId();
			log.info("sessionId", "idType", "id",
					"Pre ID generation end time : " + DateUtils.getUTCCurrentDateTimeString());

			DemographicEntity demographicEntity = demographicRepository
					.save(serviceUtil.prepareDemographicEntityForCreate(demographicRequest,
							StatusCodes.APPLICATION_INCOMPLETE.getCode(), authUserDetails().getUserId(), preId));
			DemographicCreateResponseDTO res = serviceUtil.setterForCreatePreRegistration(demographicEntity,
					demographicRequest.getDemographicDetails());

			mainResponseDTO.setResponse(res);
			isSuccess = true;
			mainResponseDTO.setResponsetime(serviceUtil.getCurrentResponseTime());
			log.info("sessionId", "idType", "id",
					"Pre Registration end time : " + DateUtils.getUTCCurrentDateTimeString());
		} catch (HttpServerErrorException | HttpClientErrorException e) {
			log.error("sessionId", "idType", "id", ExceptionUtils.getStackTrace(e));
			log.error("sessionId", "idType", "id",
					"In pre-registration service of addPreRegistration- " + e.getResponseBodyAsString());
			List<ServiceError> errorList = ExceptionUtils.getServiceErrorList(e.getResponseBodyAsString());
			new DemographicExceptionCatcher().handle(new DemographicServiceException(errorList, null), mainResponseDTO);
		} catch (Exception ex) {
			log.error("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id",
					"In pre-registration service of addPreRegistration- " + ex.getMessage());
			new DemographicExceptionCatcher().handle(ex, mainResponseDTO);

		} finally {

			if (isSuccess) {
				setAuditValues(EventId.PRE_407.toString(), EventName.PERSIST.toString(), EventType.BUSINESS.toString(),
						"Pre-Registration data is sucessfully saved in the demographic table",
						AuditLogVariables.NO_ID.toString(), authUserDetails().getUserId(),
						authUserDetails().getUsername());
			} else {
				setAuditValues(EventId.PRE_405.toString(), EventName.EXCEPTION.toString(), EventType.SYSTEM.toString(),
						"Failed to save the Pre-Registration data", AuditLogVariables.NO_ID.toString(),
						authUserDetails().getUserId(), authUserDetails().getUsername());
			}
		}
		return mainResponseDTO;

	}

	
	/*
	 * This method is used to update the demographic data by PreId
	 * 
	 * @see
	 * io.mosip.registration.service.RegistrationService#addPreRegistration(java.
	 * lang.Object, java.lang.String)
	 * 
	 * @param demographicRequest pass demographic request
	 * 
	 * @return responseDTO
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.preregistration.demographic.service.DemographicServiceIntf#
	 * updatePreRegistration(io.mosip.preregistration.core.common.dto.
	 * MainRequestDTO, java.lang.String, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public MainResponseDTO<DemographicUpdateResponseDTO> updatePreRegistration(
			MainRequestDTO<DemographicRequestDTO> request, String preRegistrationId, String userId) {
		log.info("sessionId", "idType", "id", "In updatePreRegistration method of pre-registration service ");
		log.info("sessionId", "idType", "id",
				"Pre Registration start time : " + DateUtils.getUTCCurrentDateTimeString());
		MainResponseDTO<DemographicUpdateResponseDTO> mainResponseDTO = null;
		mainResponseDTO = (MainResponseDTO<DemographicUpdateResponseDTO>) serviceUtil.getMainResponseDto(request);
		boolean isSuccess = false;
		try {
			log.info("sessionId", "idType", "id", "Get Schema from syncdata called");
			IdSchemaDto idSchema = serviceUtil.getSchema();
			log.info("sessionId", "idType", "id", "Get Schema from syncdata successful");
			validationUtil.langvalidation(request.getRequest().getLangCode());
			Map<String, String> requestParamMap = new HashMap<>();
			requestParamMap.put(DemographicRequestCodes.PRE_REGISTRAION_ID.getCode(), preRegistrationId);
			if (validationUtil.requstParamValidator(requestParamMap)) {
				DemographicRequestDTO demographicRequest = request.getRequest();
				log.info("sessionId", "idType", "id",
						"JSON validator start time : " + DateUtils.getUTCCurrentDateTimeString());

				List<String> identityKeys = convertSchemaJsonToArray(idSchema.getSchemaJson());

				log.info("IDENTITY KEYS: {}", identityKeys);

				List<String> requiredFields = demographicRequest.getRequiredFields().stream()
						.filter(field -> identityKeys.contains(field)).collect(Collectors.toList());

				JSONObject constructedObject = serviceUtil.constructNewDemographicRequest(identityKeys,
						demographicRequest.getDemographicDetails());

				log.debug("Constructed Object {}", constructedObject);

				jsonValidator.validateIdObject(idSchema.getSchemaJson(), constructedObject, requiredFields);
				log.info("sessionId", "idType", "id",
						"JSON validator end time : " + DateUtils.getUTCCurrentDateTimeString());
				DemographicEntity demographicEntity = demographicRepository.findBypreRegistrationId(preRegistrationId);
				if (!serviceUtil.isNull(demographicEntity)) {
					userValidation(userId, demographicEntity.getCreatedBy());
					if (!serviceUtil.isDemographicBookedOrExpired(demographicEntity, validationUtil)) {
						demographicEntity = demographicRepository.update(serviceUtil.prepareDemographicEntityForUpdate(
								demographicEntity, demographicRequest, demographicEntity.getStatusCode(),
								authUserDetails().getUserId(), preRegistrationId));
					} else {
						throw new RecordNotFoundException(DemographicErrorCodes.PRG_PAM_APP_022.getCode(),
								DemographicErrorMessages.NOT_POSSIBLE_TO_UPDATE.getMessage());
					}
				} else {
					throw new RecordNotFoundException(DemographicErrorCodes.PRG_PAM_APP_005.getCode(),
							DemographicErrorMessages.UNABLE_TO_FETCH_THE_PRE_REGISTRATION.getMessage());
				}
				DemographicUpdateResponseDTO res = serviceUtil.setterForUpdatePreRegistration(demographicEntity);
				mainResponseDTO.setResponse(res);
			}
			isSuccess = true;
			log.info("sessionId", "idType", "id",
					"Pre Registration end time : " + DateUtils.getUTCCurrentDateTimeString());
		} catch (HttpServerErrorException | HttpClientErrorException e) {
			log.error("sessionId", "idType", "id", ExceptionUtils.getStackTrace(e));
			log.error("sessionId", "idType", "id",
					"In pre-registration service of addPreRegistration- " + e.getResponseBodyAsString());
			List<ServiceError> errorList = ExceptionUtils.getServiceErrorList(e.getResponseBodyAsString());
			new DemographicExceptionCatcher().handle(new DemographicServiceException(errorList, null), mainResponseDTO);
		} catch (Exception ex) {
			log.error("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id",
					"In pre-registration service of updatePreRegistration- " + ex.getMessage());
			new DemographicExceptionCatcher().handle(ex, mainResponseDTO);

		} finally {
			mainResponseDTO.setResponsetime(serviceUtil.getCurrentResponseTime());
			if (isSuccess) {
				setAuditValues(EventId.PRE_402.toString(), EventName.UPDATE.toString(), EventType.BUSINESS.toString(),
						"Pre-Registration data is sucessfully updated in the demographic table",
						AuditLogVariables.NO_ID.toString(), authUserDetails().getUserId(),
						authUserDetails().getUsername());
			} else {
				setAuditValues(EventId.PRE_405.toString(), EventName.EXCEPTION.toString(), EventType.SYSTEM.toString(),
						"Failed to update the Pre-Registration data", AuditLogVariables.NO_ID.toString(),
						authUserDetails().getUserId(), authUserDetails().getUsername());
			}
		}
		return mainResponseDTO;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.preregistration.demographic.service.DemographicServiceIntf#
	 * getAllApplicationDetails(java.lang.String, java.lang.String)
	 */
	@Override
	public MainResponseDTO<DemographicMetadataDTO> getAllApplicationDetails(String userId, String pageIdx) {
		log.info("sessionId", "idType", "id", "In getAllApplicationDetails method of pre-registration service ");
		MainResponseDTO<DemographicMetadataDTO> response = new MainResponseDTO<>();
		Map<String, String> requestParamMap = new HashMap<>();
		DemographicMetadataDTO demographicMetadataDTO = new DemographicMetadataDTO();
		response.setId(retrieveId);
		response.setVersion(version);
		boolean isRetrieveSuccess = false;
		try {
			requestParamMap.put(DemographicRequestCodes.USER_ID.getCode(), userId);
			if (validationUtil.requstParamValidator(requestParamMap)) {
				log.info("sessionId", "idType", "id",
						"get demographic details start time : " + DateUtils.getUTCCurrentDateTimeString());
				List<DemographicEntity> demographicEntities = demographicRepository.findByCreatedBy(userId,
						StatusCodes.CONSUMED.getCode());
				log.info("sessionId", "idType", "id",
						"get demographic details end time : " + DateUtils.getUTCCurrentDateTimeString());
				if (!serviceUtil.isNull(demographicEntities)) {
					/*
					 * Fetch all the records for the user irrespective of page index and page size
					 */
					if (serviceUtil.isNull(pageIdx)) {
						prepareDemographicResponse(demographicMetadataDTO, demographicEntities);
						demographicMetadataDTO.setNoOfRecords("0");
						demographicMetadataDTO.setTotalRecords(Integer.toString(demographicEntities.size()));
						demographicMetadataDTO.setPageIndex("0");
						response.setResponse(demographicMetadataDTO);
					} else {
						/*
						 * Fetch all the pageable records for the user with respect to page index and
						 * page size
						 */
						log.info("sessionId", "idType", "id",
								"pagination start time : " + DateUtils.getUTCCurrentDateTimeString());
						@SuppressWarnings("static-access")
						Page<DemographicEntity> demographicEntityPage = demographicRepository
								.findByCreatedByOrderByCreateDateTime(userId, StatusCodes.CONSUMED.getCode(),
										PageRequest.of(serviceUtil.parsePageIndex(pageIdx),
												serviceUtil.parsePageSize(pageSize)));
						log.info("sessionId", "idType", "id",
								"pagination end time : " + DateUtils.getUTCCurrentDateTimeString());
						if (!serviceUtil.isNull(demographicEntityPage)
								&& !serviceUtil.isNull(demographicEntityPage.getContent())) {
							prepareDemographicResponse(demographicMetadataDTO, demographicEntityPage.getContent());
							demographicMetadataDTO
									.setNoOfRecords(Integer.toString(demographicEntityPage.getContent().size()));
							demographicMetadataDTO.setTotalRecords(Integer.toString(demographicEntities.size()));
							demographicMetadataDTO.setPageIndex(pageIdx);
							response.setResponse(demographicMetadataDTO);

						} else {
							throw new RecordNotFoundException(DemographicErrorCodes.PRG_PAM_APP_016.getCode(),
									DemographicErrorMessages.PAGE_NOT_FOUND.getMessage());
						}
					}
				} else {
					throw new RecordNotFoundException(DemographicErrorCodes.PRG_PAM_APP_005.getCode(),
							DemographicErrorMessages.NO_RECORD_FOUND_FOR_USER_ID.getMessage());
				}
			}
			isRetrieveSuccess = true;
		} catch (RuntimeException | JsonProcessingException | ParseException | IOException ex) {
			log.error("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id",
					"In getAllApplicationDetails method of pre-registration service - " + ex.getMessage());
			new DemographicExceptionCatcher().handle(ex, response);
		} finally {
			response.setResponsetime(serviceUtil.getCurrentResponseTime());
			if (isRetrieveSuccess) {
				setAuditValues(EventId.PRE_401.toString(), EventName.RETRIEVE.toString(), EventType.BUSINESS.toString(),
						"Retrieve All Pre-Registration id, Full name, Status and Appointment details by user id",
						AuditLogVariables.MULTIPLE_ID.toString(), authUserDetails().getUserId(),
						authUserDetails().getUsername());
			} else {
				setAuditValues(EventId.PRE_405.toString(), EventName.EXCEPTION.toString(), EventType.SYSTEM.toString(),
						"Retrieve All Pre-Registration data failed", AuditLogVariables.NO_ID.toString(),
						authUserDetails().getUserId(), authUserDetails().getUsername());
			}
		}
		return response;
	}

	@SuppressWarnings({ "unchecked" })
	private void prepareDemographicResponse(DemographicMetadataDTO demographicMetadataDTO,
			List<DemographicEntity> demographicEntities)
			throws ParseException, EncryptionFailedException, IOException, JsonProcessingException {
		List<DemographicViewDTO> viewList = new ArrayList<>();
		long start = System.currentTimeMillis();
		log.info("sessionId", "idType", "id", "for loop start time : " + DateUtils.getUTCCurrentDateTimeString());
		JSONParser jsonParser = new JSONParser();
		for (DemographicEntity demographicEntity : demographicEntities) {
			log.info("sessionId", "idType", "id", "decryption start time : " + DateUtils.getUTCCurrentDateTimeString());
			byte[] decryptedString = cryptoUtil.decrypt(demographicEntity.getApplicantDetailJson(),
					DateUtils.getUTCCurrentDateTime());
			log.info("sessionId", "idType", "id", "decryption end time : " + DateUtils.getUTCCurrentDateTimeString());
			log.info("sessionId", "idType", "id",
					"get document metadata start time : " + DateUtils.getUTCCurrentDateTimeString());
			JSONObject documentJsonObject = getDocumentMetadata(demographicEntity,
					DemographicRequestCodes.POA.getCode());
			log.info("sessionId", "idType", "id",
					"get document metadata end time : " + DateUtils.getUTCCurrentDateTimeString());

			JSONObject jsonObj = (JSONObject) jsonParser.parse(new String(decryptedString));
			JSONObject demographicMetadata = new JSONObject();
			String nameValue = getPreregistrationIdentityJson().getIdentity().getName().getValue();
			String poaValue = getPreregistrationIdentityJson().getDocuments().getPoa().getValue();
			String postalCodeValue = getPreregistrationIdentityJson().getIdentity().getDob().getValue();
			String[] nameKeys = nameValue.split(",");
			for (int i = 0; i < nameKeys.length; i++) {
				demographicMetadata.put(nameKeys[i], serviceUtil.getValueFromIdentity(decryptedString, nameKeys[i]));
			}
			Set<String> dataCaptureLang = serviceUtil.getDataCaptureLaanguage(jsonObj);
			demographicMetadata.put(postalCodeValue,
					serviceUtil.getIdJSONValue(jsonObj.toJSONString(), postalCodeValue));
			demographicMetadata.put(poaValue, documentJsonObject);
			DemographicViewDTO viewDto = new DemographicViewDTO();
			viewDto.setPreRegistrationId(demographicEntity.getPreRegistrationId());
			viewDto.setStatusCode(demographicEntity.getStatusCode());
			viewDto.setDemographicMetadata(demographicMetadata);
			viewDto.setDataCaptureLanguage(dataCaptureLang);
			log.info("sessionId", "idType", "id",
					"get booking details start time : " + DateUtils.getUTCCurrentDateTimeString());
			BookingRegistrationDTO bookingRegistrationDTO = getAppointmentData(demographicEntity);
			log.info("sessionId", "idType", "id",
					"get booking details end time : " + DateUtils.getUTCCurrentDateTimeString());
			viewDto.setBookingMetadata(bookingRegistrationDTO);
			viewList.add(viewDto);
		}
		log.info("sessionId", "idType", "id", "for loop end time : " + DateUtils.getUTCCurrentDateTimeString());
		long end = System.currentTimeMillis();
		log.info("sessionId", "idType", "id", "for loop total time : " + (end - start));
		demographicMetadataDTO.setBasicDetails(viewList);
	}

	private BookingRegistrationDTO getAppointmentData(DemographicEntity demographicEntity) {

		if (!serviceUtil.isNull(demographicEntity.getPreRegistrationId())) {
			ApplicationEntity applicationEntity = serviceUtil
					.findApplicationById(demographicEntity.getPreRegistrationId());
			log.info("In applicationEnity fetched {} in getAppointmentData method ", applicationEntity);
			if (applicationEntity.getAppointmentDate() != null) {
				BookingRegistrationDTO bookingRegistrationDTO = new BookingRegistrationDTO();
				bookingRegistrationDTO.setRegDate(applicationEntity.getAppointmentDate().toString());
				bookingRegistrationDTO.setRegistrationCenterId(applicationEntity.getRegistrationCenterId());
				bookingRegistrationDTO.setSlotFromTime(applicationEntity.getSlotFromTime().toString());
				bookingRegistrationDTO.setSlotToTime(applicationEntity.getSlotToTime().toString());
				return bookingRegistrationDTO;
			}
		}
		return null;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.preregistration.demographic.service.DemographicServiceIntf#
	 * getApplicationStatus(java.lang.String, java.lang.String)
	 */
	@Override
	public MainResponseDTO<PreRegistartionStatusDTO> getApplicationStatus(String preRegId, String userId) {
		log.info("sessionId", "idType", "id", "In getApplicationStatus method of pre-registration service ");
		return commonServiceUtil.getApplicationStatus(preRegId, userId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.preregistration.demographic.service.DemographicServiceIntf#
	 * deleteIndividual(java.lang.String, java.lang.String)
	 */
	@Override
	public MainResponseDTO<DeletePreRegistartionDTO> deleteIndividual(String preregId, String userId) {
		log.info("sessionId", "idType", "id", "In deleteIndividual method of pre-registration service ");
		MainResponseDTO<DeletePreRegistartionDTO> response = new MainResponseDTO<>();
		DeletePreRegistartionDTO deleteDto = new DeletePreRegistartionDTO();
		Map<String, String> requestParamMap = new HashMap<>();
		boolean isDeleteSuccess = false;
		response.setId(deleteId);
		response.setVersion(version);
		try {
			requestParamMap.put(DemographicRequestCodes.PRE_REGISTRAION_ID.getCode(), preregId);
			if (validationUtil.requstParamValidator(requestParamMap)) {
				ApplicationEntity applicationEntity = serviceUtil.findApplicationById(preregId);
				String bookingType = applicationEntity.getBookingType();
				if (bookingType.equals(BookingTypeCodes.NEW_PREREGISTRATION.toString())) {
					DemographicEntity demographicEntity = demographicRepository.findBypreRegistrationId(preregId);
					if (!serviceUtil.isNull(demographicEntity)) {
						userValidation(userId, demographicEntity.getCreatedBy());
						if (serviceUtil.checkStatusForDeletion(demographicEntity.getStatusCode())) {
							getDocumentServiceToDeleteAllByPreId(preregId);
							if ((demographicEntity.getStatusCode().equals(StatusCodes.BOOKED.getCode()))) {
								getBookingServiceToDeleteAllByPreId(preregId);
							}
							int isDeletedDemo = demographicRepository.deleteByPreRegistrationId(preregId);
							serviceUtil.deleteApplicationFromApplications(preregId);
							if (isDeletedDemo > 0) {
								deleteDto.setPreRegistrationId(preregId);
								deleteDto.setDeletedBy(userId);
								deleteDto.setDeletedDateTime(new Date(System.currentTimeMillis()));

							} else {
								throw new RecordFailedToDeleteException(DemographicErrorCodes.PRG_PAM_APP_004.getCode(),
										DemographicErrorMessages.FAILED_TO_DELETE_THE_PRE_REGISTRATION_RECORD
												.getMessage());
							}
						}
					} else {
						throw new RecordNotFoundException(DemographicErrorCodes.PRG_PAM_APP_005.getCode(),
								DemographicErrorMessages.UNABLE_TO_FETCH_THE_PRE_REGISTRATION.getMessage());
					}
					isDeleteSuccess = true;
				} else {
					throw new InvalidPreRegistrationIdException(ApplicationErrorCodes.PRG_APP_016.getCode(),
							ApplicationErrorMessages.INVALID_BOOKING_TYPE.getMessage());
				}
			}

		} catch (Exception ex) {
			log.error("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id", "In pre-registration deleteIndividual service- " + ex.getMessage());
			new DemographicExceptionCatcher().handle(ex, response);
		} finally {
			response.setResponsetime(serviceUtil.getCurrentResponseTime());
			if (isDeleteSuccess) {
				setAuditValues(EventId.PRE_403.toString(), EventName.DELETE.toString(), EventType.BUSINESS.toString(),
						"Pre-Registration data is successfully deleted from applications and demographic table",
						AuditLogVariables.NO_ID.toString(), authUserDetails().getUserId(),
						authUserDetails().getUsername());
			} else {
				setAuditValues(EventId.PRE_405.toString(), EventName.EXCEPTION.toString(), EventType.SYSTEM.toString(),
						"Deletion of Pre-Registration data failed", AuditLogVariables.NO_ID.toString(),
						authUserDetails().getUserId(), authUserDetails().getUsername());
			}
		}

		response.setResponse(deleteDto);
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.preregistration.demographic.service.DemographicServiceIntf#
	 * getDemographicData(java.lang.String)
	 */
	@Override
	public MainResponseDTO<DemographicResponseDTO> getDemographicData(String preRegId) {
		log.info("sessionId", "idType", "id", "In getDemographicData method of pre-registration service ");
		return commonServiceUtil.getDemographicData(preRegId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.preregistration.demographic.service.DemographicServiceIntf#
	 * updatePreRegistrationStatus(java.lang.String, java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public MainResponseDTO<String> updatePreRegistrationStatus(String preRegId, String status, String userId) {
		log.info("sessionId", "idType", "id", "In updatePreRegistrationStatus method of pre-registration service ");
		return commonServiceUtil.updatePreRegistrationStatus(preRegId, status, userId);
	}

	/**
	 * This method will check the status before updating.
	 * 
	 * @param demographicEntity pass demographicEntity
	 * @param status            pass status
	 */

	public void statusCheck(DemographicEntity demographicEntity, String status, String userId) {
		commonServiceUtil.statusCheck(demographicEntity, status, userId);
	}

	public boolean isupdateStausToPendingAppointmentValid(DemographicEntity demographicEntity) {
		return commonServiceUtil.isupdateStausToPendingAppointmentValid(demographicEntity);
	}

	/**
	 * This private Method is used to call rest service to delete document by preId
	 * 
	 * @param preregId
	 * @return boolean
	 * 
	 */
	private void getDocumentServiceToDeleteAllByPreId(String preregId) {
		log.info("sessionId", "idType", "id",
				"In callDocumentServiceToDeleteAllByPreId method of pre-registration service ");
		try {
			documentServiceImpl.deleteAllByPreId(preregId);
		} catch (RuntimeException ex) {
			if (!((BaseUncheckedException) ex).getErrorCode()
					.equalsIgnoreCase(DemographicErrorCodes.PRG_PAM_DOC_005.toString())) {
				throw ex;
			}
		}

	}

	/**
	 * This method is used to audit all the demographic events
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
		auditRequestDto.setId(idType);
		auditRequestDto.setSessionUserId(userId);
		auditRequestDto.setSessionUserName(userName);
		auditRequestDto.setModuleId(AuditLogVariables.DEM.toString());
		auditRequestDto.setModuleName(AuditLogVariables.DEMOGRAPHY_SERVICE.toString());
		auditLogUtil.saveAuditDetails(auditRequestDto);
	}

	private void getBookingServiceToDeleteAllByPreId(String preregId) throws Exception {
		log.info("sessionId", "idType", "id",
				"In callBookingServiceToDeleteAllByPreId method of pre-registration service ");
		MainResponseDTO<DeleteBookingDTO> deleteBooking = null;

		deleteBooking = serviceUtil.deleteBooking(preregId);
		if (deleteBooking.getErrors() != null) {
			throw new BookingDeletionFailedException(deleteBooking.getErrors().get(0).getErrorCode(),
					deleteBooking.getErrors().get(0).getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.preregistration.demographic.service.DemographicServiceIntf#
	 * getUpdatedDateTimeForPreIds(io.mosip.preregistration.core.common.dto.
	 * PreRegIdsByRegCenterIdDTO)
	 */
	@Override
	public MainResponseDTO<Map<String, String>> getUpdatedDateTimeForPreIds(
			PreRegIdsByRegCenterIdDTO preRegIdsByRegCenterIdDTO) {
		log.info("sessionId", "idType", "id",
				"In getUpdatedDateTimeForPreIds method of pre-registration service " + preRegIdsByRegCenterIdDTO);
		MainResponseDTO<Map<String, String>> mainResponseDTO = new MainResponseDTO<>();
		mainResponseDTO.setResponsetime(serviceUtil.getCurrentResponseTime());
		mainResponseDTO.setId(dateId);
		mainResponseDTO.setVersion(version);

		try {
			Map<String, String> preIdMap = new HashMap<>();
			if (preRegIdsByRegCenterIdDTO.getPreRegistrationIds() != null
					&& !preRegIdsByRegCenterIdDTO.getPreRegistrationIds().isEmpty()) {
				List<String> preIds = preRegIdsByRegCenterIdDTO.getPreRegistrationIds();

				List<String> statusCodes = new ArrayList<>();
				for (StatusCodes codes : StatusCodes.values()) {
					statusCodes.add(codes.getCode());
				}

				List<DemographicEntity> demographicEntities = demographicRepository
						.findByStatusCodeInAndPreRegistrationIdIn(statusCodes, preIds);
				if (demographicEntities != null && !demographicEntities.isEmpty()) {
					for (DemographicEntity demographicEntity : demographicEntities) {
						preIdMap.put(demographicEntity.getPreRegistrationId(),
								demographicEntity.getUpdateDateTime().toString());
					}
				} else {
					throw new RecordNotFoundForPreIdsException(DemographicErrorCodes.PRG_PAM_APP_005.getCode(),
							DemographicErrorMessages.UNABLE_TO_FETCH_THE_PRE_REGISTRATION.getMessage());
				}
			} else {
				throw new RecordNotFoundForPreIdsException(
						io.mosip.preregistration.core.errorcodes.ErrorCodes.PRG_CORE_REQ_001.getCode(),
						io.mosip.preregistration.core.errorcodes.ErrorMessages.MISSING_REQUEST_PARAMETER.getMessage());
			}

			mainResponseDTO.setResponse(preIdMap);
		} catch (Exception ex) {
			log.error("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id",
					"In getUpdatedDateTimeForPreIds method of pre-registration service- " + ex.getMessage());
			new DemographicExceptionCatcher().handle(ex, mainResponseDTO);
		}
		return mainResponseDTO;

	}

	public void userValidation(String authUserId, String preregUserId) {
		log.info("sessionId", "idType", "id", "In getDemographicData method of userValidation with priid "
				+ preregUserId + " and userID " + authUserId);
		if (!authUserId.trim().equals(preregUserId.trim())) {
			throw new PreIdInvalidForUserIdException(DemographicErrorCodes.PRG_PAM_APP_017.getCode(),
					DemographicErrorMessages.INVALID_PREID_FOR_USER.getMessage());
		}
	}

	private JSONObject getDocumentMetadata(DemographicEntity demographicEntity, String poa)
			throws JsonProcessingException, ParseException {
		String documentJsonString;
		JSONParser jsonParser = new JSONParser();
		DocumentMultipartResponseDTO documentMultipartResponseDTO = new DocumentMultipartResponseDTO();
		if (!serviceUtil.isNull(demographicEntity.getDocumentEntity())) {
			for (DocumentEntity documentEntity : demographicEntity.getDocumentEntity()) {
				if (documentEntity.getDocCatCode().equals(poa)) {
					documentMultipartResponseDTO.setDocCatCode(documentEntity.getDocCatCode());
					documentMultipartResponseDTO.setDocName(documentEntity.getDocName());
					documentMultipartResponseDTO.setDocTypCode(documentEntity.getDocTypeCode());
					documentMultipartResponseDTO.setDocumentId(documentEntity.getDocumentId());
					documentMultipartResponseDTO.setLangCode(documentEntity.getLangCode());
					documentJsonString = JsonUtils.javaObjectToJsonString(documentMultipartResponseDTO);
					return (JSONObject) jsonParser.parse(documentJsonString);
				}
			}
		}
		return null;
	}

	public DemographicIdentityRequestDTO getPreregistrationIdentityJson() {

		try {
			return objectMapper.readValue(getIdentityJsonString, DemographicIdentityRequestDTO.class);
		} catch (IOException ex) {
			log.error("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id",
					"In pre-registration service util of getPreregistrationIdentityJson- " + ex.getMessage());
		}
		return null;
	}

	private List<String> convertSchemaJsonToArray(String schemaJson) {
		List<String> idschemaAttributes = new ArrayList<String>();
		try {
			JsonNode node = objectMapper.readValue(schemaJson, JsonNode.class);

			node.get(PROPERTIES).get(INDENTITY).get(PROPERTIES).fieldNames()
					.forEachRemaining(field -> idschemaAttributes.add(field));

		} catch (JsonParseException ex) {
			throw new io.mosip.preregistration.demographic.exception.system.JsonParseException(
					DemographicErrorMessages.JSON_PARSING_FAILED.getMessage());
		} catch (JsonMappingException e) {
			throw new SystemFileIOException(DemographicErrorCodes.PRG_PAM_APP_018.getCode(),
					DemographicErrorMessages.UBALE_TO_READ_IDENTITY_JSON.getMessage(), null);
		} catch (IOException e) {
			throw new SystemFileIOException(DemographicErrorCodes.PRG_PAM_APP_018.getCode(),
					DemographicErrorMessages.UBALE_TO_READ_IDENTITY_JSON.getMessage(), null);
		}

		return idschemaAttributes;
	}
	
	public MainResponseDTO<ApplicationInfoMetadataDTO> getPregistrationInfo(String prid) {
		log.info("In getPregistrationInfo method of DemographicService for prid {}", prid);
		MainResponseDTO<ApplicationInfoMetadataDTO> response = new MainResponseDTO<>();
		response.setVersion(version);
		response.setResponsetime(DateTimeFormatter.ofPattern(mosipDateTimeFormat).format(LocalDateTime.now()));
		ApplicationInfoMetadataDTO applicationInfo = new ApplicationInfoMetadataDTO();
		DocumentsMetaData documentsMetaData = null;
		DemographicResponseDTO demographicResponse = null;
		try {
			if (prid == null) {
				throw new InvalidRequestParameterException(DemographicErrorCodes.PRG_PAM_APP_013.getCode(),
						ApplicationErrorMessages.INVALID_REQUEST_APPLICATION_ID.getMessage(), response);
			}
			log.info("In getPregistrationInfo method of DemographicService fetching demographic for prid {}", prid);
			demographicResponse = getDemographicData(prid.trim()).getResponse();
			applicationInfo.setDemographicResponse(demographicResponse);
			response.setResponse(applicationInfo);
			try {
				log.info("In getPregistrationInfo method of DemographicService fetching documents for prid {}", prid);
				documentsMetaData = documentServiceImpl.getAllDocumentForPreId(prid.trim()).getResponse();
				applicationInfo.setDocumentsMetaData(documentsMetaData);
			} catch (PreRegistrationException | DocumentNotFoundException ex) {
				log.error("Exception occured while fetching documents for prid {}", prid);
				log.error("{}", ex);
				applicationInfo.setDocumentsMetaData(documentsMetaData);
			}
		} catch (Exception ex) {
			log.error("Exception occured while fetching demographic for prid {}", prid);
			log.error("{}", ex);
			new DemographicExceptionCatcher().handle(ex, response);
		}
		return response;
	}
}