package io.mosip.preregistration.generateqrcode.exception.util;

import java.io.IOException;
import java.text.ParseException;

import org.json.JSONException;

import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.qrcodegenerator.exception.QrcodeGenerationException;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.exception.InvalidRequestException;
import io.mosip.preregistration.core.exception.InvalidRequestParameterException;
import io.mosip.preregistration.core.exception.PreRegistrationException;
import io.mosip.preregistration.generateqrcode.error.ErrorCodes;
import io.mosip.preregistration.generateqrcode.error.ErrorMessages;
import io.mosip.preregistration.generateqrcode.exception.IllegalParamException;

/**
 * @author Sanober Noor
 * @since 1.0.0
 */
public class QRcodeExceptionCatcher {
	/**
	 * Method to handle the respective exceptions
	 * 
	 * @param ex
	 *            pass the exception
	 */
	public void handle(Exception ex, MainResponseDTO<?> mainResponseDto) {
		if (ex instanceof QrcodeGenerationException) {
			throw new IllegalParamException(ErrorCodes.PRG_QRC_002.getCode(),
					ErrorMessages.QRCODE_FAILED_TO_GENERATE.getCode(), ex.getCause(), mainResponseDto);
		} else if (ex instanceof IOException || ex instanceof JSONException) {
			throw new io.mosip.preregistration.generateqrcode.exception.QrCodeIOException(
					ErrorCodes.PRG_QRC_001.getCode(), ErrorMessages.INPUT_OUTPUT_EXCEPTION.getCode(), ex.getCause(),
					mainResponseDto);
		} else if (ex instanceof NullPointerException) {
			throw new IllegalParamException(ErrorCodes.PRG_QRC_002.getCode(),
					ErrorMessages.QRCODE_FAILED_TO_GENERATE.getCode(), ex.getCause(), mainResponseDto);
		}

		else if (ex instanceof InvalidRequestParameterException) {
			throw new InvalidRequestParameterException(((InvalidRequestParameterException) ex).getErrorCode(),
					((InvalidRequestParameterException) ex).getErrorText(), mainResponseDto);
		} else if (ex instanceof InvalidRequestException) {
			throw new InvalidRequestException(((InvalidRequestException) ex).getErrorCode(),
					((InvalidRequestException) ex).getErrorText(), mainResponseDto);
		} else if (ex instanceof ParseException) {
			throw new InvalidRequestParameterException(
					io.mosip.preregistration.core.errorcodes.ErrorCodes.PRG_CORE_REQ_003.getCode(),
					io.mosip.preregistration.core.errorcodes.ErrorMessages.INVALID_REQUEST_DATETIME.getMessage(),
					mainResponseDto);
		} else if (ex instanceof ParseException) {
			throw new InvalidRequestParameterException(
					io.mosip.preregistration.core.errorcodes.ErrorCodes.PRG_CORE_REQ_003.getCode(),
					io.mosip.preregistration.core.errorcodes.ErrorMessages.INVALID_REQUEST_DATETIME.getMessage(),
					mainResponseDto);

		}
		else {
			if (ex instanceof BaseUncheckedException) {
				throw new PreRegistrationException(((BaseUncheckedException) ex).getErrorCode(),
						((BaseUncheckedException) ex).getErrorText(), mainResponseDto);
			} else if (ex instanceof BaseCheckedException) {
				throw new PreRegistrationException(((BaseCheckedException) ex).getErrorCode(),
						((BaseCheckedException) ex).getErrorText(), mainResponseDto);
			}
		}
	}

}
