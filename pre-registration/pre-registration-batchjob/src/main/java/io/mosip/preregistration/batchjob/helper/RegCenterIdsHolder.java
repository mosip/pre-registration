package io.mosip.preregistration.batchjob.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.batchjob.code.PreRegBatchContants;
import io.mosip.preregistration.core.config.LoggerConfiguration;

/**
 * Class supporting reg center ids holder in MOSIP Pre-Registration (io.mosip.preregistration.batchjob.helper).
 */
public class RegCenterIdsHolder {
    
    /**
     * Performs log config for MOSIP Pre-Registration.
     *
     * @return log config result
     */
    private Logger LOGGER = LoggerConfiguration.logConfig(RegCenterIdsHolder.class);

    /**
     * holder obj used by this Pre-Registration type.
     */
    private static RegCenterIdsHolder holderObj = null;

    /**
     * reg centers ids list used by this Pre-Registration type.
     */
    private static List<String> regCentersIdsList;
    
    /**
     * Creates a reg center ids holder.
     */
    private RegCenterIdsHolder() {
        regCentersIdsList = new ArrayList<>();
    }

    /**
     * Returns instance.
     *
     * @return get instance result
     */
    public static RegCenterIdsHolder getInstance(){
        synchronized(RegCenterIdsHolder.class){
            if (Objects.isNull(holderObj)){
                holderObj = new RegCenterIdsHolder();
            }
        }
        return holderObj;
    }

    /**
     * Performs add reg center id for MOSIP Pre-Registration.
     *
     * @param regCenterId reg center id used by add reg center id
     */
    public void addRegCenterId(String regCenterId) {
        synchronized(regCentersIdsList) {
            if (!regCentersIdsList.contains(regCenterId))
                regCentersIdsList.add(regCenterId);
        }
    }

    /**
     * Performs contains reg center id for MOSIP Pre-Registration.
     *
     * @param regCenterId reg center id used by contains reg center id
     * @return contains reg center id result
     */
    public boolean containsRegCenterId(String regCenterId) {
        return regCentersIdsList.contains(regCenterId);
    }

    /**
     * Performs print all reg center ids for MOSIP Pre-Registration.
     */
    public void printAllRegCenterIds() {
        LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY,
            "list of unique reg centers Ids: " + regCentersIdsList);
    }

    /**
     * Performs delete all reg center ids for MOSIP Pre-Registration.
     */
    public void deleteAllRegCenterIds() {
        synchronized(regCentersIdsList) {
            regCentersIdsList.clear();
        }
    }

}
