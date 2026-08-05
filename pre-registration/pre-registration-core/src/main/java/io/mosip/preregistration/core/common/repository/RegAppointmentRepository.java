package io.mosip.preregistration.core.common.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.preregistration.core.common.entity.RegistrationBookingEntity;

@Repository("regAppointmentRepository")
public interface RegAppointmentRepository extends BaseRepository<RegistrationBookingEntity, String> {

	@Query("SELECT e FROM RegistrationBookingEntity e WHERE e.preregistrationId = ?1")
	RegistrationBookingEntity getRegistrationAppointmentByPreRegistrationId(@Param("preRegId") String preRegId);
}
