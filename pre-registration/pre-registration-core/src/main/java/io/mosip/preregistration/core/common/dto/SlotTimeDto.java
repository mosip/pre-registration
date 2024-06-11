package io.mosip.preregistration.core.common.dto;

import java.time.LocalTime;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class SlotTimeDto {
	private LocalTime fromTime;
	private LocalTime toTime;
}