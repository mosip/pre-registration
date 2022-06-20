package io.mosip.preregistration.application.repository;

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
public interface ApplicationRepostiory extends BaseRepository<ApplicationEntity, String> {

	public static final String searchAppointmentsQuery = "select u from ApplicationEntity u where u.registrationCenterId=?1 and u.appointmentDate between ?2 and ?3";

	@Query("SELECT u.bookingStatusCode FROM ApplicationEntity u where u.applicationId =?1")
	public String findBookingStatusCodeById(String applicationId);

	@Modifying
	@Query("DELETE FROM ApplicationEntity e WHERE e.applicationId = ?1")
	public void deleteById(String applicationId);

	@Query(searchAppointmentsQuery)
	public List<ApplicationEntity> findByRegistrationCenterIdAndBetweenDate(String registrationCenterId,
			LocalDate appointmentFromDate, LocalDate appointmentToDate);

	@Query("SELECT e FROM ApplicationEntity e WHERE e.applicationId = ?1")
	public ApplicationEntity findByApplicationId(String applicationId);
	
	@Query("SELECT e FROM ApplicationEntity e  WHERE e.crBy= ?1 order by e.crDtime desc")
	public List<ApplicationEntity> findByCreatedBy(String userId);
	
	@Query("SELECT e FROM ApplicationEntity e  WHERE e.crBy= ?1 and e.bookingType= ?2 order by e.crDtime desc")
	public List<ApplicationEntity> findByCreatedByBookingType(String userId, String bookingType);
	
}
