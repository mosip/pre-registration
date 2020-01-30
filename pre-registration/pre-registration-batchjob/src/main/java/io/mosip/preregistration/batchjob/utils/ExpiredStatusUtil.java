package io.mosip.preregistration.batchjob.utils;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.preregistration.batchjob.exception.util.BatchServiceExceptionCatcher;
import io.mosip.preregistration.batchjob.repository.utils.BatchJpaRepositoryImpl;
import io.mosip.preregistration.core.code.AuditLogVariables;
import io.mosip.preregistration.core.code.EventId;
import io.mosip.preregistration.core.code.EventName;
import io.mosip.preregistration.core.code.EventType;
import io.mosip.preregistration.core.code.StatusCodes;
import io.mosip.preregistration.core.common.dto.AuditRequestDto;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.entity.RegistrationBookingEntity;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.util.AuditLogUtil;
import io.mosip.preregistration.core.util.AuthTokenUtil;
import io.mosip.preregistration.core.util.GenericUtil;

/**
 * @author Kishan Rathore
 * @since 1.0.0
 *
 */
@Component
public class ExpiredStatusUtil {

	/** The Constant LOGGER. */
	private Logger log = LoggerConfiguration.logConfig(ExpiredStatusUtil.class);

	@Value("${version}")
	String versionUrl;

	@Value("${mosip.preregistration.batchjob.service.expired.id}")
	String idUrl;

	@Autowired
	private BatchJpaRepositoryImpl batchServiceDAO;

	@Autowired
	AuditLogUtil auditLogUtil;

	@Autowired
	private AuthTokenUtil tokenUtil;
	
	@Value("${mosip.batch.token.authmanager.userName}")
	private String auditUsername;

	@Value("${mosip.batch.token.authmanager.appId}")
	private String auditUserId;

	/**
	 * @return Response dto
	 */

	public MainResponseDTO<String> expireAppointments() {

		HttpHeaders headers = tokenUtil.getTokenHeader();

		MainResponseDTO<String> response = new MainResponseDTO<>();
		response.setId(idUrl);
		response.setVersion(versionUrl);
		boolean isSaveSuccess = false;
		List<RegistrationBookingEntity> bookedPreIdList = null;

		try {
			bookedPreIdList = batchServiceDAO.getAllOldDateBooking();

			bookedPreIdList.forEach(iterate -> {
				String preRegId = iterate.getDemographicEntity().getPreRegistrationId();
				if (iterate.getDemographicEntity() != null) {

					if (iterate.getDemographicEntity().getStatusCode().equals(StatusCodes.BOOKED.getCode())) {
						iterate.getDemographicEntity().setStatusCode(StatusCodes.EXPIRED.getCode());
						iterate.getDemographicEntity().setUpdatedBy(auditUserId);
						iterate.getDemographicEntity().setUpdateDateTime(DateUtils.parseDateToLocalDateTime(new Date()));
						batchServiceDAO.updateApplicantDemographic(iterate.getDemographicEntity());
					}
					log.info("sessionId", "idType", "id",
							"Update the status successfully into Registration Appointment table and Demographic table for Pre-RegistrationId: "
									+ preRegId);
				}
			});
			isSaveSuccess = true;

		} catch (Exception e) {
			new BatchServiceExceptionCatcher().handle(e);
		} finally {
			if (isSaveSuccess) {
				setAuditValues(EventId.PRE_413.toString(), EventName.EXPIREDSTATUS.toString(),
						EventType.BUSINESS.toString(),
						"Updated the expired status & the expired PreRegistration ids successfully saved in the database",
						AuditLogVariables.PRE_REGISTRATION_ID.toString(), auditUserId,
						auditUsername, null, headers);
			} else {
				setAuditValues(EventId.PRE_405.toString(), EventName.EXCEPTION.toString(), EventType.SYSTEM.toString(),
						"Expired status failed to update", AuditLogVariables.NO_ID.toString(),
						auditUserId, auditUsername, null, headers);
			}
		}
		response.setResponsetime(GenericUtil.getCurrentResponseTime());
		response.setResponse("Registration appointment status updated to expired successfully");
		return response;
	}

	/**
	 * This method is used to audit all the consumed status events
	 * 
	 * @param eventId
	 * @param eventName
	 * @param eventType
	 * @param description
	 * @param idType
	 */
	public void setAuditValues(String eventId, String eventName, String eventType, String description, String idType,
			String userId, String userName, String refId, HttpHeaders headers) {
		AuditRequestDto auditRequestDto = new AuditRequestDto();
		auditRequestDto.setEventId(eventId);
		auditRequestDto.setEventName(eventName);
		auditRequestDto.setEventType(eventType);
		auditRequestDto.setSessionUserId(userId);
		auditRequestDto.setSessionUserName(userName);
		auditRequestDto.setDescription(description);
		auditRequestDto.setIdType(idType);
		auditRequestDto.setId(refId);
		auditRequestDto.setModuleId(AuditLogVariables.BAT.toString());
		auditRequestDto.setModuleName(AuditLogVariables.EXPIRED_BATCH_SERVICE.toString());

		auditLogUtil.saveAuditDetails(auditRequestDto, headers);
	}

}
