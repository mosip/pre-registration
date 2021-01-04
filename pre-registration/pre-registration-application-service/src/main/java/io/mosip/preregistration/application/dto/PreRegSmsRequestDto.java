package io.mosip.preregistration.application.dto;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.mosip.preregistration.core.common.dto.SMSRequestDTO;
import lombok.Data;

/**
 * General-purpose of {@code SmsRequestDto} class used to store Sms request
 * details
 */
@Data
public class PreRegSmsRequestDto {

	private String id;
	private Map metadata;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime requesttime;
	private String version;
	private SMSRequestDTO request;

}
