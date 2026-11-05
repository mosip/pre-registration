package io.mosip.preregistration.booking.dto;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
/**
 * Data transfer object carrying registration center response across Pre-Registration APIs.
 */
public class RegistrationCenterResponseDto {
	/**
	 * registration centers used by this Pre-Registration type.
	 */
	private List<RegistrationCenterDto> registrationCenters;
}
