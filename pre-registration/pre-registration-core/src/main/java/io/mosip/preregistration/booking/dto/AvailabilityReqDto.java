package io.mosip.preregistration.booking.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
/**
 * Data transfer object carrying availability req across Pre-Registration APIs.
 */
public class AvailabilityReqDto {
 /**
  * registration center id used by this Pre-Registration type.
  */
 private String registrationCenterId;

}
