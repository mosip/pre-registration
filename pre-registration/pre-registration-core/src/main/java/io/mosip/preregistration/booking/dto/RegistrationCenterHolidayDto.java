package io.mosip.preregistration.booking.dto;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
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
