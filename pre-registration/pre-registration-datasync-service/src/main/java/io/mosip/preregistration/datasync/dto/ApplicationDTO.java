package io.mosip.preregistration.datasync.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ApplicationDTO  implements Serializable {

	private static final long serialVersionUID = -3846790016362370645L;
	@ApiModelProperty(value = "Transaction ID", position = 1)
	private String applicationId;
	@ApiModelProperty(value = "Appointment Dt Time", position = 2)
	private String appointmentDtTime;
	@ApiModelProperty(value = "Booking Type", position = 3)
	private String bookingType;
}
