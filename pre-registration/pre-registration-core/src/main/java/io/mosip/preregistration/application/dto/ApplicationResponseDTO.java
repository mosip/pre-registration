package io.mosip.preregistration.application.dto;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * This DTO class defines the variables to accept the input parameter from
 * request.
 * 
 * @author Mayura D
 * @since 1.2.0
 *
 */
@Data
@NoArgsConstructor
@ToString
public class ApplicationResponseDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -667562552980002417L;

	/** The application-Id. */
	private String applicationId;

	/** The created by. */
	private String createdBy;

	/** The create date time. */
	private String createdDateTime;

	/** The updated by. */
	private String updatedBy;

	/** The update date time. */
	private String updatedDateTime;

	/**
	 * application status code
	 */
	private String applicationStatusCode;

	/**
	 * booking status code
	 */
	private String bookingStatusCode;

	/**
	 * language code
	 */
	private String langCode;

	/**
	 * the booking type
	 */
	private String bookingType;
}