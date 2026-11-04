package io.mosip.preregistration.core.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
/**
 * Data transfer object carrying cancel booking response across Pre-Registration APIs.
 */
public class CancelBookingResponseDTO {
	/**
	 * transaction id used by this Pre-Registration type.
	 */
	private String transactionId;

	/**
	 * message used by this Pre-Registration type.
	 */
	private String message;
}
