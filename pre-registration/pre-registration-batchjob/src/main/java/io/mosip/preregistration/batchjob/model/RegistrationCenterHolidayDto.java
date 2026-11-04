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
 * Data transfer object carrying registration center holiday across Pre-Registration APIs.
 */
public class RegistrationCenterHolidayDto {
	/**
	 * registration center used by this Pre-Registration type.
	 */
	private RegistrationCenterDto registrationCenter;
	/**
	 * holidays used by this Pre-Registration type.
	 */
	private List<HolidayDto> holidays;
}
