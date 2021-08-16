package io.mosip.preregistration.batchjob.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.batchjob.exception.util.BatchServiceExceptionCatcher;
import io.mosip.preregistration.batchjob.repository.ApplicationRepository;
import io.mosip.preregistration.batchjob.repository.utils.BatchJpaRepositoryImpl;
import io.mosip.preregistration.core.code.AuditLogVariables;
import io.mosip.preregistration.core.code.EventId;
import io.mosip.preregistration.core.code.EventName;
import io.mosip.preregistration.core.code.EventType;
import io.mosip.preregistration.core.code.StatusCodes;
import io.mosip.preregistration.core.common.dto.AuditRequestDto;
import io.mosip.preregistration.core.common.entity.ApplicationEntity;
import io.mosip.preregistration.core.common.entity.RegistrationBookingEntity;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.util.AuditLogUtil;
import io.mosip.preregistration.core.util.AuthTokenUtil;

@Component
public class UpdateApplicationsForBookingUtil {

	@Autowired
	private ApplicationRepository applicationRepository;

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

	private Logger log = LoggerConfiguration.logConfig(UpdateApplicationsForBookingUtil.class);

	public boolean UpdateBookingInfoInApplication() {

		log.info("In update Booking Info in applications util");

		boolean isSaveSuccess = false;

		HttpHeaders headers = null;

		try {

			headers = tokenUtil.getTokenHeader();

			List<RegistrationBookingEntity> registrationEntity = batchServiceDAO
					.getAllRegistrationAppointmentDetails(LocalDate.now());

			registrationEntity.stream().forEach(regEntity -> {
				log.info("Fetching applications for APPLICATION-ID: {}", regEntity.getPreregistrationId());
				ApplicationEntity applicationEntity = applicationRepository
						.findByApplicationId(regEntity.getPreregistrationId());

				if (!(Objects.nonNull(applicationEntity.getAppointmentDate())
						&& applicationEntity.getAppointmentDate().isEqual(regEntity.getRegDate())
						&& applicationEntity.getSlotFromTime().equals(regEntity.getSlotFromTime())
						&& applicationEntity.getSlotToTime().equals(regEntity.getSlotToTime()))) {
					log.info("Mismatch in appointment details for APPLICATION-ID: {}",
							regEntity.getPreregistrationId());
					applicationEntity.setBookingDate(regEntity.getUpdDate().toLocalDate());
					applicationEntity.setRegistrationCenterId(regEntity.getRegistrationCenterId());
					applicationEntity.setSlotFromTime(regEntity.getSlotFromTime());
					applicationEntity.setSlotToTime(regEntity.getSlotToTime());
					applicationEntity.setAppointmentDate(regEntity.getRegDate());
					applicationEntity.setBookingStatusCode(StatusCodes.BOOKED.getCode());
					applicationEntity.setUpdBy("PRERIGISTRATION_JOB");
					applicationEntity.setUpdDtime(LocalDateTime.now());
					log.info("updating appointment details for  APPLICATION-ID: {}", regEntity.getPreregistrationId());
					applicationRepository.save(applicationEntity);
				}
			});
			isSaveSuccess = true;
			return true;
		} catch (Exception ex) {
			log.error("ERROR OCCURED", ex.getCause().getLocalizedMessage());
			new BatchServiceExceptionCatcher().handle(ex);
		} finally {
			if (isSaveSuccess) {
				setAuditValues(EventId.PRE_413.toString(), EventName.UPDATE.toString(), EventType.BUSINESS.toString(),
						"Updated the applications table with appointment details in the database",
						AuditLogVariables.PRE_REGISTRATION_ID.toString(), auditUserId, auditUsername, null, headers);
			} else {
				setAuditValues(EventId.PRE_405.toString(), EventName.EXCEPTION.toString(), EventType.SYSTEM.toString(),
						"Failed to update the applications table with appointment details",
						AuditLogVariables.NO_ID.toString(), auditUserId, auditUsername, null, headers);
			}
		}
		return isSaveSuccess;

	}

	public void setAuditValues(String eventId, String eventName, String eventType, String description, String idType,
			String userId, String userName, String ref_id, HttpHeaders headers) {
		AuditRequestDto auditRequestDto = new AuditRequestDto();
		auditRequestDto.setEventId(eventId);
		auditRequestDto.setEventName(eventName);
		auditRequestDto.setEventType(eventType);
		auditRequestDto.setDescription(description);
		auditRequestDto.setId(idType);
		auditRequestDto.setSessionUserId(userId);
		auditRequestDto.setSessionUserName(userName);
		auditRequestDto.setModuleId(AuditLogVariables.PREREGISTRATION.toString());
		auditRequestDto.setModuleName(AuditLogVariables.PREREGISTRATION.toString());
		auditRequestDto.setId(ref_id);
		auditLogUtil.saveAuditDetails(auditRequestDto, headers);
	}

}
