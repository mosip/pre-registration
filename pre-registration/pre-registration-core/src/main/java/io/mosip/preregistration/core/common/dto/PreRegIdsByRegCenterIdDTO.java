package io.mosip.preregistration.core.common.dto;

import java.io.Serializable;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * The {@code Identity} class represents an identity with various personal
 * information fields. It implements {@link Serializable} for allowing instances
 * to be serialized. The class is annotated with {@link Data} to generate
 * getters, setters, and other utility methods. The {@link Component} annotation
 * makes it a Spring bean, and {@link JsonIgnoreProperties} is used to ignore
 * unknown JSON properties during deserialization.
 * 
 * @author Jagadishwari S
 * @since 1.0.0
 */

@Data
@ToString
public class PreRegIdsByRegCenterIdDTO implements Serializable {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8012017032440020062L;
	/**
	 * registration Center Id
	 */
	@JsonProperty("registartion_center_id")
	@ApiModelProperty(value = "Registartion Center ID", position = 1)
	private String registrationCenterId;

	/**
	 * pre-registration id
	 */
	@JsonProperty("pre_registration_ids")
	@ApiModelProperty(value = "List of Pre-Registartion IDs", position = 2)
	private List<String> preRegistrationIds;
}