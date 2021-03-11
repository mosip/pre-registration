package io.mosip.preregistration.booking.dto;

import java.util.List;

import io.mosip.preregistration.booking.dto.SlotDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class DateTimeDto {

	private String date;

	private boolean isHoliday;

	private List<SlotDto> timeSlots;
}
