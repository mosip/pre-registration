package io.mosip.preregistration.booking.dto;

import java.time.LocalTime;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
/**
 * Data transfer object carrying slot across Pre-Registration APIs.
 */
public class SlotDto {
	/**
	 * from time used by this Pre-Registration type.
	 */
	private LocalTime fromTime;

	/**
	 * to time used by this Pre-Registration type.
	 */
	private LocalTime toTime;

	/**
	 * availability used by this Pre-Registration type.
	 */
	private int availability;
}
