package io.mosip.preregistration.batchjob.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.preregistration.batchjob.code.PreRegBatchContants;
import io.mosip.preregistration.batchjob.helper.BatchIdentityResolver;
import io.mosip.preregistration.batchjob.helper.RestHelper;
import io.mosip.preregistration.batchjob.repository.utils.BatchJpaRepositoryImpl;
import io.mosip.preregistration.core.code.StatusCodes;
import io.mosip.preregistration.core.common.entity.ApplicationEntity;
import io.mosip.preregistration.core.common.entity.DemographicEntity;
import io.mosip.preregistration.core.common.entity.RegistrationBookingEntity;

/**
 * The job stamps a fixed actor onto ownership columns. It must write that actor's canonical UUID,
 * not the raw configured value, so the rows it touches do not become identity-reconciliation
 * candidates.
 */
@RunWith(JUnit4.class)
public class ApplicationExpiredStatusUpdaterTest {

	@InjectMocks
	private ApplicationExpiredStatusUpdater applicationExpiredStatusUpdater;

	@Mock
	private BatchJpaRepositoryImpl batchServiceDAO;

	@Mock
	private RestHelper restHelper;

	@Mock
	private BatchIdentityResolver batchIdentityResolver;

	private static final String PRID = "12345678901234";

	private static final String RAW_ACTOR = "mosip-prereg-client";

	private static final String RESOLVED_ACTOR = "11111111-1111-1111-1111-111111111111";

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		ReflectionTestUtils.setField(applicationExpiredStatusUpdater, "auditUserId", RAW_ACTOR);
		ReflectionTestUtils.setField(applicationExpiredStatusUpdater, "auditUsername", "batch-user");
		when(batchIdentityResolver.resolveUserUuid(RAW_ACTOR, PreRegBatchContants.EXPIRED_STATUS_JOB))
				.thenReturn(RESOLVED_ACTOR);
	}

	private RegistrationBookingEntity booking(String preRegId) {
		RegistrationBookingEntity booking = new RegistrationBookingEntity();
		booking.setPreregistrationId(preRegId);
		return booking;
	}

	@Test
	public void writesResolvedActorToApplicationAndDemographic() {
		when(batchServiceDAO.getAllOldDateBooking()).thenReturn(List.of(booking(PRID)));

		ApplicationEntity application = new ApplicationEntity();
		when(batchServiceDAO.getBookedApplicantEntityDetails(PRID)).thenReturn(application);

		DemographicEntity demographic = new DemographicEntity();
		demographic.setStatusCode(StatusCodes.BOOKED.getCode());
		when(batchServiceDAO.getApplicantDemographicDetails(PRID)).thenReturn(demographic);

		applicationExpiredStatusUpdater.updateExpiredStatus();

		assertEquals(RESOLVED_ACTOR, application.getUpdBy());
		assertEquals(RESOLVED_ACTOR, demographic.getUpdatedBy());
		assertEquals(StatusCodes.EXPIRED.getCode(), application.getBookingStatusCode());
		assertEquals(StatusCodes.EXPIRED.getCode(), demographic.getStatusCode());
	}

	/**
	 * The actor is the same for every row in a run, so it is resolved once before the loop rather
	 * than once per record.
	 */
	@Test
	public void resolvesActorOncePerRunNotPerRecord() {
		when(batchServiceDAO.getAllOldDateBooking())
				.thenReturn(Arrays.asList(booking(PRID), booking("99999999999999")));
		when(batchServiceDAO.getBookedApplicantEntityDetails(org.mockito.ArgumentMatchers.anyString()))
				.thenReturn(new ApplicationEntity());
		when(batchServiceDAO.getApplicantDemographicDetails(org.mockito.ArgumentMatchers.anyString()))
				.thenReturn(null);

		applicationExpiredStatusUpdater.updateExpiredStatus();

		verify(batchIdentityResolver, times(1)).resolveUserUuid(RAW_ACTOR,
				PreRegBatchContants.EXPIRED_STATUS_JOB);
	}
}
