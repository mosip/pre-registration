package io.mosip.preregistration.booking.dto;

import java.time.LocalTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
/**
 * Data transfer object carrying registration center across Pre-Registration APIs.
 */
public class RegistrationCenterDto {
	/**
	 * id used by this Pre-Registration type.
	 */
	private String id;

	/**
	 * name used by this Pre-Registration type.
	 */
	private String name;

	/**
	 * center type code used by this Pre-Registration type.
	 */
	private String centerTypeCode;

	/**
	 * address line1 used by this Pre-Registration type.
	 */
	private String addressLine1;

	/**
	 * address line2 used by this Pre-Registration type.
	 */
	private String addressLine2;

	/**
	 * address line3 used by this Pre-Registration type.
	 */
	private String addressLine3;

	/**
	 * latitude used by this Pre-Registration type.
	 */
	private String latitude;

	/**
	 * longitude used by this Pre-Registration type.
	 */
	private String longitude;

	/**
	 * location code used by this Pre-Registration type.
	 */
	private String locationCode;

	/**
	 * holiday location code used by this Pre-Registration type.
	 */
	private String holidayLocationCode;

	/**
	 * contact phone used by this Pre-Registration type.
	 */
	private String contactPhone;

	/**
	 * number of stations used by this Pre-Registration type.
	 */
	private Short numberOfStations;

	/**
	 * working hours used by this Pre-Registration type.
	 */
	private String workingHours;

	/**
	 * lang code used by this Pre-Registration type.
	 */
	private String langCode;

	/**
	 * number of kiosks used by this Pre-Registration type.
	 */
	private Short numberOfKiosks;

	/**
	 * per kiosk process time used by this Pre-Registration type.
	 */
	private LocalTime perKioskProcessTime;

	/**
	 * center start time used by this Pre-Registration type.
	 */
	private LocalTime centerStartTime;

	/**
	 * center end time used by this Pre-Registration type.
	 */
	private LocalTime centerEndTime;

	/**
	 * time zone used by this Pre-Registration type.
	 */
	private String timeZone;

	/**
	 * contact person used by this Pre-Registration type.
	 */
	private String contactPerson;

	/**
	 * lunch start time used by this Pre-Registration type.
	 */
	private LocalTime lunchStartTime;

	/**
	 * lunch end time used by this Pre-Registration type.
	 */
	private LocalTime lunchEndTime;

	/**
	 * is active used by this Pre-Registration type.
	 */
	private Boolean isActive;
}
