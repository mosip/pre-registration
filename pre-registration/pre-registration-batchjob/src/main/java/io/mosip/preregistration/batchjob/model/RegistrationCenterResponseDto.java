package io.mosip.preregistration.batchjob.model;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
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
