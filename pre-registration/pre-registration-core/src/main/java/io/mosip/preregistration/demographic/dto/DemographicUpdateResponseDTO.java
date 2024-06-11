/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.demographic.dto;

import java.io.Serializable;

import org.json.simple.JSONObject;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * This DTO class defines the variables to accept the input parameter from
 * request.
 * 
 * @author Rajath KR
 * @since 1.0.0
 *
 */
@Data
@NoArgsConstructor
@ToString
public class DemographicUpdateResponseDTO implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 6705845720255847210L;

	/** The pre-registration-Id. */
	private String preRegistrationId;

	/** The update date time. */
	private String updatedDateTime;

	/**
	 * status code
	 */
	private String statusCode;

	/**
	 * language code
	 */
	private String langCode;

	/**
	 * Demographic Json details
	 */
	private JSONObject demographicDetails;
}