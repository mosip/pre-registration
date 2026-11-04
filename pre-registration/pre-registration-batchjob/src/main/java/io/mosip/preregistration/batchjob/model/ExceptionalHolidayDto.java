package io.mosip.preregistration.batchjob.model;

import java.io.Serializable;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Data;

/**
 * @author Kishan Rathore
 *
 */
@Data
public class ExceptionalHolidayDto implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6447129753515705510L;

	@NotNull
	/**
	 * holiday date used by this Pre-Registration type.
	 */
	private LocalDate holidayDate;

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

	@NotBlank
	@Size(min = 1, max = 64)
	/**
	 * holiday name used by this Pre-Registration type.
	 */
	private String holidayName;

	@Size(min = 0, max = 128)
	/**
	 * holiday reason used by this Pre-Registration type.
	 */
	private String holidayReason;

	/**
	 * registration center id used by this Pre-Registration type.
	 */
	private String registrationCenterID;

	@NotNull
	/**
	 * is deleted used by this Pre-Registration type.
	 */
	private Boolean isDeleted;

	@NotNull
	/**
	 * lang code used by this Pre-Registration type.
	 */
	private String langCode;

	@NotNull
	/**
	 * is active used by this Pre-Registration type.
	 */
	private Boolean isActive;

}
