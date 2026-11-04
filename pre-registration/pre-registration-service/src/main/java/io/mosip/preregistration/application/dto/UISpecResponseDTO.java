package io.mosip.preregistration.application.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
/**
 * Data transfer object carrying ui spec response across Pre-Registration APIs.
 */
public class UISpecResponseDTO {

	/**
	 * id used by this Pre-Registration type.
	 */
	private String id;
	/**
	 * version used by this Pre-Registration type.
	 */
	private double version;
	/**
	 * title used by this Pre-Registration type.
	 */
	private String title;
	/**
	 * description used by this Pre-Registration type.
	 */
	private String description;
	/**
	 * identity schema id used by this Pre-Registration type.
	 */
	private String identitySchemaId;
	/**
	 * id schema version used by this Pre-Registration type.
	 */
	private double idSchemaVersion;
	/**
	 * domain used by this Pre-Registration type.
	 */
	private String domain;
	/**
	 * json spec used by this Pre-Registration type.
	 */
	private List<UISpecKeyValuePair> jsonSpec;
	/**
	 * status used by this Pre-Registration type.
	 */
	private String status;
	/**
	 * effective from used by this Pre-Registration type.
	 */
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
	private LocalDateTime effectiveFrom;
	/**
	 * created by used by this Pre-Registration type.
	 */
	private String createdBy;
	/**
	 * updated by used by this Pre-Registration type.
	 */
	private String updatedBy;
	/**
	 * created on used by this Pre-Registration type.
	 */
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
	private LocalDateTime createdOn;
	/**
	 * updated on used by this Pre-Registration type.
	 */
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
	private LocalDateTime updatedOn;

}
