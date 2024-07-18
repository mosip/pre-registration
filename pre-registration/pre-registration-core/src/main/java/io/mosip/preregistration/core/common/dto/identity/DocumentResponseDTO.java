/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.core.common.dto.identity;

import java.io.Serializable;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The {@code Identity} class represents an identity with various personal
 * information fields. It implements {@link Serializable} for allowing instances
 * to be serialized. The class is annotated with {@link Data} to generate
 * getters, setters, and other utility methods. The {@link Component} annotation
 * makes it a Spring bean, and {@link JsonIgnoreProperties} is used to ignore
 * unknown JSON properties during deserialization.
 * 
 * @author Kishan Rathore
 * @since 1.0.0
 */

@Data
@NoArgsConstructor
public class DocumentResponseDTO implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 7070542323407937205L;

	/**
	 * The PreRegistration ID associated with the document.
	 */
	private String preRegistrationId;
	
	/**
	 * The Document Id
	 */
	private String docId;
	
	/**
	 * The Document Name
	 */
	private String docName;
	
	/**
	 * The Document Category Code.
	 */
	private String docCatCode;

	/**
	 * The Document Type Code
	 */
	private String docTypCode;
	
	 /**
     * The Document File Format.
     */
	private String docFileFormat;
}