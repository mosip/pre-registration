package io.mosip.preregistration.core.code;

/**
 * Enumerated application status code values used by MOSIP Pre-Registration.
 */
public enum ApplicationStatusCode {
	DRAFT("DRAFT"),

	SUBMITTED("SUBMITTED"),

	CLOSED("CLOSED");

	ApplicationStatusCode(String code) {
		this.applicationStatusCode = code;
	}

	/**
	 * application status code used by this Pre-Registration type.
	 */
	private String applicationStatusCode;

	/**
	 * Returns application status code.
	 *
	 * @return get application status code result
	 */
	public String getApplicationStatusCode() {
		return applicationStatusCode;
	}
}
