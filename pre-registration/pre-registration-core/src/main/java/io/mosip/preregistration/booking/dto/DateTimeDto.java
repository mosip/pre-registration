package io.mosip.preregistration.booking.dto;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
/**
 * Data transfer object carrying date time across Pre-Registration APIs.
 */
public class DateTimeDto {
	/**
	 * date used by this Pre-Registration type.
	 */
	private String date;

	/**
	 * is holiday used by this Pre-Registration type.
	 */
	private boolean isHoliday;

	/**
	 * time slots used by this Pre-Registration type.
	 */
	private List<SlotDto> timeSlots;
}
