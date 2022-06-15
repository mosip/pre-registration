package io.mosip.preregistration.datasync.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
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
