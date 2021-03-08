package io.mosip.preregistration.application.dto;

import lombok.Data;

@Data
public class LanguagesDTO {
	
	private String code;
	
	private String family;
	
	private boolean isActive;
	
	private String name;
	
	private String nativeName;
}
