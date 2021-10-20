package io.mosip.preregistration.core.code;

public enum BookingTypeCodes {

	NEW_PREREGISTRATION("NEW_PREREGISTRATION"),

	UPDATE_REGISTRATION_DETAILS("UPDATE_REGISTRATION_DETAILS"),

	LOST_FORGOTTEN_UIN("LOST_FORGOTTEN_UIN");

	BookingTypeCodes(String bookingTypeCode) {
		this.bookingTypeCode = bookingTypeCode;
	}

	private String bookingTypeCode;

	public String getBookingTypeCode() {
		return bookingTypeCode;
	}

}
