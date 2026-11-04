package io.mosip.preregistration.booking.dto;

import java.time.LocalTime;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Slot start/end used in booked-pre-id query responses.
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@ToString
public class SlotTimeDto {

	/** Slot start. */
	private LocalTime fromTime;

	/** Slot end. */
	private LocalTime toTime;

	/**
	 * @param fromTime slot start
	 * @param toTime slot end
	 */
	public SlotTimeDto(LocalTime fromTime, LocalTime toTime) {
		this.fromTime = fromTime;
		this.toTime = toTime;
	}
}
