package io.mosip.preregistration.datasync.repository.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.datasync.repository.util.DataSyncJpaRepositoryImpl;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.exception.TableNotAccessibleException;
import io.mosip.preregistration.datasync.errorcodes.ErrorCodes;
import io.mosip.preregistration.datasync.errorcodes.ErrorMessages;
import io.mosip.preregistration.datasync.repository.DemographicConsumedRepository;

@Component
public class DataSyncJpaRepositoryImpl {

	/** The Constant LOGGER. */
	private Logger log = LoggerConfiguration.logConfig(DataSyncJpaRepositoryImpl.class);

	/**
	 * Autowired reference for {@link #demographicConsumedRepository}
	 */
	@Autowired
	@Qualifier("demographicConsumedRepository")
	private DemographicConsumedRepository demographicConsumedRepository;

	/**
	 * @param preRegistrationId
	 * @return true if preRegistrationId is present in consumed table else return
	 *         false.
	 */
	public boolean findConsumedPrid(String prid) {
		try {
			if (demographicConsumedRepository.findByPrid(prid) != null) {
				log.info("sessionId", "idType", "id", "Found consumed PRID in applicant_demographic_consumed Table");
				return true;
			} else
				return false;
		} catch (DataAccessLayerException e) {
			throw new TableNotAccessibleException(ErrorCodes.PRG_DATA_SYNC_022.getCode(),
					ErrorMessages.DEMOGRAPHIC_CONSUMED_TABLE_NOT_ACCESSIBLE.getMessage());
		}
	}

}
