package io.mosip.preregistration.application.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
/**
 * Data transfer object carrying applicant type request across Pre-Registration APIs.
 */
public class ApplicantTypeRequestDTO {
	
	@NotNull
	/**
	 * attributes used by this Pre-Registration type.
	 */
	private List<ApplicantTypeKeyValueDTO<String, Object>> attributes;

}
