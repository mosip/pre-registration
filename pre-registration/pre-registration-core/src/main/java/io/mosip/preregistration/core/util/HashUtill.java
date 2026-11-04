package io.mosip.preregistration.core.util;

import io.mosip.kernel.core.util.HMACUtils;
import io.mosip.kernel.core.util.HashUtils;


/**
 * @author Sanober Noor
 *@since 1.0.0 
 */
public class HashUtill {
	
	/**
	 * Creates a hash utill.
	 */
	private HashUtill() {
	}

	/**
	 * Indicates whether this instance has h utill.
	 *
	 * @param bytes bytes used by hash utill
	 * @return hash utill result
	 */
	public static String hashUtill(byte[] bytes) {
		return HMACUtils.digestAsPlainText(HMACUtils.generateHash(bytes));
	}
	
	/**
	 * Indicates whether hash equal.
	 *
	 * @param source source used by is hash equal
	 * @param target target used by is hash equal
	 * @return is hash equal result
	 */
	public static boolean isHashEqual(byte[] source, byte[] target) {
		return new HashUtils().isDigestEqual(source, target);
	}
}
