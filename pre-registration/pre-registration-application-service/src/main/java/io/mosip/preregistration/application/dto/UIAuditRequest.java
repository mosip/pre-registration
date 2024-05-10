package io.mosip.preregistration.application.dto;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class UIAuditRequest {

	@NotNull
	private String eventName;

	@NotNull
	private String description;

	@NotNull
	private String actionTimeStamp;

	@NotNull
	private String actionUserId;

	@NotNull
	private String moduleName;

	@NotNull
	private String moduleId;

}
