package io.mosip.preregistration.batchjob.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.batchjob.code.PreRegBatchContants;
import io.mosip.preregistration.core.config.LoggerConfiguration;

public class RegCenterIdsHolder {
    
    private Logger LOGGER = LoggerConfiguration.logConfig(RegCenterIdsHolder.class);

    private static RegCenterIdsHolder holderObj = null;

    private static List<String> regCentersIdsList;
    
    private RegCenterIdsHolder() {
        regCentersIdsList = new ArrayList<>();
    }

    public static RegCenterIdsHolder getInstance(){
        synchronized(RegCenterIdsHolder.class){
            if (Objects.isNull(holderObj)){
                holderObj = new RegCenterIdsHolder();
            }
        }
        return holderObj;
    }

    public void addRegCenterId(String regCenterId) {
        synchronized(regCentersIdsList) {
            if (!regCentersIdsList.contains(regCenterId))
                regCentersIdsList.add(regCenterId);
        }
    }

    public boolean containsRegCenterId(String regCenterId) {
        return regCentersIdsList.contains(regCenterId);
    }

    public void printAllRegCenterIds() {
        LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY,
            "list of unique reg centers Ids: " + regCentersIdsList);
    }

    public void deleteAllRegCenterIds() {
        synchronized(regCentersIdsList) {
            regCentersIdsList.clear();
        }
    }

}
