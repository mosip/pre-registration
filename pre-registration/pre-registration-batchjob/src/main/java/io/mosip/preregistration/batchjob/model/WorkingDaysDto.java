package io.mosip.preregistration.batchjob.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * Data transfer object carrying working days across Pre-Registration APIs.
 */
public class WorkingDaysDto {
	
	/**
	 * name used by this Pre-Registration type.
	 */
	private String name;
	
	/**
	 * code used by this Pre-Registration type.
	 */
	private String code;
	
	/**
	 * language code used by this Pre-Registration type.
	 */
	private String languageCode;
	
	/**
	 * order used by this Pre-Registration type.
	 */
	private boolean order;
}
	
