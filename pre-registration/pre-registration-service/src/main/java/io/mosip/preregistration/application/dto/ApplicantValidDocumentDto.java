package io.mosip.preregistration.application.dto;

import java.util.Collection;

import lombok.Data;

@Data
/**
 * Data transfer object carrying applicant valid document across Pre-Registration APIs.
 */
public class ApplicantValidDocumentDto {
	/**
	 * app type code used by this Pre-Registration type.
	 */
	private String appTypeCode;
	/**
	 * lang code used by this Pre-Registration type.
	 */
	private String langCode;
	/**
	 * is active used by this Pre-Registration type.
	 */
	private Boolean isActive;
	/**
	 * document categories used by this Pre-Registration type.
	 */
	private Collection<DocumentCategoryAndTypeResponseDto> documentCategories;
}
