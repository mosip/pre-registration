package io.mosip.preregistration.core.common.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthNResponse implements Serializable {
	/**
	 * constant serialVersion UID
	 */
	private static final long serialVersionUID = -1505080809049335366L;

	private String message;

	private String status;
}