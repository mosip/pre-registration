/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.core.common.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * This entity class defines the database table details for Booking application.
 * 
 * @author Kishan Rathore
 * @author Jagadishwari
 * @author Ravi C. Balaji
 * @since 1.0.0
 *
 */
@Data
@Entity
@Table(name = "reg_appointment", schema = "prereg")
public class RegistrationBookingEntity implements Serializable {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 7886669943207769620L;

	/** Id. */
	@Id
	@Column(name = "id")
	private String id;

//	/** Booking primary Key. */
//	@Embedded
//	private RegistrationBookingPK bookingPK;

	/** Registration center id. */
	@Column(name = "regcntr_id")
	private String registrationCenterId;

	/**
	 * Pre registration Id
	 */
	@Column(name = "prereg_id")
	private String preregistrationId;

	/**
	 * Booking date and time
	 */
	@Column(name = "booking_dtimes")
	private LocalDateTime bookingDateTime;

	/** Appointment date. */
	@Column(name = "appointment_date")
	private LocalDate regDate;

	/** Slot from time. */
	@Column(name = "slot_from_time")
	private LocalTime slotFromTime;

	/** Slot to time. */
	@Column(name = "slot_to_time")
	private LocalTime slotToTime;

	/** Language code. */
	@Column(name = "lang_code")
	private String langCode;

	/** Created by. */
	@Column(name = "cr_by")
	private String crBy;

	/** Created date time. */
	@Column(name = "cr_dtimes")
	private LocalDateTime crDate;

	/** Created by. */
	@Column(name = "upd_by")
	private String upBy;

	/** Updated date time. */
	@Column(name = "upd_dtimes")
	private LocalDateTime updDate;
}