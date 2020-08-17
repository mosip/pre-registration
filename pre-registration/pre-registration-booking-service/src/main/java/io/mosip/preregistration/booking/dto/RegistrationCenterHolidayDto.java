package io.mosip.preregistration.booking.dto;

import java.util.List;

import io.mosip.preregistration.booking.dto.HolidayDto;
import io.mosip.preregistration.booking.dto.RegistrationCenterDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class RegistrationCenterHolidayDto {
	private RegistrationCenterDto registrationCenter;
	private List<HolidayDto> holidays;
}
