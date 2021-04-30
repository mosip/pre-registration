package io.mosip.preregistration.application.errorcodes;

public enum ApplicationErrorMessages {
	
	UNABLE_TO_UPDATE_THE_UI_SPEC("Failed to update the ui spec"),
	
	UNABLE_TO_FETCH_THE_UI_SPEC("Failed to fetch the ui spec"),

	UNABLE_TO_CREATE_THE_UI_SPEC("Failed to save the ui spec"),
	
	FAILED_TO_DELETE_THE_UI_SPEC("Failed to delete the ui spec"),
	
	FAILED_TO_PUBLISH_THE_UI_SPEC("Failed to publish the ui spec"),
	
	UI_SPEC_VALUE_PARSE_ERROR("Error while parsing json");

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
