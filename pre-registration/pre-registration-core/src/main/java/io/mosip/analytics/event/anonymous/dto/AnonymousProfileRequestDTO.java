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
public class AnonymousProfileRequestDTO {
	
	/**
	 * The anonymous profile json object
	 */
	private String profileDetails;

}
