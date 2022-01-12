package io.mosip.preregistration.application.service.util;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.application.errorcodes.AppointmentErrorCodes;
import io.mosip.preregistration.application.exception.AppointmentExecption;
import io.mosip.preregistration.booking.dto.AvailabilityDto;
import io.mosip.preregistration.booking.dto.BookingRequestDTO;
import io.mosip.preregistration.booking.dto.BookingStatus;
import io.mosip.preregistration.booking.dto.BookingStatusDTO;
import io.mosip.preregistration.booking.dto.MultiBookingRequest;
import io.mosip.preregistration.core.common.dto.BookingRegistrationDTO;
import io.mosip.preregistration.core.common.dto.CancelBookingResponseDTO;
import io.mosip.preregistration.core.common.dto.DeleteBookingDTO;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.exception.RestCallException;

@Component
public class AppointmentUtil {

	private static final String PRE_REGISTRATION_ID = "preRegistrationId";

	private static final String ERROR_MSG = "Error trace {}";
	
	@Autowired
	private RestTemplate restTemplate;

	@Value("${mosip.preregistration.appointment.getavailablity.url}")
	private String availablityUrl;

	@Value("${mosip.preregistration.appointment.book.url}")
	private String appointmentUrl;

	@Value("${mosip.preregistration.appointment.multi.book.url}")
	private String multiBookingUrl;

	private Logger log = LoggerConfiguration.logConfig(AppointmentUtil.class);

	public AvailabilityDto getSlotAvailablityByRegCenterId(String regCenterId) {

		Map<String, String> params = new LinkedHashMap<>();
		params.put("registrationCenterId", regCenterId.trim());

		String constructedAvailablityUrl = UriComponentsBuilder.fromHttpUrl(availablityUrl).buildAndExpand(params)
				.toUriString();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

		HttpEntity<?> entity = new HttpEntity<>(headers);

		ResponseEntity<MainResponseDTO<AvailabilityDto>> responseEntity = null;

		try {
			log.info("Fetching slots availablity for url : {}", constructedAvailablityUrl);
			responseEntity = restTemplate.exchange(constructedAvailablityUrl, HttpMethod.GET, entity,
					new ParameterizedTypeReference<MainResponseDTO<AvailabilityDto>>() {
					});
			if (responseEntity != null && responseEntity.getBody() != null && responseEntity.getBody().getErrors() != null) {
				throw new AppointmentExecption(responseEntity.getBody().getErrors().get(0).getErrorCode(),
						responseEntity.getBody().getErrors().get(0).getMessage());
			}

		} catch (RestClientException ex) {
			log.error("Error while fetching availablity for regCenterID:{}", regCenterId);
			log.error(ERROR_MSG, ExceptionUtils.getStackTrace(ex));
			throw new AppointmentExecption(AppointmentErrorCodes.FAILED_TO_FETCH_AVAILABLITY.getCode(),
					AppointmentErrorCodes.FAILED_TO_FETCH_AVAILABLITY.getMessage());
		}

		return responseEntity.getBody().getResponse();
	}

	public BookingStatusDTO makeAppointment(MainRequestDTO<BookingRequestDTO> bookingDTO, String preRegistrationId) {

		Map<String, String> params = new LinkedHashMap<>();
		params.put(PRE_REGISTRATION_ID, preRegistrationId.trim());

		String constructedAppointmentUrl = UriComponentsBuilder.fromHttpUrl(appointmentUrl).buildAndExpand(params)
				.toUriString();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

		HttpEntity<?> entity = new HttpEntity<>(bookingDTO, headers);

		ResponseEntity<MainResponseDTO<BookingStatusDTO>> responseEntity = null;

		log.info("making an appointment rest call for url : {}", constructedAppointmentUrl);

		try {
			responseEntity = restTemplate.exchange(constructedAppointmentUrl, HttpMethod.POST, entity,
					new ParameterizedTypeReference<MainResponseDTO<BookingStatusDTO>>() {
					});

			if (responseEntity != null && responseEntity.getBody() != null && responseEntity.getBody().getErrors() != null) {

				throw new AppointmentExecption(responseEntity.getBody().getErrors().get(0).getErrorCode(),
						responseEntity.getBody().getErrors().get(0).getMessage());
			}

		} catch (RestClientException ex) {
			log.error("Error while booking an appointment for preRegistrationId:{}", preRegistrationId);
			log.error(ERROR_MSG, ExceptionUtils.getStackTrace(ex));
			throw new AppointmentExecption(AppointmentErrorCodes.BOOKING_FAILED.getCode(),
					AppointmentErrorCodes.BOOKING_FAILED.getMessage());
		}

		return responseEntity.getBody().getResponse();
	}

	public BookingRegistrationDTO fetchAppointmentDetails(String preRegistrationId) {

		Map<String, String> params = new LinkedHashMap<>();
		params.put(PRE_REGISTRATION_ID, preRegistrationId.trim());

		String constructedAppointmentUrl = UriComponentsBuilder.fromHttpUrl(appointmentUrl).buildAndExpand(params)
				.toUriString();

		HttpHeaders headers = new HttpHeaders();
		HttpEntity<HttpHeaders> entity = new HttpEntity<>(headers);

		ResponseEntity<MainResponseDTO<BookingRegistrationDTO>> responseEntity = null;

		log.info("Fetching appointment details rest call for url : {}", constructedAppointmentUrl);

		try {

			responseEntity = restTemplate.exchange(constructedAppointmentUrl, HttpMethod.GET, entity,
					new ParameterizedTypeReference<MainResponseDTO<BookingRegistrationDTO>>() {
					});

			if (responseEntity != null && responseEntity.getBody() != null && responseEntity.getBody().getErrors() != null) {
				throw new AppointmentExecption(responseEntity.getBody().getErrors().get(0).getErrorCode(),
						responseEntity.getBody().getErrors().get(0).getMessage());
			}

		} catch (RestClientException ex) {
			log.error("Error while fetching appointment details for preRegistrationId:{}", preRegistrationId);
			log.error(ERROR_MSG, ExceptionUtils.getStackTrace(ex));
			throw new AppointmentExecption(AppointmentErrorCodes.FAILED_TO_FETCH_APPOINTMENT_DETAILS.getCode(),
					AppointmentErrorCodes.FAILED_TO_FETCH_APPOINTMENT_DETAILS.getMessage());
		}

		return responseEntity.getBody().getResponse();
	}

	public DeleteBookingDTO deleteBooking(String preRegId) {

		String constructedAppointmentUrl = UriComponentsBuilder.fromHttpUrl(multiBookingUrl)
				.queryParam(PRE_REGISTRATION_ID, preRegId.trim()).toUriString();

		ResponseEntity<MainResponseDTO<DeleteBookingDTO>> responseEntity = null;

		try {

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<?> entity = new HttpEntity<>(headers);

			log.info("Delete an appointment rest call for url : {}", constructedAppointmentUrl);

			responseEntity = restTemplate.exchange(constructedAppointmentUrl, HttpMethod.DELETE, entity,
					new ParameterizedTypeReference<MainResponseDTO<DeleteBookingDTO>>() {
					});

			if (responseEntity != null && responseEntity.getBody() != null && responseEntity.getBody().getErrors() != null) {
				throw new RestCallException(responseEntity.getBody().getErrors().get(0).getErrorCode(),
						responseEntity.getBody().getErrors().get(0).getMessage());
			}

		} catch (RestClientException ex) {
			log.error("Booking RestCall Exception " + ExceptionUtils.getStackTrace(ex));
			throw new AppointmentExecption(AppointmentErrorCodes.FAILED_TO_DELETE_APPOINTMENT.getCode(),
					AppointmentErrorCodes.FAILED_TO_DELETE_APPOINTMENT.getMessage());
		}

		return responseEntity.getBody().getResponse();

	}

	public CancelBookingResponseDTO cancelAppointment(String preRegistrationId) {

		Map<String, String> params = new LinkedHashMap<>();
		params.put(PRE_REGISTRATION_ID, preRegistrationId.trim());

		String constructedAppointmentUrl = UriComponentsBuilder.fromHttpUrl(appointmentUrl).buildAndExpand(params)
				.toUriString();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

		HttpEntity<?> entity = new HttpEntity<>(headers);

		ResponseEntity<MainResponseDTO<CancelBookingResponseDTO>> responseEntity = null;

		log.info("Cancel an appointment rest call for url : {}", constructedAppointmentUrl);

		try {
			responseEntity = restTemplate.exchange(constructedAppointmentUrl, HttpMethod.PUT, entity,
					new ParameterizedTypeReference<MainResponseDTO<CancelBookingResponseDTO>>() {
					});

			if (responseEntity != null && responseEntity.getBody() != null && responseEntity.getBody().getErrors() != null) {

				throw new AppointmentExecption(responseEntity.getBody().getErrors().get(0).getErrorCode(),
						responseEntity.getBody().getErrors().get(0).getMessage());
			}

		} catch (RestClientException ex) {
			log.error("Error while Cancelling an appointment for preRegistrationId:{}", preRegistrationId);
			log.error(ERROR_MSG, ExceptionUtils.getStackTrace(ex));
			throw new AppointmentExecption(AppointmentErrorCodes.CANCEL_APPOINTMENT_FAILED.getCode(),
					AppointmentErrorCodes.CANCEL_APPOINTMENT_FAILED.getMessage());
		}
		return responseEntity.getBody().getResponse();
	}

	public BookingStatus multiAppointmentBooking(MainRequestDTO<MultiBookingRequest> bookingRequest) {

		String constructedAppointmentUrl = UriComponentsBuilder.fromHttpUrl(multiBookingUrl).toUriString();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

		HttpEntity<?> entity = new HttpEntity<>(bookingRequest, headers);

		ResponseEntity<MainResponseDTO<BookingStatus>> responseEntity = null;

		log.info("Multi Booking appointment rest call for url : {}", constructedAppointmentUrl);

		try {
			responseEntity = restTemplate.exchange(constructedAppointmentUrl, HttpMethod.POST, entity,
					new ParameterizedTypeReference<MainResponseDTO<BookingStatus>>() {
					});

			if (responseEntity != null && responseEntity.getBody() != null && responseEntity.getBody().getErrors() != null) {

				throw new AppointmentExecption(responseEntity.getBody().getErrors().get(0).getErrorCode(),
						responseEntity.getBody().getErrors().get(0).getMessage());
			}

		} catch (RestClientException ex) {
			log.error(ERROR_MSG, ExceptionUtils.getStackTrace(ex));
			throw new AppointmentExecption(AppointmentErrorCodes.MULTI_BOOKING_FAILED.getCode(),
					AppointmentErrorCodes.MULTI_BOOKING_FAILED.getMessage());
		}
		return responseEntity.getBody().getResponse();

	}

}
