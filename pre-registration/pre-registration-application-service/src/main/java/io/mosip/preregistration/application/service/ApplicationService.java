package io.mosip.preregistration.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.HMACUtils2;
import io.mosip.preregistration.application.code.RequestCodes;
import io.mosip.preregistration.application.dto.ApplicationDetailResponseDTO;
import io.mosip.preregistration.application.dto.ApplicationRequestDTO;
import io.mosip.preregistration.application.dto.ApplicationResponseDTO;
import io.mosip.preregistration.application.dto.ApplicationsListDTO;
import io.mosip.preregistration.application.dto.DeleteApplicationDTO;
import io.mosip.preregistration.application.dto.UIAuditRequest;
import io.mosip.preregistration.application.errorcodes.ApplicationErrorCodes;
import io.mosip.preregistration.application.errorcodes.ApplicationErrorMessages;
import io.mosip.preregistration.application.exception.AuditFailedException;
import io.mosip.preregistration.application.exception.BookingDeletionFailedException;
import io.mosip.preregistration.application.exception.DemographicServiceException;
import io.mosip.preregistration.application.exception.InvalidDateFormatException;
import io.mosip.preregistration.application.exception.RecordNotFoundException;
import io.mosip.preregistration.application.exception.util.DemographicExceptionCatcher;
import io.mosip.preregistration.application.repository.ApplicationRepostiory;
import io.mosip.preregistration.application.service.util.DemographicServiceUtil;
import io.mosip.preregistration.core.code.ApplicationStatusCode;
import io.mosip.preregistration.core.code.AuditLogVariables;
import io.mosip.preregistration.core.code.BookingTypeCodes;
import io.mosip.preregistration.core.code.EventId;
import io.mosip.preregistration.core.code.EventName;
import io.mosip.preregistration.core.code.EventType;
import io.mosip.preregistration.core.code.StatusCodes;
import io.mosip.preregistration.core.common.dto.AuditRequestDto;
import io.mosip.preregistration.core.common.dto.DeleteBookingDTO;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.entity.ApplicationEntity;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.exception.InvalidPreRegistrationIdException;
import io.mosip.preregistration.core.exception.InvalidRequestParameterException;
import io.mosip.preregistration.core.exception.PreIdInvalidForUserIdException;
import io.mosip.preregistration.core.util.AuditLogUtil;
import io.mosip.preregistration.core.util.ValidationUtil;

@Service
public class ApplicationService implements ApplicationServiceIntf {

	@Value("${version}")
	private String version;

	@Autowired
	ApplicationRepostiory applicationRepository;

	@Autowired
	AuditLogUtil auditUtil;

	@Autowired
	ValidationUtil validationUtil;

	/**
	 * ObjectMapper global object creation
	 */
	private ObjectMapper mapper;
	
	/**
	 * Reference for ${lostUinDeleteId} from property file
	 */
	@Value("${mosip.id.preregistration.lostuin.delete}")
	private String lostUinDeleteId;

	/**
	 * Reference for ${updateRegistrationDeleteId} from property file
	 */
	@Value("${mosip.id.preregistration.updateregistration.delete}")
	private String updateRegistrationDeleteId;
	
	/**
	 * Reference for ${miscellaneousPurposeDeleteId} from property file
	 */
	@Value("${mosip.id.preregistration.miscellaneouspurpose.delete}")
	private String miscellaneousPurposeDeleteId;

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

	@Value("${mosip.preregistration.applications.all.get}")
	private String allApplicationsId;
	/**
	 * logger instance
	 */
	private Logger log = LoggerConfiguration.logConfig(ApplicationService.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.preregistration.application.service.ApplicationServiceIntf#
	 * authUserDetails()
	 */
	@Override
	public AuthUserDetails authUserDetails() {
		return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	public MainResponseDTO<String> saveUIEventAudit(UIAuditRequest auditRequest) {
		log.info("In saveUIEventAudit method");
		MainResponseDTO<String> response = new MainResponseDTO<String>();
		response.setResponsetime(DateTimeFormatter.ofPattern(mosipDateTimeFormat).format(LocalDateTime.now()));
		response.setVersion(version);
		try {
			String description = auditRequest.getDescription();
			mapper = JsonMapper.builder().addModule(new AfterburnerModule()).build();
			mapper.registerModule(new JavaTimeModule());
			JsonNode node = mapper.readTree(description);
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

	/**
	 * Gives application details for the given applicationId
	 * 
	 * @param applicationId
	 * @return
	 */
	public MainResponseDTO<ApplicationEntity> getApplicationInfo(String applicationId) {
		MainResponseDTO<ApplicationEntity> response = new MainResponseDTO<ApplicationEntity>();
		response.setId(applicationDetailsId);
		response.setVersion(version);
		response.setResponsetime(DateTimeFormatter.ofPattern(mosipDateTimeFormat).format(LocalDateTime.now()));
		ApplicationEntity applicationEntity = null;
		try {
			if (applicationId == null) {
				throw new InvalidRequestParameterException(ApplicationErrorCodes.PRG_APP_014.getCode(),
						ApplicationErrorMessages.INVALID_REQUEST_APPLICATION_ID.getMessage(), response);
			}
			applicationEntity = applicationRepository.findByApplicationId(applicationId);
			if (applicationEntity == null) {
				throw new RecordNotFoundException(ApplicationErrorCodes.PRG_APP_013.getCode(),
						ApplicationErrorMessages.NO_RECORD_FOUND.getMessage());
			}
			userValidation(applicationEntity);
			log.info("Application Info: {} for the Application Id: {}", applicationEntity, applicationId);
			response.setResponse(applicationEntity);
		} catch (Exception ex) {
			log.error("Error while Getting the Application Info for applicationId ", applicationId);
			log.error("Exception trace", ex);
			new DemographicExceptionCatcher().handle(ex, response);
		}
		return response;
	}

	/**
	 * Get all bookings for the given regCenterId in the given appointmentDate
	 * 
	 * @param regCenterId
	 * @param appointmentFromDate
	 * @param appointmentToDate
	 * @return
	 */
	public MainResponseDTO<List<ApplicationDetailResponseDTO>> getBookingsForRegCenter(String regCenterId,
			String appointmentFromDate, String appointmentToDate) {
		MainResponseDTO<List<ApplicationDetailResponseDTO>> mainResponse = new MainResponseDTO<>();
		mainResponse.setId(applicationDetailsId);
		mainResponse.setVersion(version);
		mainResponse.setResponsetime(DateTimeFormatter.ofPattern(mosipDateTimeFormat).format(LocalDateTime.now()));
		List<ApplicationDetailResponseDTO> responseList = new ArrayList<>();
		try {
			LocalDate appFromDate = LocalDate.parse(appointmentFromDate);
			LocalDate appToDate = null; 
			if (appointmentToDate != null && !"".equals(appointmentToDate.trim())) {
				appToDate = LocalDate.parse(appointmentToDate);
			}
			if (appToDate == null) {
				appToDate = appFromDate;
			}
			List<ApplicationEntity> entity = applicationRepository
					.findByRegistrationCenterIdAndBetweenDate(regCenterId, appFromDate, appToDate);
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
					appointmentFromDate);
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

	/**
	 * This method is used to create the a new application with booking type as
	 * UPDATE_REGISTRATION or LOST_FORGOTTEN_UIN or MISCELLANEOUS_PURPOSE
	 * 
	 * @param request
	 * @param bookingType
	 * @return MainResponseDTO<ApplicationResponseDTO>
	 */
	@SuppressWarnings({ "unchecked" })
	@Override
	public MainResponseDTO<ApplicationResponseDTO> addLostOrUpdateOrMiscellaneousApplication(
			MainRequestDTO<? extends ApplicationRequestDTO> request, String bookingType) {
		log.info("sessionId", "idType", "id", "In addLostOrUpdateOrMiscellaneousApplication method of pre-registration service ");
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
					"In pre-registration service of addLostOrUpdateOrMiscellaneousApplication - " + e.getResponseBodyAsString());
			List<ServiceError> errorList = ExceptionUtils.getServiceErrorList(e.getResponseBodyAsString());
			new DemographicExceptionCatcher().handle(new DemographicServiceException(errorList, null), mainResponseDTO);
		} catch (Exception ex) {
			log.error("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id",
					"In pre-registration service of addLostOrUpdateOrMiscellaneousApplication- " + ex.getMessage());
			new DemographicExceptionCatcher().handle(ex, mainResponseDTO);
		} finally {
			if (isSuccess) {
				createAuditValues(EventId.PRE_407.toString(), EventName.PERSIST.toString(),
						EventType.BUSINESS.toString(),
						"Application data is sucessfully saved in the applications table",
						AuditLogVariables.NO_ID.toString(), authUserDetails().getUserId(),
						authUserDetails().getUsername());
			} else {
				createAuditValues(EventId.PRE_405.toString(), EventName.EXCEPTION.toString(),
						EventType.SYSTEM.toString(), "Failed to save the Application data",
						AuditLogVariables.NO_ID.toString(), authUserDetails().getUserId(),
						authUserDetails().getUsername());
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

	/**
	 * This method is used to delete the application with booking type as
	 * UPDATE_REGISTRATION or LOST_FORGOTTEN_UIN or MISCELLANEOUS_PURPOSE
	 * 
	 * @param applicationId
	 * @param bookingType   UPDATE_REGISTRATION or LOST_FORGOTTEN_UIN or MISCELLANEOUS_PURPOSE
	 * @return MainResponseDTO<DeleteApplicationDTO>
	 */
	@Override
	public MainResponseDTO<DeleteApplicationDTO> deleteLostOrUpdateOrMiscellaneousApplication(String applicationId,
			String bookingType) {
		log.info("sessionId", "idType", "id", "In deleteLostOrUpdateOrMiscellaneousApplication method of pre-registration service ");
		MainResponseDTO<DeleteApplicationDTO> response = new MainResponseDTO<>();
		DeleteApplicationDTO deleteDto = new DeleteApplicationDTO();
		Map<String, String> requestParamMap = new HashMap<>();
		boolean isDeleteSuccess = false;
		if (bookingType.equals(BookingTypeCodes.LOST_FORGOTTEN_UIN.toString())) {
			response.setId(lostUinDeleteId);
		}
		if (bookingType.equals(BookingTypeCodes.UPDATE_REGISTRATION.toString())) {
			response.setId(updateRegistrationDeleteId);
		}
		if (bookingType.equals(BookingTypeCodes.MISCELLANEOUS_PURPOSE.toString())) {
			response.setId(miscellaneousPurposeDeleteId);
		}
		response.setVersion(version);
		try {
			requestParamMap.put(RequestCodes.APPLICATION_ID.getCode(), applicationId);
			if (validationUtil.requstParamValidator(requestParamMap)) {
				ApplicationEntity applicationEntity = serviceUtil.findApplicationById(applicationId);
				if (bookingType.equals(BookingTypeCodes.LOST_FORGOTTEN_UIN.toString())
						|| bookingType.equals(BookingTypeCodes.UPDATE_REGISTRATION.toString())
						|| bookingType.equals(BookingTypeCodes.MISCELLANEOUS_PURPOSE.toString())) {
					//userValidation(applicationEntity);
					if (!authUserDetails().getUserId().trim().equals(applicationEntity.getCrBy().trim())) {
						throw new PreIdInvalidForUserIdException(ApplicationErrorCodes.PRG_APP_015.getCode(),
								ApplicationErrorMessages.INVALID_APPLICATION_ID_FOR_USER.getMessage());
					}	
					if ((applicationEntity.getBookingStatusCode().equals(StatusCodes.BOOKED.getCode()))) {
						MainResponseDTO<DeleteBookingDTO> deleteBooking = null;
						deleteBooking = serviceUtil.deleteBooking(applicationId);
						if (deleteBooking.getErrors() != null) {
							throw new BookingDeletionFailedException(deleteBooking.getErrors().get(0).getErrorCode(),
									deleteBooking.getErrors().get(0).getMessage());
						}
					}
					serviceUtil.deleteApplicationFromApplications(applicationId);
					deleteDto.setPreRegistrationId(applicationId);
					deleteDto.setDeletedBy(authUserDetails().getUserId());
					deleteDto.setDeletedDateTime(new Date(System.currentTimeMillis()));
				}
			} else {
				throw new InvalidPreRegistrationIdException(ApplicationErrorCodes.PRG_APP_016.getCode(),
						ApplicationErrorMessages.INVALID_BOOKING_TYPE.getMessage());
			}
			isDeleteSuccess = true;
		} catch (Exception ex) {
			log.error("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id",
					"In pre-registration deleteLostOrUpdateOrMiscellaneousApplication service- " + ex.getMessage());
			new DemographicExceptionCatcher().handle(ex, response);
		} finally {
			response.setResponsetime(serviceUtil.getCurrentResponseTime());
			if (isDeleteSuccess) {
				createAuditValues(EventId.PRE_403.toString(), EventName.DELETE.toString(),
						EventType.BUSINESS.toString(),
						"Application data is successfully deleted from applications table",
						AuditLogVariables.NO_ID.toString(), authUserDetails().getUserId(),
						authUserDetails().getUsername());
			} else {
				createAuditValues(EventId.PRE_405.toString(), EventName.EXCEPTION.toString(),
						EventType.SYSTEM.toString(), "Deletion of Application data failed",
						AuditLogVariables.NO_ID.toString(), authUserDetails().getUserId(),
						authUserDetails().getUsername());
			}
		}

		response.setResponse(deleteDto);
		return response;
	}

	/**
	 * Gives all the application details for the logged in user.
	 * 
	 * @return
	 */
	@Override
	public MainResponseDTO<ApplicationsListDTO> getAllApplicationsForUser() {
		String userId = authUserDetails().getUserId();
		MainResponseDTO<ApplicationsListDTO> response = new MainResponseDTO<ApplicationsListDTO>();
		ApplicationsListDTO applicationsListDTO = new ApplicationsListDTO();
		response.setId(allApplicationsId);
		response.setVersion(version);
		response.setResponsetime(DateTimeFormatter.ofPattern(mosipDateTimeFormat).format(LocalDateTime.now()));
		try {
			List<ApplicationEntity> applicationEntities = applicationRepository.findByCreatedBy(userId);
			log.info("Number of applications found for the current user: {}", applicationEntities.size());
			applicationsListDTO.setAllApplications(applicationEntities);
			response.setResponse(applicationsListDTO);
		} catch (Exception ex) {
			log.error("Error while Getting the Applications for the userId: {} ", userId);
			log.error("Exception trace", ex);
			new DemographicExceptionCatcher().handle(ex, response);
		}
		return response;
	}
	
	/**
	 * This Method is used to fetch status of particular application
	 * 
	 *  @param applicationId
	 * @return response status of the application
	 */
	@Override
	public MainResponseDTO<String> getApplicationStatus(String applicationId) {
		MainResponseDTO<String> response = new MainResponseDTO<String>();
		response.setId(applicationStatusId);
		response.setVersion(version);
		response.setResponsetime(DateTimeFormatter.ofPattern(mosipDateTimeFormat).format(LocalDateTime.now()));
		ApplicationEntity applicationEntity = null;
		String applicationBookingStatus = null;
		try {
			if (applicationId == null) {
				throw new InvalidRequestParameterException(ApplicationErrorCodes.PRG_APP_014.getCode(),
						ApplicationErrorMessages.INVALID_REQUEST_APPLICATION_ID.getMessage(), response);
			}
			applicationEntity = applicationRepository.findByApplicationId(applicationId);
			if (applicationEntity == null) {
				throw new RecordNotFoundException(ApplicationErrorCodes.PRG_APP_013.getCode(),
						ApplicationErrorMessages.NO_RECORD_FOUND.getMessage());
			}
			userValidation(applicationEntity);
			applicationBookingStatus= applicationEntity.getBookingStatusCode();
			log.info("Application STATUS : {} for the Application Id: {}", applicationBookingStatus, applicationId);
			response.setResponse(applicationBookingStatus);
		} catch (Exception ex) {
			log.error("Error while Getting the Application Info for applicationId ", applicationId);
			log.error("Exception trace", ex);
			new DemographicExceptionCatcher().handle(ex, response);
		}
		return response;
	}

	private void userValidation(ApplicationEntity applicationEntity) {
		String authUserId = authUserDetails().getUserId();
		List<String> list = listAuth(authUserDetails().getAuthorities());
		if (list.contains("ROLE_INDIVIDUAL")) {
			log.info("sessionId", "idType", "id", "In userValidation method of ApplicationService with applicationId "
					+ applicationEntity.getApplicationId() + " and userID " + authUserId);
			if (!authUserDetails().getUserId().trim().equals(applicationEntity.getCrBy().trim())) {
				throw new PreIdInvalidForUserIdException(ApplicationErrorCodes.PRG_APP_015.getCode(),
						ApplicationErrorMessages.INVALID_APPLICATION_ID_FOR_USER.getMessage());
			}	
		}	
		
	}

	/**
	 * This method is used to get the list of authorization role
	 * 
	 * @param collection
	 * @return list of auth role
	 */
	private List<String> listAuth(Collection<? extends GrantedAuthority> collection) {
		List<String> listWORole = new ArrayList<>();
		for (GrantedAuthority authority : collection) {
			String s = authority.getAuthority();
			listWORole.add(s);
		}
		return listWORole;
	}
	
	/**
	 * Gives all the application details for the logged in user for the given type.
	 * 
	 * @return
	 */
	@Override
	public MainResponseDTO<ApplicationsListDTO> getAllApplicationsForUserForBookingType(String type) {
		String userId = authUserDetails().getUserId();
		MainResponseDTO<ApplicationsListDTO> response = new MainResponseDTO<ApplicationsListDTO>();
		ApplicationsListDTO applicationsListDTO = new ApplicationsListDTO();
		response.setId(allApplicationsId);
		response.setVersion(version);
		response.setResponsetime(DateTimeFormatter.ofPattern(mosipDateTimeFormat).format(LocalDateTime.now()));
		try {
			if (!type.equalsIgnoreCase(BookingTypeCodes.NEW_PREREGISTRATION.toString())
					&& !type.equalsIgnoreCase(BookingTypeCodes.LOST_FORGOTTEN_UIN.toString())
					&& !type.equalsIgnoreCase(BookingTypeCodes.UPDATE_REGISTRATION.toString())
					&& !type.equalsIgnoreCase(BookingTypeCodes.MISCELLANEOUS_PURPOSE.toString())) {
				throw new InvalidPreRegistrationIdException(ApplicationErrorCodes.PRG_APP_016.getCode(),
						ApplicationErrorMessages.INVALID_BOOKING_TYPE.getMessage());

			}
			List<ApplicationEntity> applicationEntities = applicationRepository.findByCreatedByBookingType(userId,
					type.toUpperCase());
			log.info("Number of applications found for the current user: {} and booking type: {}",
					applicationEntities.size(), type);
			applicationsListDTO.setAllApplications(applicationEntities);
			response.setResponse(applicationsListDTO);
		} catch (Exception ex) {
			log.error("Error while Getting the Applications for the userId: {} ", userId);
			log.error("Exception trace", ex);
			new DemographicExceptionCatcher().handle(ex, response);
		}
		return response;
	}
}
