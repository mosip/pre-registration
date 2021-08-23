package io.mosip.preregistration.core.code;

public enum BookingTypeCodes {

	NEW_PREREGISTRATION("NEW_PREREGISTRATION");

	private String bookingTypeCode;

	BookingTypeCodes(String bookingTypeCode) {
		this.bookingTypeCode = bookingTypeCode;
	}

	public String getBookingTypeCode() {
		return bookingTypeCode;
	}

}
