package io.mosip.preregistration.core.exception;

import java.util.List;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.core.common.dto.ExceptionJSONInfoDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class InvalidRequestException extends BaseUncheckedException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3898906527162403384L;
	
	private MainResponseDTO<?> mainResponseDto;
	private List<ExceptionJSONInfoDTO> exptionList;
	private String operation;
	
	public InvalidRequestException() {
		super();
	}

	public InvalidRequestException(String errCode, String errMessage,MainResponseDTO<?> response) {
		super(errCode, errMessage);
		this.mainResponseDto=response;
	}
	public InvalidRequestException(String errorCode, String errorMessage, Throwable rootCause,MainResponseDTO<?> response) {
		super(errorCode, errorMessage, rootCause);
		this.mainResponseDto=response;
	}
	
	
	public InvalidRequestException(List<ExceptionJSONInfoDTO> exptionList,MainResponseDTO<?> response) {
		this.mainResponseDto=response;
		this.exptionList=exptionList;
	}
	
	public InvalidRequestException(List<ExceptionJSONInfoDTO> exptionList,String operation,MainResponseDTO<?> response) {
		this.mainResponseDto=response;
		this.exptionList=exptionList;
		this.operation=operation;
	}
}
