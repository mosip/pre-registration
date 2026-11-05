package io.mosip.preregistration.application.dto;

import java.util.Collection;

import lombok.Data;

@Data
/**
 * Data transfer object carrying document category and type response across Pre-Registration APIs.
 */
public class DocumentCategoryAndTypeResponseDto {

	/**
	 * code used by this Pre-Registration type.
	 */
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

	/**
	 * document types used by this Pre-Registration type.
	 */
	private Collection<DocumentTypeDto> documentTypes;

}
