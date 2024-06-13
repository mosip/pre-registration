package io.mosip.preregistration.application.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class UISpecResponseDTO {

	private String id;
	private double version;
	private String title;
	private String description;
	private String identitySchemaId;
	private double idSchemaVersion;
	private String domain;
	private List<UISpecKeyValuePair> jsonSpec;
	private String status;
	private LocalDateTime effectiveFrom;
	private String createdBy;
	private String updatedBy;
	private LocalDateTime createdOn;
	private LocalDateTime updatedOn;

}
