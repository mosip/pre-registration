/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.batchjob.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This entity class defines the database table details for Booking application.
 * 
 * @author Kishan Rathore
 * @since 1.0.0
 *
 */
@Embeddable
@Setter
@NoArgsConstructor
public class RegistrationBookingPKConsumed implements Serializable{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -1892490805541821923L;

	/**
	 * Pre registration Id
	 */
	@Column(name="prereg_id")
	private String preregistrationId;
	
	/**
	 * Booking date and time
	 */
	@Column(name="booking_dtimes")
	private LocalDateTime bookingDateTime;
	
}
