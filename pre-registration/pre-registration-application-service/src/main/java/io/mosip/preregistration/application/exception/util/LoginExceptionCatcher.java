package io.mosip.preregistration.application.exception.util;

import org.springframework.lang.NonNull;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;

import io.jsonwebtoken.JwtException;
import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.exception.InvalidRequestException;
import io.mosip.preregistration.core.exception.InvalidRequestParameterException;
import io.mosip.preregistration.core.exception.PreRegistrationException;
import io.mosip.preregistration.core.exception.util.ParseResponseException;
import io.mosip.preregistration.application.errorcodes.LoginErrorCodes;
import io.mosip.preregistration.application.errorcodes.LoginErrorMessages;
import io.mosip.preregistration.application.exception.ConfigFileNotFoundException;
import io.mosip.preregistration.application.exception.InvalidOtpOrUseridException;
import io.mosip.preregistration.application.exception.InvalidateTokenException;
import io.mosip.preregistration.application.exception.LoginServiceException;
import io.mosip.preregistration.application.exception.NoAuthTokenException;
import io.mosip.preregistration.application.exception.PreRegLoginException;
import io.mosip.preregistration.application.exception.SendOtpFailedException;
import io.mosip.preregistration.application.exception.UserIdOtpFaliedException;

/**
 * This class is use to catch the exception while login
 * 
 * @author Akshay
 * @since 1.0.0
 * 
 */
public class LoginExceptionCatcher {

	public void handle(Exception ex, @NonNull String serviceType, MainResponseDTO<?> mainResponsedto) {
		if ((ex instanceof RestClientException || ex instanceof HttpClientErrorException
				|| ex instanceof HttpServerErrorException) && (serviceType.equals("sendOtp"))) {
			throw new SendOtpFailedException(LoginErrorCodes.PRG_AUTH_001.name(),
					(LoginErrorMessages.SEND_OTP_FAILED.getMessage()), mainResponsedto);
		} else if (ex instanceof PreRegLoginException && (serviceType.equals("sendOtp"))) {
			throw new SendOtpFailedException(((PreRegLoginException) ex).getErrorCode(),
					((PreRegLoginException) ex).getErrorText(), mainResponsedto);
		} else if (ex instanceof RestClientException && (serviceType.equals("userIdOtp"))) {
			throw new UserIdOtpFaliedException(LoginErrorCodes.PRG_AUTH_002.name(),
					(LoginErrorMessages.USERID_OTP_VALIDATION_FAILED.getMessage()), mainResponsedto);
		} else if (ex instanceof RestClientException && (serviceType.equals("invalidateToken"))) {
			throw new InvalidateTokenException(LoginErrorCodes.PRG_AUTH_003.getCode(),
					(LoginErrorMessages.INVALIDATE_TOKEN_FAILED.getMessage()), mainResponsedto);
		} else if (ex instanceof JwtException && (serviceType.equals("invalidateToken"))) {
			throw new InvalidateTokenException(LoginErrorCodes.PRG_AUTH_003.getCode(),
					(LoginErrorMessages.INVALIDATE_TOKEN_FAILED.getMessage()), mainResponsedto);
		} else if (ex instanceof InvalidRequestParameterException && (serviceType.equals("sendOtp"))) {
			throw new InvalidRequestParameterException(((InvalidRequestParameterException) ex).getErrorCode(),
					((InvalidRequestParameterException) ex).getErrorText(), mainResponsedto);
		} else if (ex instanceof InvalidRequestParameterException && (serviceType.equals("userIdOtp"))) {
			throw new InvalidRequestParameterException(((InvalidRequestParameterException) ex).getErrorCode(),
					((InvalidRequestParameterException) ex).getErrorText(), mainResponsedto);
		} else if (ex instanceof LoginServiceException) {
			throw new LoginServiceException(((LoginServiceException) ex).getValidationErrorList(), mainResponsedto);
		} else if (ex instanceof ParseResponseException) {
			throw new ParseResponseException(((ParseResponseException) ex).getErrorCode(),
					((ParseResponseException) ex).getErrorText(), mainResponsedto);
		} else if (ex instanceof ConfigFileNotFoundException) {
			throw new ConfigFileNotFoundException(((ConfigFileNotFoundException) ex).getErrorCode(),
					((ConfigFileNotFoundException) ex).getErrorText(), mainResponsedto);
		} else if (ex instanceof InvalidOtpOrUseridException) {
			throw new InvalidOtpOrUseridException(LoginErrorCodes.PRG_AUTH_013.getCode(),
					((InvalidOtpOrUseridException) ex).getErrorText(), mainResponsedto);
		} else if (ex instanceof NoAuthTokenException) {
			throw new NoAuthTokenException(((NoAuthTokenException) ex).getErrorCode(),
					((NoAuthTokenException) ex).getErrorText(), mainResponsedto);
		} else if (ex instanceof InvalidRequestException) {
			throw new InvalidRequestException(((InvalidRequestException) ex).getErrorCode(),
					((InvalidRequestException) ex).getErrorText(), mainResponsedto);
		} else if ((ex instanceof HttpClientErrorException || ex instanceof HttpServerErrorException)
				&& serviceType.equals("refreshConfig")) {
			throw new ConfigFileNotFoundException(LoginErrorCodes.PRG_AUTH_012.getCode(),
					LoginErrorMessages.CONFIG_FILE_NOT_FOUND_EXCEPTION.getMessage(), mainResponsedto);
		} else if ((ex instanceof HttpClientErrorException || ex instanceof HttpServerErrorException)
				&& serviceType.equals("postconstruct")) {
			throw new ConfigFileNotFoundException(LoginErrorCodes.PRG_AUTH_012.getCode(),
					LoginErrorMessages.CONFIG_FILE_NOT_FOUND_EXCEPTION.getMessage(), mainResponsedto);
		} else {
			if (ex instanceof BaseUncheckedException) {
				throw new PreRegistrationException(((BaseUncheckedException) ex).getErrorCode(),
						((BaseUncheckedException) ex).getErrorText(), mainResponsedto);
			} else if (ex instanceof BaseCheckedException) {
				throw new PreRegistrationException(((BaseCheckedException) ex).getErrorCode(),
						((BaseCheckedException) ex).getErrorText(), mainResponsedto);
			}
		}

	}
}
