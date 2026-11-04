package io.mosip.preregistration.core.common.dto;

import java.time.LocalTime;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
/**
 * Data transfer object carrying slot time across Pre-Registration APIs.
 */
public class SlotTimeDto {
	/**
	 * from time used by this Pre-Registration type.
	 */
	private LocalTime fromTime;

	/**
	 * to time used by this Pre-Registration type.
	 */
	private LocalTime toTime;
}
