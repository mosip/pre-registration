package io.mosip.preregistration.application.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UISpecDTO {

	@ApiModelProperty(notes = "identitySchemaId", required = true)
	@NotEmpty
	private String identitySchemaId;

	@ApiModelProperty(notes = "UI Spec type", required = true)
	@NotBlank
	private String type;

	@ApiModelProperty(notes = "UI Spec title", required = true)
	@NotBlank
	private String title;

	@ApiModelProperty(notes = "UI Spec description", required = true)
	@NotBlank
	private String description;

	@ApiModelProperty(notes = "UISpec", required = true)
	@NotEmpty
	private String jsonspec;
}
