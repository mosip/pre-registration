package io.mosip.preregistration.application.dto;

import org.json.simple.JSONObject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The Class SchemaResponseDto.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchemaResponseDto {
	/** The id schema. */	
	private JSONObject idSchema;
}