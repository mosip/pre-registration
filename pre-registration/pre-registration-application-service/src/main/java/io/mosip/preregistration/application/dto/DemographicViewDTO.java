/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.application.dto;

import java.io.Serializable;
import java.util.Set;

import org.json.simple.JSONObject;

import io.mosip.preregistration.core.common.dto.BookingRegistrationDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * View registration response DTO
 * 
 * @author Rupika
 * @since 1.0.0
 */

@Getter
@Setter
@NoArgsConstructor
@ToString
public class DemographicViewDTO implements Serializable {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -2770204280374548395L;

	/**
	 * PreRegistration Id
	 */
	private String preRegistrationId;

	/**
	 * Status code
	 */
	private String statusCode;
	/**
	 * BookingRegistrationDTO object
	 */
	private BookingRegistrationDTO bookingMetadata;

	/**
	 * Document response DTO
	 */
	private JSONObject demographicMetadata;

	/**
	 * data captured langauage
	 */

	private Set<String> dataCaptureLanguage;

}
