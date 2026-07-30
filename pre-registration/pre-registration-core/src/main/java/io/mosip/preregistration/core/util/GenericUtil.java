package io.mosip.preregistration.core.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.preregistration.core.common.dto.ExceptionJSONInfoDTO;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;

public class GenericUtil {

	private GenericUtil() {
	}

	public static String maskIdentifier(String value) {
		if (value == null || value.isBlank()) {
			return "<empty>";
		}
		String trimmed = value.trim();
		int atIndex = trimmed.indexOf('@');
		if (atIndex > 0 && atIndex < trimmed.length() - 1) {
			String local = trimmed.substring(0, atIndex);
			String domain = trimmed.substring(atIndex);
			return local.charAt(0) + "***" + domain;
		}
		if (trimmed.matches("\\+?\\d{10,12}")) {
			boolean hasPlus = trimmed.startsWith("+");
			String digits = hasPlus ? trimmed.substring(1) : trimmed;
			if (digits.length() <= 4) {
				return (hasPlus ? "+" : "") + "****";
			}
			String masked = "*".repeat(digits.length() - 4) + digits.substring(digits.length() - 4);
			return (hasPlus ? "+" : "") + masked;
		}
		if (isUuid(trimmed)) {
			return "***" + trimmed.substring(trimmed.length() - 6);
		}
		// Scale the visible suffix to the length of the value: a fixed 4 characters would expose most
		// of a short identifier (e.g. "admin" -> "***dmin"). Values shorter than 3 are fully masked.
		int visible = Math.min(4, trimmed.length() / 3);
		if (visible <= 0) {
			return "***";
		}
		return "***" + trimmed.substring(trimmed.length() - visible);
	}

	public static boolean isUuid(String value) {
		if (value == null || value.isBlank()) {
			return false;
		}
		try {
			UUID.fromString(value.trim());
			return true;
		} catch (IllegalArgumentException ex) {
			return false;
		}
	}

	private static String dateTimeFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	public static String getCurrentResponseTime() {
		return DateUtils.formatDate(new Date(System.currentTimeMillis()), dateTimeFormat);
	}

	/**
	 * This method will return the MainResponseDTO with id and version
	 * 
	 * @param mainRequestDto
	 * @return MainResponseDTO<?>
	 */
	public MainResponseDTO<?> getMainResponseDto(MainRequestDTO<?> mainRequestDto) {
		MainResponseDTO<?> response = new MainResponseDTO<>();
		response.setId(mainRequestDto.getId());
		response.setVersion(mainRequestDto.getVersion());

		return response;
	}

	public static <T extends BaseUncheckedException> ResponseEntity<MainResponseDTO<?>> errorResponse(final T e,
			MainResponseDTO<?> response) {
		ExceptionJSONInfoDTO errorDetails = new ExceptionJSONInfoDTO(e.getErrorCode(), e.getErrorText());
		MainResponseDTO<?> errorRes = response;
		List<ExceptionJSONInfoDTO> errorList = new ArrayList<>();
		errorList.add(errorDetails);
		errorRes.setErrors(errorList);
		errorRes.setResponsetime(getCurrentResponseTime());
		return new ResponseEntity<>(errorRes, HttpStatus.OK);
	}

}
