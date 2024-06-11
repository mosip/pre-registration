package io.mosip.preregistration.core.common.dto.identity;

import java.io.Serializable;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * The {@code Identity} class represents an identity with various personal information fields.
 * It implements {@link Serializable} for allowing instances to be serialized.
 * The class is annotated with {@link Data} to generate getters, setters, and other utility methods.
 * The {@link Component} annotation makes it a Spring bean, and {@link JsonIgnoreProperties} is used
 * to ignore unknown JSON properties during deserialization.
 * 
 * @author Jagadishwari S
 * @since 1.0.0
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

	 /** The location hierarchy for profiling. */
	private IdentityJsonValues locationHierarchyForProfiling;

	/** The phone. */
	private IdentityJsonValues phone;

	/** The email. */
	private IdentityJsonValues email;
}