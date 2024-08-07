package io.mosip.preregistration.core.common.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * The {@code RequestWrapper} class is a generic wrapper for requests,
 * encapsulating the request data, metadata, and other related information. It
 * includes information such as an identifier, version, request time, metadata,
 * and the actual request payload.
 *
 * @param <T> the type of the request object
 * 
 * @since 1.0.0
 */

@Data
public class RequestWrapper<T> {
	/**
	 * The unique identifier for the request.
	 */
	private String id;

	/**
	 * The version of the request.
	 */
	private String version;

	/**
	 * The time at which the request was generated.
	 */
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime requesttime;

	/**
	 * Additional metadata related to the request.
	 */
	private Object metadata;

	/**
	 * The actual request payload.
	 */
	@NotNull
	@Valid
	private T request;
}