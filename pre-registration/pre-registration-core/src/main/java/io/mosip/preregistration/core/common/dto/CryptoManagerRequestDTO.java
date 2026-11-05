package io.mosip.preregistration.core.common.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
/**
 * Data transfer object carrying crypto manager request across Pre-Registration APIs.
 */
public class CryptoManagerRequestDTO implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1614969246905533759L;

	/**
	 * Application id of decrypting module
	 */
	private String applicationId;

	/**
	 * Data in BASE64 encoding to encrypt/decrypt
	 */
	private String data;

	/**
	 * Refrence Id
	 */

	private String referenceId;
	/**
	 * Timestamp. Keymanager 1.3 parses {@code yyyy-MM-dd'T'HH:mm:ss.SSS'Z'}.
	 */
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime timeStamp;

	/**
	 * prepend thumbprint used by this Pre-Registration type.
	 */
	private boolean prependThumbprint;
}
