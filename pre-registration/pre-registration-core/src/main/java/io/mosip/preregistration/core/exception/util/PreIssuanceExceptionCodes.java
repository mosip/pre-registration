package io.mosip.preregistration.core.exception.util;

/**
 * Class supporting pre issuance exception codes in MOSIP Pre-Registration (io.mosip.preregistration.core.exception.util).
 */
public final class PreIssuanceExceptionCodes {
	
	/**
	 * pre id login prefix used by this Pre-Registration type.
	 */
	private static final String PRE_ID_LOGIN_PREFIX = "REG-AUTH-LOGIN";
	/**
	 * pre id update prefix used by this Pre-Registration type.
	 */
	private static final String PRE_ID_UPDATE_PREFIX = "REG-AUTH-UPDATE";
	/**
	 * pre id registartion prefix used by this Pre-Registration type.
	 */
	private static final String PRE_ID_REGISTARTION_PREFIX = "REG-AUTH-REGISTRATION";
	

	/**
	 * Creates a pre issuance exception codes.
	 */
	private PreIssuanceExceptionCodes() {
		throw new IllegalStateException("Utility class");
	}
	
	
	/**
	 * invalid user name used by this Pre-Registration type.
	 */
	public static final String INVALID_USER_NAME = PRE_ID_LOGIN_PREFIX+"001-";
	/**
	 * user insertion used by this Pre-Registration type.
	 */
	public static final String USER_INSERTION = PRE_ID_LOGIN_PREFIX+"002-";
	/**
	 * user already exist used by this Pre-Registration type.
	 */
	public static final String USER_ALREADY_EXIST = PRE_ID_UPDATE_PREFIX+"003-";
	/**
	 * table not found exception used by this Pre-Registration type.
	 */
	public static final String TABLE_NOT_FOUND_EXCEPTION = PRE_ID_REGISTARTION_PREFIX+"004-";
	
}
