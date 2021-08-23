package io.mosip.preregistration.application.dto;

import lombok.Data;

@Data
public class ApplicationDetailResponseDTO {

	private String applicationId;

	private String applicationStatusCode;

	private String bookingStatusCode;

	private String appointmentDate;

	private String slotFromTime;

	private String slotToTime;

	private String crBy;

	private String crDtime;

	private String registrationCenterId;
	
	private String bookingType;

}
