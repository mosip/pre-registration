/**
 * 
 */
package io.mosip.preregistration.core.application.repository;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.preregistration.core.common.entity.ApplicationEntity;

/**
 * @author Aiham H
 *
 */

@Repository
@Transactional
public interface ApplicationRepository extends BaseRepository<ApplicationEntity, String> {

	@Query("SELECT u.bookingType FROM ApplicationEntity u where u.applicationId =?1")
	public String findBookingTypeById(String applicationId);

}
