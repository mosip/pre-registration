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
import io.mosip.preregistration.batchjob.entity.DemographicEntityConsumed;
import io.mosip.preregistration.batchjob.entity.DocumentEntityConsumed;
import io.mosip.preregistration.batchjob.entity.ProcessedPreRegEntity;
import io.mosip.preregistration.batchjob.entity.RegistrationBookingEntityConsumed;
import io.mosip.preregistration.batchjob.helper.RestHelper;
import io.mosip.preregistration.batchjob.repository.utils.BatchJpaRepositoryImpl;
import io.mosip.preregistration.core.code.AuditLogVariables;
import io.mosip.preregistration.core.code.EventId;
import io.mosip.preregistration.core.code.EventName;
import io.mosip.preregistration.core.code.EventType;
import io.mosip.preregistration.core.code.StatusCodes;
import io.mosip.preregistration.core.common.entity.ApplicationEntity;
import io.mosip.preregistration.core.common.entity.DemographicEntity;
import io.mosip.preregistration.core.common.entity.DocumentEntity;
import io.mosip.preregistration.core.common.entity.RegistrationBookingEntity;
import io.mosip.preregistration.core.config.LoggerConfiguration;

/**
 * @author Mahammed Taheer
 * @since 1.2.0
 *
 */
@Component
public class ApplicationConsumedStatusUpdater {
    
    private Logger LOGGER = LoggerConfiguration.logConfig(ApplicationConsumedStatusUpdater.class);

    @Value("${mosip.batch.token.authmanager.userName}")
	private String auditUsername;

	@Value("${mosip.batch.token.authmanager.appId}")
	private String auditUserId;

    @Autowired
	private BatchJpaRepositoryImpl batchJpaRepositoryImpl;

    @Autowired
	private RestHelper restHelper;
    
    public void updateConsumedStatus() {

        LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.APPLICATION_CONSUMED_JOB, 
		 			"Starting Application Consumed Status Update Job.");

		List<ProcessedPreRegEntity> processedPreRegList = batchJpaRepositoryImpl.getAllConsumedPreIds(
                    PreRegBatchContants.PROCESSED_STATUS_COMMENTS);

        LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.APPLICATION_CONSUMED_JOB, 
		 			"Total Number of Processed Pre Registration applications: " + processedPreRegList.size());
        
        List<String> errorredPreRegIds = new ArrayList<>();
        processedPreRegList.forEach(processedPreReg -> {
            String processedPreRegId = processedPreReg.getPreRegistrationId();
            try {
                LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.APPLICATION_CONSUMED_JOB, 
                        "Updating status for pre reg id: " + processedPreRegId);
                
                DemographicEntity demoEntity = batchJpaRepositoryImpl.getApplicantDemographicObject(processedPreRegId);
                if (Objects.isNull(demoEntity)) {
                    int deleted = batchJpaRepositoryImpl.deleteInvalidProcessedPreReg(processedPreRegId);
                    LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.APPLICATION_CONSUMED_JOB, 
                        "Deleted invalid processed pre reg id: " + processedPreRegId + ", deleted count: " + deleted);
                } else {
                    //adding processed pre reg details in applicant demographic consumed. 
                    addApplicantDemographicConsumed(demoEntity);

                    //adding processed pre reg details in applicant document consumed and purging from applicant document.
                    List<DocumentEntity> applicantDocumentList = demoEntity.getDocumentEntity();
                    addApplicantDocumentConsumed(applicantDocumentList, processedPreRegId);
                    // purging from applicant document
                    purgeApplicantDocumentConsumed(applicantDocumentList, processedPreRegId);

                    //adding processed pre reg details in registration appointment consumed and purging from registration appointment.
                    if (demoEntity.getStatusCode().equals(StatusCodes.BOOKED.getCode())) {
                        RegistrationBookingEntity regAppointmentObj = batchJpaRepositoryImpl.getRegistrationAppointmentDetails(
                                                        demoEntity.getPreRegistrationId());
                        addRegistrationAppointmentConsumed(regAppointmentObj, processedPreRegId);
                        batchJpaRepositoryImpl.deleteBooking(regAppointmentObj);
                    }

                    // purging demographic details
                    batchJpaRepositoryImpl.deleteDemographic(demoEntity);

                    // Purging appointment details.
                    ApplicationEntity applicationObj = batchJpaRepositoryImpl.getApplicantEntityDetails(processedPreRegId);
					batchJpaRepositoryImpl.deleteApplications(applicationObj);
					LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.APPLICATION_CONSUMED_JOB, 
                                "Purge appointment details for pre reg id: ", processedPreRegId);
                    
                    // updating status in processed pre reg table 
                    processedPreReg.setStatusComments(PreRegBatchContants.NEW_STATUS_COMMENTS);
                    batchJpaRepositoryImpl.updateProcessedList(processedPreReg);
                    LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.APPLICATION_CONSUMED_JOB, 
                            "Updated comments successfully in Processed PreId List for Pre-RegistrationId: " + processedPreRegId);
                }
            } catch(Exception exp) {
                errorredPreRegIds.add(processedPreRegId);
                LOGGER.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.APPLICATION_CONSUMED_JOB, 
                        "Errorred in updating status processed pre reg id: " + processedPreRegId, ExceptionUtils.getStackTrace(exp));
            }
        });  

        if (errorredPreRegIds.size() > 0) {
			String preRegIds = String.join(",", errorredPreRegIds);
			restHelper.sendAuditDetails(EventId.PRE_405.toString(), EventName.EXCEPTION.toString(), EventType.SYSTEM.toString(),
						"Updating Consumed data Failed for Pre Reg id, List of Pre Reg Ids. ", AuditLogVariables.NO_ID.toString(), 
						auditUserId, auditUsername, preRegIds, AuditLogVariables.BAT.toString(), 
                        AuditLogVariables.CONSUMED_BATCH_SERVICE.toString());
			return;
		}

        // No Processed Pre Reg Ids has resulted in Error.
		restHelper.sendAuditDetails(EventId.PRE_412.toString(), EventName.CONSUMEDSTATUS.toString(), EventType.BUSINESS.toString(),
                    "Updated the consumed status for all processed pre reg ids.", AuditLogVariables.PRE_REGISTRATION_ID.toString(), 
                    auditUserId, auditUsername, PreRegBatchContants.EMPTY, AuditLogVariables.BAT.toString(), 
                    AuditLogVariables.BOOKING_SERVICE.toString());
    }

    private void addApplicantDemographicConsumed(DemographicEntity demoEntity) {
        DemographicEntityConsumed demographicEntityConsumed = new DemographicEntityConsumed();
        demographicEntityConsumed.setApplicantDetailJson(demoEntity.getApplicantDetailJson());
        demographicEntityConsumed.setCrAppuserId(demoEntity.getCrAppuserId());
        demographicEntityConsumed.setCreateDateTime(demoEntity.getCreateDateTime());
        demographicEntityConsumed.setCreatedBy(demoEntity.getCreatedBy());
        demographicEntityConsumed.setDemogDetailHash(demoEntity.getDemogDetailHash());
        demographicEntityConsumed.setEncryptedDateTime(demoEntity.getEncryptedDateTime());
        demographicEntityConsumed.setLangCode(demoEntity.getLangCode());
        demographicEntityConsumed.setPreRegistrationId(demoEntity.getPreRegistrationId());
        demographicEntityConsumed.setUpdateDateTime(DateUtils.parseDateToLocalDateTime(new Date()));
        demographicEntityConsumed.setUpdatedBy(auditUserId);
        demographicEntityConsumed.setStatusCode(StatusCodes.CONSUMED.getCode());
        boolean added = batchJpaRepositoryImpl.updateConsumedDemographic(demographicEntityConsumed);
        LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.APPLICATION_CONSUMED_JOB, 
                        "Added demographic consumed in table pre reg id: " + demoEntity.getPreRegistrationId() + ", added status: " + added);
    }

    private void addApplicantDocumentConsumed(List<DocumentEntity> applicantDocumentList, String preRegId) {
        if (Objects.isNull(applicantDocumentList)) {
            LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.APPLICATION_CONSUMED_JOB, 
                        "No Documents found to add in consumed table for pre reg id: " + preRegId);
            return;
        }

        applicantDocumentList.forEach(applicantDoc -> {
            DocumentEntityConsumed documentEntityConsumed = new DocumentEntityConsumed();
            documentEntityConsumed.setCrBy(applicantDoc.getCrBy());
            documentEntityConsumed.setCrDtime(applicantDoc.getCrDtime());
            documentEntityConsumed.setDocCatCode(applicantDoc.getDocCatCode());
            documentEntityConsumed.setDocFileFormat(applicantDoc.getDocFileFormat());
            documentEntityConsumed.setDocHash(applicantDoc.getDocHash());
            documentEntityConsumed.setDocId(applicantDoc.getDocId());
            documentEntityConsumed.setDocName(applicantDoc.getDocName());
            documentEntityConsumed.setDocTypeCode(applicantDoc.getDocTypeCode());
            documentEntityConsumed.setDocumentId(applicantDoc.getDocumentId());
            documentEntityConsumed.setEncryptedDateTime(applicantDoc.getEncryptedDateTime());
            documentEntityConsumed.setLangCode(applicantDoc.getLangCode());
            documentEntityConsumed.setPreregId(applicantDoc.getDemographicEntity().getPreRegistrationId());
            documentEntityConsumed.setStatusCode(applicantDoc.getStatusCode());
            documentEntityConsumed.setUpdBy(auditUserId);
            documentEntityConsumed.setUpdDtime(DateUtils.parseDateToLocalDateTime(new Date()));
            documentEntityConsumed.setDocRefId(applicantDoc.getRefNumber());
            batchJpaRepositoryImpl.updateConsumedDocument(documentEntityConsumed);
        });
        LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.APPLICATION_CONSUMED_JOB, 
                "Found Documents added in consumed table for pre reg id: " + preRegId + ", documents count: " + applicantDocumentList.size());
        
        
    }

    private void addRegistrationAppointmentConsumed(RegistrationBookingEntity regAppointmentObj, String preRegId) {
        RegistrationBookingEntityConsumed regAppointmentConsumed = new RegistrationBookingEntityConsumed();
        regAppointmentConsumed.setBookingDateTime(regAppointmentObj.getBookingDateTime());
        regAppointmentConsumed.setPreregistrationId(regAppointmentObj.getPreregistrationId());
        regAppointmentConsumed.setCrBy(regAppointmentObj.getCrBy());
        regAppointmentConsumed.setCrDate(regAppointmentObj.getCrDate());
        regAppointmentConsumed.setId(regAppointmentObj.getId());
        regAppointmentConsumed.setLangCode(regAppointmentObj.getLangCode());
        regAppointmentConsumed.setRegDate(regAppointmentObj.getRegDate());
        regAppointmentConsumed.setRegistrationCenterId(regAppointmentObj.getRegistrationCenterId());
        regAppointmentConsumed.setSlotFromTime(regAppointmentObj.getSlotFromTime());
        regAppointmentConsumed.setSlotToTime(regAppointmentObj.getSlotToTime());
        regAppointmentConsumed.setUpBy(auditUserId);
        regAppointmentConsumed.setUpdDate(DateUtils.parseDateToLocalDateTime(new Date()));
        boolean added = batchJpaRepositoryImpl.updateConsumedBooking(regAppointmentConsumed);
        LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.APPLICATION_CONSUMED_JOB, 
                "Added Reg Appointment consumed in table for pre reg id: " + preRegId + ", added status: " + added);

        
    }

    private void purgeApplicantDocumentConsumed(List<DocumentEntity> applicantDocumentList, String preRegId) {
        if (Objects.isNull(applicantDocumentList)) {
            LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.APPLICATION_CONSUMED_JOB, 
                        "No Documents found to delete from table for pre reg id: " + preRegId);
            return;
        }

        applicantDocumentList.forEach(applicantDoc -> {
            batchJpaRepositoryImpl.deleteApplicantDocument(applicantDoc);
            LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.APPLICATION_CONSUMED_JOB, 
                "Deleted Applicant Document for pre reg id: " + preRegId + ", documents id: " + applicantDoc.getDocId());
        });
    }
    
}
