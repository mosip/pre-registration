package io.mosip.preregistration.batchjob.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.preregistration.core.common.entity.DocumentEntity;

/**
 * @author Kishan Rathore
 * @since 1.0.0
 *
 */
@Repository("documentRespository")
public interface DocumentRespository extends BaseRepository<DocumentEntity, String> {

	/**
	 * @param preregId
	 * @return document entity based on given preId
	 */
	public List<DocumentEntity> findByDemographicEntityPreRegistrationId(String preId);

}
