package io.mosip.preregistration.batchjob.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.preregistration.core.common.entity.ApplicationEntity;

@Repository("applicationRepository")
public interface ApplicationRepository extends JpaRepository<ApplicationEntity, String> {

	@Query("SELECT e FROM ApplicationEntity e WHERE e.applicationId = ?1")
	public ApplicationEntity findByApplicationId(String applicationId);
	
	public ApplicationEntity findByApplicationIdAndBookingStatusCode(String applicationId, String bookingStatusCode);
}
