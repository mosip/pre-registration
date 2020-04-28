/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.core.common.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

/**
 * This entity class defines the database table details for Booking application.
 * 
 * @author Kishan Rathore
 * @author Jagadishwari
 * @author Ravi C. Balaji
 * @since 1.0.0
 *
 */
@Getter
@Setter
@Entity
@Table(name = "reg_appointment", schema = "prereg")
public class RegistrationBookingEntity implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 7886669943207769620L;

	@OneToOne
	@JoinColumn(name = "prereg_id", nullable = false)
	private DemographicEntity demographicEntity;

	/** Id. */
	@Id
	@Column(name = "id")
	private String id;

	/** Booking primary Key. */
	@Embedded
	private RegistrationBookingPK bookingPK;

	/** Registration center id. */
	@Column(name = "regcntr_id")
	private String registrationCenterId;

	/** Slot from time. */
	@Column(name = "slot_from_time")
	private LocalTime slotFromTime;

	/** Slot to time. */
	@Column(name = "slot_to_time")
	private LocalTime slotToTime;

	/** Appointment date. */
	@Column(name = "appointment_date")
	private LocalDate regDate;

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
