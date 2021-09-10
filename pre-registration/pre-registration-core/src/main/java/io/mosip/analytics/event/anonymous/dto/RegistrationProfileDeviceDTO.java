/* 
 * Copyright
 * 
 */
package io.mosip.analytics.event.anonymous.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Anonymous Registration Profile Device DTO
 * 
 * @author Mayura D
 * @since 1.2.0
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class RegistrationProfileDeviceDTO {

	/**
	 * Name of the browser from which Pre-Registration is being done.
	 */
	private String browser;
	
	/**
	 * Version of the browser from which Pre-Registration is being done.
	 */
	private String browserVersion;

}
