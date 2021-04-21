package io.mosip.preregistration.datasync.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientPublickeyDTO {
	private String signingPublicKey;
	private String encryptionPublicKey;
}
