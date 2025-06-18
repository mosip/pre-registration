package io.mosip.preregistration.application.errorcodes;

public enum ApplicationErrorMessages {

	UNABLE_TO_UPDATE_THE_UI_SPEC("Failed to update the ui spec"),

	UNABLE_TO_FETCH_THE_UI_SPEC("Failed to fetch the ui spec"),

	UNABLE_TO_CREATE_THE_UI_SPEC("Failed to save the ui spec"),

	FAILED_TO_DELETE_THE_UI_SPEC("Failed to delete the ui spec"),

	FAILED_TO_PUBLISH_THE_UI_SPEC("Failed to publish the ui spec"),

	UI_SPEC_VALUE_PARSE_ERROR("Error while parsing json"),

	AUDIT_FAILED("Audit Failed"),
	
	DEPRECATED_MESSAGE("The requested resource has been Deprecated"),
	
	FAILED_TO_UPDATE_APPLICATIONS("Save/Update failed for applications table"),
	
	STATUS_UPDATE_FOR_APPLICATIONS_FAILED("Status update failed in applications table"),
	
	DELETE_FAILED_FOR_APPLICATION("Failed to delete an appllication in applications table"),
	
	INVAILD_REQUEST_ARGUMENT("Invaild Request Argument"),
	
	NO_RECORD_FOUND("No Records Found"),
	
	INVALID_REQUEST_APPLICATION_ID("Invalid application Id"),

	INVALID_REQUEST_REGISTRATION_CENTER_ID("Invalid registration center Id"),

	INVALID_APPLICATION_ID_FOR_USER("Requested application id does not belong to the user"),
	
	INVALID_BOOKING_TYPE("Invalid booking type");
	
	
	private ApplicationErrorMessages(String message) {
		this.message = message;
	}

	private final String message;

	/**
	 * @return message
	 */
	public String getMessage() {
		return message;
	}
}
