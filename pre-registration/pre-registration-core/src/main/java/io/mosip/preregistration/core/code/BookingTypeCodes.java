package io.mosip.preregistration.core.code;

/**
 * Enumerated booking type codes values used by MOSIP Pre-Registration.
 */
public enum BookingTypeCodes {

	NEW_PREREGISTRATION("NEW_PREREGISTRATION"),

	UPDATE_REGISTRATION("UPDATE_REGISTRATION"),

	LOST_FORGOTTEN_UIN("LOST_FORGOTTEN_UIN");

	BookingTypeCodes(String bookingTypeCode) {
		this.bookingTypeCode = bookingTypeCode;
	}

	/**
	 * booking type code used by this Pre-Registration type.
	 */
	private String bookingTypeCode;

	/**
	 * Returns booking type code.
	 *
	 * @return get booking type code result
	 */
	public String getBookingTypeCode() {
		return bookingTypeCode;
	}
}
