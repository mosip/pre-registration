package io.mosip.preregistration.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.amazonaws.services.pinpoint.model.ApplicationResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.HMACUtils2;
import io.mosip.preregistration.application.dto.ApplicationDetailResponseDTO;
import io.mosip.preregistration.application.dto.ApplicationInfoMetadataDTO;
import io.mosip.preregistration.application.dto.ApplicationRequestDTO;
import io.mosip.preregistration.application.dto.ApplicationResponseDTO;
import io.mosip.preregistration.application.dto.UIAuditRequest;
import io.mosip.preregistration.application.errorcodes.ApplicationErrorCodes;
import io.mosip.preregistration.application.errorcodes.ApplicationErrorMessages;
import io.mosip.preregistration.application.errorcodes.DemographicErrorCodes;
import io.mosip.preregistration.application.errorcodes.DemographicErrorMessages;
import io.mosip.preregistration.application.exception.AuditFailedException;
import io.mosip.preregistration.application.exception.DemographicServiceException;
import io.mosip.preregistration.application.exception.DocumentNotFoundException;
import io.mosip.preregistration.application.exception.InvalidDateFormatException;
import io.mosip.preregistration.application.exception.RecordFailedToUpdateException;
import io.mosip.preregistration.application.exception.RecordNotFoundException;
import io.mosip.preregistration.application.exception.util.DemographicExceptionCatcher;
import io.mosip.preregistration.application.repository.ApplicationRepostiory;
import io.mosip.preregistration.application.service.util.DemographicServiceUtil;
import io.mosip.preregistration.core.code.ApplicationStatusCode;
import io.mosip.preregistration.core.code.AuditLogVariables;
import io.mosip.preregistration.core.code.EventId;
import io.mosip.preregistration.core.code.EventName;
import io.mosip.preregistration.core.code.EventType;
import io.mosip.preregistration.core.code.StatusCodes;
import io.mosip.preregistration.core.common.dto.AuditRequestDto;
import io.mosip.preregistration.core.common.dto.DemographicResponseDTO;
import io.mosip.preregistration.core.common.dto.DocumentsMetaData;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.entity.ApplicationEntity;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.exception.InvalidRequestParameterException;
import io.mosip.preregistration.core.exception.PreRegistrationException;
import io.mosip.preregistration.core.util.AuditLogUtil;
import io.mosip.preregistration.core.util.ValidationUtil;

@Service
public class ApplicationService implements ApplicationServiceIntf {

	@Value("${version}")
	private String version;

	@Autowired
	DemographicServiceIntf demographicService;

	@Autowired
	DocumentServiceIntf documentService;

	@Autowired
	ApplicationRepostiory applicationRepository;

	@Autowired
	AuditLogUtil auditUtil;

	@Autowired
	ValidationUtil validationUtil;
	
	/**
	 * Autowired reference for {@link #DemographicServiceUtil}
	 */
	@Autowired
	private DemographicServiceUtil serviceUtil;


	@Value("${mosip.utc-datetime-pattern}")
	private String mosipDateTimeFormat;

	@Value("${mosip.preregistration.applications.status.get}")
	private String applicationStatusId;

	@Value("${mosip.preregistration.applications.details.get}")
	private String applicationDetailsId;
	/**
	 * logger instance
	 */
	private Logger log = LoggerConfiguration.logConfig(ApplicationService.class);
	

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

	public MainResponseDTO<ApplicationInfoMetadataDTO> getPregistrationInfo(String prid) {
		log.info("In getPregistrationInfo method of Application service for prid {}", prid);
		MainResponseDTO<ApplicationInfoMetadataDTO> response = new MainResponseDTO<ApplicationInfoMetadataDTO>();
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
			log.info("In getPregistrationInfo method of Application service fetching demographic for prid {}", prid);
			demographicResponse = demographicService.getDemographicData(prid.trim(), false).getResponse();
			applicationInfo.setDemographicResponse(demographicResponse);
			response.setResponse(applicationInfo);
			try {
				log.info("In getPregistrationInfo method of Application service fetching documents for prid {}", prid);
				documentsMetaData = documentService.getAllDocumentForPreId(prid.trim()).getResponse();
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

	public MainResponseDTO<String> saveUIEventAudit(UIAuditRequest auditRequest) {
		log.info("In saveUIEventAudit method");
		MainResponseDTO<String> response = new MainResponseDTO<String>();
		response.setResponsetime(DateTimeFormatter.ofPattern(mosipDateTimeFormat).format(LocalDateTime.now()));
		response.setVersion(version);
		try {
			String description = auditRequest.getDescription();
			JsonNode node = new ObjectMapper().readTree(description);
			String template = node.get("template").toString();
			String hashedDescription = node.get("description").asText().concat("  ")
					.concat("Request_url :" + node.get("url").asText()).concat("  ")
					.concat("Consent_Text: " + HMACUtils2.digestAsPlainText(template.getBytes()));
			auditRequest.setDescription(hashedDescription);
			AuditRequestDto auditRequestDto = setAuditValues(auditRequest);
			log.info("In saveUIEventAudit method saving audit  details {}", auditRequestDto);
			auditUtil.saveAuditDetails(auditRequestDto);
			log.info("Request audit logged successfully");
			response.setResponse("Audit Logged Successfully");
		} catch (Exception ex) {
			log.error("Exception error occured while saving audit Request {}", ex);
			throw new AuditFailedException(ApplicationErrorCodes.PRG_APP_007.getCode(),
					ApplicationErrorMessages.AUDIT_FAILED.getMessage());
		}

		return response;
	}

	private AuditRequestDto setAuditValues(UIAuditRequest auditRequest) {
		AuditRequestDto auditRequestDto = new AuditRequestDto();
		auditRequestDto.setEventId(EventId.PRE_414.toString());
		auditRequestDto.setEventName(auditRequest.getEventName());
		auditRequestDto.setEventType(EventType.BUSINESS.toString());
		auditRequestDto.setDescription(auditRequest.getDescription());
		auditRequestDto.setId(AuditLogVariables.NO_ID.toString());
		auditRequestDto.setSessionUserId(auditRequest.getActionUserId());
		auditRequestDto.setSessionUserName(auditRequest.getActionUserId());
		auditRequestDto.setModuleId(auditRequest.getModuleId());
		auditRequestDto.setModuleName(auditRequest.getModuleName());
		auditRequestDto.setActionTimeStamp(LocalDateTime.parse(auditRequest.getActionTimeStamp()));
		return auditRequestDto;
	}

	public MainResponseDTO<String> getApplicationsStatusForApplicationId(String applicationId) {
		MainResponseDTO<String> response = new MainResponseDTO<String>();
		response.setId(applicationStatusId);
		response.setVersion(version);
		response.setResponsetime(DateTimeFormatter.ofPattern(mosipDateTimeFormat).format(LocalDateTime.now()));
		String bookingStatusCode = null;
		try {
			if (applicationId == null) {
				throw new InvalidRequestParameterException(ApplicationErrorCodes.PRG_APP_014.getCode(),
						ApplicationErrorMessages.INVALID_REQUEST_APPLICATION_ID.getMessage(), response);
			}
			bookingStatusCode = applicationRepository.findBookingStatusCodeById(applicationId);
			if (bookingStatusCode == null) {
				throw new RecordNotFoundException(ApplicationErrorCodes.PRG_APP_013.getCode(),
						ApplicationErrorMessages.NO_RECORD_FOUND.getMessage());
			}
			log.info("Application Booking status: {} for the Application Id : {}", bookingStatusCode, applicationId);
			response.setResponse(bookingStatusCode);
		} catch (Exception ex) {
			log.error("Error while Getting the Booking statuscode for applicationId ", applicationId);
			log.error("Exception trace", ex);
			log.error("Error while updating status for applications");
			log.error("Excepction {}", ex);
			throw new RecordFailedToUpdateException(ApplicationErrorCodes.PRG_APP_010.getCode(),
					ApplicationErrorMessages.STATUS_UPDATE_FOR_APPLICATIONS_FAILED.getMessage());
		}
		return response;
	}

	public MainResponseDTO<List<ApplicationDetailResponseDTO>> getApplicationsForApplicationId(String regCenterId,
			String appointmentDate) {
		MainResponseDTO<List<ApplicationDetailResponseDTO>> mainResponse = new MainResponseDTO<>();
		mainResponse.setId(applicationDetailsId);
		mainResponse.setVersion(version);
		mainResponse.setResponsetime(DateTimeFormatter.ofPattern(mosipDateTimeFormat).format(LocalDateTime.now()));
		List<ApplicationDetailResponseDTO> responseList = new ArrayList<>();
		try {
			LocalDate appDate = LocalDate.parse(appointmentDate);
			List<ApplicationEntity> entity = applicationRepository
					.findByRegistrationCenterIdAndAppointmentDate(regCenterId, appDate);
			if (entity != null) {
				entity.forEach(obj -> {
					ApplicationDetailResponseDTO response = new ApplicationDetailResponseDTO();
					response.setApplicationId(obj.getApplicationId());
					response.setApplicationStatusCode(obj.getApplicationStatusCode());
					response.setAppointmentDate(obj.getAppointmentDate().toString());
					response.setBookingStatusCode(obj.getBookingStatusCode());
					response.setRegistrationCenterId(obj.getRegistrationCenterId());
					response.setSlotFromTime(obj.getSlotFromTime().toString());
					response.setSlotToTime(obj.getSlotToTime().toString());
					response.setCrBy(obj.getCrBy());
					response.setCrDtime(obj.getCrDtime().toString());
					response.setBookingType(obj.getBookingType());
					responseList.add(response);
				});
				mainResponse.setResponse(responseList);

			} else {
				throw new RecordNotFoundException(ApplicationErrorCodes.PRG_APP_012.getCode(),
						ApplicationErrorMessages.NO_RECORD_FOUND.getMessage());
			}
		} catch (RecordNotFoundException ex) {
			log.error("Record Not Found Exception for the request regCenterId and appointmentDate", regCenterId,
					appointmentDate);
			log.error("Exception trace", ex);
			throw new RecordNotFoundException(ApplicationErrorCodes.PRG_APP_012.getCode(),
					ApplicationErrorMessages.NO_RECORD_FOUND.getMessage(), mainResponse);
		} catch (IllegalArgumentException ex) {
			log.error("Illegal Argument exception", ex);
			throw new InvalidRequestParameterException(ApplicationErrorCodes.PRG_APP_013.getCode(),
					ApplicationErrorMessages.INVAILD_REQUEST_ARGUMENT.getMessage(), mainResponse);
		} catch (DateTimeParseException ex) {
			log.error("Invaild Date as argument", ex);
			throw new InvalidDateFormatException(ApplicationErrorCodes.PRG_APP_013.getCode(),
					ApplicationErrorMessages.INVAILD_REQUEST_ARGUMENT.getMessage(), mainResponse);
		}

		return mainResponse;
	}
	
	/*
	 * This method is used to create the a new application with booking type as
	 * UPDATE_REGISTRATION_DETAILS or LOST_FORGOTTEN_UIN
	 * 
	 * 
	 * @param request pass application request
	 * 
	 * @param bookingType UPDATE_REGISTRATION_DETAILS or LOST_FORGOTTEN_UIN
	 * 
	 * @return MainResponseDTO<ApplicationResponseDTO>
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.preregistration.demographic.service.DemographicServiceIntf#
	 * addApplication(io.mosip.preregistration.core.common.dto.MainRequestDTO)
	 */
	@SuppressWarnings({ "unchecked" })
	@Override
	public MainResponseDTO<ApplicationResponseDTO> addLostOrUpdateApplication(
			MainRequestDTO<ApplicationRequestDTO> request, String bookingType) {
		log.info("sessionId", "idType", "id", "In addLostOrUpdateApplication method of pre-registration service ");
		log.info("sessionId", "idType", "id",
				"Add Application start time : " + DateUtils.getUTCCurrentDateTimeString());
		MainResponseDTO<ApplicationResponseDTO> mainResponseDTO = null;
		boolean isSuccess = false;
		try {
			log.info("sessionId", "idType", "id", "bookingType : " + bookingType);
			mainResponseDTO = (MainResponseDTO<ApplicationResponseDTO>) serviceUtil.getMainResponseDto(request);
			ApplicationRequestDTO applicationRequest = request.getRequest();
			validationUtil.langvalidation(applicationRequest.getLangCode());
			String applicationId = serviceUtil.generateId();
			log.info("sessionId", "idType", "id", "applicationId : " + applicationId);
			ApplicationEntity applicationEntity = serviceUtil.saveAndUpdateApplicationEntity(applicationId, bookingType,
					ApplicationStatusCode.SUBMITTED.getApplicationStatusCode(),
					StatusCodes.PENDING_APPOINTMENT.getCode(), authUserDetails().getUserId());
			isSuccess = true;
			ApplicationResponseDTO appplicationResponse = new ApplicationResponseDTO();
			appplicationResponse.setApplicationId(applicationEntity.getApplicationId());
			appplicationResponse.setBookingType(applicationEntity.getBookingType());
			appplicationResponse.setApplicationStatusCode(applicationEntity.getApplicationStatusCode());
			appplicationResponse.setBookingStatusCode(applicationEntity.getBookingStatusCode());
			appplicationResponse.setLangCode(applicationRequest.getLangCode());
			appplicationResponse.setCreatedBy(applicationEntity.getCrBy());
			appplicationResponse.setCreatedDateTime(serviceUtil.getLocalDateString(applicationEntity.getCrDtime()));
			appplicationResponse.setUpdatedBy(applicationEntity.getUpdBy());
			appplicationResponse.setUpdatedDateTime(serviceUtil.getLocalDateString(applicationEntity.getUpdDtime()));
			mainResponseDTO.setResponse(appplicationResponse);
			mainResponseDTO.setResponsetime(serviceUtil.getCurrentResponseTime());
			log.info("sessionId", "idType", "id",
					"Add Application n end time : " + DateUtils.getUTCCurrentDateTimeString());
		} catch (HttpServerErrorException | HttpClientErrorException e) {
			log.error("sessionId", "idType", "id", ExceptionUtils.getStackTrace(e));
			log.error("sessionId", "idType", "id",
					"In pre-registration service of addLostOrUpdateApplication - " + e.getResponseBodyAsString());
			List<ServiceError> errorList = ExceptionUtils.getServiceErrorList(e.getResponseBodyAsString());
			new DemographicExceptionCatcher().handle(new DemographicServiceException(errorList, null), mainResponseDTO);
		} catch (Exception ex) {
			log.error("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id",
					"In pre-registration service of addLostOrUpdateApplication- " + ex.getMessage());
			new DemographicExceptionCatcher().handle(ex, mainResponseDTO);
		} finally {
			if (isSuccess) {
				createAuditValues(EventId.PRE_407.toString(), EventName.PERSIST.toString(), EventType.BUSINESS.toString(),
						"Application data is sucessfully saved in the applications table",
						AuditLogVariables.NO_ID.toString(), authUserDetails().getUserId(),
						authUserDetails().getUsername());
			} else {
				createAuditValues(EventId.PRE_405.toString(), EventName.EXCEPTION.toString(), EventType.SYSTEM.toString(),
						"Failed to save the Application data", AuditLogVariables.NO_ID.toString(),
						authUserDetails().getUserId(), authUserDetails().getUsername());
			}
		}
		return mainResponseDTO;
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
	public void createAuditValues(String eventId, String eventName, String eventType, String description, String idType,
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
		auditUtil.saveAuditDetails(auditRequestDto);
	}
	

}
