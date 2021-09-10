package io.mosip.analytics.event.anonymous.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Anonymous Registration Profile Request DTO
 * 
 * @author Mayura D
 * @since 1.2.0
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class AnonymousProfileResponseDTO {

	/** The id of anonymous profile */
	private String id;

	/** The created by. */
	private String createdBy;

	/** The create date time. */
	private String createdDateTime;

	/** The updated by. */
	private String updatedBy;

	/** The update date time. */
	private String updatedDateTime;

	/**
	 * The anonymous profile json string
	 */
	private String profile;
}
