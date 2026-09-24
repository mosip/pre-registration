package io.mosip.preregistration.batchjob.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.mosip.preregistration.batchjob.code.PreRegBatchContants;
import io.mosip.preregistration.core.common.service.UserDetailsService;
import io.mosip.preregistration.core.exception.UserLookupException;

@RunWith(JUnit4.class)
public class BatchIdentityResolverTest {

	@InjectMocks
	private BatchIdentityResolver batchIdentityResolver;

	@Mock
	private UserDetailsService userDetailsService;

	private static final String RAW_ACTOR = "mosip-prereg-client";

	private static final String RESOLVED_ACTOR = "11111111-1111-1111-1111-111111111111";

	private static final String JOB_ID = PreRegBatchContants.EXPIRED_STATUS_JOB;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void resolvesRawActorToCanonicalUuid() {
		when(userDetailsService.getOrCreateInternalUserId(RAW_ACTOR)).thenReturn(RESOLVED_ACTOR);

		assertEquals(RESOLVED_ACTOR, batchIdentityResolver.resolveUserUuid(RAW_ACTOR, JOB_ID));
	}

	@Test
	public void trimsBeforeResolving() {
		when(userDetailsService.getOrCreateInternalUserId(RAW_ACTOR)).thenReturn(RESOLVED_ACTOR);

		assertEquals(RESOLVED_ACTOR, batchIdentityResolver.resolveUserUuid("  " + RAW_ACTOR + "  ", JOB_ID));
	}

	/**
	 * A lookup failure must not fail the calling job — the actor stays raw and the identity
	 * reconciliation converts it on a later pass.
	 */
	@Test
	public void returnsValueUnchangedWhenLookupFails() {
		when(userDetailsService.getOrCreateInternalUserId(RAW_ACTOR))
				.thenThrow(new UserLookupException("PRG_CORE_REQ_024", "resolution failed"));

		assertEquals(RAW_ACTOR, batchIdentityResolver.resolveUserUuid(RAW_ACTOR, JOB_ID));
	}

	@Test
	public void returnsNullAndBlankUnchangedWithoutLookup() {
		assertNull(batchIdentityResolver.resolveUserUuid(null, JOB_ID));
		assertEquals("   ", batchIdentityResolver.resolveUserUuid("   ", JOB_ID));
	}
}
