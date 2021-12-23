package io.mosip.preregistration.batchjob.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkingDaysDto {
	
	private String name;
	
	private String code;
	
	private String languageCode;
	
	private boolean order;
}
	

