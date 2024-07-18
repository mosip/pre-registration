package io.mosip.preregistration.application.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The MasterDataException class represents an exception related to master data
 * operations. This class extends {@link BaseUncheckedException} and includes
 * various constructors for different use cases, as well as a
 * {@link MainResponseDTO} object for additional error information.
 * 
 * @author Rajath KR
 * @since 1.0.0
 * 
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MasterDataException extends BaseUncheckedException {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	private String errorCode;
	private String errorMessage;
}