package io.mosip.preregistration.core.common.dto;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This DTO class is used to accept the response values for document upload.
 * 
 * @author Rajath Kumar
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
public class DocumentDTO implements Serializable {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 7070542323407937205L;

	/**
	 * File content
	 */
	private byte[] document;

	public byte[] getDocument() {
		return document != null ? document.clone() : null;
	}

	public void setDocument(byte[] document) {
		this.document = document != null ? document.clone() : null;
	}
}