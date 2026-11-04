package io.mosip.preregistration.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
/**
 * Data transfer object carrying ui spec across Pre-Registration APIs.
 */
public class UISpecDTO {

	@ApiModelProperty(notes = "identitySchemaId", required = true)
	@NotEmpty
	/**
	 * identity schema id used by this Pre-Registration type.
	 */
	private String identitySchemaId;

	@ApiModelProperty(notes = "UI Spec type", required = true)
	@NotBlank
	/**
	 * type used by this Pre-Registration type.
	 */
	private String type;

	@ApiModelProperty(notes = "UI Spec title", required = true)
	@NotBlank
	/**
	 * title used by this Pre-Registration type.
	 */
	private String title;

	@ApiModelProperty(notes = "UI Spec description", required = true)
	@NotBlank
	/**
	 * description used by this Pre-Registration type.
	 */
	private String description;

	@ApiModelProperty(notes = "UISpec", required = true)
	/**
	 * jsonspec used by this Pre-Registration type.
	 */
	private JsonNode jsonspec;
}
