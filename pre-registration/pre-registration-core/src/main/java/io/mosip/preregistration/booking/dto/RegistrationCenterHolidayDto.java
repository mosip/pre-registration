package io.mosip.preregistration.booking.dto;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class RegistrationCenterHolidayDto {
	private RegistrationCenterDto registrationCenter;

	private List<HolidayDto> holidays;
}