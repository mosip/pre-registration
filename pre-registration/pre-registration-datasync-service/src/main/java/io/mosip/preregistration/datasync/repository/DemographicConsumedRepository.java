package io.mosip.preregistration.datasync.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.preregistration.datasync.entity.DemographicEntityConsumed;

@Repository("demographicConsumedRepository")
public interface DemographicConsumedRepository extends BaseRepository<DemographicEntityConsumed, String> {

	@Query("SELECT e FROM DemographicEntityConsumed e WHERE e.preRegistrationId = ?1")
	public DemographicEntityConsumed findByPrid(String preRegistrationId);

}
