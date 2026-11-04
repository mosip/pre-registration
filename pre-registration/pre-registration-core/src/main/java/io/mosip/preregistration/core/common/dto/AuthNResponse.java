package io.mosip.preregistration.core.common.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
/**
 * Class supporting auth n response in MOSIP Pre-Registration (io.mosip.preregistration.core.common.dto).
 */
public class AuthNResponse implements Serializable {
	/**
	 * constant serialVersion UID
	 */
	private static final long serialVersionUID = -1505080809049335366L;

	/**
	 * message used by this Pre-Registration type.
	 */
	private String message;

	/**
	 * status used by this Pre-Registration type.
	 */
	private String status;
}
