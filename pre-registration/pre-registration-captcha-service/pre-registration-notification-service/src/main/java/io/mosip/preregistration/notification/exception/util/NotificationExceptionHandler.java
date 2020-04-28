
package io.mosip.preregistration.notification.exception.util;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.util.ContentCachingRequestWrapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.preregistration.core.common.dto.ExceptionJSONInfoDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.dto.ResponseWrapper;
import io.mosip.preregistration.core.errorcodes.ErrorCodes;
import io.mosip.preregistration.core.errorcodes.ErrorMessages;
import io.mosip.preregistration.core.exception.IllegalParamException;
import io.mosip.preregistration.core.exception.InvalidRequestException;
import io.mosip.preregistration.core.exception.InvalidRequestParameterException;
import io.mosip.preregistration.core.exception.PreRegistrationException;
import io.mosip.preregistration.core.util.GenericUtil;
import io.mosip.preregistration.notification.exception.BookingDetailsNotFoundException;
import io.mosip.preregistration.notification.exception.DemographicDetailsNotFoundException;
import io.mosip.preregistration.notification.exception.MandatoryFieldException;
import io.mosip.preregistration.notification.exception.NotificationSeriveException;
import io.mosip.preregistration.notification.exception.RestCallException;

/**
 * Exception Handler for acknowledgement application.
 * 
 * @author Sanober Noor
 * @since 1.0.0
 *
 */
@RestControllerAdvice
public class NotificationExceptionHandler {

	protected boolean falseStatus = false;

	/**
	 * @param e
	 * @param request
	 * @return response of MandatoryFieldRequiredException
	 */
	@ExceptionHandler(MandatoryFieldException.class)
	public ResponseEntity<MainResponseDTO<?>> mandatoryFieldrequired(final MandatoryFieldException e) {

		return GenericUtil.errorResponse(e, e.getMainResponseDTO());
	}

	 /**
	 * @param e
	 * @param request
	 * @return response of InvalidRequestParameterException
	 */
	 @ExceptionHandler(InvalidRequestParameterException.class)
	 public ResponseEntity<MainResponseDTO<?>> notificationFailed(final
			 InvalidRequestParameterException e){
		 return GenericUtil.errorResponse(e, e.getMainResponseDto());
	 }

	/**
	 * @param e
	 *            pass the exception
	 * @param request
	 *            pass the request
	 * @return response for NotificationSeriveException
	 */
	
	
	@ExceptionHandler(NotificationSeriveException.class)
	public ResponseEntity<MainResponseDTO<?>> authServiceException(final NotificationSeriveException e,WebRequest request){
		List<ExceptionJSONInfoDTO> errorList = new ArrayList<>();
		e.getValidationErrorList().stream().forEach(serviceError->errorList.add(new ExceptionJSONInfoDTO(serviceError.getErrorCode(),serviceError.getMessage())));
		MainResponseDTO<?> errorRes = e.getMainResposneDTO();
		errorRes.setErrors(errorList);
		errorRes.setResponsetime(GenericUtil.getCurrentResponseTime());
		return new ResponseEntity<>(errorRes, HttpStatus.OK);
	}
	/**
	 * @param e
	 *            pass the exception
	 * @param request
	 *            pass the request
	 * @return response for IllegalParamException
	 */
	@ExceptionHandler(IllegalParamException.class)
	public ResponseEntity<MainResponseDTO<?>> recException(final IllegalParamException e) {
		return GenericUtil.errorResponse(e, e.getMainResponseDto());
	}
	
	@ExceptionHandler(InvalidFormatException.class)
	public ResponseEntity<MainResponseDTO<?>> DateFormatException(final InvalidFormatException e,WebRequest request){
		ExceptionJSONInfoDTO errorDetails = new ExceptionJSONInfoDTO(ErrorCodes.PRG_CORE_REQ_003.getCode(),ErrorMessages.INVALID_REQUEST_DATETIME.getMessage());
		MainResponseDTO<?> errorRes = new MainResponseDTO<>();
		List<ExceptionJSONInfoDTO> errorList = new ArrayList<>();
		errorList.add(errorDetails);
		errorRes.setErrors(errorList);
		errorRes.setResponsetime(GenericUtil.getCurrentResponseTime());
		return new ResponseEntity<>(errorRes, HttpStatus.OK);
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> methodArgumentNotValidException(
			final HttpServletRequest httpServletRequest, final MethodArgumentNotValidException e) throws IOException {
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		final List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
		fieldErrors.forEach(x -> {
			ServiceError error = new ServiceError(io.mosip.preregistration.core.errorcodes.ErrorCodes.PRG_CORE_REQ_015.getCode(),
					x.getField() + ": " + x.getDefaultMessage());
			errorResponse.getErrors().add(error);
		});
		return new ResponseEntity<>(errorResponse, HttpStatus.OK);
	}
	
	
	/**
	 * @param e
	 *            pass the exception
	 * @param request
	 *            pass the request
	 * @return response for RestCallException
	 */
	@ExceptionHandler(RestCallException.class)
	public ResponseEntity<MainResponseDTO<?>> restCallException(final RestCallException e) {
		return GenericUtil.errorResponse(e, e.getMainresponseDTO());
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> onHttpMessageNotReadable(
			final HttpServletRequest httpServletRequest, final HttpMessageNotReadableException e) throws IOException {
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		ServiceError error = new ServiceError(io.mosip.preregistration.core.errorcodes.ErrorCodes.PRG_CORE_REQ_015.getCode(), e.getMessage());
		errorResponse.getErrors().add(error);
		return new ResponseEntity<>(errorResponse, HttpStatus.OK);
	}
	
	


	@ExceptionHandler(value = { Exception.class, RuntimeException.class })
	public ResponseEntity<ResponseWrapper<ServiceError>> defaultErrorHandler(
			final HttpServletRequest httpServletRequest, Exception e) throws IOException {
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		ServiceError error = new ServiceError(io.mosip.preregistration.core.errorcodes.ErrorCodes.PRG_CORE_REQ_016.getCode(), e.getMessage());
		errorResponse.getErrors().add(error);
		return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	/**
	 * @param e
	 *            pass the exception
	 * @param request
	 *            pass the request
	 * @return response for DemographicDetailsNotFoundException
	 */
	
	
	@ExceptionHandler(DemographicDetailsNotFoundException.class)
	public ResponseEntity<MainResponseDTO<?>> demographicDetailsNotFoundException(final DemographicDetailsNotFoundException e,WebRequest request){
		MainResponseDTO<?> errorRes = e.getMainResposneDTO();
		errorRes.setErrors(e.getErrorList());
		errorRes.setResponsetime(GenericUtil.getCurrentResponseTime());
		return new ResponseEntity<>(errorRes, HttpStatus.OK);
	}
	
	/**
	 * @param e
	 *            pass the exception
	 * @param request
	 *            pass the request
	 * @return response for DemographicDetailsNotFoundException
	 */
	
	
	@ExceptionHandler(BookingDetailsNotFoundException.class)
	public ResponseEntity<MainResponseDTO<?>> bookingDetailsNotFoundException(final BookingDetailsNotFoundException e,WebRequest request){
		MainResponseDTO<?> errorRes = e.getMainResposneDTO();
		errorRes.setErrors(e.getErrorList());
		errorRes.setResponsetime(GenericUtil.getCurrentResponseTime());
		return new ResponseEntity<>(errorRes, HttpStatus.OK);
	}
	
	

	@Autowired
	private ObjectMapper objectMapper;
	
	private ResponseWrapper<ServiceError> setErrors(HttpServletRequest httpServletRequest) throws IOException {
		ResponseWrapper<ServiceError> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponsetime(LocalDateTime.now(ZoneId.of("UTC")));
		String requestBody = null;
		if (httpServletRequest instanceof ContentCachingRequestWrapper) {
			requestBody = new String(((ContentCachingRequestWrapper) httpServletRequest).getContentAsByteArray());
		}
		if (EmptyCheckUtils.isNullEmpty(requestBody)) {
			return responseWrapper;
		}
		objectMapper.registerModule(new JavaTimeModule());
		JsonNode reqNode = objectMapper.readTree(requestBody);
		responseWrapper.setId(reqNode.path("id").asText());
		responseWrapper.setVersion(reqNode.path("version").asText());
		return responseWrapper;
	}
	/**
	 * 
	 * @param e
	 * @return
	 */
	@ExceptionHandler(InvalidRequestException.class)
	public ResponseEntity<MainResponseDTO<?>> invalidRequestException(final InvalidRequestException e) {
		return GenericUtil.errorResponse(e, e.getMainResponseDto());
	}
	
	@ExceptionHandler(PreRegistrationException.class)
	public ResponseEntity<MainResponseDTO<?>> commonException(final PreRegistrationException e) {
		return GenericUtil.errorResponse(e, e.getMainresponseDTO());
	}
}
