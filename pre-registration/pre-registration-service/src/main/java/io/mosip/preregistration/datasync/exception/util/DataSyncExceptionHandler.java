package io.mosip.preregistration.datasync.exception.util;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.util.ContentCachingRequestWrapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.util.DateUtils2;
import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.preregistration.core.common.dto.ExceptionJSONInfoDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.dto.ResponseWrapper;
import io.mosip.preregistration.core.errorcodes.ErrorCodes;
import io.mosip.preregistration.core.errorcodes.ErrorMessages;
import io.mosip.preregistration.core.exception.InvalidRequestException;
import io.mosip.preregistration.core.exception.InvalidRequestParameterException;
import io.mosip.preregistration.core.exception.PreRegistrationException;
import io.mosip.preregistration.core.exception.TableNotAccessibleException;
import io.mosip.preregistration.core.util.GenericUtil;
import io.mosip.preregistration.datasync.exception.DataSyncRecordNotFoundException;
import io.mosip.preregistration.datasync.exception.DemographicGetDetailsException;
import io.mosip.preregistration.datasync.exception.DocumentGetDetailsException;
import io.mosip.preregistration.datasync.exception.ParseResponseException;
import io.mosip.preregistration.datasync.exception.RecordNotFoundForDateRange;
import io.mosip.preregistration.datasync.exception.ReverseDataFailedToStoreException;
import io.mosip.preregistration.datasync.exception.ZipFileCreationException;
import io.mosip.preregistration.datasync.exception.system.SystemFileIOException;

/**
 * Exception Handler
 * 
 * @author M1046129
 *
 */
// Runs before PreRegistrationExceptionHandler, whose catch-all would otherwise take datasync errors.
// After the kernel AuthAdapterExceptionHandler (HIGHEST_PRECEDENCE), which keeps auth errors.
@RestControllerAdvice(basePackages = "io.mosip.preregistration.datasync")
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class DataSyncExceptionHandler {
	/**
	 * date time format used by this Pre-Registration type.
	 */
	private String dateTimeFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	
	@Autowired
	/**
	 * object mapper used by this Pre-Registration type.
	 */
	private ObjectMapper objectMapper;

	/**
	 * DataSyncRecordNotFoundException Handling
	 * 
	 * @param e
	 * @param request
	 * @return
	 */
	@ExceptionHandler(DataSyncRecordNotFoundException.class)
	public ResponseEntity<MainResponseDTO<?>> dataSyncRecordNotFound(final DataSyncRecordNotFoundException e) {
		return GenericUtil.errorResponse(e, e.getMainResponseDto());
	}

	/**
	 * ReverseDataFailedToStoreException Handling
	 * 
	 * @param e
	 * @param request
	 * @return
	 */
	@ExceptionHandler(ReverseDataFailedToStoreException.class)
	public ResponseEntity<MainResponseDTO<?>> reverseDataSyncFailedToStore(final ReverseDataFailedToStoreException e) {
		return GenericUtil.errorResponse(e, e.getMainResponseDto());
	}

	/**
	 * RecordNotFoundForDateRange hanlding
	 * 
	 * @param e
	 * @param request
	 * @return
	 */
	@ExceptionHandler(RecordNotFoundForDateRange.class)
	public ResponseEntity<MainResponseDTO<?>> databaseerror(final RecordNotFoundForDateRange e) {
		return GenericUtil.errorResponse(e, e.getMainResponseDto());
	}

	/**
	 * TablenotAccessibleException handling
	 * 
	 * @param e
	 * @param request
	 * @return
	 */
	@ExceptionHandler(TableNotAccessibleException.class)
	public ResponseEntity<MainResponseDTO<?>> databaseerror(final TableNotAccessibleException e) {
		return GenericUtil.errorResponse(e, e.getMainResposneDTO());
	}

	/**
	 * ZipFileCreationException handling
	 * 
	 * @param e
	 * @param request
	 * @return
	 */
	@ExceptionHandler(ZipFileCreationException.class)
	public ResponseEntity<MainResponseDTO<?>> zipNotCreated(final ZipFileCreationException e) {
		return GenericUtil.errorResponse(e, e.getMainResponseDto());
	}

	/**
	 * @param e
	 * @param request
	 * @return
	 */
	@ExceptionHandler(DemographicGetDetailsException.class)
	public ResponseEntity<MainResponseDTO<?>> demogetDetails(final DemographicGetDetailsException e) {
		return GenericUtil.errorResponse(e, e.getMainResponseDto());

	}

	/**
	 * @param e
	 * @param request
	 * @return
	 */
	@ExceptionHandler(DocumentGetDetailsException.class)
	public ResponseEntity<MainResponseDTO<?>> docGetDetails(final DocumentGetDetailsException e) {
		return GenericUtil.errorResponse(e, e.getMainResponseDto());

	}

	/**
	 * @param e
	 * @param request
	 * @return
	 */
	@ExceptionHandler(InvalidRequestParameterException.class)
	public ResponseEntity<MainResponseDTO<?>> invalidRequestParamCheck(final InvalidRequestParameterException e) {
		return GenericUtil.errorResponse(e, e.getMainResponseDto());

	}

	/**
	 * @param e
	 * @param request
	 * @return
	 */
	@ExceptionHandler(SystemFileIOException.class)
	public ResponseEntity<MainResponseDTO<?>> fileIOException(final SystemFileIOException e) {
		return GenericUtil.errorResponse(e, e.getMainResponseDto());

	}
	
	@ExceptionHandler(InvalidFormatException.class)
	/**
	 * Performs date format exception for MOSIP Pre-Registration.
	 *
	 * @param e e used by date format exception
	 * @return date format exception result
	 */
	public ResponseEntity<MainResponseDTO<?>> DateFormatException(final InvalidFormatException e){
		ExceptionJSONInfoDTO errorDetails = new ExceptionJSONInfoDTO(ErrorCodes.PRG_CORE_REQ_003.getCode(),ErrorMessages.INVALID_REQUEST_DATETIME.getMessage());
		MainResponseDTO<?> errorRes = new MainResponseDTO<>();
		List<ExceptionJSONInfoDTO> errorList = new ArrayList<>();
		errorList.add(errorDetails);
		errorRes.setErrors(errorList);
		errorRes.setResponsetime(GenericUtil.getCurrentResponseTime());
		return new ResponseEntity<>(errorRes, HttpStatus.OK);
	}
	
	@ExceptionHandler(ParseResponseException.class)
	/**
	 * Performs parse response exception for MOSIP Pre-Registration.
	 *
	 * @param e e used by parse response exception
	 * @return parse response exception result
	 */
	public ResponseEntity<MainResponseDTO<?>> parseResponseException(final ParseResponseException e){
		return GenericUtil.errorResponse(e, e.getResponse());
	}

	/**
	 * Returns current response time.
	 *
	 * @return get current response time result
	 */
	public String getCurrentResponseTime() {
		return DateUtils2.formatDate(new Date(System.currentTimeMillis()), dateTimeFormat);
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	/**
	 * Performs method argument not valid exception for MOSIP Pre-Registration.
	 *
	 * @param httpServletRequest http servlet request used by method argument not valid exception
	 * @param e e used by method argument not valid exception
	 * @return method argument not valid exception result
	 * @throws IOException when the operation cannot complete
	 */
	public ResponseEntity<ResponseWrapper<ServiceError>> methodArgumentNotValidException(
			final HttpServletRequest httpServletRequest, final MethodArgumentNotValidException e) throws IOException {
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		/**
		 * Returns binding result.
		 *
		 * @return get binding result result
		 */
		final List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
		fieldErrors.forEach(x -> {
			ServiceError error = new ServiceError(ErrorCodes.PRG_CORE_REQ_015.getCode(),
					x.getField() + ": " + x.getDefaultMessage());
			errorResponse.getErrors().add(error);
		});
		return new ResponseEntity<>(errorResponse, HttpStatus.OK);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	/**
	 * Performs on http message not readable for MOSIP Pre-Registration.
	 *
	 * @param httpServletRequest http servlet request used by on http message not readable
	 * @param e e used by on http message not readable
	 * @return on http message not readable result
	 * @throws IOException when the operation cannot complete
	 */
	public ResponseEntity<ResponseWrapper<ServiceError>> onHttpMessageNotReadable(
			final HttpServletRequest httpServletRequest, final HttpMessageNotReadableException e) throws IOException {
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		ServiceError error = new ServiceError(ErrorCodes.PRG_CORE_REQ_015.getCode(), e.getMessage());
		errorResponse.getErrors().add(error);
		return new ResponseEntity<>(errorResponse, HttpStatus.OK);
	}
	
	


	@ExceptionHandler(value = { Exception.class, RuntimeException.class })
	/**
	 * Performs default error handler for MOSIP Pre-Registration.
	 *
	 * @param httpServletRequest http servlet request used by default error handler
	 * @param e e used by default error handler
	 * @return default error handler result
	 * @throws IOException when the operation cannot complete
	 */
	public ResponseEntity<ResponseWrapper<ServiceError>> defaultErrorHandler(
			final HttpServletRequest httpServletRequest, Exception e) throws IOException {
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		ServiceError error = new ServiceError(ErrorCodes.PRG_CORE_REQ_016.getCode(), e.getMessage());
		errorResponse.getErrors().add(error);
		return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/**
	 * Sets errors.
	 *
	 * @param httpServletRequest http servlet request used by set errors
	 * @return set errors result
	 * @throws IOException when the operation cannot complete
	 */
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
	/**
	 * Performs common exception for MOSIP Pre-Registration.
	 *
	 * @param e e used by common exception
	 * @return common exception result
	 */
	public ResponseEntity<MainResponseDTO<?>> commonException(final PreRegistrationException e) {
		return GenericUtil.errorResponse(e, e.getMainresponseDTO());
	}
}
