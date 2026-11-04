package io.mosip.preregistration.application.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
/**
 * Data transfer object carrying ui spec publish request across Pre-Registration APIs.
 */
public class UISpecPublishRequestDTO {

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	/**
	 * effective from used by this Pre-Registration type.
	 */
	private LocalDateTime effectiveFrom;

	/**
	 * id used by this Pre-Registration type.
	 */
	private String id;
}
