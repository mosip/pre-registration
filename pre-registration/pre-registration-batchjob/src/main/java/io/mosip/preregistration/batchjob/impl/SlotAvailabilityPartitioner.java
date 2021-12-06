package io.mosip.preregistration.batchjob.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.batchjob.code.PreRegBatchContants;
import io.mosip.preregistration.batchjob.helper.RestHelper;
import io.mosip.preregistration.batchjob.model.RegistrationCenterDto;
import io.mosip.preregistration.core.config.LoggerConfiguration;

/**
 * @author Mahammed Taheer
 * @since 1.2.0
 *
 */

@Component
public class SlotAvailabilityPartitioner implements Partitioner {
    
    private Logger LOGGER = LoggerConfiguration.logConfig(SlotAvailabilityPartitioner.class);

    @Autowired
	private RestHelper restHelper;
    
    @Override
	public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> regCentersMap = new HashMap<String, ExecutionContext>(gridSize);

        List<RegistrationCenterDto> regCentersList = restHelper.getRegistrationCenterDetails(null);
        int regCentersCount = regCentersList.size();
        int partitionSize = getPartitionSize(regCentersCount, gridSize);

        LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
		 				"Total Number of registration Found available in Master Data: <" + regCentersCount + 
                         ">, GridSize Configured: <" + gridSize + ">, partitionSize:: <" + partitionSize + ">");
        
        int regCenterCounter = 0;
        for (int i = 0; i < gridSize; i++) {
            ExecutionContext execContext = new ExecutionContext();

            List<String> partRegCentersList = new ArrayList<>();
            for (int j = 0; j < partitionSize; j++) {
                if (regCenterCounter >= regCentersCount){
                    break;
                }
                partRegCentersList.add(regCentersList.get(regCenterCounter++).getId());
            } 
            execContext.put("regCenterIdsPartList", partRegCentersList);
            execContext.putString("name", "regCenterIdsPartList-" + i);

            regCentersMap.put("regCenterPartition" + i, execContext);
            if (regCenterCounter >= regCentersCount){
				break;
			}
        }

        return regCentersMap;
    }

    private int getPartitionSize(int regCentersCount, int gridSize) {

        if (regCentersCount <= gridSize)
            return 1;
        
        return (regCentersCount/gridSize) + 1;
    }
}
