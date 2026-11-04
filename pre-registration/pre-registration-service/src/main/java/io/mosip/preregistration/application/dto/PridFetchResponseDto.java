package io.mosip.preregistration.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
/**
 * Data transfer object carrying prid fetch response across Pre-Registration APIs.
 */
public class PridFetchResponseDto {

	String prid;
}
