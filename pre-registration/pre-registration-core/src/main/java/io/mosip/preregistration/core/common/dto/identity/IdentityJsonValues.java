package io.mosip.preregistration.core.common.dto.identity;

import java.io.Serializable;

import lombok.Data;

/**
 * The {@code IdentityJsonValues} class represents an identity JSON value with
 * a mandatory check.
 * It implements {@link Serializable} for allowing instances to be serialized.
 * 
 * @author Jagadishwari S
 * @since 1.0.0
 */

@Data
public class IdentityJsonValues implements Serializable {
	/**
	 * constant serialVersion UID
	 */
	private static final long serialVersionUID = 8450727654084571180L;

	/** The value of the identity JSON. */
	private String value;

	/** Indicates whether the value is mandatory. */
	private Boolean isMandatory;
}