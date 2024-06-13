package io.mosip.preregistration.application.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;

@Data
public class UISpecMetaDataDTO {

	private String id;
	private double version;
	private String title;
	private String description;
	private String identitySchemaId;
	private double idSchemaVersion;
	private JsonNode jsonSpec;
	private String status;
	private LocalDateTime effectiveFrom;
	private LocalDateTime createdOn;
	private LocalDateTime updatedOn;

}
