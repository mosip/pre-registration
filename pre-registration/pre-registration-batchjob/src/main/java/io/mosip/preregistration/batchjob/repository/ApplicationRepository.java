package io.mosip.preregistration.batchjob.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.mosip.preregistration.core.common.entity.ApplicationEntity;

@Repository("applicationRepository")
public interface ApplicationRepository extends JpaRepository<ApplicationEntity, String> {

	@Query("SELECT e FROM ApplicationEntity e WHERE e.applicationId = ?1")
	public ApplicationEntity findByApplicationId(String applicationId);
	
	public ApplicationEntity findByApplicationIdAndBookingStatusCode(String applicationId, String bookingStatusCode);

	/**
	 * Returns up to {@code batchSize} pre-registration ids whose {@code cr_by} still holds a raw
	 * (non-canonical-UUID) identifier — i.e. rows the best-effort identity migration never converted.
	 * The regex narrows the candidate set on the DB side; callers must still confirm each column with a
	 * UUID check before acting. Bounded by {@code LIMIT} so a large legacy backlog is not loaded at
	 * once; reconciled rows drop out of the predicate, so callers re-read the first page to advance.
	 *
	 * @param batchSize maximum number of ids to return
	 * @return pre-registration ids with a raw creator identifier
	 */
	@Query(value = "SELECT a.application_id FROM applications a WHERE a.cr_by IS NOT NULL AND a.cr_by <> '' "
			+ "AND a.cr_by !~* '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$' LIMIT :batchSize", nativeQuery = true)
	public List<String> findPreRegIdsWithRawCreatedBy(@Param("batchSize") int batchSize);
}
