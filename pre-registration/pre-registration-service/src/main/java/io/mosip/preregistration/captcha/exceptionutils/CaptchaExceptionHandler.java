package io.mosip.preregistration.captcha.exceptionutils;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import jakarta.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.mosip.kernel.core.util.DateUtils2;
import io.mosip.preregistration.captcha.exception.CaptchaException;
import io.mosip.preregistration.captcha.exception.InvalidRequestCaptchaException;
import io.mosip.preregistration.core.common.dto.ExceptionJSONInfoDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.exception.InvalidRequestParameterException;
import io.mosip.preregistration.core.util.GenericUtil;

@RestControllerAdvice
/**
 * Class supporting captcha exception handler in MOSIP Pre-Registration (io.mosip.preregistration.captcha.exceptionutils).
 */
public class CaptchaExceptionHandler {

	@Autowired
	/**
	 * env used by this Pre-Registration type.
	 */
	protected Environment env;

	/** The id. */
	@Resource
	protected Map<String, String> id;

	@Value("${mosip.preregistration.captcha.id.validate}")
	/**
	 * mosipcaptcha validate id used by this Pre-Registration type.
	 */
	public String mosipcaptchaValidateId;

	@Value("${version}")
	/**
	 * version used by this Pre-Registration type.
	 */
	private String version;

	/**
	 * date time format used by this Pre-Registration type.
	 */
	private static String dateTimeFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	/**
	 * Returns current response time.
	 *
	 * @return get current response time result
	 */
	public static String getCurrentResponseTime() {
		return DateUtils2.formatDate(new Date(System.currentTimeMillis()), dateTimeFormat);
	}

	@ExceptionHandler(InvalidRequestCaptchaException.class)
	/**
	 * Performs handle invalid captcha reqest for MOSIP Pre-Registration.
	 *
	 * @param ex ex used by handle invalid captcha reqest
	 * @return handle invalid captcha reqest result
	 */
	public MainResponseDTO<?> handleInvalidCaptchaReqest(InvalidRequestCaptchaException ex) {
		MainResponseDTO<?> response = new MainResponseDTO<>();
		response.setId(mosipcaptchaValidateId);
		response.setVersion(version);
		response.setResponsetime(getCurrentResponseTime());
		response.setResponse(null);
		ArrayList<ExceptionJSONInfoDTO> errors = new ArrayList<ExceptionJSONInfoDTO>();
		ExceptionJSONInfoDTO errorDetails = new ExceptionJSONInfoDTO(ex.getErrorCode(), ex.getErrorMessage());
		errors.add(errorDetails);
		response.setErrors(errors);
		return response;
	}

	@ExceptionHandler(InvalidRequestParameterException.class)
	/**
	 * Performs invalid request for MOSIP Pre-Registration.
	 *
	 * @param e e used by invalid request
	 * @return invalid request result
	 */
	public ResponseEntity<MainResponseDTO<?>> invalidRequest(final InvalidRequestParameterException e) {
		MainResponseDTO<?> errorRes = e.getMainResponseDto();
		errorRes.setId(id.get(e.getOperation()));
		errorRes.setVersion(env.getProperty("version"));
		errorRes.setErrors(e.getExptionList());
		errorRes.setResponsetime(GenericUtil.getCurrentResponseTime());
		return new ResponseEntity<>(errorRes, HttpStatus.OK);
	}
	
	@ExceptionHandler(CaptchaException.class)
	/**
	 * Performs handle captcha exception for MOSIP Pre-Registration.
	 *
	 * @param ex ex used by handle captcha exception
	 * @return handle captcha exception result
	 */
	public MainResponseDTO<?> handleCaptchaException(CaptchaException ex) {
		MainResponseDTO<?> response = new MainResponseDTO<>();
		response.setId(mosipcaptchaValidateId);
		response.setVersion(version);
		response.setResponsetime(getCurrentResponseTime());
		response.setResponse(null);
		ArrayList<ExceptionJSONInfoDTO> errors = new ArrayList<ExceptionJSONInfoDTO>();
		ExceptionJSONInfoDTO errorDetails = new ExceptionJSONInfoDTO(ex.getErrorCode(), ex.getErrorMessage());
		errors.add(errorDetails);
		response.setErrors(errors);
		return response;
	}
	
	@ExceptionHandler(Exception.class)
	/**
	 * Performs handle exception for MOSIP Pre-Registration.
	 *
	 * @param ex ex used by handle exception
	 * @return handle exception result
	 */
	public MainResponseDTO<?> handleException(Exception ex) {
		MainResponseDTO<?> response = new MainResponseDTO<>();
		response.setId(mosipcaptchaValidateId);
		response.setVersion(version);
		response.setResponsetime(getCurrentResponseTime());
		response.setResponse(null);
		ArrayList<ExceptionJSONInfoDTO> errors = new ArrayList<ExceptionJSONInfoDTO>();
		ExceptionJSONInfoDTO errorDetails = new ExceptionJSONInfoDTO("",ex.getMessage());
		errors.add(errorDetails);
		response.setErrors(errors);
		return response;
	}

}
