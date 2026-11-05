package io.mosip.preregistration.core.common.dto;

import java.time.LocalDate;
import java.util.Map;

import lombok.Data;

@Data
/**
 * Data transfer object carrying booking data by reg id across Pre-Registration APIs.
 */
public class BookingDataByRegIdDto {
	/**
	 * registration center id used by this Pre-Registration type.
	 */
	private String registrationCenterId;

	/**
	 * ids with appointment date used by this Pre-Registration type.
	 */
	private Map<String, Map<LocalDate, SlotTimeDto>> idsWithAppointmentDate;
}
