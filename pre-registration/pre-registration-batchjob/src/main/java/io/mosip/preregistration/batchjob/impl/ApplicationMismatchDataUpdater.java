package io.mosip.preregistration.batchjob.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.batchjob.code.PreRegBatchContants;
import io.mosip.preregistration.batchjob.helper.RestHelper;
import io.mosip.preregistration.batchjob.repository.utils.BatchJpaRepositoryImpl;
import io.mosip.preregistration.core.code.AuditLogVariables;
import io.mosip.preregistration.core.code.EventId;
import io.mosip.preregistration.core.code.EventName;
import io.mosip.preregistration.core.code.EventType;
import io.mosip.preregistration.core.code.StatusCodes;
import io.mosip.preregistration.core.common.entity.ApplicationEntity;
import io.mosip.preregistration.core.common.entity.RegistrationBookingEntity;
import io.mosip.preregistration.core.config.LoggerConfiguration;

/**
 * @author Mahammed Taheer
 * @since 1.2.0
 *
 */
@Component
public class ApplicationMismatchDataUpdater {
    
    private Logger LOGGER = LoggerConfiguration.logConfig(ApplicationExpiredStatusUpdater.class);

    @Value("${mosip.batch.token.authmanager.userName}")
	private String auditUsername;

	@Value("${mosip.batch.token.authmanager.appId}")
	private String auditUserId;

    @Autowired
	private BatchJpaRepositoryImpl batchServiceDAO;

    @Autowired
	private RestHelper restHelper;

    public void updateMismatchData() {

        LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.APPOINTMENT_MISMATCH_JOB, 
		 			"Starting Application Data Mismatch updater Job.");
        
        LocalDate now = LocalDate.now();
        List<RegistrationBookingEntity> regAppointmentDetailsList = batchServiceDAO.getAllRegistrationAppointmentDetails(now);

        if (Objects.isNull(regAppointmentDetailsList) || regAppointmentDetailsList.size() == 0) {
            LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.APPOINTMENT_MISMATCH_JOB, 
                        "No Registration Appointments found to validate the mismatch for the day: " + now.toString());
            restHelper.sendAuditDetails(EventId.PRE_413.toString(), EventName.EXPIREDSTATUS.toString(), EventType.BUSINESS.toString(),
                        "No Registration Appointments found to validate the mismatch for the day.", AuditLogVariables.PRE_REGISTRATION_ID.toString(), 
                        auditUserId, auditUsername, PreRegBatchContants.EMPTY, AuditLogVariables.BOOK.toString(), 
                        AuditLogVariables.EXPIRED_BATCH_SERVICE.toString());
            return;
        }

        LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.APPOINTMENT_MISMATCH_JOB, 
                    "Total Number of application found to update as Expired: " + regAppointmentDetailsList.size());
        List<String> errorredPreRegIds = new ArrayList<>();
        regAppointmentDetailsList.forEach(regAppointment -> {
            String preRegId = regAppointment.getPreregistrationId();
            try {
                LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.APPOINTMENT_MISMATCH_JOB, 
                        "Checking and Updating Mismatch data in Application for Pre Reg Id: " + preRegId);
                ApplicationEntity applicationEntity = batchServiceDAO.getApplicantEntityDetails(preRegId);
                boolean dataMismatch = isAppointmentDataMismatch(regAppointment, applicationEntity);
                if(dataMismatch) {
                    LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.APPOINTMENT_MISMATCH_JOB, 
                        "Mismatch data found for Pre Reg Id: " + preRegId);
                    updateMismatchData(regAppointment, applicationEntity);
                }
            } catch(Exception exp) {
                errorredPreRegIds.add(preRegId);
                LOGGER.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.APPOINTMENT_MISMATCH_JOB, 
                        "Errorred in updating appointment mismatch data for pre reg id: " + preRegId, ExceptionUtils.getStackTrace(exp));
            }
        });

        if (errorredPreRegIds.size() > 0) {
			String preRegIds = String.join(",", errorredPreRegIds);
			restHelper.sendAuditDetails(EventId.PRE_405.toString(), EventName.EXCEPTION.toString(), EventType.SYSTEM.toString(),
						"Updating Mismatch data Failed for Pre Reg id, List of Pre Reg Ids. ", AuditLogVariables.NO_ID.toString(), 
						auditUserId, auditUsername, preRegIds, AuditLogVariables.PREREGISTRATION.toString(), 
                        AuditLogVariables.PREREGISTRATION.toString());
			return;
		}

        // No Pre Reg Ids has resulted in Error.
		restHelper.sendAuditDetails(EventId.PRE_413.toString(), EventName.UPDATE.toString(), EventType.BUSINESS.toString(),
                    "Updated All Mismatched data for all pre reg ids.", AuditLogVariables.PRE_REGISTRATION_ID.toString(), 
                    auditUserId, auditUsername, PreRegBatchContants.EMPTY, AuditLogVariables.PREREGISTRATION.toString(), 
                    AuditLogVariables.PREREGISTRATION.toString());
    }

    private boolean isAppointmentDataMismatch(RegistrationBookingEntity regAppointment, ApplicationEntity application) {

        if (Objects.isNull(application.getAppointmentDate()) || 
                !(regAppointment.getRegDate().isEqual(application.getAppointmentDate())))
            return true;
        
        if (Objects.isNull(application.getSlotFromTime()) || 
                        !(regAppointment.getSlotFromTime().equals(application.getSlotFromTime())))
            return true;
        
        if (Objects.isNull(application.getSlotToTime()) || 
                !(regAppointment.getSlotToTime().equals(application.getSlotToTime())))
            return true;
        
        return false;
    }

    private void updateMismatchData(RegistrationBookingEntity regAppointment, ApplicationEntity application) {
        application.setBookingDate(regAppointment.getUpdDate().toLocalDate());
		application.setRegistrationCenterId(regAppointment.getRegistrationCenterId());
		application.setSlotFromTime(regAppointment.getSlotFromTime());
		application.setSlotToTime(regAppointment.getSlotToTime());
		application.setAppointmentDate(regAppointment.getRegDate());
		application.setBookingStatusCode(StatusCodes.BOOKED.getCode());
		application.setUpdBy("PRERIGISTRATION_JOB");
		application.setUpdDtime(LocalDateTime.now());
        batchServiceDAO.updateApplicantEntity(application);
        LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.APPOINTMENT_MISMATCH_JOB, 
                        "Updated Mismatch data in Application for Pre Reg Id: " + regAppointment.getPreregistrationId());
    }
}
