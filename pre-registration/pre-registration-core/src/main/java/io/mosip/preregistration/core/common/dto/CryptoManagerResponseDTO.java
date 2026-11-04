package io.mosip.preregistration.core.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
/**
 * Data transfer object carrying crypto manager response across Pre-Registration APIs.
 */
public class CryptoManagerResponseDTO {
	/**
	 * data used by this Pre-Registration type.
	 */
	private String data;
}
