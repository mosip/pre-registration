package io.mosip.preregistration.batchjob.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.preregistration.batchjob.code.PreRegBatchContants;
import io.mosip.preregistration.batchjob.helper.RestHelper;
import io.mosip.preregistration.batchjob.repository.utils.BatchJpaRepositoryImpl;
import io.mosip.preregistration.core.code.AuditLogVariables;
import io.mosip.preregistration.core.code.EventId;
import io.mosip.preregistration.core.code.EventName;
import io.mosip.preregistration.core.code.EventType;
import io.mosip.preregistration.core.code.StatusCodes;
import io.mosip.preregistration.core.common.entity.ApplicationEntity;
import io.mosip.preregistration.core.common.entity.DemographicEntity;
import io.mosip.preregistration.core.common.entity.RegistrationBookingEntity;
import io.mosip.preregistration.core.config.LoggerConfiguration;

/**
 * @author Mahammed Taheer
 * @since 1.2.0
 *
 */
@Component
public class ApplicationExpiredStatusUpdater {
    
    private Logger LOGGER = LoggerConfiguration.logConfig(ApplicationExpiredStatusUpdater.class);

    @Value("${mosip.batch.token.authmanager.userName}")
	private String auditUsername;

	@Value("${mosip.batch.token.authmanager.appId}")
	private String auditUserId;

    @Autowired
	private BatchJpaRepositoryImpl batchServiceDAO;

    @Autowired
	private RestHelper restHelper;

    public void updateExpiredStatus(){

        LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EXPIRED_STATUS_JOB, 
		 			"Starting Application Expired Status updater Job.");
        
        List<RegistrationBookingEntity> bookedPreRegDetailsList = batchServiceDAO.getAllOldDateBooking();

        if (Objects.isNull(bookedPreRegDetailsList) || bookedPreRegDetailsList.size() == 0) {
            LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EXPIRED_STATUS_JOB, 
		 			"No Booked Applications found which are expired.");
            restHelper.sendAuditDetails(EventId.PRE_413.toString(), EventName.EXPIREDSTATUS.toString(), EventType.BUSINESS.toString(),
                     "No Booked Application found to update status as Expired.", AuditLogVariables.PRE_REGISTRATION_ID.toString(), 
                     auditUserId, auditUsername, PreRegBatchContants.EMPTY, AuditLogVariables.BOOK.toString(), 
                     AuditLogVariables.EXPIRED_BATCH_SERVICE.toString());
            return;
        }

        LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EXPIRED_STATUS_JOB, 
		 			"Total Number of application found to update as Expired: " + bookedPreRegDetailsList.size());
        List<String> errorredPreRegIds = new ArrayList<>();
        bookedPreRegDetailsList.forEach(bookedPreReg -> {
            String preRegId = bookedPreReg.getPreregistrationId();
            try {

                // Updating application status.
                updateApplicationStatus(preRegId);

                // updating applicant demographic.
                updateApplicantDemographicStatus(preRegId);
            } catch(Exception exp){
                errorredPreRegIds.add(preRegId);
                LOGGER.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EXPIRED_STATUS_JOB, 
                        "Errorred in updating expired status for pre reg id: " + preRegId, ExceptionUtils.getStackTrace(exp));
            }
        });

        if (errorredPreRegIds.size() > 0) {
			String preRegIds = String.join(",", errorredPreRegIds);
			restHelper.sendAuditDetails(EventId.PRE_405.toString(), EventName.EXCEPTION.toString(), EventType.SYSTEM.toString(),
						"Updating Expired status Failed for Pre Reg id, List of Pre Reg Ids. ", AuditLogVariables.NO_ID.toString(), 
						auditUserId, auditUsername, preRegIds, AuditLogVariables.BAT.toString(), 
                        AuditLogVariables.EXPIRED_BATCH_SERVICE.toString());
			return;
		}

        // No Pre Reg Ids has resulted in Error.
		restHelper.sendAuditDetails(EventId.PRE_413.toString(), EventName.EXPIREDSTATUS.toString(), EventType.BUSINESS.toString(),
                    "Updated Expired status for all pre reg ids.", AuditLogVariables.PRE_REGISTRATION_ID.toString(), 
                    auditUserId, auditUsername, PreRegBatchContants.EMPTY, AuditLogVariables.BAT.toString(), 
                    AuditLogVariables.EXPIRED_BATCH_SERVICE.toString());
    }


    private void updateApplicationStatus(String preRegId) {
        LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EXPIRED_STATUS_JOB, 
		 			"Updating Expired status in Application for Pre Reg Id: " + preRegId);
        
        ApplicationEntity applicationEntity = batchServiceDAO.getBookedApplicantEntityDetails(preRegId);
        if (Objects.isNull(applicationEntity)) {
            LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EXPIRED_STATUS_JOB, 
		 			"No Application found to update expire status for Pre Reg Id: " + preRegId);
            return;
        }

        applicationEntity.setBookingStatusCode(StatusCodes.EXPIRED.getCode());
        applicationEntity.setUpdBy(auditUserId);
        applicationEntity.setUpdDtime(DateUtils.parseDateToLocalDateTime(new Date()));
        batchServiceDAO.updateApplicantEntity(applicationEntity);
        LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EXPIRED_STATUS_JOB, 
            "Application updated as expire status for Pre Reg Id: " + preRegId);
    }

    private void updateApplicantDemographicStatus(String preRegId) {
        LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EXPIRED_STATUS_JOB, 
		 			"Updating Expired status in Application Demograhpic for Pre Reg Id: " + preRegId);
        
        DemographicEntity demographicEntity = batchServiceDAO.getApplicantDemographicDetails(preRegId);
        if (Objects.isNull(demographicEntity)) {
            LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EXPIRED_STATUS_JOB, 
		 			"No Applicant Demographic found to update expire status for Pre Reg Id: " + preRegId);
            return;
        }

        if (demographicEntity.getStatusCode().equals(StatusCodes.BOOKED.getCode())) {
            demographicEntity.setStatusCode(StatusCodes.EXPIRED.getCode());
            demographicEntity.setUpdatedBy(auditUserId);
            demographicEntity.setUpdateDateTime(DateUtils.parseDateToLocalDateTime(new Date()));
            batchServiceDAO.updateApplicantDemographic(demographicEntity);
            LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EXPIRED_STATUS_JOB, 
                "Applicant Demographic updated as expire status for Pre Reg Id: " + preRegId);
            return;
        }
        LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EXPIRED_STATUS_JOB, 
            "Applicant demographic status is not Booked status for Pre Reg Id: " + preRegId);
    }
}
