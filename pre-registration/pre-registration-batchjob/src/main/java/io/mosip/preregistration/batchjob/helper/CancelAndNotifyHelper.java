package io.mosip.preregistration.batchjob.helper;

import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.preregistration.batchjob.repository.utils.BatchJpaRepositoryImpl;
import io.mosip.preregistration.core.common.entity.ApplicationEntity;
import io.mosip.preregistration.core.common.entity.RegistrationBookingEntity;

/**
 * @author Mahammed Taheer
 * @since 1.2.0
 *
 */
@Component
public class CancelAndNotifyHelper {
    
    /** 
	 * Autowired reference for {@link #batchServiceDAO}
	 */
	@Autowired
	private BatchJpaRepositoryImpl batchServiceDAO;

    @Autowired
	private RestHelper restHelper;

    public void cancelAndNotifyApplicant(RegistrationBookingEntity bookedSlot, String logIdentifier, Map<String, Boolean> cancelledTracker,
						Map<String, Boolean> notifierTracker) {

		String preRegId = bookedSlot.getPreregistrationId();
		ApplicationEntity bookedApplication  = batchServiceDAO.getBookedApplicantEntityDetails(preRegId);
		if (Objects.nonNull(bookedApplication)) {
			boolean cancelled = restHelper.cancelBookedApplication(preRegId, logIdentifier);
			if (cancelled) {
				boolean notified = restHelper.sendCancelledNotification(bookedSlot.getPreregistrationId(), bookedSlot.getRegDate().toString(), 
							bookedSlot.getSlotFromTime().toString(), bookedSlot.getLangCode(), logIdentifier);
				notifierTracker.put(bookedSlot.getPreregistrationId(), notified);
			}
			cancelledTracker.put(bookedSlot.getPreregistrationId(), cancelled);
		}
	}
}
