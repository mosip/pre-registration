package io.mosip.preregistration.application.dto;

import java.util.Collection;

import lombok.Data;

@Data
public class ApplicantValidDocumentDto {
	private String appTypeCode;
	private String langCode;
	private Boolean isActive;
	private Collection<DocumentCategoryAndTypeResponseDto> documentCategories;
}
