package io.mosip.preregistration.booking.dto;

import java.util.List;

import io.mosip.preregistration.booking.dto.MultiBookingRequestDTO;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Kishan Rathore
 * @since 1.0.0
 *
 */
@Getter
@Setter
public class MultiBookingRequest {

	List<MultiBookingRequestDTO> bookingRequest;
}
