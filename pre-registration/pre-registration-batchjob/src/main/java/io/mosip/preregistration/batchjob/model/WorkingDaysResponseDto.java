package io.mosip.preregistration.batchjob.model;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * Data transfer object carrying working days response across Pre-Registration APIs.
 */
public class WorkingDaysResponseDto implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5966823885108013755L;
	
	/**
	 * workingdays used by this Pre-Registration type.
	 */
	private List<WorkingDaysDto> workingdays;
}
