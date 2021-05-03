package io.mosip.preregistration.core.common.dto;

import java.time.LocalDate;
import java.util.Map;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class BookingDataByRegIdDto {

	private String registrationCenterId;

	private Map<String, Map<LocalDate, SlotTimeDto>> idsWithAppointmentDate;

}
