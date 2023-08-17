package io.mosip.preregistration.application.exception.util;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.util.ContentCachingRequestWrapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.preregistration.application.code.DocumentStatusMessages;
import io.mosip.preregistration.application.errorcodes.DocumentErrorCodes;
import io.mosip.preregistration.application.exception.AuditFailedException;
import io.mosip.preregistration.application.exception.BookingDeletionFailedException;
import io.mosip.preregistration.application.exception.BookingDetailsNotFoundException;
import io.mosip.preregistration.application.exception.ConfigFileNotFoundException;
import io.mosip.preregistration.application.exception.CryptocoreException;
import io.mosip.preregistration.application.exception.DTOMappigException;
import io.mosip.preregistration.application.exception.DemographicDetailsNotFoundException;
import io.mosip.preregistration.application.exception.DemographicGetDetailsException;
import io.mosip.preregistration.application.exception.DemographicServiceException;
import io.mosip.preregistration.application.exception.DeprecatedException;
import io.mosip.preregistration.application.exception.DocumentFailedToCopyException;
import io.mosip.preregistration.application.exception.DocumentFailedToDeleteException;
import io.mosip.preregistration.application.exception.DocumentFailedToUploadException;
import io.mosip.preregistration.application.exception.DocumentNotFoundException;
import io.mosip.preregistration.application.exception.DocumentNotValidException;
import io.mosip.preregistration.application.exception.DocumentSizeExceedException;
import io.mosip.preregistration.application.exception.DocumentVirusScanException;
import io.mosip.preregistration.application.exception.DuplicatePridKeyException;
import io.mosip.preregistration.application.exception.FSServerException;
import io.mosip.preregistration.application.exception.FileNotFoundException;
import io.mosip.preregistration.application.exception.IdValidationException;
import io.mosip.preregistration.application.exception.InvalidDateFormatException;
import io.mosip.preregistration.application.exception.InvalidDocumentIdExcepion;
import io.mosip.preregistration.application.exception.InvalidOtpOrUseridException;
import io.mosip.preregistration.application.exception.InvalidateTokenException;
import io.mosip.preregistration.application.exception.LoginServiceException;
import io.mosip.preregistration.application.exception.MandatoryFieldException;
import io.mosip.preregistration.application.exception.MandatoryFieldNotFoundException;
import io.mosip.preregistration.application.exception.MandatoryFieldRequiredException;
import io.mosip.preregistration.application.exception.MasterDataException;
import io.mosip.preregistration.application.exception.MissingRequestParameterException;
import io.mosip.preregistration.application.exception.NoAuthTokenException;
import io.mosip.preregistration.application.exception.NotificationSeriveException;
import io.mosip.preregistration.application.exception.OperationNotAllowedException;
import io.mosip.preregistration.application.exception.ParsingException;
import io.mosip.preregistration.application.exception.PrimaryKeyValidationException;
import io.mosip.preregistration.application.exception.QrCodeIOException;
import io.mosip.preregistration.application.exception.RecordFailedToUpdateException;
import io.mosip.preregistration.application.exception.RecordNotFoundException;
import io.mosip.preregistration.application.exception.RecordNotFoundForPreIdsException;
import io.mosip.preregistration.application.exception.SchemaValidationException;
import io.mosip.preregistration.application.exception.SendOtpFailedException;
import io.mosip.preregistration.application.exception.UnSupportedLanguageException;
import io.mosip.preregistration.application.exception.UserIdOtpFaliedException;
import io.mosip.preregistration.core.common.dto.ExceptionJSONInfoDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.dto.ResponseWrapper;
import io.mosip.preregistration.core.errorcodes.ErrorCodes;
import io.mosip.preregistration.core.errorcodes.ErrorMessages;
import io.mosip.preregistration.core.exception.DecryptionFailedException;
import io.mosip.preregistration.core.exception.EncryptionFailedException;
import io.mosip.preregistration.core.exception.HashingException;
import io.mosip.preregistration.core.exception.IllegalParamException;
import io.mosip.preregistration.core.exception.InvalidRequestException;
import io.mosip.preregistration.core.exception.InvalidRequestParameterException;
import io.mosip.preregistration.core.exception.MasterDataNotAvailableException;
import io.mosip.preregistration.core.exception.PreIdInvalidForUserIdException;
import io.mosip.preregistration.core.exception.PreRegistrationException;
import io.mosip.preregistration.core.exception.RecordFailedToDeleteException;
import io.mosip.preregistration.core.exception.RestCallException;
import io.mosip.preregistration.core.exception.TableNotAccessibleException;
import io.mosip.preregistration.core.exception.util.ParseResponseException;
import io.mosip.preregistration.core.util.GenericUtil;
import io.mosip.preregistration.demographic.exception.system.JsonParseException;
import io.mosip.preregistration.demographic.exception.system.JsonValidationException;
import io.mosip.preregistration.demographic.exception.system.SystemFileIOException;
import io.mosip.preregistration.demographic.exception.system.SystemIllegalArgumentException;
import io.mosip.preregistration.demographic.exception.system.SystemUnsupportedEncodingException;
import net.logstash.logback.encoder.org.apache.commons.lang3.StringUtils;

@RestControllerAdvice
public class PreRegistrationExceptionHandler {

	/** The Environment. */
	@Autowired
	protected Environment env;

	/** The id. */
	@Resource
	protected Map<String, String> id;

	@Autowired
	private ObjectMapper objectMapper;

	@Value("${mosip.utc-datetime-pattern}")
	private String utcDateTimePattern;

	/**
	 * Reference for ${mosip.preregistration.document.upload.id} from property file
	 */
	@Value("${mosip.preregistration.document.upload.id}")
	private String uploadId;

	/**
	 * Reference for ${ver} from property file
	 */
	@Value("${version}")
	private String ver;

	@ExceptionHandler(SendOtpFailedException.class)
	public ResponseEntity<MainResponseDTO<?>> sendOtpException(final SendOtpFailedException e) {
		return GenericUtil.errorResponse(e, e.getMainResposneDto());
	}

	@ExceptionHandler(UserIdOtpFaliedException.class)
	public ResponseEntity<MainResponseDTO<?>> userIdOtpException(final UserIdOtpFaliedException e) {
		return GenericUtil.errorResponse(e, e.getMainResponseDto());
	}

	@ExceptionHandler(InvalidateTokenException.class)
	public ResponseEntity<MainResponseDTO<?>> invalidateTokenException(final InvalidateTokenException e) {
		return GenericUtil.errorResponse(e, e.getMainResponseDto());
	}

	@ExceptionHandler(LoginServiceException.class)
	public ResponseEntity<MainResponseDTO<?>> authServiceException(final LoginServiceException e) {
		List<ExceptionJSONInfoDTO> errorList = new ArrayList<>();
		e.getValidationErrorList().stream().forEach(serviceError -> errorList
				.add(new ExceptionJSONInfoDTO(serviceError.getErrorCode(), serviceError.getMessage())));
		MainResponseDTO<?> errorRes = e.getMainResposneDTO();
		errorRes.setErrors(errorList);
		errorRes.setResponsetime(GenericUtil.getCurrentResponseTime());
		return new ResponseEntity<>(errorRes, HttpStatus.OK);
	}

	@ExceptionHandler(ParseResponseException.class)
	public ResponseEntity<MainResponseDTO<?>> parseResponseException(final ParseResponseException e) {
		return GenericUtil.errorResponse(e, e.getMainResponseDto());
	}

	/**
	 * @param e       pass the exception
	 * @param request pass the request
	 * @return response for ConfigFileNotFoundException
	 */
	@ExceptionHandler(ConfigFileNotFoundException.class)
	public ResponseEntity<MainResponseDTO<?>> configFileNotFoundException(final ConfigFileNotFoundException e) {
		return GenericUtil.errorResponse(e, e.getMainResposneDto());
	}

	@ExceptionHandler(InvalidOtpOrUseridException.class)
	public ResponseEntity<MainResponseDTO<?>> InavlidOtpOrUserIdException(final InvalidOtpOrUseridException e) {
		return GenericUtil.errorResponse(e, e.getMainResponseDto());
	}

	@ExceptionHandler(NoAuthTokenException.class)
	public ResponseEntity<MainResponseDTO<?>> NoAuthTokenException(final NoAuthTokenException e) {
		return GenericUtil.errorResponse(e, e.getMainResponseDto());
	}

	/**
	 * @param e       pass the exception
	 * @param request pass the request
	 * @return response for TableNotAccessibleException
	 */
	@ExceptionHandler(TableNotAccessibleException.class)
	public ResponseEntity<MainResponseDTO<?>> databaseerror(final TableNotAccessibleException e) {
		return GenericUtil.errorResponse(e, e.getMainResposneDTO());
	}

	/**
	 * @param e       pass the exception
	 * @param request pass the request
	 * @return response for JsonValidationException
	 */
	@ExceptionHandler(JsonValidationException.class)
	public ResponseEntity<MainResponseDTO<?>> jsonValidationException(final JsonValidationException e) {
		return GenericUtil.errorResponse(e, e.getMainResposneDTO());
	}

	/**
	 * @param e       pass the exception
	 * @param request pass the request
	 * @return response for RecordNotFoundException
	 */
	@ExceptionHandler(RecordNotFoundException.class)
	public ResponseEntity<MainResponseDTO<?>> recException(final RecordNotFoundException e) {
		return GenericUtil.errorResponse(e, e.getMainresponseDTO());
	}

	/**
	 * @param e       pass the exception
	 * @param request pass the request
	 * @return response for RecordNotFoundException
	 */
	@ExceptionHandler(RecordNotFoundForPreIdsException.class)
	public ResponseEntity<MainResponseDTO<?>> recPreIdsException(final RecordNotFoundForPreIdsException e) {
		return GenericUtil.errorResponse(e, e.getMainResponseDTO());
	}

	/**
	 * @param e       pass the exception
	 * @param request pass the request
	 * @return response for SystemIllegalArgumentException
	 */
	@ExceptionHandler(SystemIllegalArgumentException.class)
	public ResponseEntity<MainResponseDTO<?>> illegalArgumentException(final SystemIllegalArgumentException e) {
		return GenericUtil.errorResponse(e, e.getMainresponseDTO());
	}

	/**
	 * @param e       pass the exception
	 * @param request pass the request
	 * @return response for DocumentFailedToDeleteException
	 */
	@ExceptionHandler(DocumentFailedToDeleteException.class)
	public ResponseEntity<MainResponseDTO<?>> documentFailedToDeleteException(final DocumentFailedToDeleteException e) {
		return GenericUtil.errorResponse(e, e.getMainresponseDTO());
	}

	/**
	 * @param e       pass the exception
	 * @param request pass the request
	 * @return response for RecordFailedToDeleteException
	 */
	@ExceptionHandler(RecordFailedToDeleteException.class)
	public ResponseEntity<MainResponseDTO<?>> recordFailedToDeleteException(final RecordFailedToDeleteException e) {
		return GenericUtil.errorResponse(e, e.getMainResponseDTO());
	}

	/**
	 * @param e       pass the exception
	 * @param request pass the request
	 * @return response for OperationNotAllowedException
	 */
	@ExceptionHandler(OperationNotAllowedException.class)
	public ResponseEntity<MainResponseDTO<?>> operationNotAllowedException(final OperationNotAllowedException e) {
		return GenericUtil.errorResponse(e, e.getMainresponseDTO());
	}

	@ExceptionHandler(AuditFailedException.class)
	public ResponseEntity<MainResponseDTO<?>> auditFailedException(final AuditFailedException e) {
		ExceptionJSONInfoDTO errorDetails = new ExceptionJSONInfoDTO(e.getErrorCode(), e.getErrorMessage());
		MainResponseDTO<?> errorRes = new MainResponseDTO<>();
		errorRes.setResponsetime(getCurrentResponseTime());
		errorRes.setVersion(ver);

		List<ExceptionJSONInfoDTO> errorList = new ArrayList<>();
		errorList.add(errorDetails);
		errorRes.setErrors(errorList);

		return new ResponseEntity<>(errorRes, HttpStatus.OK);
	}

	/**
	 * @param e       pass the exception
	 * @param request pass the request
	 * @return response for InvalidRequestParameterException
	 */
	@ExceptionHandler(InvalidRequestParameterException.class)
	public ResponseEntity<MainResponseDTO<?>> invalidRequestParameterException(
			final InvalidRequestParameterException e) {

		MainResponseDTO<?> errorRes = e.getMainResponseDto();
		if (errorRes.getId() == null) {
			errorRes.setId(id.get(e.getOperation()));
		}
		errorRes.setVersion(env.getProperty("version"));
		errorRes.setErrors(e.getExptionList());
		errorRes.setResponsetime(GenericUtil.getCurrentResponseTime());
		if (errorRes.getErrors() == null) {
			List<ExceptionJSONInfoDTO> errorList = new ArrayList<>();
			ExceptionJSONInfoDTO error = new ExceptionJSONInfoDTO();
			error.setErrorCode(e.getErrorCode());
			error.setMessage(e.getErrorText());
			errorList.add(error);
			errorRes.setErrors(errorList);
		}
		return new ResponseEntity<>(errorRes, HttpStatus.OK);
	}

	/**
	 * @param e       pass the exception
	 * @param request pass the request
	 * @return response for InvalidRequestParameterException
	 */
	@ExceptionHandler(CryptocoreException.class)
	public ResponseEntity<MainResponseDTO<?>> cryptocoreException(final CryptocoreException e) {
		return GenericUtil.errorResponse(e, e.getMainresponseDTO());
	}

	/**
	 * @param e       pass the exception
	 * @param request pass the request
	 * @return response for RecordFailedToUpdateException
	 */
	@ExceptionHandler(RecordFailedToUpdateException.class)
	public ResponseEntity<MainResponseDTO<?>> recordFailedToUpdateException(final RecordFailedToUpdateException e) {
		return GenericUtil.errorResponse(e, e.getMainResponseDTO());
	}

	/**
	 * @param e       pass the exception
	 * @param request pass the request
	 * @return response for SystemUnsupportedEncodingException
	 */
	@ExceptionHandler(SystemUnsupportedEncodingException.class)
	public ResponseEntity<MainResponseDTO<?>> systemUnsupportedEncodingException(
			final SystemUnsupportedEncodingException e) {
		return GenericUtil.errorResponse(e, e.getMainresponseDTO());
	}

	/**
	 * @param e       pass the exception
	 * @param request pass the request
	 * @return response for MissingRequestParameterException
	 */
	@ExceptionHandler(MissingRequestParameterException.class)
	public ResponseEntity<MainResponseDTO<?>> missingRequestParameterException(
			final MissingRequestParameterException e) {
		return GenericUtil.errorResponse(e, e.getMainresponseDTO());
	}

	/**
	 * @param e       pass the exception
	 * @param request pass the request
	 * @return response for JsonParseException
	 */
	@ExceptionHandler(JsonParseException.class)
	public ResponseEntity<MainResponseDTO<?>> jsonParseException(final JsonParseException e) {
		return GenericUtil.errorResponse(e, e.getMainresponseDTO());
	}

	/**
	 * @param e       pass the exception
	 * @param request pass the request
	 * @return response for SystemFileIOException
	 */
	@ExceptionHandler(SystemFileIOException.class)
	public ResponseEntity<MainResponseDTO<?>> systemFileIOException(final SystemFileIOException e) {
		return GenericUtil.errorResponse(e, e.getMainResposneDTO());
	}

	/**
	 * @param e       pass the exception
	 * @param request pass the request
	 * @return response for SystemFileIOException
	 */
	@ExceptionHandler(InvalidDateFormatException.class)
	public ResponseEntity<MainResponseDTO<?>> InvalidDateFormatException(final InvalidDateFormatException e) {
		ExceptionJSONInfoDTO errorDetails = new ExceptionJSONInfoDTO(e.getErrorCode(), e.getErrorText());
		MainResponseDTO<?> errorRes = new MainResponseDTO<>();
		List<ExceptionJSONInfoDTO> errorList = new ArrayList<>();
		errorList.add(errorDetails);
		errorRes.setId(e.getMainResponseDTO().getId());
		errorRes.setVersion(e.getMainResponseDTO().getVersion());
		errorRes.setErrors(errorList);
		errorRes.setResponsetime(GenericUtil.getCurrentResponseTime());
		return new ResponseEntity<>(errorRes, HttpStatus.OK);
	}

	/**
	 * @param e       pass the exception
	 * @param request pass the request
	 * @return response for SystemFileIOException
	 */
	@ExceptionHandler(BookingDeletionFailedException.class)
	public ResponseEntity<MainResponseDTO<?>> bookingDeletionFailedException(final BookingDeletionFailedException e) {
		return GenericUtil.errorResponse(e, e.getMainresponseDTO());
	}

	/**
	 * @param e       pass the exception
	 * @param request pass the request
	 * @return response for SystemFileIOException
	 */
	@ExceptionHandler(HashingException.class)
	public ResponseEntity<MainResponseDTO<?>> HashingException(final HashingException e) {
		return GenericUtil.errorResponse(e, e.getMainresponseDTO());
	}

	/**
	 * @param e       pass the exception
	 * @param request pass the request
	 * @return response for DecryptionFailedException
	 */
	@ExceptionHandler(DecryptionFailedException.class)
	public ResponseEntity<MainResponseDTO<?>> decryptionFailedException(final DecryptionFailedException e) {
		return GenericUtil.errorResponse(e, e.getMainresponseDTO());
	}

	/**
	 * @param e       pass the exception
	 * @param request pass the request
	 * @return response for RestCallException
	 */
	@ExceptionHandler(RestCallException.class)
	public ResponseEntity<MainResponseDTO<?>> restCallException(final RestCallException e) {
		return GenericUtil.errorResponse(e, e.getMainResponseDTO());
	}

	/**
	 * @param e       pass the exception
	 * @param request pass the request
	 * @return response for SchemaValidationException
	 */
	@ExceptionHandler(SchemaValidationException.class)
	public ResponseEntity<MainResponseDTO<?>> restCallException(final SchemaValidationException e) {
		return GenericUtil.errorResponse(e, e.getMainresponseDTO());
	}

	@ExceptionHandler(InvalidFormatException.class)
	public ResponseEntity<MainResponseDTO<?>> DateFormatException(final InvalidFormatException e, WebRequest request) {
		ExceptionJSONInfoDTO errorDetails = new ExceptionJSONInfoDTO(ErrorCodes.PRG_CORE_REQ_003.getCode(),
				ErrorMessages.INVALID_REQUEST_DATETIME.getMessage());
		MainResponseDTO<?> errorRes = new MainResponseDTO<>();
		List<ExceptionJSONInfoDTO> errorList = new ArrayList<>();
		errorList.add(errorDetails);
		errorRes.setErrors(errorList);
		errorRes.setResponsetime(GenericUtil.getCurrentResponseTime());
		return new ResponseEntity<>(errorRes, HttpStatus.OK);
	}

	/**
	 * @param e       pass the exception
	 * @param request pass the request
	 * @return response for EncryptionFailedException
	 */
	@ExceptionHandler(EncryptionFailedException.class)
	public ResponseEntity<MainResponseDTO<?>> encryptionFailedException(final EncryptionFailedException e) {
		List<ExceptionJSONInfoDTO> errorList = new ArrayList<>();
		e.getValidationErrorList().stream().forEach(serviceError -> errorList
				.add(new ExceptionJSONInfoDTO(serviceError.getErrorCode(), serviceError.getMessage())));
		MainResponseDTO<?> errorRes = e.getMainresponseDTO();
		errorRes.setErrors(errorList);
		errorRes.setResponsetime(GenericUtil.getCurrentResponseTime());
		return new ResponseEntity<>(errorRes, HttpStatus.OK);
	}

	@ExceptionHandler(IdValidationException.class)
	public ResponseEntity<MainResponseDTO<?>> idValidationException(final IdValidationException e) {
		List<ExceptionJSONInfoDTO> errorList = new ArrayList<>();
		e.getErrorMessageList().stream().forEach(s -> errorList.add(new ExceptionJSONInfoDTO(e.getErrorCode(), s)));
		MainResponseDTO<?> errorRes = e.getMainResposneDTO();
		errorRes.setErrors(errorList);
		errorRes.setResponsetime(GenericUtil.getCurrentResponseTime());
		return new ResponseEntity<>(errorRes, HttpStatus.OK);
	}

	@ExceptionHandler(DemographicServiceException.class)
	public ResponseEntity<MainResponseDTO<?>> demographicServiceException(final DemographicServiceException e) {
		List<ExceptionJSONInfoDTO> errorList = new ArrayList<>();
		e.getValidationErrorList().stream().forEach(serviceError -> errorList
				.add(new ExceptionJSONInfoDTO(serviceError.getErrorCode(), serviceError.getMessage())));
		MainResponseDTO<?> errorRes = e.getMainResposneDTO();
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
			ServiceError error = new ServiceError(ErrorCodes.PRG_CORE_REQ_015.getCode(),
					x.getField() + ": " + x.getDefaultMessage());
			errorResponse.getErrors().add(error);
		});
		return new ResponseEntity<>(errorResponse, HttpStatus.OK);
	}

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResponseWrapper<ServiceError>> onHttpMessageNotReadable(
            final HttpServletRequest httpServletRequest, final HttpMessageNotReadableException e) throws IOException {
        ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
        String errorMessage = StringUtils.substringBefore(e.getMessage(), ":");
        ServiceError error = new ServiceError(ErrorCodes.PRG_CORE_REQ_015.getCode(), errorMessage);
        errorResponse.getErrors().add(error);
        return new ResponseEntity<>(errorResponse, HttpStatus.OK);
	}

	@ExceptionHandler(value = { Exception.class, RuntimeException.class })
	public ResponseEntity<ResponseWrapper<ServiceError>> defaultErrorHandler(
			final HttpServletRequest httpServletRequest, Exception e) throws IOException {
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		ServiceError error = new ServiceError(ErrorCodes.PRG_CORE_REQ_016.getCode(), e.getMessage());
		errorResponse.getErrors().add(error);
		return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(value = AccessDeniedException.class)
	public ResponseEntity<MainResponseDTO<?>> AccessDeniedExceptionHandler(final AccessDeniedException e) {
		MainResponseDTO<?> errorRes = new MainResponseDTO<>();
		List<ExceptionJSONInfoDTO> errorList = new ArrayList<>();
		ExceptionJSONInfoDTO errorDetails = new ExceptionJSONInfoDTO("KER-401", "Authentication Failed");
		errorList.add(errorDetails);
		errorRes.setErrors(errorList);
		errorRes.setResponsetime(DateUtils.formatDate(new Date(), utcDateTimePattern));
		return new ResponseEntity<>(errorRes, HttpStatus.UNAUTHORIZED);
	}

	/**
	 * @param e       pass the exception
	 * @param request pass the request
	 * @return response for DuplicateKeyException
	 */
	@ExceptionHandler(DuplicatePridKeyException.class)
	public ResponseEntity<MainResponseDTO<?>> duplicateKeyException(final DuplicatePridKeyException e) {
		return GenericUtil.errorResponse(e, e.getMainresponseDTO());
	}

	@ExceptionHandler(PreRegistrationException.class)
	public ResponseEntity<MainResponseDTO<?>> commonException(final PreRegistrationException e) {
		return GenericUtil.errorResponse(e, e.getMainresponseDTO());
	}

	/**
	 * 
	 * @param InvalidRequestException e
	 * @return ResponseEntity<MainResponseDTO<?>>
	 */
	@ExceptionHandler(InvalidRequestException.class)
	public ResponseEntity<MainResponseDTO<?>> invalidRequestException(final InvalidRequestException e) {
		return GenericUtil.errorResponse(e, e.getMainResponseDto());
	}

	@ExceptionHandler(MandatoryFieldRequiredException.class)
	public ResponseEntity<MainResponseDTO<?>> mandatoryFieldrequired(final MandatoryFieldRequiredException e,
			WebRequest request) {

		ExceptionJSONInfoDTO errorDetails = new ExceptionJSONInfoDTO(e.getErrorCode(), e.getErrorText());
		MainResponseDTO<?> errorRes = new MainResponseDTO<>();
		List<ExceptionJSONInfoDTO> errorList = new ArrayList<>();
		errorList.add(errorDetails);
		errorRes.setErrors(errorList);
		errorRes.setId(e.getMainResponseDTO().getId());
		errorRes.setVersion(e.getMainResponseDTO().getVersion());
		errorRes.setResponsetime(DateUtils.formatDate(new Date(), utcDateTimePattern));

		return new ResponseEntity<>(errorRes, HttpStatus.OK);
	}

	@ExceptionHandler(MasterDataException.class)
	public ResponseEntity<MainResponseDTO<?>> masterDataCallException(final MasterDataException e) {

		MainResponseDTO<?> failureResponse = new MainResponseDTO<>();

		List<ExceptionJSONInfoDTO> exceptionList = new ArrayList<ExceptionJSONInfoDTO>();
		ExceptionJSONInfoDTO exception = new ExceptionJSONInfoDTO();
		exception.setErrorCode(e.getErrorCode());
		exception.setMessage(e.getErrorMessage());

		exceptionList.add(exception);
		failureResponse.setErrors(exceptionList);
		return ResponseEntity.status(HttpStatus.OK).body(failureResponse);
	}

	/**
	 * @param e       pass the exception
	 * @param request pass the request
	 * @return response for IllegalParamException
	 */
	@ExceptionHandler(IllegalParamException.class)
	public ResponseEntity<MainResponseDTO<?>> recException(final IllegalParamException e) {
		return GenericUtil.errorResponse(e, e.getMainResponseDto());
	}

	/**
	 * @param e       pass the exception
	 * @param request pass the request
	 * @return response for UnSupportedLanguageException
	 */
	@ExceptionHandler(UnSupportedLanguageException.class)
	public ResponseEntity<MainResponseDTO<?>> recException(final UnSupportedLanguageException e) {
		return GenericUtil.errorResponse(e, e.getMainResponseDTO());
	}

	protected boolean falseStatus = false;

	/**
	 * @param e
	 * @param request
	 * @return response of MandatoryFieldRequiredException
	 */
	@ExceptionHandler(QrCodeIOException.class)
	public ResponseEntity<MainResponseDTO<?>> mandatoryFieldrequired(final QrCodeIOException e) {
		return GenericUtil.errorResponse(e, e.getMainResponseDTO());
	}

	@ExceptionHandler(NotificationSeriveException.class)
	public ResponseEntity<MainResponseDTO<?>> authServiceException(final NotificationSeriveException e,
			WebRequest request) {
		List<ExceptionJSONInfoDTO> errorList = new ArrayList<>();
		e.getValidationErrorList().stream().forEach(serviceError -> errorList
				.add(new ExceptionJSONInfoDTO(serviceError.getErrorCode(), serviceError.getMessage())));
		MainResponseDTO<?> errorRes = e.getMainResposneDTO();
		errorRes.setErrors(errorList);
		errorRes.setResponsetime(GenericUtil.getCurrentResponseTime());
		return new ResponseEntity<>(errorRes, HttpStatus.OK);
	}

	@ExceptionHandler(DemographicDetailsNotFoundException.class)
	public ResponseEntity<MainResponseDTO<?>> demographicDetailsNotFoundException(
			final DemographicDetailsNotFoundException e, WebRequest request) {
		MainResponseDTO<?> errorRes = e.getMainResposneDTO();
		errorRes.setErrors(e.getErrorList());
		errorRes.setResponsetime(GenericUtil.getCurrentResponseTime());
		return new ResponseEntity<>(errorRes, HttpStatus.OK);
	}

	/**
	 * @param e       pass the exception
	 * @param request pass the request
	 * @return response for DemographicDetailsNotFoundException
	 */

	@ExceptionHandler(BookingDetailsNotFoundException.class)
	public ResponseEntity<MainResponseDTO<?>> bookingDetailsNotFoundException(final BookingDetailsNotFoundException e,
			WebRequest request) {
		MainResponseDTO<?> errorRes = e.getMainResposneDTO();
		errorRes.setErrors(e.getErrorList());
		errorRes.setResponsetime(GenericUtil.getCurrentResponseTime());
		return new ResponseEntity<>(errorRes, HttpStatus.OK);
	}

	@ExceptionHandler(PreIdInvalidForUserIdException.class)
	public ResponseEntity<MainResponseDTO<?>> invalidUserException(final PreIdInvalidForUserIdException e) {
		return GenericUtil.errorResponse(e, e.getMainresponseDTO());
	}

	/**
	 * @param e       pass the exception
	 * @param request pass the request
	 * @return response for DemographicGetDetailsException
	 */
	@ExceptionHandler(DemographicGetDetailsException.class)
	public ResponseEntity<MainResponseDTO<?>> databaseerror(final DemographicGetDetailsException e) {
		return GenericUtil.errorResponse(e, e.getResponse());
	}

	/**
	 * @param nv         pass the exception
	 * @param webRequest pass the request
	 * @return response for DocumentNotValidException
	 */
	@ExceptionHandler(DocumentNotValidException.class)
	public ResponseEntity<MainResponseDTO<?>> notValidExceptionhadler(final DocumentNotValidException e) {
		return GenericUtil.errorResponse(e, e.getResponse());
	}

	/**
	 * @param nv         pass the exception
	 * @param webRequest pass the request
	 * @return response for DTOMappigException
	 */
	@ExceptionHandler(DTOMappigException.class)
	public ResponseEntity<MainResponseDTO<?>> dtoMappingExc(final DTOMappigException e) {
		return GenericUtil.errorResponse(e, e.getResponse());
	}

	/**
	 * @param nv         pass the exception
	 * @param webRequest pass the request
	 * @return response for FileNotFoundException
	 */
	@ExceptionHandler(FileNotFoundException.class)
	public ResponseEntity<MainResponseDTO<?>> fileNotFoundException(final FileNotFoundException e) {
		return GenericUtil.errorResponse(e, e.getResponse());
	}

	/**
	 * @param nv         pass the exception
	 * @param webRequest pass the request
	 * @return response for MandatoryFieldNotFoundException
	 */
	@ExceptionHandler(MandatoryFieldNotFoundException.class)
	public ResponseEntity<MainResponseDTO<?>> mandatoryFieldNotFoundException(final MandatoryFieldNotFoundException e) {
		return GenericUtil.errorResponse(e, e.getResponse());

	}

	/**
	 * @param nv         pass the exception
	 * @param webRequest pass the request
	 * @return response for ParsingException
	 */
	@ExceptionHandler(ParsingException.class)
	public ResponseEntity<MainResponseDTO<?>> parsingException(final ParsingException e) {
		return GenericUtil.errorResponse(e, e.getResponse());

	}

	/**
	 * @param me         pass the exception
	 * @param webRequest pass the request
	 * @return response for MultipartException
	 */
	@ExceptionHandler(MultipartException.class)
	public ResponseEntity<MainResponseDTO<?>> sizeExceedException(final MultipartException e) {
		ExceptionJSONInfoDTO errorDetails = new ExceptionJSONInfoDTO(DocumentErrorCodes.PRG_PAM_DOC_004.toString(),
				DocumentStatusMessages.DOCUMENT_EXCEEDING_PERMITTED_SIZE.toString());
		MainResponseDTO<?> errorRes = new MainResponseDTO<>();
		List<ExceptionJSONInfoDTO> errorList = new ArrayList<>();
		errorList.add(errorDetails);
		errorRes.setErrors(errorList);
		errorRes.setResponsetime(getCurrentResponseTime());
		errorRes.setId(uploadId);
		errorRes.setVersion(ver);
		return new ResponseEntity<>(errorRes, HttpStatus.OK);
	}

	/**
	 * @param e       pass the exception
	 * @param request pass the request
	 * @return response for DocumentNotFoundException
	 */
	@ExceptionHandler(DocumentNotFoundException.class)
	public ResponseEntity<MainResponseDTO<?>> documentNotFound(final DocumentNotFoundException e) {
		return GenericUtil.errorResponse(e, e.getResponse());
	}

	/**
	 * @param e       pass the exception
	 * @param request pass the request
	 * @return response for MasterDataNotAvailableException
	 */
	@ExceptionHandler(MasterDataNotAvailableException.class)
	public ResponseEntity<MainResponseDTO<?>> masterDataNotAvailableException(final MasterDataNotAvailableException e) {
		return GenericUtil.errorResponse(e, e.getMainResponseDTO());
	}

	/**
	 * @param e       pass the exception
	 * @param request pass the request
	 * @return response for DocumentSizeExceedException
	 */
	@ExceptionHandler(DocumentSizeExceedException.class)
	public ResponseEntity<MainResponseDTO<?>> documentSizeExceedException(final DocumentSizeExceedException e) {
		return GenericUtil.errorResponse(e, e.getResponse());
	}

	/**
	 * @param e       pass the exception
	 * @param request pass the request
	 * @return response for DocumentFailedToUploadException
	 */
	@ExceptionHandler(DocumentFailedToUploadException.class)
	public ResponseEntity<MainResponseDTO<?>> documentFailedToUploadException(final DocumentFailedToUploadException e) {
		return GenericUtil.errorResponse(e, e.getResponse());
	}

	/**
	 * @param e       pass the exception
	 * @param request pass the request
	 * @return response for DocumentVirusScanException
	 */
	@ExceptionHandler(DocumentVirusScanException.class)
	public ResponseEntity<MainResponseDTO<?>> documentVirusScanException(final DocumentVirusScanException e) {
		return GenericUtil.errorResponse(e, e.getResponse());
	}

	/**
	 * @param e       pass the exception
	 * @param request pass the request
	 * @return response for DocumentFailedToCopyException
	 */
	@ExceptionHandler(DocumentFailedToCopyException.class)
	public ResponseEntity<MainResponseDTO<?>> documentFailedToCopyException(final DocumentFailedToCopyException e) {
		return GenericUtil.errorResponse(e, e.getResponse());
	}

	/**
	 * @param e       pass the exception
	 * @param request pass the request
	 * @return response for InvalidDocumnetIdExcepion
	 */
	@ExceptionHandler(InvalidDocumentIdExcepion.class)
	public ResponseEntity<MainResponseDTO<?>> invalidDocumnetIdExcepion(final InvalidDocumentIdExcepion e) {
		return GenericUtil.errorResponse(e, e.getResponse());
	}

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
	 * @param e       pass the exception
	 * @param request pass the request
	 * @return response for CephServerException
	 */
	@ExceptionHandler(FSServerException.class)
	public ResponseEntity<MainResponseDTO<?>> cephServerException(final FSServerException e) {
		return GenericUtil.errorResponse(e, e.getResponse());
	}

	/**
	 * @param e       pass the exception
	 * @param request pass the request
	 * @return response for PrimaryKeyValidationException
	 */
	@ExceptionHandler(PrimaryKeyValidationException.class)
	public ResponseEntity<MainResponseDTO<?>> primaryKeyValidationException(final PrimaryKeyValidationException e) {
		return GenericUtil.errorResponse(e, e.getResponse());
	}
	
	
	@ExceptionHandler(DeprecatedException.class)
	public ResponseEntity<MainResponseDTO<?>> sizeExceedException(DeprecatedException e) {
		ExceptionJSONInfoDTO errorDetails = new ExceptionJSONInfoDTO(e.getErrorCode(),e.getErrorMessage());
		MainResponseDTO<?> errorRes = new MainResponseDTO<>();
		List<ExceptionJSONInfoDTO> errorList = new ArrayList<>();
		errorList.add(errorDetails);
		errorRes.setErrors(errorList);
		errorRes.setResponsetime(getCurrentResponseTime());
		errorRes.setId("");
		errorRes.setVersion(ver);
		return new ResponseEntity<>(errorRes, HttpStatus.OK);
	}

	public String getCurrentResponseTime() {
		return DateUtils.formatDate(new Date(System.currentTimeMillis()), utcDateTimePattern);
	}

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

}
