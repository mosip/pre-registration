package io.mosip.preregistration.application.dto;

import lombok.Data;

@Data
public class DocumentTypeDto {

	private String code;

	private String name;

	private String description;

	private String langCode;

	private Boolean isActive;
}
