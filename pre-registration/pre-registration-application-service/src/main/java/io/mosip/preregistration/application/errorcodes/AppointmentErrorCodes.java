package io.mosip.preregistration.application.errorcodes;

public enum AppointmentErrorCodes {

	FAILED_TO_FETCH_AVAILABLITY("PRG_APP_BCK_01", "Failed to fetch availablity"),

	BOOKING_FAILED("PRG_APP_BCK_02", "Appointment Booking failed"),

	FAILED_TO_DELETE_APPOINTMENT("PRG_APP_BCK_03", "Failed to delete the appointment"),

	CANCEL_APPOINTMENT_FAILED("PRG_APP_BCK_04", "Cancelling appointment failed"),

	FAILED_TO_FETCH_APPOINTMENT_DETAILS("PRG_APP_BCK_05", "Failed to fetch appointment details"),

	MULTI_BOOKING_FAILED("PRG_APP_BCK_06", "Appointment Booking failed"),

	FAILED_TO_UPDATE_APPLICATIONS("PRG_APP_BCK_07","Failed to update Appointment for the %s"),
	
	INVALID_APP_ID_FOR_USER("PRG_APP_BCK_08", "Requested application id does not belong to the user");

	private String code;
	private String message;

	AppointmentErrorCodes(String code, String message) {
		this.code = code;
		this.message = message;
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

}
