package io.mosip.preregistration.core.common.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This DTO class browser info details.
 * 
 * @author Mayura D
 * @since 1.2.0
 *
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class BrowserInfoDTO {
	
	/** The browserName. */
	private String browserName;
	
	/** The browserVersion. */
	private String browserVersion;

}
