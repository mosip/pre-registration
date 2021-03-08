package io.mosip.preregistration.application.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class ConfigResponseDTO {

	private List<LanguagesDTO> languages;
	
	private Map<String,String> configParams;
}
