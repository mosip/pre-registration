package io.mosip.preregistration.booking.service.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import jakarta.annotation.PostConstruct;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils2;
import io.mosip.preregistration.booking.codes.RequestCodes;
import io.mosip.preregistration.booking.dto.BookingRequestDTO;
import io.mosip.preregistration.booking.dto.CancelBookingDTO;
import io.mosip.preregistration.booking.dto.DateTimeDto;
import io.mosip.preregistration.booking.dto.RegistrationCenterDto;
import io.mosip.preregistration.booking.dto.RegistrationCenterResponseDto;
import io.mosip.preregistration.booking.dto.SlotDto;
import io.mosip.preregistration.booking.entity.AvailibityEntity;
import io.mosip.preregistration.booking.errorcodes.ErrorCodes;
import io.mosip.preregistration.booking.errorcodes.ErrorMessages;
import io.mosip.preregistration.booking.exception.AppointmentCannotBeCanceledException;
import io.mosip.preregistration.booking.exception.AppointmentReBookingFailedException;
import io.mosip.preregistration.booking.exception.AvailablityNotFoundException;
import io.mosip.preregistration.booking.exception.BookingDataNotFoundException;
import io.mosip.preregistration.booking.exception.BookingDateNotSeletectedException;
import io.mosip.preregistration.booking.exception.BookingPreIdNotFoundException;
import io.mosip.preregistration.booking.exception.BookingRegistrationCenterIdNotFoundException;
import io.mosip.preregistration.booking.exception.BookingTimeSlotNotSeletectedException;
import io.mosip.preregistration.booking.exception.AppointmentBookingFailedException;
import io.mosip.preregistration.booking.exception.DemographicGetStatusException;
import io.mosip.preregistration.booking.exception.InvalidDateTimeFormatException;
import io.mosip.preregistration.booking.exception.RecordNotFoundException;
import io.mosip.preregistration.booking.exception.TimeSpanException;
import io.mosip.preregistration.core.code.StatusCodes;
import io.mosip.preregistration.core.common.dto.BookingRegistrationDTO;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.dto.NotificationDTO;
import io.mosip.preregistration.core.common.dto.RequestWrapper;
import io.mosip.preregistration.core.common.dto.ResponseWrapper;
import io.mosip.preregistration.application.errorcodes.ApplicationErrorCodes;
import io.mosip.preregistration.application.errorcodes.ApplicationErrorMessages;
import io.mosip.preregistration.core.common.entity.ApplicationEntity;
import io.mosip.preregistration.core.common.entity.RegistrationBookingEntity;
import io.mosip.preregistration.core.common.repository.ApplicationRepostiory;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.exception.MasterDataNotAvailableException;
import io.mosip.preregistration.core.exception.PreIdInvalidForUserIdException;
import io.mosip.preregistration.core.exception.NotificationException;
import io.mosip.preregistration.core.exception.RestCallException;
import io.mosip.preregistration.core.util.UUIDGeneratorUtil;
import io.mosip.preregistration.core.util.ValidationUtil;
import io.mosip.preregistration.core.common.service.UserDetailsService;
import io.mosip.preregistration.core.exception.UserLookupException;
import io.mosip.preregistration.core.util.GenericUtil;

/**
 * This class provides the utility methods for Booking application.
 * 
 * @author Kishan Rathore
 * @author Jagadishwari
 * @author Ravi C. Balaji
 * @since 1.0.0
 *
 */
@Component
public class BookingServiceUtil {

	@Qualifier("selfTokenRestTemplate")
	@Autowired
	private RestTemplate selfTokenRestTemplate;

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private ApplicationRepostiory applicationRepostiory;

	@Autowired
	@Lazy
	private io.mosip.preregistration.pipeline.InProcessApplicationClient applicationClient;

	@Value("${mosip.prereg.pii.backward.compatibility:true}")
	private boolean piiBackwardCompatibility;

	/**
	 * Reference for ${regCenter.url} from property file
	 */
	@Value("${regCenter.url}")
	String regCenterUrl;

	/**
	 * Reference for ${holiday.url} from property file
	 */
	@Value("${holiday.url}")
	String holidayListUrl;

	/**
	 * Reference for ${demographic.resource.url} from property file
	 */
	@Value("${demographic.resource.url}")
	private String preRegResourceUrl;

	@Value("${preregistration.timespan.cancel}")
	private long timeSpanCheckForCancel;

	@Value("${preregistration.timespan.rebook}")
	private long timeSpanCheckForRebook;

	@Value("${mosip.utc-datetime-pattern}")
	private String utcDateTimePattern;

	@Value("${notification.url}")
	private String notificationResourseurl;

	@Value("${mosip.notification.timezone}")
	private String specificZoneId;

	/**
	 * ObjectMapper global object creation
	 */
	private ObjectMapper mapper;

	@PostConstruct
    public void init() {
		mapper = JsonMapper.builder().addModule(new AfterburnerModule()).build();
		mapper.registerModule(new JavaTimeModule());
	}
	
	private Logger log = LoggerConfiguration.logConfig(BookingServiceUtil.class);

	public AuthUserDetails authUserDetails() {
		return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	/**
	 * This method will call kernel service for registration center date.
	 * 
	 * @return List of RegistrationCenterDto
	 */
	public List<RegistrationCenterDto> getRegCenterMasterData(String regCenterId) {
		log.info("sessionId", "idType", "id", "In callRegCenterDateRestService method of Booking Service Util");
		List<RegistrationCenterDto> regCenter = null;
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<RequestWrapper<RegistrationCenterResponseDto>> entity = new HttpEntity<>(headers);
			String uriBuilder = UriComponentsBuilder.fromUriString(regCenterUrl)
					.pathSegment(regCenterId, "all")
					.build()
					.encode()
					.toUriString();
			log.info("sessionId", "idType", "id",
					"In callRegCenterDateRestService method of Booking Service URL- " + uriBuilder);
			ResponseEntity<ResponseWrapper<RegistrationCenterResponseDto>> responseEntity = selfTokenRestTemplate.exchange(
					uriBuilder, HttpMethod.GET, entity,
					new ParameterizedTypeReference<ResponseWrapper<RegistrationCenterResponseDto>>() {
					});
			ResponseWrapper<RegistrationCenterResponseDto> body = responseEntity.getBody();
			if (body != null) {
				throwIfMasterDataErrors(body.getErrors());
				if (body.getResponse() != null) {
					regCenter = body.getResponse().getRegistrationCenters();
				}
			}
			if (regCenter == null || regCenter.isEmpty()) {
				throw new MasterDataNotAvailableException(ErrorCodes.PRG_BOOK_RCI_020.getCode(),
						ErrorMessages.MASTER_DATA_NOT_FOUND.getMessage());
			}
		} catch (MasterDataNotAvailableException ex) {
			throw ex;
		} catch (HttpStatusCodeException ex) {
			log.debug("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id",
					"In callRegCenterDateRestService method of Booking Service Util for HttpStatusCodeException- "
							+ ex.getMessage());
			List<ServiceError> errors = ExceptionUtils.getServiceErrorList(ex.getResponseBodyAsString());
			throwIfMasterDataErrors(errors);
			throw new RestCallException(ErrorCodes.PRG_BOOK_RCI_020.getCode(),
					ErrorMessages.MASTER_DATA_NOT_FOUND.getMessage());
		}
		return regCenter;
	}

	/**
	 * Re-throws the first masterdata {@link ServiceError} (for example
	 * {@code KER-MSD-215} when the centre id is unknown). Concatenating the centre
	 * id into a URI string used to treat {@code #} as a fragment, so invalid ids
	 * such as {@code @#@$W#} never reached masterdata and were wrapped as
	 * {@code PRG_BOOK_RCI_020}.
	 *
	 * @param errors masterdata {@code errors} list; ignored when null or empty
	 */
	private void throwIfMasterDataErrors(List<ServiceError> errors) {
		if (errors == null || errors.isEmpty() || errors.get(0) == null) {
			return;
		}
		ServiceError error = errors.get(0);
		if (error.getErrorCode() == null || error.getErrorCode().isBlank()) {
			return;
		}
		throw new MasterDataNotAvailableException(error.getErrorCode(), error.getMessage());
	}

	/**
	 * This method will call demographic service for status.
	 * 
	 * @param preId
	 * @return status code
	 */
	public String getApplicationBookingStatus(String preId) {
		log.info("sessionId", "idType", "id", "In callGetStatusRestService method of Booking Service Util");

		MainResponseDTO<String> getApplicationStatus = getApplicationStatus(preId);

		if (getApplicationStatus.getErrors() != null) {
			throw new DemographicGetStatusException(getApplicationStatus.getErrors().get(0).getErrorCode(),
					getApplicationStatus.getErrors().get(0).getMessage());
		}
		return getApplicationStatus.getResponse();

	}

	/**
	 * This method will call demographic service for cancel status.
	 * 
	 * @param preId
	 * @return status code
	 */
	public boolean getDemographicStatusForCancel(String preId) {
		log.info("sessionId", "idType", "id", "In callGetStatusForCancelRestService method of Booking Service Util");
		
		MainResponseDTO<String> getApplicationStatus = getApplicationStatus(preId);
		if (getApplicationStatus.getErrors() != null) {
			throw new DemographicGetStatusException(getApplicationStatus.getErrors().get(0).getErrorCode(),
					getApplicationStatus.getErrors().get(0).getMessage());
		}
		String statusCode = getApplicationStatus.getResponse();
		
		if (!statusCode.equals(StatusCodes.BOOKED.getCode())) {
			if (statusCode.equals(StatusCodes.PENDING_APPOINTMENT.getCode())
					|| statusCode.equals(StatusCodes.APPLICATION_INCOMPLETE.getCode())) {
				throw new BookingDataNotFoundException(ErrorCodes.PRG_BOOK_RCI_013.getCode(),
						ErrorMessages.BOOKING_DATA_NOT_FOUND.getMessage());
			}

			else {
				throw new AppointmentCannotBeCanceledException(ErrorCodes.PRG_BOOK_RCI_018.getCode(),
						ErrorMessages.APPOINTMENT_CANNOT_BE_CANCELED.getMessage());
			}

		}
		return true;
	}

	/**
	 * This method will call demographic service to get application status for
	 * delete flow and it will also check if the PRID belongs to the logged in user
	 * or not.
	 * 
	 * @param preId
	 * @return status code
	 */
	public boolean checkApplicationStatus(String preId) {
		log.info("sessionId", "idType", "id", "In callgetDemographicStatusForDelete method of Booking Service Util");
		// This call will check if the PRID belongs to the logged in user or not
		MainResponseDTO<String> getApplicationStatus = getApplicationStatus(preId);
		if (getApplicationStatus.getErrors() != null) {
			throw new DemographicGetStatusException(getApplicationStatus.getErrors().get(0).getErrorCode(),
					getApplicationStatus.getErrors().get(0).getMessage());
		}
		return true;
	}

	/**
	 * Residents with {@code ROLE_INDIVIDUAL} may only book, read, cancel, or delete
	 * appointments for applications they created. Staff roles skip this check.
	 *
	 * @param preId pre-registration / application id
	 */
	public void assertAppointmentOwner(String preId) {
		AuthUserDetails user = authUserDetails();
		if (user == null || !hasIndividualRole(user)) {
			return;
		}
		ApplicationEntity applicationEntity = applicationRepostiory.findByApplicationId(preId);
		if (applicationEntity == null) {
			throw new RecordNotFoundException(ApplicationErrorCodes.PRG_APP_013.getCode(),
					ApplicationErrorMessages.NO_RECORD_FOUND.getMessage());
		}
		String expectedCrBy = applicationEntity.getEffectiveCrBy() == null ? ""
				: applicationEntity.getEffectiveCrBy().trim();
		if (!userDetailsService.matchesUser(user.getUserId(), expectedCrBy, piiBackwardCompatibility)) {
			log.warn("sessionId", "idType", "id",
					"Appointment ownership validation failed for preId " + preId + " and masked user "
							+ GenericUtil.maskIdentifier(user.getUserId()));
			throw new PreIdInvalidForUserIdException(ApplicationErrorCodes.PRG_APP_015.getCode(),
					ApplicationErrorMessages.INVALID_APPLICATION_ID_FOR_USER.getMessage());
		}
	}

	private boolean hasIndividualRole(AuthUserDetails user) {
		Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
		if (authorities == null) {
			return false;
		}
		for (GrantedAuthority authority : authorities) {
			if ("ROLE_INDIVIDUAL".equals(authority.getAuthority())) {
				return true;
			}
		}
		return false;
	}
	
	public boolean timeSpanCheckForCancle(LocalDateTime bookedDateTime) {
		boolean isTimeSpanCheckForCancel = true;
		ZonedDateTime currentTime = ZonedDateTime.now();
		LocalDateTime requestTimeCountrySpecific = currentTime.toInstant().atZone(ZoneId.of(specificZoneId))
				.toLocalDateTime();
		log.info("sessionId", "idType", "id",
				"In timeSpanCheckForCancle method of Booking Service for request Date Time- "
						+ requestTimeCountrySpecific);
		long hours = ChronoUnit.HOURS.between(requestTimeCountrySpecific, bookedDateTime);
		if (timeSpanCheckForCancel > 0) {
			if (hours >= timeSpanCheckForCancel)
				isTimeSpanCheckForCancel = true;
			else
				throw new TimeSpanException(ErrorCodes.PRG_BOOK_RCI_026.getCode(),
						ErrorMessages.CANCEL_BOOKING_CANNOT_BE_DONE.getMessage() + " " + timeSpanCheckForCancel
								+ "hours");
		}
		return isTimeSpanCheckForCancel;
	}

	public boolean timeSpanCheckForRebook(LocalDateTime bookedDateTime, Date requestTime) {

		boolean isTimeSpanCheckForRebook = true;
		LocalDateTime requestTimeCountrySpecific = requestTime.toInstant().atZone(ZoneId.of(specificZoneId))
				.toLocalDateTime();

		log.info("sessionId", "idType", "id",
				"In timeSpanCheckForRebook method of Booking Service for request Date Time- "
						+ requestTimeCountrySpecific);
		long hours = ChronoUnit.HOURS.between(requestTimeCountrySpecific, bookedDateTime);

		if (timeSpanCheckForRebook > 0) {
			if (hours >= timeSpanCheckForRebook)
				isTimeSpanCheckForRebook = true;

			else
				throw new TimeSpanException(ErrorCodes.PRG_BOOK_RCI_026.getCode(),
						ErrorMessages.BOOKING_CANNOT_BE_DONE.getMessage() + " " + timeSpanCheckForRebook + " hours");
		}
		return isTimeSpanCheckForRebook;

	}

	/**
	 * This method will check mandatory parameter check.
	 * 
	 * @param bookingDto
	 * @return true or false
	 * @throws java.text.ParseException
	 */
	public boolean mandatoryParameterCheck(String preRegistrationId, BookingRequestDTO bookingRequestDTO) {
		log.info("sessionId", "idType", "id", "In mandatoryParameterCheck method of Booking Service Util");
		boolean flag = true;
		if (isNull(preRegistrationId)) {
			throw new BookingPreIdNotFoundException(ErrorCodes.PRG_BOOK_RCI_006.getCode(),
					ErrorMessages.PREREGISTRATION_ID_NOT_ENTERED.getMessage());
		} else if (bookingRequestDTO != null) {
			if (isNull(bookingRequestDTO.getRegistrationCenterId())) {
				throw new BookingRegistrationCenterIdNotFoundException(ErrorCodes.PRG_BOOK_RCI_007.getCode(),
						ErrorMessages.REGISTRATION_CENTER_ID_NOT_ENTERED.getMessage());
			} else if (isNull(bookingRequestDTO.getRegDate())) {
				throw new BookingDateNotSeletectedException(ErrorCodes.PRG_BOOK_RCI_008.getCode(),
						ErrorMessages.BOOKING_DATE_TIME_NOT_SELECTED.getMessage());
			} else if (isNull(bookingRequestDTO.getSlotFromTime()) || isNull(bookingRequestDTO.getSlotToTime())) {
				throw new BookingTimeSlotNotSeletectedException(ErrorCodes.PRG_BOOK_RCI_003.getCode(),
						ErrorMessages.USER_HAS_NOT_SELECTED_TIME_SLOT.getMessage());
			}
		} else {
			flag = false;
		}
		return flag;

	}
	
	/**
	 * This method will check if Slot Time is Valid or not.
	 * 
	 * @param preRegistrationId
	 * @param bookingDto
	 * @return true or false
	 */
	public boolean slotTimeValidCheck(String preRegistrationId, BookingRequestDTO bookingRequestDTO) {
		log.info("sessionId", "idType", "id", "In slotTimeValidCheck method of Booking Service Util");
		boolean flag = true;
		try {
			LocalTime.parse(bookingRequestDTO.getSlotFromTime());
			LocalTime.parse(bookingRequestDTO.getSlotToTime());
		} catch (DateTimeParseException e) {
			throw new BookingTimeSlotNotSeletectedException(ErrorCodes.PRG_BOOK_RCI_003.getCode(),
					ErrorMessages.USER_HAS_NOT_SELECTED_TIME_SLOT.getMessage());
		}
		return flag;
	}

	/**
	 * This method is used as Null checker for different input keys.
	 *
	 * @param key pass the key
	 * @return true if key not null and return false if key is null.
	 */

	public boolean isNull(Object key) {
		log.info("sessionId", "idType", "id", "In isNull method of Booking Service Util");
		if (key instanceof String) {
			if (key.equals("") || ((String) key).trim().length() == 0)
				return true;
		} else {
			if (key == null)
				return true;
		}
		return false;
	}

	/**
	 * 
	 * @param date
	 * @param dateTimeList
	 * @param dateTime
	 * @param entity
	 * @return
	 */
	public int slotSetter(LocalDate date, List<DateTimeDto> dateTimeList, DateTimeDto dateTime,
			List<AvailibityEntity> entity) {
		int noOfHoliday = 0;
		List<SlotDto> slotList = new ArrayList<>();
		for (AvailibityEntity en : entity) {
			if (en.getAvailableKiosks() > 0) {
				SlotDto slots = new SlotDto();
				slots.setAvailability(en.getAvailableKiosks());
				slots.setFromTime(en.getFromTime());
				slots.setToTime(en.getToTime());
				slotList.add(slots);
			}
		}
		if (entity.size() == 1) {
			dateTime.setHoliday(true);
			noOfHoliday++;
		} else {
			dateTime.setHoliday(false);
		}
		if (!slotList.isEmpty()) {
			dateTime.setTimeSlots(slotList);
			dateTime.setDate(date.toString());
			dateTimeList.add(dateTime);
		}
		return noOfHoliday;

	}

	/**
	 * This method will do mandatory parameter check for cancel.
	 * 
	 * @param cancelBookingDTO
	 * @return true or false
	 */
	public boolean mandatoryParameterCheckforCancel(String preRegistrationId) {
		log.info("sessionId", "idType", "id", "In mandatoryParameterCheckforCancel method of Booking Service Util");
		boolean flag = true;

		if (isNull(preRegistrationId)) {
			throw new BookingPreIdNotFoundException(ErrorCodes.PRG_BOOK_RCI_006.getCode(),
					ErrorMessages.PREREGISTRATION_ID_NOT_ENTERED.getMessage());
		}

		return flag;

	}

	/**
	 * This method will check for duplicates.
	 * 
	 * @param oldBookingRegistrationDTO
	 * @param newBookingRegistrationDTO
	 * @return boolean
	 */
	public boolean isNotDuplicate(BookingRegistrationDTO oldBookingRegistrationDTO,
			BookingRegistrationDTO newBookingRegistrationDTO) {
		log.info("sessionId", "idType", "id", "In isNotDuplicate method of Booking Service Util");
		if (oldBookingRegistrationDTO.getRegDate().equals(newBookingRegistrationDTO.getRegDate())
				&& oldBookingRegistrationDTO.getRegistrationCenterId()
						.equals(newBookingRegistrationDTO.getRegistrationCenterId())
				&& oldBookingRegistrationDTO.getSlotFromTime().equals(newBookingRegistrationDTO.getSlotFromTime())
				&& oldBookingRegistrationDTO.getSlotToTime().equals(newBookingRegistrationDTO.getSlotToTime())) {
			throw new AppointmentReBookingFailedException(ErrorCodes.PRG_BOOK_RCI_021.getCode(),
					ErrorMessages.APPOINTMENT_REBOOKING_FAILED.getMessage());
		}
		return true;
	}

	/**
	 * This method is used to add the initial request values into a map for request
	 * map.
	 * 
	 * @param MainRequestDTO pass requestDTO
	 * @return a map for request request map
	 */
	public Map<String, String> prepareRequestMap(MainRequestDTO<?> requestDto) {
		log.info("sessionId", "idType", "id", "In prepareRequestMap method of Booking Service Util");
		Map<String, String> requestMap = new HashMap<>();
		requestMap.put("id", requestDto.getId());
		requestMap.put("ver", requestDto.getVersion());
		requestMap.put("reqTime", new SimpleDateFormat(utcDateTimePattern).format(requestDto.getRequesttime()));
		requestMap.put("request", requestDto.getRequest().toString());
		return requestMap;
	}

	/**
	 * Helper method for setting CancelBookingDTO.
	 * 
	 * @param preRegistrationId
	 * @param oldBookingRegistrationDTO
	 * @return
	 */
	public CancelBookingDTO cancelBookingDtoSetter(String preRegistrationId,
			BookingRegistrationDTO oldBookingRegistrationDTO) {
		log.info("sessionId", "idType", "id", "In cancelBookingDtoSetter method of Booking Service Util");
		CancelBookingDTO cancelBookingDTO = new CancelBookingDTO();
		cancelBookingDTO.setRegistrationCenterId(oldBookingRegistrationDTO.getRegistrationCenterId());
		cancelBookingDTO.setRegDate(oldBookingRegistrationDTO.getRegDate());
		cancelBookingDTO.setSlotFromTime(oldBookingRegistrationDTO.getSlotFromTime());
		cancelBookingDTO.setSlotToTime(oldBookingRegistrationDTO.getSlotToTime());
		return cancelBookingDTO;
	}

	public String getCurrentResponseTime() {
		log.info("sessionId", "idType", "id", "In getCurrentResponseTime method of Booking Service Util");
		return DateUtils2.formatDate(new Date(System.currentTimeMillis()), utcDateTimePattern);
	}

	/**
	 * This method will check for availability.
	 * 
	 * @param availableEntity
	 * @return boolean
	 */
	public boolean isKiosksAvailable(AvailibityEntity availableEntity) {
		log.info("sessionId", "idType", "id", "In isKiosksAvailable method of Booking Service Util");

		if (availableEntity.getAvailableKiosks() > 0) {
			return true;
		} else {
			throw new AvailablityNotFoundException(ErrorCodes.PRG_BOOK_RCI_002.getCode(),
					ErrorMessages.AVAILABILITY_NOT_FOUND_FOR_THE_SELECTED_TIME.getMessage());
		}
	}

	/**
	 * Helper method for setting RegistrationBookingEntity.
	 * 
	 * @param preRegistrationId
	 * @param oldBookingRegistrationDTO
	 * @return
	 */
	public RegistrationBookingEntity bookingEntitySetter(String preRegistrationId,
			BookingRequestDTO bookingRequestDTO) {// should set preid
		log.info("sessionId", "idType", "id", "In bookingEntitySetter method of Booking Service Util");
		RegistrationBookingEntity entity = new RegistrationBookingEntity();
		//entity.setBookingPK(new RegistrationBookingPK(DateUtils2.parseDateToLocalDateTime(new Date())));
		entity.setBookingDateTime(DateUtils2.parseDateToLocalDateTime(new Date()));
		entity.setRegistrationCenterId(bookingRequestDTO.getRegistrationCenterId());
		entity.setId(UUIDGeneratorUtil.generateId());
		entity.setLangCode("12L");
		String userId = authUserDetails().getUserId();
		entity.setCrBy(resolveEffectiveCrBy(userId));
		entity.setCrDate(DateUtils2.parseDateToLocalDateTime(new Date()));
		entity.setRegDate(LocalDate.parse(bookingRequestDTO.getRegDate()));
		entity.setSlotFromTime(LocalTime.parse(bookingRequestDTO.getSlotFromTime()));
		entity.setSlotToTime(LocalTime.parse(bookingRequestDTO.getSlotToTime()));
		entity.setPreregistrationId(preRegistrationId);
		return entity;
	}

	private String resolveEffectiveCrBy(String userId) {
		String maskedUserId = GenericUtil.maskIdentifier(userId);
		/*
		 * getOrCreateInternalUserId returns null for a null or blank id rather than
		 * throwing, so without this guard a missing authenticated user would flow
		 * through as a null crBy and fail only at the NOT NULL constraint - surfacing
		 * as an opaque "table not accessible" error instead of an identity one.
		 */
		if (userId == null || userId.isBlank()) {
			log.warn("sessionId", "idType", "id",
					"Cannot resolve effective booking user id: authenticated user id is absent");
			throw new AppointmentBookingFailedException(ErrorCodes.PRG_BOOK_RCI_005.getCode(),
					ErrorMessages.APPOINTMENT_BOOKING_FAILED.getMessage());
		}
		try {
			String effectiveCrBy = userDetailsService.getOrCreateInternalUserId(userId);
			boolean canonicalApplied = effectiveCrBy != null && !effectiveCrBy.isBlank()
					&& !effectiveCrBy.trim().equals(userId == null ? "" : userId.trim());
			log.info("sessionId", "idType", "id",
					"Resolved effective user id for booking write. maskedUserId=" + maskedUserId
							+ ", canonicalApplied=" + canonicalApplied);
			return effectiveCrBy;
		} catch (UserLookupException ex) {
			log.warn("sessionId", "idType", "id",
					"Failed to resolve effective booking user id for " + maskedUserId);
			throw new AppointmentBookingFailedException(ErrorCodes.PRG_BOOK_RCI_005.getCode(),
					ErrorMessages.APPOINTMENT_BOOKING_FAILED.getMessage());
		}
	}

	/**
	 * 
	 * @param notificationDTO
	 * @param langCode
	 * @return NotificationResponseDTO
	 * @throws JsonProcessingException
	 */
	public void emailNotification(NotificationDTO notificationDTO, String langCode) throws JsonProcessingException {
		String emailResourseUrl = notificationResourseurl + "/notify";
		ResponseEntity<String> resp = null;
		HttpHeaders headers = new HttpHeaders();
		MainRequestDTO<NotificationDTO> request = new MainRequestDTO<>();
		mapper.setTimeZone(TimeZone.getDefault());
		try {
			request.setRequest(notificationDTO);
			request.setId("mosip.pre-registration.notification.notify");
			request.setVersion("1.0");
			request.setRequesttime(new Date());
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
			MultiValueMap<Object, Object> emailMap = new LinkedMultiValueMap<>();
			emailMap.add("NotificationRequestDTO", mapper.writeValueAsString(request));
			emailMap.add("langCode", langCode);
			HttpEntity<MultiValueMap<Object, Object>> httpEntity = new HttpEntity<>(emailMap, headers);
			log.info("sessionId", "idType", "id",
					"In emailNotification method of NotificationUtil service emailResourseUrl: " + emailResourseUrl);
			resp = selfTokenRestTemplate.exchange(emailResourseUrl, HttpMethod.POST, httpEntity, String.class);
			List<ServiceError> validationErrorList = ExceptionUtils.getServiceErrorList(resp.getBody());
			if (validationErrorList != null && !validationErrorList.isEmpty()) {
				throw new NotificationException(validationErrorList, null);
			}
		} catch (HttpClientErrorException ex) {
			log.debug("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id",
					"In emailNotification method of Booking Service Util for HttpClientErrorException- "
							+ ex.getMessage());
			throw new RestCallException(ErrorCodes.PRG_BOOK_RCI_033.getCode(),
					ErrorMessages.NOTIFICATION_CALL_FAILED.getMessage());

		}
	}

	/**
	 * This method is used to check whether the appointment date is valid or not
	 * 
	 * @param requestMap
	 * @return true if the appointment date time is not older date or false if the
	 *         appointment date is older date
	 */
	public boolean validateAppointmentDate(Map<String, String> requestMap) {
		try {
			if (requestMap.get(RequestCodes.REG_DATE.getCode()) != null
					&& !requestMap.get(RequestCodes.REG_DATE.getCode()).isEmpty()) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				sdf.setLenient(false);
				sdf.parse(requestMap.get(RequestCodes.REG_DATE.getCode()));
				LocalDate localDate = LocalDate.parse(requestMap.get(RequestCodes.REG_DATE.getCode()));
				Date currentDate = new Date();
				LocalDate date = currentDate.toInstant().atZone(ZoneId.of(specificZoneId)).toLocalDate();

				if (localDate.isBefore(date)) {
					throw new InvalidDateTimeFormatException(
							ErrorCodes.PRG_BOOK_RCI_031.getCode(), ErrorMessages.INVALID_BOOKING_DATE_TIME.getMessage()
									+ " found for - " + requestMap.get(RequestCodes.PRE_REGISTRAION_ID.getCode()),
							null);
				} else if (localDate.isEqual(date) && (requestMap.get(RequestCodes.FROM_SLOT_TIME.getCode()) != null
						&& !requestMap.get(RequestCodes.FROM_SLOT_TIME.getCode()).isEmpty())) {
					LocalTime localTime = LocalTime.parse(requestMap.get(RequestCodes.FROM_SLOT_TIME.getCode()));
					LocalTime time = currentDate.toInstant().atZone(ZoneId.of(specificZoneId)).toLocalTime();
					if (localTime.isBefore(time)) {
						throw new InvalidDateTimeFormatException(ErrorCodes.PRG_BOOK_RCI_031.getCode(),
								ErrorMessages.INVALID_BOOKING_DATE_TIME.getMessage() + " found for - "
										+ requestMap.get(RequestCodes.PRE_REGISTRAION_ID.getCode()),
								null);
					}
				}
			}
		} catch (RuntimeException | ParseException ex) {
			log.debug("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			throw new InvalidDateTimeFormatException(ErrorCodes.PRG_BOOK_RCI_031.getCode(),
					ErrorMessages.INVALID_BOOKING_DATE_TIME.getMessage() + " found for preregistration id - "
							+ requestMap.get(RequestCodes.PRE_REGISTRAION_ID.getCode()),
					null);
		}
		return true;
	}

	/**
	 * This method is used to validate from date and to date params
	 * 
	 * @param fromDate
	 * @param toDate
	 * @param format
	 * @return true or false
	 */
	public boolean validateFromDateAndToDate(String fromDate, String toDate, String format) {
		log.info("sessionId", "idType", "id", "In validateDataSyncRequest method of datasync service util");

		if (isNull(fromDate) || !ValidationUtil.parseDate(fromDate, format)) {
			throw new InvalidDateTimeFormatException(
					io.mosip.preregistration.core.errorcodes.ErrorCodes.PRG_CORE_REQ_019.getCode(),
					io.mosip.preregistration.core.errorcodes.ErrorMessages.INVALID_DATE_TIME_FORMAT.getMessage(), null);
		} else if (!isNull(toDate) && !ValidationUtil.parseDate(toDate, format)) {
			throw new InvalidDateTimeFormatException(
					io.mosip.preregistration.core.errorcodes.ErrorCodes.PRG_CORE_REQ_019.getCode(),
					io.mosip.preregistration.core.errorcodes.ErrorMessages.INVALID_DATE_TIME_FORMAT.getMessage(), null);
		} else if (!isNull(fromDate) && !isNull(toDate)
				&& ((LocalDate.parse(fromDate)).isAfter(LocalDate.parse(toDate)))) {
			throw new InvalidDateTimeFormatException(
					io.mosip.preregistration.core.errorcodes.ErrorCodes.PRG_CORE_REQ_020.getCode(),
					io.mosip.preregistration.core.errorcodes.ErrorMessages.FROM_DATE_GREATER_THAN_TO_DATE.getMessage(),
					null);
		}
		return true;
	}

	public boolean isValidRegCenter(String regId) {
		List<RegistrationCenterDto> regCenter = getRegCenterMasterData(regId);
		Boolean isValidRegCenter = regCenter.stream().anyMatch(iterate -> iterate.getId().contains(regId));

		if (!isValidRegCenter) {
			throw new RecordNotFoundException(ErrorCodes.PRG_BOOK_RCI_035.getCode(),
					ErrorMessages.REG_CENTER_ID_NOT_FOUND.getMessage());
		}
		return true;

	}

	public MainResponseDTO<String> getApplicationStatus(String applicationId) {
		return applicationClient.getApplicationStatus(applicationId);
	}

//	public MainResponseDTO<String> updatePreRegistrationStatus(String preRegId, String status) {
//		MainResponseDTO<String> response = new MainResponseDTO<>();
//		String url = preRegResourceUrl + "/applications/status/" + preRegId;
//		MultiValueMap<String, String> paramStatus = new LinkedMultiValueMap<>();
//		paramStatus.add("statusCode", status);
//		HttpHeaders headers = new HttpHeaders();
//		HttpEntity<?> entity = new HttpEntity<>(paramStatus, headers);
//		System.out.println(url);
//		log.info("sessionId", "idType", "id", "In call to demographic rest service :" + url);
//		try {
//			ResponseEntity<MainResponseDTO<String>> responseEntity = restTemplate.exchange(url, HttpMethod.PUT, entity,
//					new ParameterizedTypeReference<MainResponseDTO<String>>() {
//					});
//			if (responseEntity.getBody().getErrors() != null && !responseEntity.getBody().getErrors().isEmpty()) {
//				response.setErrors(responseEntity.getBody().getErrors());
//			} else {
//				response.setResponse(responseEntity.getBody().getResponse());
//			}
//
//			log.info("sessionId", "idType", "id", "In call to demographic rest service :" + url);
//		} catch (Exception ex) {
//			log.debug("sessionId", "idType", "id",
//					"demographic rest call exception " + ExceptionUtils.getStackTrace(ex));
//			throw new RestClientException("rest call failed");
//		}
//		return response;
//	}

}

