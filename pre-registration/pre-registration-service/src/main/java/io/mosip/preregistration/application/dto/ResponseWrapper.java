package io.mosip.preregistration.application.dto;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.mosip.kernel.core.exception.ServiceError;
import lombok.Data;

@Data
/**
 * Class supporting response wrapper in MOSIP Pre-Registration (io.mosip.preregistration.application.dto).
 */
public class ResponseWrapper<T> {
	/**
	 * id used by this Pre-Registration type.
	 */
	private String id;
	/**
	 * version used by this Pre-Registration type.
	 */
	private String version;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	/**
	 * Performs now for MOSIP Pre-Registration.
	 *
	 * @return now result
	 */
	private LocalDateTime responsetime = LocalDateTime.now(ZoneId.of("UTC"));
	/**
	 * metadata used by this Pre-Registration type.
	 */
	private Object metadata;
	@NotNull
	@Valid
	/**
	 * response used by this Pre-Registration type.
	 */
	private T response;

	private List<ServiceError> errors = new ArrayList<>();

}
