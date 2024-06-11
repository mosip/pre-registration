package io.mosip.preregistration.core.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidDocumentsResponseDTO {

	private String docTypeCode;

	private String docCategoryCode;

	private Boolean isActive;
}