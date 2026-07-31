package io.mosip.preregistration.core.util.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.mosip.preregistration.core.util.GenericUtil;

/**
 * GenericUtil Test
 * 
 * @version 1.0.0
 * @author M1043226
 *
 */

public class GenericUtilTest {

	@Autowired
	GenericUtil genericUtil;
	
	private static String dateTimeFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	@Test
	public void getCurrentResponseTimeTest() {
		String time=GenericUtil.getCurrentResponseTime();
		assertNotNull(time);
	}

	/**
	 * Short, low-entropy identifiers (admin / operator accounts) are exactly the ones likely to reach
	 * logs, so the visible suffix scales with length instead of being a fixed 4 characters.
	 */
	@Test
	public void maskIdentifierScalesVisibleSuffixWithLength() {
		// "admin" previously exposed 4 of 5 characters as ***dmin.
		assertEquals("***n", GenericUtil.maskIdentifier("admin"));
		// 11 chars -> 11/3 = 3 visible
		assertEquals("***tor", GenericUtil.maskIdentifier("regoperator"));
		// 16 chars -> 16/3 = 5, capped at 4 visible
		assertEquals("***oper", GenericUtil.maskIdentifier("registrationoper"));
	}

	/** Anything too short to keep a suffix from is masked outright. */
	@Test
	public void maskIdentifierFullyMasksVeryShortValues() {
		assertEquals("***", GenericUtil.maskIdentifier("ab"));
		assertEquals("***", GenericUtil.maskIdentifier("x"));
	}

	/** Never reveals more than a third of the value, and never more than 4 characters. */
	@Test
	public void maskIdentifierNeverRevealsMoreThanAThirdOfTheValue() {
		for (int length = 1; length <= 40; length++) {
			String value = "a".repeat(length);
			String masked = GenericUtil.maskIdentifier(value);
			int revealed = masked.length() - "***".length();
			assertTrue("revealed too much for length " + length, revealed <= Math.max(0, length / 3));
			assertTrue("revealed more than 4 for length " + length, revealed <= 4);
		}
	}

	/** The dedicated email, phone and UUID branches are unaffected by the fallback change. */
	@Test
	public void maskIdentifierKeepsTypedBranchesUnchanged() {
		assertEquals("j***@example.com", GenericUtil.maskIdentifier("john@example.com"));
		assertEquals("******7890", GenericUtil.maskIdentifier("1234567890"));
		assertEquals("***000001",
				GenericUtil.maskIdentifier("00000000-0000-0000-0000-000000000001"));
		assertEquals("<empty>", GenericUtil.maskIdentifier(null));
		assertEquals("<empty>", GenericUtil.maskIdentifier("   "));
	}

	/**
	 * Collapsed from three byte-identical private copies in DemographicServiceUtil,
	 * DocumentServiceUtil and DataSyncServiceUtil; behaviour must stay exactly as it was.
	 */
	@Test
	public void isCanonicalAppliedReportsOnlyRealConversions() {
		// Raw resolved to a different canonical value — a genuine conversion.
		assertTrue(GenericUtil.isCanonicalApplied("user@example.com",
				"00000000-0000-0000-0000-000000000001"));
		// Already canonical, resolution was a no-op.
		assertFalse(GenericUtil.isCanonicalApplied("00000000-0000-0000-0000-000000000001",
				"00000000-0000-0000-0000-000000000001"));
		// Whitespace-only difference is not a conversion.
		assertFalse(GenericUtil.isCanonicalApplied("  user@example.com  ", "user@example.com"));
		// Nothing resolved.
		assertFalse(GenericUtil.isCanonicalApplied("user@example.com", null));
		assertFalse(GenericUtil.isCanonicalApplied("user@example.com", "   "));
		// Null original with a resolved value still counts as applied.
		assertTrue(GenericUtil.isCanonicalApplied(null, "00000000-0000-0000-0000-000000000001"));
	}

}