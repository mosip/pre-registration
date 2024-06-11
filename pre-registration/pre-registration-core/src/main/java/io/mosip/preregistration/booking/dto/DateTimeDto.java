package io.mosip.preregistration.booking.dto;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class DateTimeDto {
	private String date;

	private boolean isHoliday;

	private List<SlotDto> timeSlots;
}