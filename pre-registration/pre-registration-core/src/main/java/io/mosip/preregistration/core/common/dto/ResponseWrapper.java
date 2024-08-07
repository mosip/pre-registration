package io.mosip.preregistration.core.common.dto;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.mosip.kernel.core.exception.ServiceError;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * /** The {@code ResponseWrapper} class is a generic wrapper for responses,
 * encapsulating the response data, metadata, and any potential errors. It
 * includes information such as an identifier, version, response time, metadata,
 * the actual response payload, and a list of service errors.
 *
 * @param <T> the type of the response object
 * 
 * @since 1.0.0
 */

@Data
public class ResponseWrapper<T> {
	/**
	 * The unique identifier for the response.
	 */
	private String id;

	/**
	 * The version of the response.
	 */
	private String version;

	/**
	 * The time at which the response was generated.
	 */
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime responsetime = LocalDateTime.now(ZoneId.of("UTC"));
	/**
	 * Additional metadata related to the response.
	 */
	private Object metadata;

	/**
	 * The actual response payload.
	 */
	@NotNull
	@Valid
	private T response;

	/**
	 * A list of service errors, if any occurred.
	 */
	private List<ServiceError> errors = new ArrayList<>();
}