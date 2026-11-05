package io.mosip.preregistration.datasync.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * Data transfer object carrying client publickey across Pre-Registration APIs.
 */
public class ClientPublickeyDTO {
	/**
	 * signing public key used by this Pre-Registration type.
	 */
	private String signingPublicKey;
	/**
	 * encryption public key used by this Pre-Registration type.
	 */
	private String encryptionPublicKey;
}
