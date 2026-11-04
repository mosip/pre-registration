package io.mosip.preregistration.application.exception;

import java.util.List;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.core.common.dto.ExceptionJSONInfoDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import lombok.Getter;

@Getter
/**
 * Unchecked exception raised when demographic details not found fails in Pre-Registration.
 */
public class DemographicDetailsNotFoundException extends BaseUncheckedException {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	/**
	 * main response dto used by this Pre-Registration type.
	 */
	private final MainResponseDTO<?> mainResponseDTO;
	/**
	 * error list used by this Pre-Registration type.
	 */
	private List<ExceptionJSONInfoDTO> errorList;

	/**
	 * Returns error list.
	 *
	 * @return get error list result
	 */
	public List<ExceptionJSONInfoDTO> getErrorList() {
		return errorList;
	}

	/**
	 * Returns main resposne dto.
	 *
	 * @return get main resposne dto result
	 */
	public MainResponseDTO<?> getMainResposneDTO() {
		return mainResponseDTO;
	}

	/**
	 * @param msg
	 */
	public DemographicDetailsNotFoundException(List<ExceptionJSONInfoDTO> errorList, MainResponseDTO<?> response) {
		this.errorList = errorList;
		this.mainResponseDTO = response;
	}

	/**
	 * @param errCode
	 * @param msg
	 */
	public DemographicDetailsNotFoundException(String errCode, String msg, MainResponseDTO<?> response) {
		super(errCode, msg);
		this.mainResponseDTO = response;
	}

	/**
	 * @param errCode
	 * @param msg
	 * @param cause
	 */
	public DemographicDetailsNotFoundException(String errCode, String msg, Throwable cause,
			MainResponseDTO<?> response) {
		super(errCode, msg, cause);
		this.mainResponseDTO = response;
	}
}
