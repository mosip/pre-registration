package io.mosip.preregistration.booking.dto;

import java.time.LocalTime;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class SlotDto {
	private LocalTime fromTime;

	private LocalTime toTime;

	private int availability;
}