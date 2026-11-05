package io.mosip.preregistration.application.dto;

import lombok.Data;

@Data
/**
 * Data transfer object carrying application detail response across Pre-Registration APIs.
 */
public class ApplicationDetailResponseDTO {

	/**
	 * application id used by this Pre-Registration type.
	 */
	private String applicationId;

	/**
	 * application status code used by this Pre-Registration type.
	 */
	private String applicationStatusCode;

	/**
	 * booking status code used by this Pre-Registration type.
	 */
	private String bookingStatusCode;

	/**
	 * appointment date used by this Pre-Registration type.
	 */
	private String appointmentDate;

	/**
	 * slot from time used by this Pre-Registration type.
	 */
	private String slotFromTime;

	/**
	 * slot to time used by this Pre-Registration type.
	 */
	private String slotToTime;

	/**
	 * cr by used by this Pre-Registration type.
	 */
	private String crBy;

	/**
	 * cr dtime used by this Pre-Registration type.
	 */
	private String crDtime;

	/**
	 * registration center id used by this Pre-Registration type.
	 */
	private String registrationCenterId;
	
	/**
	 * booking type used by this Pre-Registration type.
	 */
	private String bookingType;

}
