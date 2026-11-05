package io.mosip.preregistration.datasync.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;


/**
 * Data transfer object carrying pre reg archive across Pre-Registration APIs.
 */
public class PreRegArchiveDTO {
	@JsonProperty("pre-registration-id")
	@ApiModelProperty(value = "Pre-Registration-ID", position = 1)
	/**
	 * pre registration id used by this Pre-Registration type.
	 */
	private String preRegistrationId;
	
	@JsonProperty("registration-client-id")
	@ApiModelProperty(value = "Registration-Client-Id", position = 2)
	/**
	 * registration center id used by this Pre-Registration type.
	 */
	private String registrationCenterId;
	
	@JsonProperty("appointment-date")
	@ApiModelProperty(value = "Appointment-Date", position = 3)
	/**
	 * appointment date used by this Pre-Registration type.
	 */
	private String appointmentDate;
	
	@JsonProperty("from-time-slot")
	@ApiModelProperty(value = "from-time-slot", position = 4)
	/**
	 * time slot from used by this Pre-Registration type.
	 */
	private String timeSlotFrom;
	
	@JsonProperty("to-time-slot")
	@ApiModelProperty(value = "to-time-slot", position = 5)
	/**
	 * time slot to used by this Pre-Registration type.
	 */
	private String timeSlotTo;   
	
	@JsonProperty("zip-filename")
	@ApiModelProperty(value = "zip-filename", position = 6)
	/**
	 * file name used by this Pre-Registration type.
	 */
	private String fileName;
	
	@JsonProperty("zip-bytes")
	@ApiModelProperty(value = "zip-bytes", position = 7)
	/**
	 * zip bytes used by this Pre-Registration type.
	 */
	private byte[] zipBytes;

	/**
	 * Returns pre registration id.
	 *
	 * @return get pre registration id result
	 */
	public String getPreRegistrationId() {
		return preRegistrationId;
	}

	/**
	 * Sets pre registration id.
	 *
	 * @param preRegistrationId pre registration id used by set pre registration id
	 */
	public void setPreRegistrationId(String preRegistrationId) {
		this.preRegistrationId = preRegistrationId;
	}

	/**
	 * Returns registration center id.
	 *
	 * @return get registration center id result
	 */
	public String getRegistrationCenterId() {
		return registrationCenterId;
	}

	/**
	 * Sets registration center id.
	 *
	 * @param registrationCenterId registration center id used by set registration center id
	 */
	public void setRegistrationCenterId(String registrationCenterId) {
		this.registrationCenterId = registrationCenterId;
	}

	/**
	 * Returns appointment date.
	 *
	 * @return get appointment date result
	 */
	public String getAppointmentDate() {
		return appointmentDate;
	}

	/**
	 * Sets appointment date.
	 *
	 * @param appointmentDate appointment date used by set appointment date
	 */
	public void setAppointmentDate(String appointmentDate) {
		this.appointmentDate = appointmentDate;
	}

	/**
	 * Returns time slot from.
	 *
	 * @return get time slot from result
	 */
	public String getTimeSlotFrom() {
		return timeSlotFrom;
	}

	/**
	 * Sets time slot from.
	 *
	 * @param timeSlotFrom time slot from used by set time slot from
	 */
	public void setTimeSlotFrom(String timeSlotFrom) {
		this.timeSlotFrom = timeSlotFrom;
	}

	/**
	 * Returns time slot to.
	 *
	 * @return get time slot to result
	 */
	public String getTimeSlotTo() {
		return timeSlotTo;
	}

	/**
	 * Sets time slot to.
	 *
	 * @param timeSlotTo time slot to used by set time slot to
	 */
	public void setTimeSlotTo(String timeSlotTo) {
		this.timeSlotTo = timeSlotTo;
	}

	/**
	 * Returns file name.
	 *
	 * @return get file name result
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Sets file name.
	 *
	 * @param fileName file name used by set file name
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Returns zip bytes.
	 *
	 * @return get zip bytes result
	 */
	public byte[] getZipBytes() {
		return zipBytes!=null ? zipBytes.clone():null;
	}

	/**
	 * Sets zip bytes.
	 *
	 * @param zipBytes zip bytes used by set zip bytes
	 */
	public void setZipBytes(byte[] zipBytes) {
		this.zipBytes =zipBytes!=null ? zipBytes.clone():null ;
	}
	
	
}
