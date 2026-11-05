package io.mosip.preregistration.booking.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
/**
 * Data transfer object carrying holiday across Pre-Registration APIs.
 */
public class HolidayDto {
	/**
	 * holiday id used by this Pre-Registration type.
	 */
	private String holidayId;

	/**
	 * holiday date used by this Pre-Registration type.
	 */
	private String holidayDate;
	
	/**
	 * Holiday day is day of week as integer value, week start from Monday , Monday
	 * is 1 and Sunday is 7
	 */
	private String holidayDay;
	
	/**
	 * Holiday month is month of the year as integer value.
	 */
	private String holidayMonth;
	
	/**
	 * holiday year used by this Pre-Registration type.
	 */
	private String holidayYear;
	
	/**
	 * holiday name used by this Pre-Registration type.
	 */
	private String holidayName;
	
	/**
	 * language code used by this Pre-Registration type.
	 */
	private String languageCode;
	
	/**
	 * is active used by this Pre-Registration type.
	 */
	private Boolean isActive;
}
