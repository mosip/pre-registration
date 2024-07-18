package io.mosip.preregistration.application.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class ApplicantTypeRequestDTO {
	
	@NotNull
	private List<ApplicantTypeKeyValueDTO<String, Object>> attributes;

}
