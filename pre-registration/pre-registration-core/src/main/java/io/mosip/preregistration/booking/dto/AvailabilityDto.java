package io.mosip.preregistration.booking.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
/**
 * Data transfer object carrying availability across Pre-Registration APIs.
 */
public class AvailabilityDto {
	
	/**
	 * reg center id used by this Pre-Registration type.
	 */
	private String regCenterId;
	
	/**
	 * center details used by this Pre-Registration type.
	 */
	private List<DateTimeDto> centerDetails;
	

}
