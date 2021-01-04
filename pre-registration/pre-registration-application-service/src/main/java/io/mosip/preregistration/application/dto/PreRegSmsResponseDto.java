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

	
	private String id;
	
	private String version;
	
	private LocalDateTime responsetime;
	
	private Map metadata;
	 
	private List<String> errors;
	
	private SMSResponseDto response;


}
