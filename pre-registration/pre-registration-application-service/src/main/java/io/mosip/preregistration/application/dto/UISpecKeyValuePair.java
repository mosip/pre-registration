package io.mosip.preregistration.application.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UISpecKeyValuePair {	
	private String type;
	private JsonNode spec;
}