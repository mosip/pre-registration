package io.mosip.preregistration.core.common.repository;

import java.time.LocalDate;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.preregistration.core.common.entity.ApplicationEntity;

@Repository
@Transactional
/**
 * Contract for application repostiory in MOSIP Pre-Registration.
 */
public interface ApplicationRepostiory extends BaseRepository<ApplicationEntity, String> {

	/**
	 * search appointments query used by this Pre-Registration type.
	 */
	public static final String searchAppointmentsQuery = "select u from ApplicationEntity u where u.registrationCenterId=?1 and u.appointmentDate between ?2 and ?3";

	@Query("SELECT u.bookingStatusCode FROM ApplicationEntity u where u.applicationId =?1")
	/**
	 * Performs find booking status code by id for MOSIP Pre-Registration.
	 *
	 * @param applicationId application id used by find booking status code by id
	 * @return find booking status code by id result
	 */
	public String findBookingStatusCodeById(String applicationId);

	@Modifying
	@Query("DELETE FROM ApplicationEntity e WHERE e.applicationId = ?1")
	/**
	 * Performs delete by id for MOSIP Pre-Registration.
	 *
	 * @param applicationId application id used by delete by id
	 */
	public void deleteById(String applicationId);

	@Query(searchAppointmentsQuery)
	/**
	 * Performs find by registration center id and between date for MOSIP Pre-Registration.
	 *
	 * @param registrationCenterId registration center id used by find by registration center id and between date
	 * @param appointmentFromDate appointment from date used by find by registration center id and between date
	 * @param appointmentToDate appointment to date used by find by registration center id and between date
	 * @return find by registration center id and between date result
	 */
	public List<ApplicationEntity> findByRegistrationCenterIdAndBetweenDate(String registrationCenterId,
			LocalDate appointmentFromDate, LocalDate appointmentToDate);

	@Query("SELECT e FROM ApplicationEntity e WHERE e.applicationId = ?1")
	/**
	 * Performs find by application id for MOSIP Pre-Registration.
	 *
	 * @param applicationId application id used by find by application id
	 * @return find by application id result
	 */
	public ApplicationEntity findByApplicationId(String applicationId);
	
	@Query("SELECT e FROM ApplicationEntity e  WHERE e.crBy= ?1 order by e.crDtime desc")
	/**
	 * Performs find by created by for MOSIP Pre-Registration.
	 *
	 * @param userId user id used by find by created by
	 * @return find by created by result
	 */
	public List<ApplicationEntity> findByCreatedBy(String userId);
	
	@Query("SELECT e FROM ApplicationEntity e  WHERE e.crBy= ?1 and e.bookingType= ?2 order by e.crDtime desc")
	/**
	 * Performs find by created by booking type for MOSIP Pre-Registration.
	 *
	 * @param userId user id used by find by created by booking type
	 * @param bookingType booking type used by find by created by booking type
	 * @return find by created by booking type result
	 */
	public List<ApplicationEntity> findByCreatedByBookingType(String userId, String bookingType);

	@Query("SELECT e FROM ApplicationEntity e WHERE lower(e.crBy) IN ?1 order by e.crDtime desc")
	/**
	 * Performs find by created by in for MOSIP Pre-Registration.
	 *
	 * @param userIds user ids used by find by created by in
	 * @return find by created by in result
	 */
	public List<ApplicationEntity> findByCreatedByIn(List<String> userIds);

	@Query("SELECT e FROM ApplicationEntity e WHERE lower(e.crBy) IN ?1 and e.bookingType= ?2 order by e.crDtime desc")
	/**
	 * Performs find by created by in booking type for MOSIP Pre-Registration.
	 *
	 * @param userIds user ids used by find by created by in booking type
	 * @param bookingType booking type used by find by created by in booking type
	 * @return find by created by in booking type result
	 */
	public List<ApplicationEntity> findByCreatedByInBookingType(List<String> userIds, String bookingType);
	
}
