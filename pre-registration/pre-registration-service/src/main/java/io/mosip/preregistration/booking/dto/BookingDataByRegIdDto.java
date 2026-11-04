package io.mosip.preregistration.booking.dto;

import java.time.LocalDate;
import java.util.Map;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Booked pre-ids grouped by appointment date for one registration centre.
 */
@Data
@Getter
@Setter
public class BookingDataByRegIdDto {

	/** Registration centre id. */
	private String registrationCenterId;

	/** pre-id → (date → slot). */
	private Map<String, Map<LocalDate, SlotTimeDto>> idsWithAppointmentDate;
}
