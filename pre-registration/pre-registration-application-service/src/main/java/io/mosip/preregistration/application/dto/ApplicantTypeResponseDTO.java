package io.mosip.preregistration.application.dto;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class ApplicantTypeResponseDTO {
	
	@NotNull
	private ApplicantTypeCodeDTO applicantType;

}
