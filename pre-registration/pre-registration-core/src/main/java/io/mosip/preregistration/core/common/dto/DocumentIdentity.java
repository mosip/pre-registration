package io.mosip.preregistration.core.common.dto;

import io.mosip.preregistration.core.common.dto.identity.IdentityJsonValues;
import lombok.Data;

@Data
public class DocumentIdentity {
	/** Proof of Address. */
	private IdentityJsonValues poa;

	/** Proof of Identity. */
	private IdentityJsonValues poi;

	/** Proff of Birth. */
	private IdentityJsonValues pob;

	/** Proof of exception. */
	private IdentityJsonValues poe;

	/** Proof of Relation. */
	private IdentityJsonValues por;
}