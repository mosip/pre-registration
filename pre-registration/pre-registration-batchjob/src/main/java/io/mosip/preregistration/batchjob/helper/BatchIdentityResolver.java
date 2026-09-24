package io.mosip.preregistration.batchjob.helper;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.batchjob.code.PreRegBatchContants;
import io.mosip.preregistration.core.common.service.UserDetailsService;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.exception.UserLookupException;
import io.mosip.preregistration.core.util.GenericUtil;

/**
 * Resolves an identifier to its canonical UUID for batch jobs that stamp an actor onto ownership
 * columns.
 *
 * <p>Batch jobs write a fixed actor rather than an end user — a configured audit account, or a job
 * constant. Those values are not personal data, but they are still raw identifiers as far as the
 * rest of the system is concerned: the identity reconciliation treats any non-UUID ownership column
 * as a candidate, so a job that writes its actor raw hands work to reconciliation on every record it
 * touches, and leaves the same logical actor represented two different ways depending on which job
 * wrote the row. Resolving before the write keeps every job writing one stable UUID.
 *
 * <p>Resolution failures return the value unchanged rather than throwing, so a lookup problem
 * degrades a status update to a raw actor instead of failing the job. Reconciliation converts such a
 * value on a later pass.
 */
@Component
public class BatchIdentityResolver {

	private Logger LOGGER = LoggerConfiguration.logConfig(BatchIdentityResolver.class);

	@Autowired
	private UserDetailsService userDetailsService;

	/**
	 * @param userId the raw actor identifier to resolve; null and blank are returned unchanged
	 * @param jobId  the calling job's logging constant, so a failure is attributed to that job
	 * @return the canonical UUID, or {@code userId} unchanged if it could not be resolved
	 */
	public String resolveUserUuid(String userId, String jobId) {
		if (Objects.isNull(userId) || userId.trim().isEmpty()) {
			return userId;
		}
		String trimmedUserId = userId.trim();
		try {
			return userDetailsService.getOrCreateInternalUserId(trimmedUserId);
		} catch (UserLookupException ex) {
			LOGGER.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, jobId,
					"Failed to resolve UUID for masked user " + GenericUtil.maskIdentifier(trimmedUserId)
							+ "; leaving the actor raw for this record.");
			return userId;
		}
	}
}
