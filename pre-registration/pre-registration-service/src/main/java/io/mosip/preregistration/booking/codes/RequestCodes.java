package io.mosip.preregistration.booking.codes;

/**
 * JSON / persistence field names used when reading MOSIP booking requests.
 */
public enum RequestCodes {

	/** Envelope id. */
	id("id"),

	/** Envelope version. */
	version("version"),

	/** Envelope request timestamp. */
	requesttime("requesttime"),

	/** Envelope request object. */
	request("request"),

	/** Pre-registration identifier. */
	PRE_REGISTRAION_ID("preRegistrationId"),

	/** Appointment date column / JSON key. */
	REG_DATE("appointment_date"),

	/** Slot start time column / JSON key. */
	FROM_SLOT_TIME("time_slot_from");

	/**
	 * @param code wire or column name
	 */
	private RequestCodes(String code) {
		this.code = code;
	}

	/** Canonical code stored with this enum constant. */
	private final String code;

	/**
	 * @return the wire or column name
	 */
	public String getCode() {
		return code;
	}
}
