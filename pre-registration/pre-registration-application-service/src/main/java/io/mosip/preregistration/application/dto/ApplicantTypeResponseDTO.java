package io.mosip.preregistration.application.dto;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class ApplicantTypeResponseDTO {
	
	@NotNull
	private ApplicantTypeCodeDTO applicantType;

}
