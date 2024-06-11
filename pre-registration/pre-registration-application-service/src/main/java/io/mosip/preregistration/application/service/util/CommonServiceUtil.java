package io.mosip.preregistration.application.service.util;

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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.preregistration.application.code.DemographicRequestCodes;
import io.mosip.preregistration.application.dto.ApplicantTypeRequestDTO;
import io.mosip.preregistration.application.dto.ApplicantValidDocumentDto;
import io.mosip.preregistration.application.errorcodes.DemographicErrorCodes;
import io.mosip.preregistration.application.errorcodes.DemographicErrorMessages;
import io.mosip.preregistration.application.exception.DemographicServiceException;
import io.mosip.preregistration.application.exception.RecordFailedToUpdateException;
import io.mosip.preregistration.application.exception.RecordNotFoundException;
import io.mosip.preregistration.application.exception.util.DemographicExceptionCatcher;
import io.mosip.preregistration.application.repository.DemographicRepository;
import io.mosip.preregistration.core.code.StatusCodes;
import io.mosip.preregistration.core.common.dto.DemographicResponseDTO;
import io.mosip.preregistration.core.common.dto.ExceptionJSONInfoDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.dto.PreRegistartionStatusDTO;
import io.mosip.preregistration.core.common.entity.DemographicEntity;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.exception.EncryptionFailedException;
import io.mosip.preregistration.core.exception.HashingException;
import io.mosip.preregistration.core.exception.PreIdInvalidForUserIdException;
import io.mosip.preregistration.core.util.CryptoUtil;
import io.mosip.preregistration.core.util.HashUtill;
import io.mosip.preregistration.core.util.ValidationUtil;
import io.mosip.preregistration.demographic.exception.system.JsonParseException;

/**
 * This class provides the common service implementation for DocumentService and
 * DemographicService
 * 
 * @author Janardhan B S
 * @since 1.0.0
 */
@Component
public class CommonServiceUtil {
	/**
	 * logger instance
	 */
	private Logger log = LoggerConfiguration.logConfig(CommonServiceUtil.class);

	@Value("${mosip.utc-datetime-pattern}")
	private String utcDateTimePattern;

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
	 * Reference for ${ver} from property file
	 */
	@Value("${version}")
	private String version;

	@Value("${masterdata.resource.url}")
	private String masterdataResourseUrl;

	/**
	 * Reference for ${updateStatusId} from property file
	 */
	@Value("${mosip.preregistration.demographic.update.status.id}")
	private String updateStatusId;

	@Autowired
	private ValidationUtil validationUtil;

	@Autowired
	private CryptoUtil cryptoUtil;

	@Autowired
	private DemographicServiceUtil demographicServiceUtil;

	/**
	 * Autowired reference for {@link #RegistrationRepositary}
	 */
	@Autowired
	private DemographicRepository demographicRepository;

	public MainResponseDTO<DemographicResponseDTO> getDemographicData(String preRegId) {
		MainResponseDTO<DemographicResponseDTO> response = new MainResponseDTO<>();
		Map<String, String> requestParamMap = new HashMap<>();
		response.setResponsetime(getCurrentResponseTime());
		response.setId(retrieveDetailsId);
		response.setVersion(version);
		try {
			requestParamMap.put(DemographicRequestCodes.PRE_REGISTRAION_ID.getCode(), preRegId);
			if (validationUtil.requstParamValidator(requestParamMap)) {
				DemographicEntity demographicEntity = demographicRepository.findBypreRegistrationId(preRegId);
				if (demographicEntity != null) {
					List<String> list = listAuth(authUserDetails().getAuthorities());
					log.info("sessionId", "idType", "id",
							"In getDemographicData method of pre-registration service with list  " + list);
					if (list.contains("ROLE_INDIVIDUAL")) {
						userValidation(authUserDetails().getUserId(), demographicEntity.getCreatedBy());
					}
					
					String hashString = HashUtill.hashUtill(demographicEntity.getApplicantDetailJson());
					if (HashUtill.isHashEqual(demographicEntity.getDemogDetailHash().getBytes(),
							hashString.getBytes())) {

						DemographicResponseDTO createDto = setterForCreateDTO(demographicEntity);
						response.setResponse(createDto);
					} else {
						throw new HashingException(
								io.mosip.preregistration.core.errorcodes.ErrorCodes.PRG_CORE_REQ_010.name(),
								io.mosip.preregistration.core.errorcodes.ErrorMessages.HASHING_FAILED.name());
					}
				} else {
					throw new RecordNotFoundException(DemographicErrorCodes.PRG_PAM_APP_005.getCode(),
							DemographicErrorMessages.UNABLE_TO_FETCH_THE_PRE_REGISTRATION.getMessage());
				}
			}
		} catch (Exception ex) {
			log.error("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id",
					"In getDemographicData of pre-registration service- " + ex.getMessage());
			new DemographicExceptionCatcher().handle(ex, response);
		}

		response.setErrors(null);
		return response;
	}

	public String getCurrentResponseTime() {
		log.info("sessionId", "idType", "id", "In getCurrentResponseTime method of document service util");
		return DateUtils.formatDate(new Date(System.currentTimeMillis()), utcDateTimePattern);
	}

	public AuthUserDetails authUserDetails() {
		return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	public void userValidation(String authUserId, String preregUserId) {
		log.info("sessionId", "idType", "id", "In getDemographicData method of userValidation with priid "
				+ preregUserId + " and userID " + authUserId);
		if (!authUserId.trim().equals(preregUserId.trim())) {
			throw new PreIdInvalidForUserIdException(DemographicErrorCodes.PRG_PAM_APP_017.getCode(),
					DemographicErrorMessages.INVALID_PREID_FOR_USER.getMessage());
		}
	}

	public DemographicResponseDTO setterForCreateDTO(DemographicEntity demographicEntity) {
		log.info("sessionId", "idType", "id", "In setterForCreateDTO method of pre-registration service util");
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
			log.error("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id",
					"In setterForCreateDTO method of pre-registration service- " + ex.getMessage());
			throw new JsonParseException(DemographicErrorCodes.PRG_PAM_APP_007.getCode(),
					DemographicErrorMessages.JSON_PARSING_FAILED.getMessage(), ex.getCause());
		} catch (EncryptionFailedException ex) {
			log.error("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id",
					"In setterForCreateDTO method of pre-registration service- " + ex.getMessage());
			throw ex;
		}
		return createDto;
	}

	public String getLocalDateString(LocalDateTime date) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(utcDateTimePattern);
		return date.format(dateTimeFormatter);
	}

	/**
	 * This method is used to get the list of authorization role
	 * 
	 * @param collection
	 * @return list of auth role
	 */
	public List<String> listAuth(Collection<? extends GrantedAuthority> collection) {
		List<String> listWORole = new ArrayList<>();
		for (GrantedAuthority authority : collection) {
			String s = authority.getAuthority();
			listWORole.add(s);
		}
		return listWORole;
	}

	public List<String> validMandatoryDocumentsForApplicant(DemographicEntity demographicEntity) throws ParseException {

		String applicantTypeCode = null;
		ApplicantValidDocumentDto applicantValidDocuments = null;

		ApplicantTypeRequestDTO applicantTypeRequest = demographicServiceUtil
				.createApplicantTypeRequest(demographicEntity);

		applicantTypeCode = demographicServiceUtil.getApplicantypeCode(applicantTypeRequest);

		applicantValidDocuments = demographicServiceUtil.getDocCatAndTypeForApplicantCode(applicantTypeCode,
				demographicEntity.getLangCode());
		Set<String> mandatoryDocCat = demographicServiceUtil.getMandatoryDocCatogery();

		log.info("mandatory Docs category --> {}", mandatoryDocCat);
		List<String> validMandatoryDocumentForApplicant = applicantValidDocuments.getDocumentCategories().stream()
				.filter(docCat -> mandatoryDocCat.contains(docCat.getCode())).map(docCat -> docCat.getCode())
				.collect(Collectors.toList());

		return validMandatoryDocumentForApplicant;
	}

	public MainResponseDTO<String> updatePreRegistrationStatus(String preRegId, String status, String userId) {
		log.info("sessionId", "idType", "id", "In updatePreRegistrationStatus method of pre-registration service ");
		MainResponseDTO<String> response = new MainResponseDTO<>();
		Map<String, String> requestParamMap = new HashMap<>();
		response.setResponsetime(getCurrentResponseTime());
		response.setId(updateStatusId);
		response.setVersion(version);
		try {
			requestParamMap.put(DemographicRequestCodes.PRE_REGISTRAION_ID.getCode(), preRegId);
			requestParamMap.put(DemographicRequestCodes.STATUS_CODE.getCode(), status);
			if (validationUtil.requstParamValidator(requestParamMap)) {
				DemographicEntity demographicEntity = demographicRepository.findBypreRegistrationId(preRegId);
				statusCheck(demographicEntity, status, userId);
				response.setResponse("STATUS_UPDATED_SUCESSFULLY");
			}
		} catch (RecordFailedToUpdateException | RecordNotFoundException ex) {
			response.setResponse("STATUS_NOT_UPDATED_SUCESSFULLY");
			log.error("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id",
					"Error in updatePreRegistrationStatus method of pre-registration service- " + ex.getMessage());
			// new DemographicExceptionCatcher().handle(ex, response);
			ExceptionJSONInfoDTO errorDetails = new ExceptionJSONInfoDTO(ex.getErrorCode(), ex.getErrorText());
			List<ExceptionJSONInfoDTO> errorList = new ArrayList<>();
			errorList.add(errorDetails);
			response.setErrors(errorList);
		} catch (Exception ex) {
			log.error("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id",
					"Error in updatePreRegistrationStatus method of pre-registration service- " + ex.getMessage());
			new DemographicExceptionCatcher().handle(ex, response);
		}
		return response;
	}

	public MainResponseDTO<PreRegistartionStatusDTO> getApplicationStatus(String preRegId, String userId) {
		log.info("sessionId", "idType", "id", "In getApplicationStatus method of pre-registration service ");
		PreRegistartionStatusDTO statusdto = new PreRegistartionStatusDTO();
		MainResponseDTO<PreRegistartionStatusDTO> response = new MainResponseDTO<>();
		Map<String, String> requestParamMap = new HashMap<>();
		response.setId(retrieveStatusId);
		response.setVersion(version);
		response.setResponsetime(getCurrentResponseTime());
		try {
			requestParamMap.put(DemographicRequestCodes.PRE_REGISTRAION_ID.getCode(), preRegId);
			if (validationUtil.requstParamValidator(requestParamMap)) {
				DemographicEntity demographicEntity = demographicRepository.findBypreRegistrationId(preRegId);
				List<String> list = listAuth(authUserDetails().getAuthorities());
				if (demographicEntity != null) {
					if (list.contains("ROLE_INDIVIDUAL")) {
						userValidation(authUserDetails().getUserId(), demographicEntity.getCreatedBy());
					}
					String hashString = HashUtill.hashUtill(demographicEntity.getApplicantDetailJson());

					if (HashUtill.isHashEqual(demographicEntity.getDemogDetailHash().getBytes(),
							hashString.getBytes())) {
						statusdto.setPreRegistartionId(demographicEntity.getPreRegistrationId());
						statusdto.setStatusCode(demographicEntity.getStatusCode());
						response.setResponse(statusdto);

					} else {
						throw new HashingException(
								io.mosip.preregistration.core.errorcodes.ErrorCodes.PRG_CORE_REQ_010.name(),
								io.mosip.preregistration.core.errorcodes.ErrorMessages.HASHING_FAILED.name());

					}
				} else {
					throw new RecordNotFoundException(DemographicErrorCodes.PRG_PAM_APP_005.getCode(),
							DemographicErrorMessages.UNABLE_TO_FETCH_THE_PRE_REGISTRATION.getMessage());

				}
			}
		} catch (Exception ex) {
			log.error("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id",
					"In getApplicationStatus method of pre-registration service - " + ex.getMessage());
			new DemographicExceptionCatcher().handle(ex, response);
		}
		return response;
	}
	
	public void statusCheck(DemographicEntity demographicEntity, String status, String userId) {
		if (demographicEntity != null) {
			if (demographicServiceUtil.isStatusValid(status)) {
				demographicEntity.setStatusCode(StatusCodes.valueOf(status.toUpperCase()).getCode());
				List<String> list = listAuth(authUserDetails().getAuthorities());
				if (list.contains("ROLE_INDIVIDUAL")) {
					userValidation(authUserDetails().getUserId(), demographicEntity.getCreatedBy());
				}
				if (status.toLowerCase().equals(StatusCodes.PENDING_APPOINTMENT.getCode().toLowerCase())) {
					try {
						if (isupdateStausToPendingAppointmentValid(demographicEntity)) {
							String prid = demographicEntity.getPreRegistrationId();
							demographicServiceUtil.updateApplicationStatus(prid, status, userId);
							log.info("Application booking status updated succesfully --> {}", status);
							demographicRepository.update(demographicEntity);
							log.info("demographic booking status updated succesfully --> {}", status);

						} else {
							throw new RecordFailedToUpdateException(DemographicErrorCodes.PRG_PAM_APP_023.getCode(),
									DemographicErrorMessages.FAILED_TO_UPDATE_STATUS_PENDING_APPOINTMENT.getMessage());
						}
					} catch (Exception e) {
						throw new RecordFailedToUpdateException(DemographicErrorCodes.PRG_PAM_APP_023.getCode(),
								DemographicErrorMessages.FAILED_TO_UPDATE_STATUS_PENDING_APPOINTMENT.getMessage());
					}
				} else {
					String prid = demographicEntity.getPreRegistrationId();
					demographicServiceUtil.updateApplicationStatus(prid, status, userId);
					log.info("Application booking status updated succesfully --> {}", status);
					demographicRepository.update(demographicEntity);
					log.info("demographic booking status updated succesfully --> {}", status);

				}
			} else {
				throw new RecordFailedToUpdateException(DemographicErrorCodes.PRG_PAM_APP_005.getCode(),
						DemographicErrorMessages.INVALID_STATUS_CODE.getMessage());
			}
		} else {
			throw new RecordNotFoundException(DemographicErrorCodes.PRG_PAM_APP_005.getCode(),
					DemographicErrorMessages.UNABLE_TO_FETCH_THE_PRE_REGISTRATION.getMessage());
		}
	}

	public boolean isupdateStausToPendingAppointmentValid(DemographicEntity demographicEntity) {
		boolean isValid = false;
		try {
			List<String> validMandatoryDocForApplicant = validMandatoryDocumentsForApplicant(demographicEntity);

			log.info("valid mandatory Docs category for applicant-->{}", validMandatoryDocForApplicant);
			List<String> uploadedDocs = demographicEntity.getDocumentEntity().stream().map(doc -> doc.getDocCatCode())
					.collect(Collectors.toList());
			log.info("uploaded Docs category --> {}", uploadedDocs);

			isValid = compareUploadedDocListAndValidMandatoryDocList(uploadedDocs, validMandatoryDocForApplicant);

		} catch (Exception ex) {

			log.error("Exception Docs category -->", ex);
			throw new DemographicServiceException(((DemographicServiceException) ex).getErrorCode(),
					((DemographicServiceException) ex).getErrorText());

		}
		return isValid;
	}

	private boolean compareUploadedDocListAndValidMandatoryDocList(List<String> uploadedDocs,
			List<String> validMandatoryDocForApplicant) {
		if (validMandatoryDocForApplicant.isEmpty()) {
			return true;
		} else {
			uploadedDocs.forEach(docCat -> validMandatoryDocForApplicant.remove(docCat));
			if (!validMandatoryDocForApplicant.isEmpty()) {
				return false;
			} else {
				return true;
			}
		}
	}
}