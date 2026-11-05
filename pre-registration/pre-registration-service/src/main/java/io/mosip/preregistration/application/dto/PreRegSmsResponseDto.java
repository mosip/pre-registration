package io.mosip.preregistration.application.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import io.mosip.kernel.core.notification.model.SMSResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * General-purpose of {@code SmsRequestDto} class used to store Sms response
 * details
 * 
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PreRegSmsResponseDto {

	
	/**
	 * id used by this Pre-Registration type.
	 */
	private String id;
	
	/**
	 * version used by this Pre-Registration type.
	 */
	private String version;
	
	/**
	 * responsetime used by this Pre-Registration type.
	 */
	private LocalDateTime responsetime;
	
	/**
	 * metadata used by this Pre-Registration type.
	 */
	private Map metadata;
	 
	/**
	 * errors used by this Pre-Registration type.
	 */
	private List<String> errors;
	
	/**
	 * response used by this Pre-Registration type.
	 */
	private SMSResponseDto response;


}
