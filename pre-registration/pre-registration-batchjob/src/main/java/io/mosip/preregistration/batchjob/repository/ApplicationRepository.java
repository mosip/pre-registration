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

	/** Canonical UUID shape, repeated per column by the candidate query below. */
	String UUID_REGEX = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";

	/**
	 * Returns up to {@code batchSize} pre-registration ids that still hold a raw (non-canonical-UUID)
	 * identifier in <em>any</em> of the columns the reconciliation converts — i.e. rows the best-effort
	 * identity migration never converted, or converted only partially.
	 * <p>
	 * Anchoring on {@code applications.cr_by} alone would miss partially migrated records (for example
	 * {@code cr_by} already canonical while {@code contact_info} or a child-table column is still raw),
	 * so candidates are unioned across every table and column that {@code reconcileOne} touches, and
	 * {@code UNION} collapses ids that are raw in more than one place. Each predicate is spelled out
	 * per column so it is applied during the scan rather than after the rows are unpivoted. A
	 * {@code NULL} identifier drops out of the {@code WHERE} on its own, so only the empty-string case
	 * needs an explicit guard.
	 * <p>
	 * The regex is the SQL-side counterpart of {@code GenericUtil.isUuid} — it only narrows the
	 * candidate set on the DB side, so callers must still confirm each column with that check before
	 * acting, and no column is converted on the strength of this query alone. Bounded by {@code LIMIT}
	 * so a large legacy backlog is not loaded at once; reconciled rows drop out of the predicate, so
	 * callers re-read the first page to advance.
	 *
	 * @param batchSize maximum number of ids to return
	 * @return pre-registration ids carrying at least one raw identifier
	 */
	@Query(value = "SELECT c.id FROM ("
			+ "SELECT a.application_id AS id FROM applications a WHERE "
			+ "(a.cr_by <> '' AND a.cr_by !~* '" + UUID_REGEX + "') OR "
			+ "(a.upd_by <> '' AND a.upd_by !~* '" + UUID_REGEX + "') OR "
			+ "(a.contact_info <> '' AND a.contact_info !~* '" + UUID_REGEX + "') "
			+ "UNION "
			+ "SELECT d.prereg_id FROM applicant_demographic d WHERE "
			+ "(d.cr_by <> '' AND d.cr_by !~* '" + UUID_REGEX + "') OR "
			+ "(d.upd_by <> '' AND d.upd_by !~* '" + UUID_REGEX + "') OR "
			+ "(d.cr_appuser_id <> '' AND d.cr_appuser_id !~* '" + UUID_REGEX + "') "
			+ "UNION "
			+ "SELECT doc.prereg_id FROM applicant_document doc WHERE "
			+ "(doc.cr_by <> '' AND doc.cr_by !~* '" + UUID_REGEX + "') OR "
			+ "(doc.upd_by <> '' AND doc.upd_by !~* '" + UUID_REGEX + "') "
			+ "UNION "
			+ "SELECT r.prereg_id FROM reg_appointment r WHERE "
			+ "(r.cr_by <> '' AND r.cr_by !~* '" + UUID_REGEX + "') OR "
			+ "(r.upd_by <> '' AND r.upd_by !~* '" + UUID_REGEX + "') "
			+ ") c LIMIT :batchSize", nativeQuery = true)
	public List<String> findPreRegIdsWithRawIdentifier(@Param("batchSize") int batchSize);
}
