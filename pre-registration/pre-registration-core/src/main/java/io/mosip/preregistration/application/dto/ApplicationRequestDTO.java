package io.mosip.preregistration.application.dto;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * This DTO class defines the variables to accept the input parameter from
 * request.
 * 
 * @author Mayura D
 * @since 1.2.0
 *
 */
@Data
@NoArgsConstructor
@ToString
public class ApplicationRequestDTO implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 686366342082625076L;
	/**
	 * language code
	 */
	private String langCode;
}