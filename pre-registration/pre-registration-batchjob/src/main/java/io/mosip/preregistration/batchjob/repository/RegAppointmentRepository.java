/* 
  * Copyright
 * 
 */
package io.mosip.preregistration.batchjob.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.preregistration.core.common.entity.RegistrationBookingEntity;

/**
 * @author Kishan Rathore
 * @since 1.0.0
 *
 */
@Repository("regAppointmentRepository")
public interface RegAppointmentRepository extends BaseRepository<RegistrationBookingEntity, String> {

	public static final String preIdQuery = "SELECT u FROM RegistrationBookingEntity u WHERE u.preregistrationId = ?1";

	/**
	 * @param currentdate
	 * @return List of RegistrationBookingEntity before current date.
	 */
	@Query(value = "SELECT * FROM reg_appointment u JOIN applications e ON u.prereg_id = e.application_id WHERE e.booking_status_code = :statusCode AND e.appointment_date < :currentDate", nativeQuery = true)
	List<RegistrationBookingEntity> findByRegDateBetween(@Param("statusCode") String statusCode,
			@Param("currentDate") LocalDate currentDate);

	public List<RegistrationBookingEntity> findByRegistrationCenterIdAndRegDate(String registrationCenterId,
			LocalDate regDate);

	@Query("SELECT e FROM RegistrationBookingEntity e  WHERE e.registrationCenterId= ?1 and e.regDate>=?2")
	public List<RegistrationBookingEntity> findByRegId(String registrationCenterId, LocalDate regDate);

	/**
	 * @param preRegId
	 * @return RegistrationBookingEntity of the given Pre Id.
	 */
	@Query(preIdQuery)
	RegistrationBookingEntity getRegistrationAppointmentByPreRegistrationId(@Param("preRegId") String preRegId);

	List<RegistrationBookingEntity> findByRegistrationCenterIdAndRegDateAndSlotFromTimeBetween(String regCenterId,
			LocalDate date, LocalTime fromTime, LocalTime toTime);

	@Query(value = "SELECT * FROM reg_appointment WHERE CAST(booking_dtimes AS DATE) = :bookingDate", nativeQuery = true)
	List<RegistrationBookingEntity> findByBookingPKBookingDate(@Param("bookingDate") LocalDate bookingDate);
}
