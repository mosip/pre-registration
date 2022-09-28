/* 
 * Copyright
 * 
 */
package io.mosip.analytics.event.anonymous.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Anonymous Registration Profile DTO
 * 
 * @author Mayura D
 * @since 1.2.0
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class RegistrationProfileDTO {

	/**
	 * The process name in the Anonymous Registration Profile.
	 */
	private String processName;

	/**
	 * The process stage in the Anonymous Registration Profile.
	 */
	private String processStage;

	/**
	 * The date when Anonymous Registration Profile was created.
	 */
	private LocalDateTime date;

	/**
	 * The yearOfBirth in the Anonymous Registration Profile.
	 */
	private String yearOfBirth;
	
	/**
	 * The gender in the Anonymous Registration Profile.
	 */
	private String gender;

	/**
	 * The location details of the Anonymous Registration Profile.
	 */
	private List<String> location;

	/**
	 * The language preferred for communication by the Anonymous Registration
	 * Profile.
	 */
	private String preferredLanguage;

	/**
	 * The channels available for communication by the Anonymous Registration
	 * Profile.
	 */
	private List<String> channel;

	/**
	 * The Reg Center Id where appointment is booked by the Anonymous Registration
	 * Profile.
	 */
	private String enrollmentCenterId;

	/**
	 * The types of documents uploaded by the Anonymous Registration Profile.
	 */
	private List<String> documents;

	/**
	 * The status of application of the Anonymous Registration Profile.
	 */
	private String status;
}
