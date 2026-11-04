package io.mosip.preregistration.pipeline;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import io.mosip.preregistration.application.service.ApplicationServiceIntf;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;

/**
 * SDK adapter for application lookups within the consolidated Pre-Registration JVM.
 * <p>
 * Booking used to HTTP-call {@code /applications/status/{id}}. That now delegates to
 * {@link ApplicationServiceIntf} (no internal HTTP). Same pattern as id-repository
 * {@code InProcessIdentityClient}.
 * </p>
 *
 * @see InProcessBookingClient
 * @see ApplicationServiceIntf#getApplicationStatus(String)
 */
@Component
public class InProcessApplicationClient {

	@Autowired
	@Lazy
	private ApplicationServiceIntf applicationService;

	/**
	 * @param applicationId application / pre-registration id
	 * @return status envelope
	 */
	public MainResponseDTO<String> getApplicationStatus(String applicationId) {
		return applicationService.getApplicationStatus(applicationId);
	}
}
