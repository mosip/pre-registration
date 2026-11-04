package io.mosip.preregistration.application.dto;

import lombok.Data;

@Data
/**
 * Data transfer object carrying applicant type code across Pre-Registration APIs.
 */
public class ApplicantTypeCodeDTO {

	/**
	 * applicant type code used by this Pre-Registration type.
	 */
	private String applicantTypeCode;

}
