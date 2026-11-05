package io.mosip.preregistration.application.dto;

import lombok.Data;

/**
 * The DTO for template response.
 * 
 * @author Sanober Noor
 * @since 1.0.0
 */
@Data
public class NotificationResponseDTO {
	/**
	 * The list of {@link TemplateResponseDTO}.
	 */
	String message;
}
