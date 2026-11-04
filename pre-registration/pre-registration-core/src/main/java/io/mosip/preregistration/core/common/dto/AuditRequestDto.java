package io.mosip.preregistration.core.common.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The Audit Request class with required fields to be captured and recorded
 * 
 * @author Jagadishwari S
 * @since 1.0.0
 *
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditRequestDto {
	@NotNull
	@Size(min = 1, max = 64)
	/**
	 * event id used by this Pre-Registration type.
	 */
	private String eventId;

	@NotNull
	@Size(min = 1, max = 128)
	/**
	 * event name used by this Pre-Registration type.
	 */
	private String eventName;

	@NotNull
	@Size(min = 1, max = 64)
	/**
	 * event type used by this Pre-Registration type.
	 */
	private String eventType;

	@NotNull
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	/**
	 * action time stamp used by this Pre-Registration type.
	 */
	private LocalDateTime actionTimeStamp;

	@NotNull
	@Size(min = 1, max = 128)
	/**
	 * host name used by this Pre-Registration type.
	 */
	private String hostName;

	@NotNull
	@Size(min = 1, max = 16)
	/**
	 * host ip used by this Pre-Registration type.
	 */
	private String hostIp;

	@NotNull
	@Size(min = 1, max = 64)
	/**
	 * application id used by this Pre-Registration type.
	 */
	private String applicationId;

	@NotNull
	@Size(min = 1, max = 128)
	/**
	 * application name used by this Pre-Registration type.
	 */
	private String applicationName;

	@NotNull
	@Size(min = 1, max = 64)
	/**
	 * session user id used by this Pre-Registration type.
	 */
	private String sessionUserId;

	@Size(min = 1, max = 128)
	/**
	 * session user name used by this Pre-Registration type.
	 */
	private String sessionUserName;

	@NotNull
	@Size(min = 1, max = 64)

	/**
	 * id used by this Pre-Registration type.
	 */
	private String id;
	@NotNull
	@Size(min = 1, max = 64)
	/**
	 * id type used by this Pre-Registration type.
	 */
	private String idType;

	@NotNull
	@Size(min = 1, max = 255)
	/**
	 * created by used by this Pre-Registration type.
	 */
	private String createdBy;

	@Size(max = 128)
	/**
	 * module name used by this Pre-Registration type.
	 */
	private String moduleName;

	@Size(max = 64)
	/**
	 * module id used by this Pre-Registration type.
	 */
	private String moduleId;

	@Size(max = 2048)
	/**
	 * description used by this Pre-Registration type.
	 */
	private String description;
}
