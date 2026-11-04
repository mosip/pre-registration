package io.mosip.preregistration.application.code;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Enumerated document mapping values used by MOSIP Pre-Registration.
 */
public enum DocumentMapping {

	POA("proofOfAddress", "POA"),

	POI("proofOfIdentity", "POI"),

	POR("proofOfRelationship", "POR"),

	POE("proofOfException", "POE");

	/**
	 * attribute name used by this Pre-Registration type.
	 */
	private final String attributeName;

	/**
	 * code used by this Pre-Registration type.
	 */
	private final String code;

	DocumentMapping(String attributeName, String code) {
		this.attributeName = attributeName;
		this.code = code;
	}

	/**
	 * Returns attribute name.
	 *
	 * @return get attribute name result
	 */
	public String getAttributeName() {
		return attributeName;
	}

	/**
	 * Returns code.
	 *
	 * @return get code result
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Returns all mapping.
	 *
	 * @return get all mapping result
	 */
	public static Map<String, String> getAllMapping() {
		return Arrays.stream(values()).parallel()
				.collect(Collectors.toMap(DocumentMapping::getCode, DocumentMapping::getAttributeName));
	}
}
