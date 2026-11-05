package io.mosip.preregistration.application.dto;

import lombok.Data;

@Data
/**
 * Data transfer object carrying applicant type key value across Pre-Registration APIs.
 */
public class ApplicantTypeKeyValueDTO<K, V> {

	/**
	 * attribute used by this Pre-Registration type.
	 */
	private K attribute;
	/**
	 * value used by this Pre-Registration type.
	 */
	private V value;

}
