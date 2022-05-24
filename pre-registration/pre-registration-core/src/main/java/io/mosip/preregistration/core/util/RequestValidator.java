package io.mosip.preregistration.core.util;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.core.code.BookingTypeCodes;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.errorcodes.ErrorCodes;
import io.mosip.preregistration.core.errorcodes.ErrorMessages;

@Component
public class RequestValidator extends BaseValidator implements Validator {

	/** The Constant REQUEST. */
	private static final String ID = "id";

	/** The Constant REQUEST. */
	private static final String BOOKING_TYPE = "bookingType";

	/** The Constant REQUEST. */
	private static final String PURPOSE = "purpose";

	@Value("${mosip.preregistration.miscellaneouspurpose.length}")
	private int miscPurposeLength;

	/**
	 * Logger configuration for BaseValidator
	 */
	private static Logger mosipLogger = LoggerConfiguration.logConfig(RequestValidator.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return clazz.isAssignableFrom(MainRequestDTO.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 * org.springframework.validation.Errors)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void validate(@NonNull Object target, Errors errors) {
		MainRequestDTO<Object> request = (MainRequestDTO<Object>) target;
		validateReqTime(request.getRequesttime(), errors);
		validateVersion(request.getVersion(), errors);
		validateRequest(request.getRequest(), errors);
	}

	public void validateId(String operation, String requestId, Errors errors) {
		if (Objects.nonNull(requestId)) {
			if (!requestId.equals(id.get(operation))) {
				mosipLogger.error("", "", "validateId", "\n" + "Id is not correct");
				errors.rejectValue(ID, ErrorCodes.PRG_CORE_REQ_001.getCode(),
						String.format(ErrorMessages.INVALID_REQUEST_ID.getMessage(), ID));
			}
		} else {
			mosipLogger.error("", "", "validateId", "\n" + "Id is null");
			errors.rejectValue(ID, ErrorCodes.PRG_CORE_REQ_001.getCode(),
					String.format(ErrorMessages.INVALID_REQUEST_ID.getMessage(), ID));
		}
	}

	public void validateBookingType(String bookingType, Errors errors) {
		if (Objects.nonNull(bookingType)) {
			List<BookingTypeCodes> bookingTypes = Arrays.asList(BookingTypeCodes.values());
			boolean found = false;
			for (BookingTypeCodes typeCode : bookingTypes) {
				if (typeCode.toString().equals(bookingType)) {
					found = true;
				}
			}
			if (!found) {
				mosipLogger.error("", "", "validateBookingType", "\n" + "BookingType is not correct");
				errors.rejectValue(ID, ErrorCodes.PRG_CORE_REQ_023.getCode(),
						String.format(ErrorMessages.INVALID_BOOKING_TYPE.getMessage(), BOOKING_TYPE));
			}
		} else {
			mosipLogger.error("", "", "validateId", "\n" + "Id is null");
			errors.rejectValue(ID, ErrorCodes.PRG_CORE_REQ_001.getCode(),
					String.format(ErrorMessages.INVALID_REQUEST_ID.getMessage(), BOOKING_TYPE));
		}
	}

	public void validatePurpose(String purpose, Errors errors) {
		if (purpose == null) {
			mosipLogger.error("", "", "validatePurpose", "\n" + "purpose is null");
			errors.rejectValue(ID, ErrorCodes.PRG_CORE_REQ_024.getCode(),
					String.format(ErrorMessages.INVALID_PURPOSE.getMessage(), PURPOSE));
		} else {
			if (purpose.length() > miscPurposeLength) {
				mosipLogger.error("", "", "validatePurpose", "\n" + "purpose length is greater than 200 words");
				errors.rejectValue(ID, ErrorCodes.PRG_CORE_REQ_025.getCode(),
						String.format(ErrorMessages.INVALID_PURPOSE_LENGTH.getMessage(), PURPOSE));
			}
		}
	}

}
