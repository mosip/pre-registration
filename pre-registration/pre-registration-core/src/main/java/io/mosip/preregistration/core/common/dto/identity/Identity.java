package io.mosip.preregistration.core.common.dto.identity;

import java.io.Serializable;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.mosip.preregistration.core.common.dto.DocumentIdentity;
import lombok.Data;

/**
 * Instantiates a new identity.
 */
/**
 * @author Jagadishwari S
 * @since 1.0.0
 *
 */
@Data
@Component
@JsonIgnoreProperties(ignoreUnknown = true)
public class Identity implements Serializable {

	/**
	 * constant serialVersion UID
	 */
	private static final long serialVersionUID = -608499873502236074L;

	/** The name. */
	private IdentityJsonValues name;

	/** The gender. */
	private IdentityJsonValues gender;

	/** The date of birth. */
	private IdentityJsonValues dob;

	/** The preferredLanguages. */
	private IdentityJsonValues preferredLanguage;

	/** The locationHierarchyForProfiling fields. */
	private IdentityJsonValues locationHierarchyForProfiling;

	/** The phone. */
	private IdentityJsonValues phone;

	/** The email. */
	private IdentityJsonValues email;

}
