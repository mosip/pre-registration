package io.mosip.preregistration.core.common.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
	 * Created By (legacy plaintext or user identifier)
	 */
	@Column(name = "cr_by")
	private String crBy;

	/**
	 * Created Date Time
	 */
	@Column(name = "cr_dtimes")
	private LocalDateTime crDtime;

	/**
	 * Updated By (legacy plaintext or user identifier)
	 */
	@Column(name = "upd_by")
	private String updBy;

	/**
	 * Updated Date Time
	 */
	@Column(name = "upd_dtimes")
	private LocalDateTime updDtime;

	/**
	 * Returns {@code cr_by} as stored. <b>Does not canonicalise.</b> The value is a canonical user id
	 * only once the identity migration has reached this row; until then it is still the raw legacy
	 * identifier. Callers that put this on a response or compare it must resolve it themselves —
	 * choosing this accessor over {@link #getCrBy()} changes nothing at runtime.
	 */
	@JsonIgnore
	public String getEffectiveCrBy() {
		return this.crBy;
	}

	/**
	 * Returns {@code upd_by} as stored. <b>Does not canonicalise</b> — see {@link #getEffectiveCrBy()}.
	 */
	@JsonIgnore
	public String getEffectiveUpdBy() {
		return this.updBy;
	}
}