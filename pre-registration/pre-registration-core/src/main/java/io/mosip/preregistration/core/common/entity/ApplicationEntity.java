package io.mosip.preregistration.core.common.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "applications", schema = "prereg")
public class ApplicationEntity {

	@Id
	@Column(name = "application_id")
	private String applicationId;

	/** Booking Type. **/
	@Column(name = "booking_type", nullable = false)
	private String bookingType;

	/** Booking status. **/
	@Column(name = "booking_status_code")
	private String bookingStatusCode;

	/** Application status. **/
	@Column(name = "application_status_code")
	private String applicationStatusCode;

	/** Appointment date. **/
	@Column(name = "appointment_date")
	private LocalDate appointmentDate;

	/** Booking date. **/
	@Column(name = "booking_date")
	private LocalDate bookingDate;

	/** Registration center id. */
	@Column(name = "regcntr_id")
	private String registrationCenterId;

	/** Slot from time. */
	@Column(name = "slot_from_time")
	private LocalTime slotFromTime;

	/** Slot to time. */
	@Column(name = "slot_to_time")
	private LocalTime slotToTime;

	@Column(name = "contact_info")
	private String contactInfo;

	/**
	 * Created By
	 */
	@Column(name = "cr_by")
	private String crBy;

	/**
	 * Created Date Time
	 */
	@Column(name = "cr_dtimes")
	private LocalDateTime crDtime;

	/**
	 * Updated By
	 */
	@Column(name = "upd_by")
	private String updBy;

	/**
	 * Updated Date Time
	 */
	@Column(name = "upd_dtimes")
	private LocalDateTime updDtime;

}
