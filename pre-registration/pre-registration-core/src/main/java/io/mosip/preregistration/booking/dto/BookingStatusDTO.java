package io.mosip.preregistration.booking.dto;

import lombok.Data;

@Data
/**
 * Data transfer object carrying booking status across Pre-Registration APIs.
 */
public class BookingStatusDTO {
	/**
	 * booking message used by this Pre-Registration type.
	 */
	private String bookingMessage;
}
