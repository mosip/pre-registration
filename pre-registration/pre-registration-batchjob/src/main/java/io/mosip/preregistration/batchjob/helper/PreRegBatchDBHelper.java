package io.mosip.preregistration.batchjob.helper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils2;
import io.mosip.preregistration.batchjob.entity.AvailibityEntity;
import io.mosip.preregistration.batchjob.repository.utils.BatchJpaRepositoryImpl;
import io.mosip.preregistration.core.config.LoggerConfiguration;

/**
 * @author Mahammed Taheer
 * @since 1.2.0
 *
 */
@Component
public class PreRegBatchDBHelper {
    
    /**
     * Performs log config for MOSIP Pre-Registration.
     *
     * @return log config result
     */
    private Logger LOGGER = LoggerConfiguration.logConfig(PreRegBatchDBHelper.class);

    @Value("${mosip.batch.token.authmanager.userName}")
	/**
	 * audit username used by this Pre-Registration type.
	 */
	private String auditUsername;

    /**
	 * Autowired reference for {@link #batchServiceDAO}
	 */
	@Autowired
	private BatchJpaRepositoryImpl batchServiceDAO;

    /**
     * Performs save availability for MOSIP Pre-Registration.
     *
     * @param regCenterId reg center id used by save availability
     * @param contactPerson contact person used by save availability
     * @param noOfKiosks no of kiosks used by save availability
     * @param date date used by save availability
     * @param slotStartTime slot start time used by save availability
     * @param slotEndTime slot end time used by save availability
     */
    public void saveAvailability(String regCenterId, String contactPerson, 
            Short noOfKiosks, LocalDate date, LocalTime slotStartTime, LocalTime slotEndTime) {

        /* LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY,
                 "Saving Slot Generation for Date: " + date); */
		AvailibityEntity avaEntity = new AvailibityEntity();
		avaEntity.setRegDate(date);
		avaEntity.setRegcntrId(regCenterId);
		avaEntity.setFromTime(slotStartTime);
		avaEntity.setToTime(slotEndTime);
		avaEntity.setCrBy(auditUsername);
		avaEntity.setCrDate(DateUtils2.parseDateToLocalDateTime(new Date()));
		if (Objects.isNull(contactPerson) || contactPerson.trim().length() == 0) {
			avaEntity.setCrBy(auditUsername);
		} else {
			avaEntity.setCrBy(contactPerson);
		}
		avaEntity.setAvailableKiosks(slotStartTime.equals(slotEndTime) ? 0 : noOfKiosks);
		batchServiceDAO.saveAvailability(avaEntity);
	}
}
