package io.mosip.preregistration.application.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class UIAuditRequest {

	@NotNull
	private String eventName;

	@NotNull
	private String description;

	@NotNull
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime actionTimeStamp;

	@NotNull
	private String actionUserId;

	@NotNull
	private String moduleName;

	@NotNull
	private String moduleId;

}
