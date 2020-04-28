/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.core.code;

/**
 * 
 * This Enum provides the constant variables to accept input request.
 * 
 * @author Kishan Rathore
 * @author Jagadishwari
 * @author Ravi C. Balaji
 * @since 1.0.0
 *
 */
public enum StatusCodes {
	
	/* Pending_Appointment */
	PENDING_APPOINTMENT("Pending_Appointment"),
	
	/* Booked */
	BOOKED("Booked"),
	
	/* Expired */
	EXPIRED("Expired"), 
	
	/* Consumed */
	CONSUMED("Consumed"), 
	
	/**
	 * Canceled
	 */
	CANCELED("Canceled"),
	
	/**
	 * Document_Uploaded
	 */
	DOCUMENT_UPLOADED("Document_Uploaded");
	
	/**
	 * @param code
	 */
	private StatusCodes(String code) {
		this.code = code;
	}

	private final String code;

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}
}
