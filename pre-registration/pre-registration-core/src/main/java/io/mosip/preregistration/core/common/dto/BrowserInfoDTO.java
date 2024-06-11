package io.mosip.preregistration.core.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * This DTO class browser info details.
 * 
 * @author Mayura D
 * @since 1.2.0
 *
 */
@Data
@NoArgsConstructor
@ToString
public class BrowserInfoDTO {
	/** The browserName. */
	private String browserName;

	/** The browserVersion. */
	private String browserVersion;
}