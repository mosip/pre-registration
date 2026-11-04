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

	/**
	 * id used by this Pre-Registration type.
	 */
	private String id;
	/**
	 * metadata used by this Pre-Registration type.
	 */
	private Map metadata;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	/**
	 * requesttime used by this Pre-Registration type.
	 */
	private LocalDateTime requesttime;
	/**
	 * version used by this Pre-Registration type.
	 */
	private String version;
	/**
	 * request used by this Pre-Registration type.
	 */
	private SMSRequestDTO request;

}
