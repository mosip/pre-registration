package io.mosip.preregistration.demographic.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class IdSchemaDto {
	
	private String id;
	private double idVersion;
	private String schemaJson;
	private LocalDateTime effectiveFrom;
}
