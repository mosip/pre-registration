package io.mosip.preregistration.datasync.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.analytics.event.anonymous.exception.AnonymousProfileException;
import io.mosip.analytics.event.anonymous.util.AnonymousProfileUtil;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.core.code.AuditLogVariables;
import io.mosip.preregistration.core.code.EventId;
import io.mosip.preregistration.core.code.EventName;
import io.mosip.preregistration.core.code.EventType;
import io.mosip.preregistration.core.code.StatusCodes;
import io.mosip.preregistration.core.common.dto.AuditRequestDto;
import io.mosip.preregistration.core.common.dto.BookingDataByRegIdDto;
import io.mosip.preregistration.core.common.dto.BookingRegistrationDTO;
import io.mosip.preregistration.core.common.dto.DemographicResponseDTO;
import io.mosip.preregistration.core.common.dto.DocumentsMetaData;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.dto.SlotTimeDto;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.util.AuditLogUtil;
import io.mosip.preregistration.core.util.ValidationUtil;
import io.mosip.preregistration.datasync.dto.ApplicationInfoMetadataDTO;
import io.mosip.preregistration.datasync.dto.DataSyncRequestDTO;
import io.mosip.preregistration.datasync.dto.PreRegArchiveDTO;
import io.mosip.preregistration.datasync.dto.PreRegistrationIdsDTO;
import io.mosip.preregistration.datasync.dto.ReverseDataSyncRequestDTO;
import io.mosip.preregistration.datasync.dto.ReverseDatasyncReponseDTO;
import io.mosip.preregistration.datasync.exception.util.DataSyncExceptionCatcher;
import io.mosip.preregistration.datasync.service.util.DataSyncServiceUtil;

/**
 * This class provides different service to perform operation for datasync
 * 
 * @version 1.0.0
 * 
 * @author Jagadishwari
 * @author Tapaswini Behera
 * @author Sanober Noor
 *
 */
@Service
public class DataSyncService {

	private static final String UTC_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS";

	/**
	 * Autowired reference for {@link #DataSyncServiceUtil}
	 */
	@Autowired
	private DataSyncServiceUtil serviceUtil;

	@Autowired
	private ValidationUtil validationUtil;

	/**
	 * Autowired reference for {@link #AuditLogUtil}
	 */
	@Autowired
	AuditLogUtil auditLogUtil;
	
	/**
	 * Autowired reference for {@link #AnonymousProfileUtil}
	 */
	@Autowired
	AnonymousProfileUtil anonymousProfileUtil;
	
	/**
	 * This method acts as a post constructor to initialize the required request
	 * parameters.
	 */
	@PostConstruct
	public void setup() {
		requiredRequestMap.put("version", version);
	}

	/**
	 * Reference for ${mosip.id.preregistration.datasync.fetch.ids} from property
	 * file
	 */
	@Value("${mosip.id.preregistration.datasync.fetch.ids}")
	private String fetchAllId;

	/**
	 * Reference for ${mosip.id.preregistration.datasync.store} from property file
	 */
	@Value("${mosip.id.preregistration.datasync.store}")
	private String storeId;

	/**
	 * Reference for ${mosip.id.preregistration.datasync.fetch} from property file
	 */
	@Value("${mosip.id.preregistration.datasync.fetch}")
	private String fetchId;

	/**
	 * Reference for ${ver} from property file
	 */
	@Value("${version}")
	private String version;

	/**
	 * Request map to store the id and version and this is to be passed to request
	 * validator method.
	 */
	Map<String, String> requiredRequestMap = new HashMap<>();

	private Logger log = LoggerConfiguration.logConfig(DataSyncService.class);

	public AuthUserDetails authUserDetails() {
		return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	/**
	 * This method is use to retrieve all the preRegistratioId
	 * 
	 * @param dataSyncRequest
	 * @return list of preRgistrationDto
	 */
	public MainResponseDTO<PreRegistrationIdsDTO> retrieveAllPreRegIds(
			MainRequestDTO<DataSyncRequestDTO> dataSyncRequest) {
		PreRegistrationIdsDTO preRegistrationIdsDTO = null;
		MainResponseDTO<PreRegistrationIdsDTO> responseDto = new MainResponseDTO<>();
		log.info("sessionId", "idType", "id", "In retrieveAllPreRegIds method of datasync service ");
		boolean isRetrieveAllSuccess = false;
		responseDto.setId(fetchAllId);
		responseDto.setVersion(version);
		requiredRequestMap.put("id", fetchAllId);
		try {
			validationUtil.requestValidator(dataSyncRequest);
			if (validationUtil.requestValidator(serviceUtil.prepareRequestMap(dataSyncRequest), requiredRequestMap)) {
				serviceUtil.validateDataSyncRequest(dataSyncRequest.getRequest(), responseDto);
				DataSyncRequestDTO dataSyncRequestDTO = dataSyncRequest.getRequest();
				if (serviceUtil.isNull(dataSyncRequestDTO.getToDate())) {
					dataSyncRequestDTO.setToDate(dataSyncRequestDTO.getFromDate());
				}
				BookingDataByRegIdDto preRegIdsDTO = serviceUtil.getBookedPreIdsByDateAndRegCenterIdRestService(
						dataSyncRequestDTO.getFromDate(), dataSyncRequestDTO.getToDate(),
						dataSyncRequestDTO.getRegistrationCenterId());
				preRegistrationIdsDTO = new PreRegistrationIdsDTO();
				preRegistrationIdsDTO.setPreRegistrationIds(getIdsWithTime(preRegIdsDTO));
				preRegistrationIdsDTO
						.setCountOfPreRegIds(String.valueOf(preRegIdsDTO.getIdsWithAppointmentDate().size()));
				responseDto.setResponsetime(serviceUtil.getCurrentResponseTime());
				responseDto.setResponse(preRegistrationIdsDTO);
			}
			isRetrieveAllSuccess = true;
		} catch (

		Exception ex) {
			log.debug("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id",
					"In retrieveAllPreRegIds method of datasync service - " + ex.getMessage());
			new DataSyncExceptionCatcher().handle(ex, responseDto);
		} finally {
			if (isRetrieveAllSuccess) {
				setAuditValues(EventId.PRE_406.toString(), EventName.SYNC.toString(), EventType.BUSINESS.toString(),
						"Retrieval of all the Preregistration Id is successful",
						AuditLogVariables.MULTIPLE_ID.toString(), authUserDetails().getUserId(),
						authUserDetails().getUsername(), dataSyncRequest.getRequest().getRegistrationCenterId());
			} else {
				setAuditValues(EventId.PRE_405.toString(), EventName.EXCEPTION.toString(), EventType.SYSTEM.toString(),
						"Retrieval of all the Preregistration Id is unsuccessful", AuditLogVariables.NO_ID.toString(),
						authUserDetails().getUserId(), authUserDetails().getUsername(),
						dataSyncRequest.getRequest().getRegistrationCenterId());
			}
		}
		return responseDto;
	}

	/**
	 * 
	 * @param preRegIdsDTO
	 * @return
	 */
	private Map<String, String> getIdsWithTime(BookingDataByRegIdDto preRegIdsDTO) {
		Map<String, String> idWithTime = new HashMap<String, String>();
		for (Entry<String, Map<LocalDate, SlotTimeDto>> preRegWithTime : preRegIdsDTO.getIdsWithAppointmentDate()
				.entrySet()) {
			idWithTime.put(preRegWithTime.getKey(), getUTCTimeStamp(preRegWithTime.getValue()));
		}
		return idWithTime;
	}

	/**
	 * Assuming one pre-reg id will have one appointTime and single time slot
	 * 
	 * @param value
	 * @return
	 */
	private String getUTCTimeStamp(Map<LocalDate, SlotTimeDto> value) {
		String timestamp = null;
		for (Entry<LocalDate, SlotTimeDto> v : value.entrySet()) {
			timestamp = v.getKey().atTime(v.getValue().getFromTime()).format(DateTimeFormatter.ofPattern(UTC_DATETIME_PATTERN));
		}
		return timestamp;
	}

	/**
	 * This method use to get all the details for an individual preRegistrationId
	 * 
	 * @param preId
	 * @return PreRegArchiveDTO contain all Zipped File
	 */
	public MainResponseDTO<PreRegArchiveDTO> getPreRegistrationData(String preId) {
		MainResponseDTO<PreRegArchiveDTO> responseDto = new MainResponseDTO<>();
		PreRegArchiveDTO preRegArchiveDTO = null;
		log.info("sessionId", "idType", "id", "In getPreRegistrationData method of datasync service ");
		boolean isRetrieveSuccess = false;
		responseDto.setId(fetchId);
		responseDto.setVersion(version);
		try {
			DemographicResponseDTO preRegistrationDTO = serviceUtil.getPreRegistrationData(preId.trim());
			DocumentsMetaData documentsMetaData = serviceUtil.getDocDetails(preId.trim());
			BookingRegistrationDTO bookingRegistrationDTO = serviceUtil.getAppointmentDetails(preId.trim());
			preRegArchiveDTO = serviceUtil.archivingFiles(preRegistrationDTO, bookingRegistrationDTO, documentsMetaData,
					null);
			responseDto.setResponsetime(serviceUtil.getCurrentResponseTime());
			responseDto.setResponse(preRegArchiveDTO);
			isRetrieveSuccess = true;
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id",
					"In getPreRegistrationData method of datasync service - " + ex.getMessage());
			new DataSyncExceptionCatcher().handle(ex, responseDto);
		} finally {
			if (isRetrieveSuccess) {
				setAuditValues(EventId.PRE_406.toString(), EventName.SYNC.toString(), EventType.BUSINESS.toString(),
						"Retrieval of the Preregistration data is successful", AuditLogVariables.MULTIPLE_ID.toString(),
						authUserDetails().getUserId(), authUserDetails().getUsername(), null);
			} else {
				setAuditValues(EventId.PRE_405.toString(), EventName.EXCEPTION.toString(), EventType.SYSTEM.toString(),
						"Retrieval of the Preregistration data is unsuccessful", AuditLogVariables.NO_ID.toString(),
						authUserDetails().getUserId(), authUserDetails().getUsername(), null);
			}
		}
		return responseDto;
	}

	/**
	 * This method use to get all the details for an individual preRegistrationId
	 * 
	 * @param preId
	 * @return PreRegArchiveDTO contain all Zipped File
	 */
	@SuppressWarnings("unlikely-arg-type")
	public MainResponseDTO<PreRegArchiveDTO> fetchPreRegistrationData(String preId, String machineId) {
		MainResponseDTO<PreRegArchiveDTO> responseDto = new MainResponseDTO<>();
		PreRegArchiveDTO preRegArchiveDTO = null;
		log.info("sessionId", "idType", "id", "In fetchPreRegistrationData method of datasync service ");
		boolean isRetrieveSuccess = false;
		responseDto.setId(fetchId);
		responseDto.setVersion(version);
		try {
			ApplicationInfoMetadataDTO preRegInfo = serviceUtil.getPreRegistrationInfo(preId.trim());
			DemographicResponseDTO preRegistrationDTO = preRegInfo.getDemographicResponse();
			DocumentsMetaData documentsMetaData = preRegInfo.getDocumentsMetaData();
			BookingRegistrationDTO bookingRegistrationDTO = null;
			if (preRegistrationDTO.getStatusCode().equals(StatusCodes.BOOKED.getCode())
					|| preRegistrationDTO.getStatusCode().equals(StatusCodes.EXPIRED.getCode())) {
				bookingRegistrationDTO = serviceUtil.getAppointmentDetails(preId.trim());
			}
			preRegArchiveDTO = serviceUtil.archivingFiles(preRegistrationDTO, bookingRegistrationDTO, documentsMetaData,
					machineId);
			responseDto.setResponsetime(serviceUtil.getCurrentResponseTime());
			responseDto.setResponse(preRegArchiveDTO);
			isRetrieveSuccess = true;
			// insert the anonymous profile only if the appointment is being prefetched for the first time
			if (!preRegistrationDTO.getStatusCode().equals(StatusCodes.BOOKED.getCode())
					&& !preRegistrationDTO.getStatusCode().equals(StatusCodes.PREFETCHED.getCode())
					&& !preRegistrationDTO.getStatusCode().equals(StatusCodes.EXPIRED.getCode())
					&& !preRegistrationDTO.getStatusCode().equals(StatusCodes.CANCELLED.getCode())) {
				preRegistrationDTO.setStatusCode(StatusCodes.PREFETCHED.getCode());
				anonymousProfileUtil.saveAnonymousProfile(preRegistrationDTO, documentsMetaData, bookingRegistrationDTO,
						null);
				// update status to prefetched
				serviceUtil.updateApplicationStatusToPreFectched(preId);
			}
		} catch (AnonymousProfileException apex) {
			log.debug("sessionId", "idType", "id" + ExceptionUtils.getStackTrace(apex));
			log.error("Unable to save AnonymousProfile in getPreRegistrationData method of datasync service -" + apex.getMessage());
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id" + ExceptionUtils.getStackTrace(ex));
			log.error("In getPreRegistrationData method of datasync service -" + ex.getMessage());
			new DataSyncExceptionCatcher().handle(ex, responseDto);
		} finally {
			if (isRetrieveSuccess) {
				setAuditValues(EventId.PRE_406.toString(), EventName.SYNC.toString(), EventType.BUSINESS.toString(),
						"Retrieval of the Preregistration data is successful", AuditLogVariables.MULTIPLE_ID.toString(),
						authUserDetails().getUserId(), authUserDetails().getUsername(), null);
			} else {
				setAuditValues(EventId.PRE_405.toString(), EventName.EXCEPTION.toString(), EventType.SYSTEM.toString(),
						"Retrieval of the Preregistration data is unsuccessful", AuditLogVariables.NO_ID.toString(),
						authUserDetails().getUserId(), authUserDetails().getUsername(), null);
			}
		}
		return responseDto;
	}

	/**
	 * This method is use to store all the consumed preRegistrationId and store it
	 * in the database
	 * 
	 * @param reverseDto
	 * @return responseDTO
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public MainResponseDTO<ReverseDatasyncReponseDTO> storeConsumedPreRegistrations(
			MainRequestDTO<ReverseDataSyncRequestDTO> reverseDataSyncRequest) {
		MainResponseDTO<ReverseDatasyncReponseDTO> responseDto = new MainResponseDTO<>();
		ReverseDatasyncReponseDTO reverseDatasyncReponse = null;
		log.info("sessionId", "idType", "id", "In storeConsumedPreRegistrations method of datasync service ");
		boolean isSaveSuccess = false;
		responseDto.setId(storeId);
		responseDto.setVersion(version);
		requiredRequestMap.put("id", storeId);
		try {
			if (validationUtil.requestValidator(reverseDataSyncRequest)
					&& serviceUtil.validateReverseDataSyncRequest(reverseDataSyncRequest.getRequest(), responseDto)) {
				if (validationUtil.requestValidator(serviceUtil.prepareRequestMap(reverseDataSyncRequest),
						requiredRequestMap)) {
					reverseDatasyncReponse = serviceUtil.reverseDateSyncSave(reverseDataSyncRequest.getRequesttime(),
							reverseDataSyncRequest.getRequest(), "user");
					responseDto.setResponse(reverseDatasyncReponse);
					responseDto.setResponsetime(serviceUtil.getCurrentResponseTime());
					responseDto.setErrors(null);
				}
			}
			isSaveSuccess = true;
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id",
					"In storeConsumedPreRegistrations method of datasync service - " + ex.getMessage());

			new DataSyncExceptionCatcher().handle(ex, responseDto);
		} finally {
			if (isSaveSuccess) {
				setAuditValues(EventId.PRE_408.toString(), EventName.REVERSESYNC.toString(),
						EventType.BUSINESS.toString(),
						"Reverse Data sync & the consumed PreRegistration ids successfully saved in the database",
						AuditLogVariables.MULTIPLE_ID.toString(), authUserDetails().getUserId(),
						authUserDetails().getUsername(), null);
			} else {
				setAuditValues(EventId.PRE_405.toString(), EventName.EXCEPTION.toString(), EventType.SYSTEM.toString(),
						"Reverse Data sync failed", AuditLogVariables.NO_ID.toString(), authUserDetails().getUserId(),
						authUserDetails().getUsername(), null);
			}
		}
		return responseDto;
	}

	/**
	 * This method is used to audit all the datasync & reverse datasync events
	 * 
	 * @param eventId
	 * @param eventName
	 * @param eventType
	 * @param description
	 * @param idType
	 */
	public void setAuditValues(String eventId, String eventName, String eventType, String description, String idType,
			String userId, String userName, String refId) {
		AuditRequestDto auditRequestDto = new AuditRequestDto();
		auditRequestDto.setEventId(eventId);
		auditRequestDto.setEventName(eventName);
		auditRequestDto.setEventType(eventType);
		auditRequestDto.setSessionUserId(userId);
		auditRequestDto.setSessionUserName(userName);
		auditRequestDto.setDescription(description);
		auditRequestDto.setIdType(idType);
		auditRequestDto.setId(refId);
		if (!eventName.equalsIgnoreCase("REVERSESYNC")) {
			auditRequestDto.setModuleId(AuditLogVariables.DAT.toString());
			auditRequestDto.setModuleName(AuditLogVariables.DATASYNC_SERVICE.toString());
			auditRequestDto.setModuleId(AuditLogVariables.REV.toString());
			auditRequestDto.setModuleName(AuditLogVariables.REVERSE_DATASYNC_SERVICE.toString());
		}
		auditLogUtil.saveAuditDetails(auditRequestDto);
	}

}
