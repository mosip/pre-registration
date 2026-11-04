package io.mosip.preregistration.application.dto;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
/**
 * Data transfer object carrying applicant type response across Pre-Registration APIs.
 */
public class ApplicantTypeResponseDTO {
	
	@NotNull
	/**
	 * applicant type used by this Pre-Registration type.
	 */
	private ApplicantTypeCodeDTO applicantType;

}
