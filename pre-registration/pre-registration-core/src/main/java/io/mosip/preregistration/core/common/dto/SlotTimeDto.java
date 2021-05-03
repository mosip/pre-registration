package io.mosip.preregistration.core.common.dto;

import java.time.LocalTime;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@Getter
@Setter
@NoArgsConstructor
@ToString
public class SlotTimeDto {
	
	private LocalTime fromTime;
	
	private LocalTime toTime;

}
