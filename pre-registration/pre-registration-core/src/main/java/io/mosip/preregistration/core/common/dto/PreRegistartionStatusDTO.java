/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.core.common.dto;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * The {@code PreRegistartionStatusDTO} class represents the status information
 * of a pre-registration. It includes the pre-registration ID and the status
 * code associated with the pre-registration.
 * 
 * @author Jagadishwari S
 * @since 1.0.0
 */

@Data
@NoArgsConstructor
@ToString
public class PreRegistartionStatusDTO implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -1088172470058130997L;

	/**
	 * The pre-registration ID.
	 */
	private String PreRegistartionId;

	/**
	 * The status code of the pre-registration.
	 */
	private String StatusCode;
}