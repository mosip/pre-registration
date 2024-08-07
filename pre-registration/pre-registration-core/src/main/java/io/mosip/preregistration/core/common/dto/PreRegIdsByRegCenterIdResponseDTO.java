package io.mosip.preregistration.core.common.dto;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * The {@code PreRegIdsByRegCenterIdResponseDTO} class represents a response
 * containing a list of pre-registration IDs associated with a specific
 * registration center ID.
 * 
 * @author Jagadishwari S
 * @since 1.0.0
 */

@Data
public class PreRegIdsByRegCenterIdResponseDTO implements Serializable {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 8225498964365164064L;

	/**
	 * The registration center ID.
	 */
	@JsonProperty("registration_center_id")
	@ApiModelProperty(value = "Registration Center ID", position = 1)
	private String registrationCenterId;

	/**
	 * The list of pre-registration IDs.
	 */
	@JsonProperty("pre_registration_ids")
	@ApiModelProperty(value = "List of Pre-Registartion IDs", position = 2)
	private List<String> preRegistrationIds;
}