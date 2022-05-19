package io.mosip.preregistration.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


/**
 * This DTO class defines the variables to accept the input parameter from
 * request.
 * 
 * @author Ritik Jain
 * 
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class MiscApplicationRequestDTO extends ApplicationRequestDTO {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 686366342082625076L;
	/**
	 * purpose
	 */
	private String purpose;

}
