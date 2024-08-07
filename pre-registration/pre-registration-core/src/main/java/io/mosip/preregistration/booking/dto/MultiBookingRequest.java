package io.mosip.preregistration.booking.dto;

import java.util.List;

import lombok.Data;

/**
 * @author Kishan Rathore
 * @since 1.0.0
 *
 */
@Data
public class MultiBookingRequest {
	private List<MultiBookingRequestDTO> bookingRequest;
}