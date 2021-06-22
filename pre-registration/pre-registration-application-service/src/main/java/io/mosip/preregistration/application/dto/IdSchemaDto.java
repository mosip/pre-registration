package io.mosip.preregistration.application.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class IdSchemaDto {
	
	private String id;
	private double idVersion;
	private String schemaJson;
	private LocalDateTime effectiveFrom;
	private List<JsonNode> schema;
}
