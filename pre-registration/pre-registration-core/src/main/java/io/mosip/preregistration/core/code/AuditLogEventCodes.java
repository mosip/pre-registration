package io.mosip.preregistration.core.code;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class is used to define the events for audit logging.
 * 
 * @author Jagadishwari S
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogEventCodes implements Serializable {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4812141703022119560L;

	/**
	 * event id used by this Pre-Registration type.
	 */
	private String eventId;

	/**
	 * event type used by this Pre-Registration type.
	 */
	private String eventType;

	/**
	 * event name used by this Pre-Registration type.
	 */
	private String eventName;
}
