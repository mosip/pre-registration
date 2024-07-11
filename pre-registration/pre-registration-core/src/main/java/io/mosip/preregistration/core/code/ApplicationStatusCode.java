package io.mosip.preregistration.core.code;

public enum ApplicationStatusCode {
	DRAFT("DRAFT"),

	SUBMITTED("SUBMITTED"),

	CLOSED("CLOSED");

	ApplicationStatusCode(String code) {
		this.applicationStatusCode = code;
	}

	private String applicationStatusCode;

	public String getApplicationStatusCode() {
		return applicationStatusCode;
	}
}