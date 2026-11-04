package io.mosip.preregistration.core.common.dto;

import lombok.Data;

/**
 * @author Sanober Noor
 * @since 1.0.0
 */
@Data
public class NotificationResponseDTO {
	/**
	 * status used by this Pre-Registration type.
	 */
	private String status;

	/**
	 * message used by this Pre-Registration type.
	 */
	private String message;
}
