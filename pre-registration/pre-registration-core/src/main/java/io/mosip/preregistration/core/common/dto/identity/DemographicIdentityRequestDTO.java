package io.mosip.preregistration.core.common.dto.identity;

import java.io.Serializable;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.mosip.preregistration.core.common.dto.DocumentIdentity;
import lombok.Data;

/**
 * The {@code DemographicIdentityRequestDTO} class represents the data transfer
 * object (DTO) for a demographic identity request. It implements
 * {@link Serializable} to allow instances to be serialized.
 * 
 * 
 * @author Jagadishwari S
 * @since 1.0.0
 */

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DemographicIdentityRequestDTO implements Serializable {
	/**
	 * constant serialVersion UID
	 */
	private static final long serialVersionUID = -912216321976514052L;

	/**
	 * The identity information.
	 */
	private Identity identity;

	/**
	 * The documents associated with the identity.
	 */
	private DocumentIdentity documents;
}