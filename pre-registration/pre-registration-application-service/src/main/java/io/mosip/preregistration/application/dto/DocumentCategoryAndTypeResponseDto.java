package io.mosip.preregistration.application.dto;

import java.util.Collection;

import lombok.Data;

@Data
public class DocumentCategoryAndTypeResponseDto {

	private String code;

	/**
	 * Document category name.
	 */
	private String name;

	/**
	 * Document category description
	 */
	private String description;

	/**
	 * The Language Code.
	 */
	private String langCode;

	/**
	 * Is active or not.
	 */
	private Boolean isActive;

	private Collection<DocumentTypeDto> documentTypes;

}
