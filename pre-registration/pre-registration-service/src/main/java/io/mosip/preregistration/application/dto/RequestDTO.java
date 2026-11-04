package io.mosip.preregistration.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
/**
 * Data transfer object carrying request across Pre-Registration APIs.
 */
public class RequestDTO {
	
	/**
	 * key used by this Pre-Registration type.
	 */
	private String key;

}
