package io.mosip.preregistration.core.common.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.preregistration.core.common.entity.RegistrationBookingEntity;

/**
 * Bean name is deliberately core-scoped. pre-registration-batchjob declares its
 * own {@code io.mosip.preregistration.batchjob.repository.RegAppointmentRepository}
 * as {@code @Repository("regAppointmentRepository")}; both are picked up by the
 * kernel's {@code @EnableJpaRepositories}, and an unqualified name here collides
 * and fails batchjob startup.
 */
@Repository("coreRegAppointmentRepository")
public interface RegAppointmentRepository extends BaseRepository<RegistrationBookingEntity, String> {

	@Query("SELECT e FROM RegistrationBookingEntity e WHERE e.preregistrationId = ?1")
	RegistrationBookingEntity getRegistrationAppointmentByPreRegistrationId(@Param("preRegId") String preRegId);
}
