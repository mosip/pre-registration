package io.mosip.preregistration.application.dto;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
/**
 * Class supporting ui audit request in MOSIP Pre-Registration (io.mosip.preregistration.application.dto).
 */
public class UIAuditRequest {

	@NotNull
	/**
	 * event name used by this Pre-Registration type.
	 */
	private String eventName;

	@NotNull
	/**
	 * description used by this Pre-Registration type.
	 */
	private String description;

	@NotNull
	/**
	 * action time stamp used by this Pre-Registration type.
	 */
	private String actionTimeStamp;

	@NotNull
	/**
	 * action user id used by this Pre-Registration type.
	 */
	private String actionUserId;

	@NotNull
	/**
	 * module name used by this Pre-Registration type.
	 */
	private String moduleName;

	@NotNull
	/**
	 * module id used by this Pre-Registration type.
	 */
	private String moduleId;

}
