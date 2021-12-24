package io.mosip.preregistration.batchjob.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.batchjob.code.PreRegBatchContants;
import io.mosip.preregistration.batchjob.helper.CancelAndNotifyHelper;
import io.mosip.preregistration.batchjob.helper.RestHelper;
import io.mosip.preregistration.batchjob.model.RegistrationCenterDto;
import io.mosip.preregistration.batchjob.repository.utils.BatchJpaRepositoryImpl;
import io.mosip.preregistration.core.common.entity.RegistrationBookingEntity;
import io.mosip.preregistration.core.config.LoggerConfiguration;

/**
 * @author Mahammed Taheer
 * @since 1.2.0
 *
 */
@Component
public class PurgeExpiredRegCentersSlots {

    private Logger LOGGER = LoggerConfiguration.logConfig(PurgeExpiredRegCentersSlots.class);

    /** 
	 * Autowired reference for {@link #batchServiceDAO}
	 */
	@Autowired
	private BatchJpaRepositoryImpl batchServiceDAO;

    @Autowired
	private RestHelper restHelper;

    @Autowired
	private CancelAndNotifyHelper cancelAndNotifyHelper;

    // Deleting all the added slots for the expired registration centers. 
    public void purgeSlots(){

        LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
		 				"Started Purge Expired Registration Centers slots.");

		int totalNoOfPages = restHelper.getRegistrationCenterTotalPages();
		List<String> pageNosList = new ArrayList<>();
		for (int i = 0; i < totalNoOfPages; i++){
			pageNosList.add(Integer.toString(i));
		}

        List<RegistrationCenterDto> regCentersList = restHelper.getRegistrationCenterDetails(pageNosList, null);
		LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
		 				"Total Number of registration Found available in Master Data: <" + regCentersList.size() + ">");

        Map<String, Boolean> cancelledTracker = new HashMap<>();
        Map<String, Boolean> notifierTracker = new HashMap<>();
        List<String> processingRegCentersList = regCentersList.stream().map(RegistrationCenterDto::getId)
															  .distinct()
															  .collect(Collectors.toList());
		List<String> slotsAddedRegCenters = batchServiceDAO.findRegCenter(LocalDate.now());
		slotsAddedRegCenters.stream().filter(regCenterId ->  !processingRegCentersList.contains(regCenterId))
									 .forEach(regCenterId -> purgeExpiredRegCenterSlots(regCenterId, cancelledTracker, notifierTracker));
                                     
        LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
                                     "Completed deleting Expired Registration Centers slots.");
    }

    private void purgeExpiredRegCenterSlots(String regCenterId,  Map<String, Boolean> cancelledTracker,	
					Map<String, Boolean> notifierTracker) {
		LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
						"Deleting Slots for expired reg Center: " + regCenterId);
		List<RegistrationBookingEntity> regBookingEntityList = batchServiceDAO.findAllPreIdsByregID(regCenterId, LocalDate.now());
		LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
						"Total Number of bookings available for the reg center: " + regBookingEntityList.size());
		regBookingEntityList.stream().forEach(bookedSlot -> {
			cancelAndNotifyHelper.cancelAndNotifyApplicant(bookedSlot, PreRegBatchContants.EMPTY, cancelledTracker, notifierTracker);
		});
		int deletedSlots = batchServiceDAO.deleteAllSlotsByRegId(regCenterId, LocalDate.now());
		LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
												"Deleted All Slots for expired reg Center: " + regCenterId + 
												", Deleted Slot Count: " + deletedSlots);
	}
    
}
