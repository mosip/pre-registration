package io.mosip.preregistration.batchjob.utils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.preregistration.batchjob.code.ErrorCodes;
import io.mosip.preregistration.batchjob.code.ErrorMessages;
import io.mosip.preregistration.batchjob.entity.AvailibityEntity;
import io.mosip.preregistration.batchjob.exception.NoRecordFoundException;
import io.mosip.preregistration.batchjob.exception.RestCallException;
import io.mosip.preregistration.batchjob.exception.util.BatchServiceExceptionCatcher;
import io.mosip.preregistration.batchjob.model.ExceptionalHolidayDto;
import io.mosip.preregistration.batchjob.model.ExceptionalHolidayResponseDto;
import io.mosip.preregistration.batchjob.model.HolidayDto;
import io.mosip.preregistration.batchjob.model.RegistrationCenterDto;
import io.mosip.preregistration.batchjob.model.RegistrationCenterHolidayDto;
import io.mosip.preregistration.batchjob.model.RegistrationCenterResponseDto;
import io.mosip.preregistration.batchjob.model.WorkingDaysResponseDto;
import io.mosip.preregistration.batchjob.repository.utils.BatchJpaRepositoryImpl;
import io.mosip.preregistration.core.code.AuditLogVariables;
import io.mosip.preregistration.core.code.EventId;
import io.mosip.preregistration.core.code.EventName;
import io.mosip.preregistration.core.code.EventType;
import io.mosip.preregistration.core.code.StatusCodes;
import io.mosip.preregistration.core.common.dto.AuditRequestDto;
import io.mosip.preregistration.core.common.dto.CancelBookingResponseDTO;
import io.mosip.preregistration.core.common.dto.ExceptionJSONInfoDTO;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.dto.NotificationDTO;
import io.mosip.preregistration.core.common.dto.NotificationResponseDTO;
import io.mosip.preregistration.core.common.dto.PreRegistartionStatusDTO;
import io.mosip.preregistration.core.common.dto.RequestWrapper;
import io.mosip.preregistration.core.common.dto.ResponseWrapper;
import io.mosip.preregistration.core.common.entity.ApplicationEntity;
import io.mosip.preregistration.core.common.entity.RegistrationBookingEntity;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.exception.NotificationException;
import io.mosip.preregistration.core.util.AuditLogUtil;

/**
 * @author Rajath Kr
 * @since 1.0.0
 *
 */
@Component
public class AvailabilityUtil {

	/**
	 * Reference for ${preregistration.availability.sync} from property file
	 */
	@Value("${preregistration.availability.sync}")
	int syncDays;

	@Value("${version}")
	String versionUrl;

	@Value("${mosip.preregistration.booking.availability.sync.id}")
	String idUrlSync;

	@Value("${mosip.mandatory-languages}")
	private String mandatoryLangCodes;

	@Value("${mosip.optional-languages}")
	private String optionalLangCodes;

	@Value("${notification.url}")
	private String notificationResourseurl;

	@Value("${mosip.batch.token.authmanager.userName}")
	private String auditUsername;

	@Value("${mosip.batch.token.authmanager.appId}")
	private String auditUserId;

	/**
	 * Reference for ${holiday.url} from property file
	 */
	@Value("${holiday.url}")
	String holidayListUrl;

	/**
	 * Reference for ${holiday.exceptional.url} from property file
	 */
	@Value("${holiday.exceptional.url}")
	String exceptionalHolidayListUrl;

	/**
	 * Reference for ${working.day.url} from property file
	 */
	@Value("${working.day.url}")
	String workingDayListUrl;

	@Value("${mosip.utc-datetime-pattern}")
	private String utcDateTimePattern;

	/**
	 * Reference for ${regCenter.url} from property file
	 */
	@Value("${regCenter.url}")
	String regCenterUrl;

	@Value("${batch.appointment.cancel}")
	String cancelResourceUrl;

	/**
	 * Autowired reference for {@link #batchServiceDAO}
	 */
	@Autowired
	private BatchJpaRepositoryImpl batchServiceDAO;

	@Autowired
	private AuditLogUtil auditLogUtil;

	/**
	 * Autowired reference for {@link #restTemplateBuilder}
	 */
	@Autowired
	RestTemplate restTemplate;

	private String langCode;
	
	/**
	 * ObjectMapper global object creation
	 */
	private ObjectMapper mapper;

	@PostConstruct
	private void setup() {
		langCode = mandatoryLangCodes.split(",").length > 0 ? mandatoryLangCodes.split(",")[0]
				: optionalLangCodes.split(",")[0];
		mapper = JsonMapper.builder().addModule(new AfterburnerModule()).build();
		mapper.registerModule(new JavaTimeModule());
	
	}

	private Logger log = LoggerConfiguration.logConfig(AvailabilityUtil.class);

	public MainResponseDTO<String> addAvailability(HttpHeaders headers) {
		log.info("sessionId", "idType", "id", "In addAvailability method of AvailabilityUtil");
		MainResponseDTO<String> response = new MainResponseDTO<>();
		response.setId(idUrlSync);
		response.setVersion(versionUrl);
		boolean isSaveSuccess = false;
		List<String> cancelFailedList = new ArrayList<String>();
		List<String> notificationFailedList = new ArrayList<String>();

		try {
			LocalDate endDate = LocalDate.now().plusDays(syncDays - 1);
			List<RegistrationCenterDto> regCenter = getRegCenterMasterData(headers);
//			List<RegistrationCenterDto> regCenterDtos = regCenter.stream()
//					.filter(regCenterDto -> regCenterDto.getLangCode().equals(langCode)).collect(Collectors.toList());
			List<String> regCenterDumped = batchServiceDAO.findRegCenter(LocalDate.now());
			for (RegistrationCenterDto regDto : regCenter) {
				try {
					List<LocalDate> insertedDate = batchServiceDAO.findDistinctDate(LocalDate.now(), regDto.getId());
					List<String> holidaylist = getHolidayListMasterData(regDto, headers);
					regCenterDumped.remove(regDto.getId());
					for (LocalDate sDate = LocalDate.now(); (sDate.isBefore(endDate)
							|| sDate.isEqual(endDate)); sDate = sDate.plusDays(1)) {
						List<AvailibityEntity> regSlots = batchServiceDAO.findSlots(sDate, regDto.getId());
						if (insertedDate.isEmpty()) {
							timeSlotCalculator(regDto, holidaylist, sDate);
						} else if (regSlots.size() == 1) {
							batchServiceDAO.deleteSlots(regDto.getId(), sDate);
							timeSlotCalculator(regDto, holidaylist, sDate);
						} else if (!insertedDate.contains(sDate)) {
							timeSlotCalculator(regDto, holidaylist, sDate);
						} else if (holidaylist.contains(sDate.toString())) {
							List<RegistrationBookingEntity> regBookingEntityList = batchServiceDAO
									.findAllPreIds(regDto.getId(), sDate);
							if (!regBookingEntityList.isEmpty()) {
								for (int i = 0; i < regBookingEntityList.size(); i++) {
									RegistrationBookingEntity bookingEntity = regBookingEntityList.get(i);
									String preRegId = bookingEntity.getPreregistrationId();
									ApplicationEntity applicationEntity  = batchServiceDAO.getApplicantEntityDetails(preRegId);
									if (applicationEntity.getBookingStatusCode().equals(StatusCodes.BOOKED.getCode())) {
										if (cancelBooking(preRegId, headers, cancelFailedList)) {	
											sendNotification(bookingEntity, headers, notificationFailedList);
										}
									}
								}
							}
							batchServiceDAO.deleteSlots(regDto.getId(), sDate);
							timeSlotCalculator(regDto, holidaylist, sDate);
						} else {
							// Filter slots max min time
							LocalTime newStartTime = regDto.getCenterStartTime();
							int index = 0;
							while (index < regSlots.size() - 1) {
								LocalTime timeDiff = regSlots.get(index + 1).getFromTime()
										.minusHours(regSlots.get(index).getFromTime().getHour())
										.minusMinutes(regSlots.get(index).getFromTime().getMinute());
								if (!timeDiff.equals(regDto.getPerKioskProcessTime())) {
									break;
								} else
									index++;
							}
							LocalTime lunchstartTime = regSlots.get(index).getToTime();
							LocalTime lunchEndTime = regSlots.get(index + 1).getFromTime();
							LocalTime newEndTime = regDto.getCenterEndTime();

							if (!regSlots.get(0).getFromTime().equals(newStartTime)) {
								if (regSlots.get(0).getFromTime().isAfter(newStartTime)) {
									// create appointment on hourly basis
									workingHoursCalculator(newStartTime, regSlots.get(0).getFromTime(), regDto, sDate);
								} else {
									// cancel appointment
									LocalTime lastfromTime = newStartTime
											.minusHours(regDto.getPerKioskProcessTime().getHour())
											.minusMinutes(regDto.getPerKioskProcessTime().getMinute());
									List<RegistrationBookingEntity> regBookingEntityList = batchServiceDAO
											.findAllPreIdsBydateAndBetweenHours(regDto.getId(), sDate,
													regSlots.get(0).getFromTime(), lastfromTime);
									if (!regBookingEntityList.isEmpty()) {
										for (int i = 0; i < regBookingEntityList.size(); i++) {
											RegistrationBookingEntity bookingEntity = regBookingEntityList.get(i);
											String preRegId = bookingEntity.getPreregistrationId();
											ApplicationEntity applicationEntity  = batchServiceDAO.getApplicantEntityDetails(preRegId);
											if (applicationEntity.getBookingStatusCode().equals(StatusCodes.BOOKED.getCode())) {
												if (cancelBooking(preRegId, headers, cancelFailedList)) {	
													sendNotification(bookingEntity, headers, notificationFailedList);
												}
											}
										}
									}
									batchServiceDAO.deleteSlotsBetweenHours(regDto.getId(), sDate,
											regSlots.get(0).getFromTime(), lastfromTime);
								}

							}
							if (!lunchstartTime.equals(regDto.getLunchStartTime())) {
								if (lunchstartTime.isAfter(regDto.getLunchStartTime())) {
									// cancel appointment
									LocalTime lastfromTime = lunchstartTime
											.minusHours(regDto.getPerKioskProcessTime().getHour())
											.minusMinutes(regDto.getPerKioskProcessTime().getMinute());
									List<RegistrationBookingEntity> regBookingEntityList = batchServiceDAO
											.findAllPreIdsBydateAndBetweenHours(regDto.getId(), sDate,
													regDto.getLunchStartTime(), lastfromTime);
									if (!regBookingEntityList.isEmpty()) {
										for (int i = 0; i < regBookingEntityList.size(); i++) {
											RegistrationBookingEntity bookingEntity = regBookingEntityList.get(i);
											String preRegId = bookingEntity.getPreregistrationId();
											ApplicationEntity applicationEntity  = batchServiceDAO.getApplicantEntityDetails(preRegId);
											if (applicationEntity.getBookingStatusCode().equals(StatusCodes.BOOKED.getCode())) {
												if (cancelBooking(preRegId, headers, cancelFailedList)) {	
													sendNotification(bookingEntity, headers, notificationFailedList);
												}
											}
										}
									}
									batchServiceDAO.deleteSlotsBetweenHours(regDto.getId(), sDate,
											regDto.getLunchStartTime(), lastfromTime);
								} else {
									// create appointment slots on hourly basis
									workingHoursCalculator(lunchstartTime, regDto.getLunchStartTime(), regDto, sDate);

								}

							}
							if (!lunchEndTime.equals(regDto.getLunchEndTime())) {
								if (lunchEndTime.isAfter(regDto.getLunchEndTime())) {
									// create appointment slots on hourly basis
									workingHoursCalculator(regDto.getLunchEndTime(), lunchEndTime, regDto, sDate);
								} else {
									// cancel appointment
									LocalTime lastfromTime = regDto.getLunchEndTime()
											.minusHours(regDto.getPerKioskProcessTime().getHour())
											.minusMinutes(regDto.getPerKioskProcessTime().getMinute());
									List<RegistrationBookingEntity> regBookingEntityList = batchServiceDAO
											.findAllPreIdsBydateAndBetweenHours(regDto.getId(), sDate, lunchEndTime,
													lastfromTime);
									if (!regBookingEntityList.isEmpty()) {
										for (int i = 0; i < regBookingEntityList.size(); i++) {
											RegistrationBookingEntity bookingEntity = regBookingEntityList.get(i);
											String preRegId = bookingEntity.getPreregistrationId();
											ApplicationEntity applicationEntity  = batchServiceDAO.getApplicantEntityDetails(preRegId);
											if (applicationEntity.getBookingStatusCode().equals(StatusCodes.BOOKED.getCode())) {
												if (cancelBooking(preRegId, headers, cancelFailedList)) {	
													sendNotification(bookingEntity, headers, notificationFailedList);
												}
											}
										}
									}
									batchServiceDAO.deleteSlotsBetweenHours(regDto.getId(), sDate, lunchEndTime,
											lastfromTime);

								}

							}
							if (!regSlots.get(regSlots.size() - 1).getToTime().equals(newEndTime)) {
								if (regSlots.get(regSlots.size() - 1).getToTime().isAfter(newEndTime)) {
									// cancel appointment
									LocalTime lastfromTime = regSlots.get(regSlots.size() - 1).getFromTime();
									List<RegistrationBookingEntity> regBookingEntityList = batchServiceDAO
											.findAllPreIdsBydateAndBetweenHours(regDto.getId(), sDate, newEndTime,
													lastfromTime);
									if (!regBookingEntityList.isEmpty()) {
										for (int i = 0; i < regBookingEntityList.size(); i++) {
											RegistrationBookingEntity bookingEntity = regBookingEntityList.get(i);
											String preRegId = bookingEntity.getPreregistrationId();
											ApplicationEntity applicationEntity  = batchServiceDAO.getApplicantEntityDetails(preRegId);
											if (applicationEntity.getBookingStatusCode().equals(StatusCodes.BOOKED.getCode())) {
												if (cancelBooking(preRegId, headers, cancelFailedList)) {	
													sendNotification(bookingEntity, headers, notificationFailedList);
												}
											}
										}
									}
									batchServiceDAO.deleteSlotsBetweenHours(regDto.getId(), sDate, newEndTime,
											lastfromTime);
								} else {
									// create appointment on hourly basis
									workingHoursCalculator(regSlots.get(regSlots.size() - 1).getToTime(), newEndTime,
											regDto, sDate);
								}
							}
						}
					}
				} catch (Throwable ex) {
					log.error("sessionId", "idType", "id",
							"In  addAvailability method reg center loop AvailabilityUtil- " + ex.getMessage());
				}

			}
			if (!regCenterDumped.isEmpty()) {
				for (int i = 0; i < regCenterDumped.size(); i++) {
					List<RegistrationBookingEntity> entityList = batchServiceDAO
							.findAllPreIdsByregID(regCenterDumped.get(i), LocalDate.now());
					if (!entityList.isEmpty()) {
						for (int j = 0; j < entityList.size(); j++) {
							RegistrationBookingEntity bookingEntity = entityList.get(j);
							String preRegId = bookingEntity.getPreregistrationId();
							ApplicationEntity applicationEntity  = batchServiceDAO.getApplicantEntityDetails(preRegId);
							if (applicationEntity.getBookingStatusCode().equals(StatusCodes.BOOKED.getCode())) {
								if (cancelBooking(preRegId, headers, cancelFailedList)) {	
									sendNotification(bookingEntity, headers, notificationFailedList);
								}
							}
						}
					}

					batchServiceDAO.deleteAllSlotsByRegId(regCenterDumped.get(i), LocalDate.now());
				}
			}
			isSaveSuccess = true;
		} catch (Exception ex) {
			log.error("sessionId", "idType", "id", "In addAvailability method of AvailabilityUtil- " + ex.getMessage());
			new BatchServiceExceptionCatcher().handle(ex);
		} finally {
			log.info("sessionId", "idType", "id", "cancel appointment failed for - " + cancelFailedList.size());
			cancelFailedList.forEach((id) -> log.error("sessionId", "idType", "id", "cancel booking failed for ", id));
			log.info("sessionId", "idType", "id", "cancel notifications failed for - " + notificationFailedList.size());
			notificationFailedList.forEach((id) -> log.error("sessionId", "idType", "id", "cancel booking notification failed for ", id));
			response.setResponsetime(getCurrentResponseTime());
			if (isSaveSuccess) {
				setAuditValues(EventId.PRE_407.toString(), EventName.PERSIST.toString(), EventType.SYSTEM.toString(),
						"Availability for booking successfully saved in the database",
						AuditLogVariables.MULTIPLE_ID.toString(), auditUserId, auditUsername, null, headers);
			} else {
				setAuditValues(EventId.PRE_405.toString(), EventName.EXCEPTION.toString(), EventType.SYSTEM.toString(),
						"addAvailability failed", AuditLogVariables.NO_ID.toString(), auditUserId, auditUsername, null,
						headers);
			}
		}
		log.info("sessionId", "idType", "id", "Completed addAvailability");
		response.setResponsetime(getCurrentResponseTime());
		response.setResponse("MASTER_DATA_SYNCED_SUCCESSFULLY");
		return response;
	}

	private boolean cancelBooking(String preRegistrationId, HttpHeaders headers, List<String> cancelFailedList) {

		log.info("sessionId", "idType", "id", "In cancelBooking method of Availability Util: " + preRegistrationId);
		try {
			Map<String, Object> params = new HashMap<>();
			params.put("preRegistrationId", preRegistrationId);
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(cancelResourceUrl);
			String uriBuilder = builder.buildAndExpand(params).encode().toUriString();
			log.info("sessionId", "idType", "id", "In cancelBooking method of Availability Util URL- " + uriBuilder);
			HttpEntity<MainResponseDTO<PreRegistartionStatusDTO>> httpEntity = new HttpEntity<>(headers);			
			ResponseEntity<MainResponseDTO<CancelBookingResponseDTO>> respEntity = restTemplate.exchange(uriBuilder,
					HttpMethod.PUT, httpEntity,
					new ParameterizedTypeReference<MainResponseDTO<CancelBookingResponseDTO>>() {
					});
			MainResponseDTO<CancelBookingResponseDTO> body = respEntity.getBody();
			if (body != null) {
				if (body.getErrors() != null && !body.getErrors().isEmpty()) {
					for (ExceptionJSONInfoDTO dto : body.getErrors()) {
						if (dto != null) {
							throw new NoRecordFoundException(dto.getErrorCode(), dto.getMessage());
						}
					}
				}
			}	
		} catch (Exception ex) {
			log.error("sessionId", "idType", "id",
					"Exception in cancelBooking method of Availability Util - ", ex);
			cancelFailedList.add(preRegistrationId);
			return false;
		}
		return true;
	}

	/**
	 * This method will call kernel service for registration center date.
	 * 
	 * @return List of RegistrationCenterDto
	 */
	public List<RegistrationCenterDto> getRegCenterMasterData(HttpHeaders headers) {
		log.info("sessionId", "idType", "id", "In callRegCenterDateRestService method of AvailabilityUtil");
		List<RegistrationCenterDto> regCenter = null;
		try {
			UriComponentsBuilder regbuilder = UriComponentsBuilder.fromHttpUrl(regCenterUrl);
			HttpEntity<RequestWrapper<RegistrationCenterResponseDto>> entity = new HttpEntity<>(headers);
			String uriBuilder = regbuilder.build().encode().toUriString();
			log.info("sessionId", "idType", "id",
					"In callRegCenterDateRestService method of Booking Service URL- " + uriBuilder);
			ResponseEntity<ResponseWrapper<RegistrationCenterResponseDto>> responseEntity = restTemplate.exchange(
					uriBuilder, HttpMethod.GET, entity,
					new ParameterizedTypeReference<ResponseWrapper<RegistrationCenterResponseDto>>() {
					});
			ResponseWrapper<RegistrationCenterResponseDto> body = responseEntity.getBody();
			if (body != null) {
				if (body.getErrors() != null && !body.getErrors().isEmpty()) {
					throw new NoRecordFoundException(body.getErrors().get(0).getErrorCode(),
							body.getErrors().get(0).getMessage());
				}
				if (body.getResponse() != null) {
					regCenter = body.getResponse().getRegistrationCenters();	
				}
			}
			if (regCenter == null || regCenter.isEmpty()) {
				throw new NoRecordFoundException(ErrorCodes.PRG_PAM_BAT_011.getCode(),
						ErrorMessages.MASTER_DATA_NOT_FOUND.getMessage());
			}

		} catch (HttpClientErrorException ex) {
			log.error("sessionId", "idType", "id",
					"In callRegCenterDateRestService method of Booking Service Util for HttpClientErrorException- "
							+ ex.getMessage());
			throw new RestCallException(ErrorCodes.PRG_PAM_BAT_011.getCode(),
					ErrorMessages.MASTER_DATA_NOT_FOUND.getMessage());
		}
		return regCenter;
	}

	/**
	 * This method will call kernel service holiday list
	 * 
	 * @param regDto
	 * @return List of string
	 */
	public List<String> getHolidayListMasterData(RegistrationCenterDto regDto, HttpHeaders headers) {
		log.info("sessionId", "idType", "id",
				"In callGetHolidayListRestService method of AvailabilityUtil for regcenter: " + regDto.getId());
		List<String> holidaylist = null;
		try {

			String holidayUrl = holidayListUrl + regDto.getLangCode() + "/" + regDto.getId() + "/"
					+ LocalDate.now().getYear();
			UriComponentsBuilder builder1 = UriComponentsBuilder.fromHttpUrl(holidayUrl);
			HttpEntity<RequestWrapper<RegistrationCenterHolidayDto>> httpHolidayEntity = new HttpEntity<>(headers);
			String uriBuilder = builder1.build().encode().toUriString();
			log.info("sessionId", "idType", "id",
					"In callGetHolidayListRestService method of Booking Service URL- " + uriBuilder);
			/** Rest call to master for regular holidays. */
			ResponseEntity<ResponseWrapper<RegistrationCenterHolidayDto>> responseEntity1 = restTemplate.exchange(
					uriBuilder, HttpMethod.GET, httpHolidayEntity,
					new ParameterizedTypeReference<ResponseWrapper<RegistrationCenterHolidayDto>>() {
					});
			ResponseWrapper<RegistrationCenterHolidayDto> body1 = responseEntity1.getBody();
			holidaylist = new ArrayList<>();
			if (body1 != null) {
				if (body1.getErrors() != null && !body1.getErrors().isEmpty()) {
					throw new NoRecordFoundException(body1.getErrors().get(0).getErrorCode(),
							body1.getErrors().get(0).getMessage());
				}
				if (body1.getResponse() != null && body1.getResponse().getHolidays() != null
						&& !body1.getResponse().getHolidays().isEmpty()) {
					for (HolidayDto holiday : body1.getResponse().getHolidays()) {
						holidaylist.add(holiday.getHolidayDate());
					}
				}	
			}
			

			/** Rest call to master for exceptional holidays. */
			String exceptionalHolidayUrl = exceptionalHolidayListUrl + regDto.getId() + "/"
					+ regDto.getLangCode();
			UriComponentsBuilder builder2 = UriComponentsBuilder.fromHttpUrl(exceptionalHolidayUrl);
			HttpEntity<RequestWrapper<RegistrationCenterHolidayDto>> httpExceptionalHolidayEntity = new HttpEntity<>(
					headers);
			String uriBuilder2 = builder2.build().encode().toUriString();
			ResponseEntity<ResponseWrapper<ExceptionalHolidayResponseDto>> responseEntity2 = restTemplate.exchange(
					uriBuilder2, HttpMethod.GET, httpExceptionalHolidayEntity,
					new ParameterizedTypeReference<ResponseWrapper<ExceptionalHolidayResponseDto>>() {
					});
			ResponseWrapper<ExceptionalHolidayResponseDto> body2 = responseEntity2.getBody();
			if (body2 != null) {
				if (body2.getErrors() != null && !body2.getErrors().isEmpty()) {
					log.error("sessionId", "idType", "id",
							"In get exceptional holiday fetch call of Booking Service Util for HttpClientErrorException- "
									+ body2.getErrors().get(0).getErrorCode()
									+ body2.getErrors().get(0).getMessage());
				}
				if (body2.getResponse() != null && body2.getResponse().getExceptionalHolidayList() != null
						&& !body2.getResponse().getExceptionalHolidayList().isEmpty()) {
					for (ExceptionalHolidayDto exceptionalHoliday : body2.getResponse()
							.getExceptionalHolidayList()) {
						holidaylist.add(exceptionalHoliday.getHolidayDate().toString());
					}
				}	
			}
			

			/** Rest call to master for working holidays. */
			String workingDayUrl = workingDayListUrl  + regDto.getId() + "/" + regDto.getLangCode();
			UriComponentsBuilder builder3 = UriComponentsBuilder.fromHttpUrl(workingDayUrl);
			HttpEntity<RequestWrapper<WorkingDaysResponseDto>> httpWorkingDayEntity = new HttpEntity<>(headers);
			String uriBuilder3 = builder3.build().encode().toUriString();

			ResponseEntity<ResponseWrapper<WorkingDaysResponseDto>> responseEntity3 = restTemplate.exchange(uriBuilder3,
					HttpMethod.GET, httpWorkingDayEntity,
					new ParameterizedTypeReference<ResponseWrapper<WorkingDaysResponseDto>>() {
					});
			ResponseWrapper<WorkingDaysResponseDto> body3 = responseEntity3.getBody();
			if (body3 != null) {
				if (body3.getErrors() != null && !body3.getErrors().isEmpty()) {
					throw new NoRecordFoundException(body3.getErrors().get(0).getErrorCode(),
							body3.getErrors().get(0).getMessage());
				}
				
			}
			
			// Code to retrive date of days and add it to holidays.
			/* if (responseEntity3.getBody().getResponse().getWeekdays() != null) {
				List<String> workingDays = responseEntity3.getBody().getResponse().getWeekdays().stream()
						.filter(regCenterWorkingDays -> regCenterWorkingDays.isWorking())
						.flatMap(wd -> Stream.of(wd.getName())).collect(Collectors.toList());
				log.info("WorkingDays >>> {}", workingDays);
				log.info("Weekdays Response {}", responseEntity3.getBody().getResponse().getWeekdays());
				List<String> nonWorkingDays = responseEntity3.getBody().getResponse().getWeekdays().stream()
						.filter(regCenterWorkingDays -> !regCenterWorkingDays.isWorking())
						.flatMap(wd -> Stream.of(wd.getName())).collect(Collectors.toList());
				log.info("nonWorkingDays >>> {}", nonWorkingDays);
				for (String nonWorkingDay : nonWorkingDays) {
					for (LocalDate date = LocalDate.now(); date
							.isBefore(LocalDate.now().plusDays(syncDays)); date = date.plusDays(1)) {
						if (nonWorkingDay.equalsIgnoreCase(date.getDayOfWeek()
								.getDisplayName(TextStyle.SHORT, new Locale(langCode.substring(0, 2))).toString())) {
							holidaylist.add(date.toString());
						}
					}
				}
			} */

			/*
			 * if (!responseEntity3.getBody().getResponse().getWorkingdays().isEmpty()) {
			 * for (WorkingDaysDto workingDay :
			 * responseEntity3.getBody().getResponse().getWorkingdays()) { Days.values() //
			 * Get the non working days to add it to holiday list . if
			 * (!workingDay.isWorking()) { for (LocalDate date = LocalDate.now(); date
			 * .isBefore(LocalDate.now().plusDays(syncDays)); date = date.plusDays(1)) { if
			 * (workingDay.getName().equalsIgnoreCase(date.getDayOfWeek().toString().
			 * substring(0,3))) { holidaylist.add(date.toString()); } } } } }
			 */

		} catch (HttpClientErrorException ex) {
			log.error("sessionId", "idType", "id",
					"In callGetHolidayListRestService method of Booking Service Util for HttpClientErrorException- "
							+ ex.getMessage());
			throw ex;

		}
		return holidaylist;
	}

	/**
	 * This method will do booking time slots.
	 * 
	 * @param regDto
	 * @param holidaylist
	 * @param sDate
	 * @param batchServiceDAO
	 */
	public void timeSlotCalculator(RegistrationCenterDto regDto, List<String> holidaylist, LocalDate sDate) {
		log.info("sessionId", "idType", "id",
				"In timeSlotCalculator method of AvailabilityUtil for " + regDto + " on date " + sDate);
		if (holidaylist.contains(sDate.toString())) {
			LocalTime localTime = LocalTime.MIDNIGHT;
			saveAvailability(regDto, sDate, localTime, localTime);

		} else {

			int window1 = ((regDto.getLunchStartTime().getHour() * 60 + regDto.getLunchStartTime().getMinute())
					- (regDto.getCenterStartTime().getHour() * 60 + regDto.getCenterStartTime().getMinute()))
					/ (regDto.getPerKioskProcessTime().getHour() * 60 + regDto.getPerKioskProcessTime().getMinute());

			int window2 = ((regDto.getCenterEndTime().getHour() * 60 + regDto.getCenterEndTime().getMinute())
					- (regDto.getLunchEndTime().getHour() * 60 + regDto.getLunchEndTime().getMinute()))
					/ (regDto.getPerKioskProcessTime().getHour() * 60 + regDto.getPerKioskProcessTime().getMinute());

			int extraTime1 = ((regDto.getLunchStartTime().getHour() * 60 + regDto.getLunchStartTime().getMinute())
					- (regDto.getCenterStartTime().getHour() * 60 + regDto.getCenterStartTime().getMinute()))
					% (regDto.getPerKioskProcessTime().getHour() * 60 + regDto.getPerKioskProcessTime().getMinute());

			int extraTime2 = ((regDto.getCenterEndTime().getHour() * 60 + regDto.getCenterEndTime().getMinute())
					- (regDto.getLunchEndTime().getHour() * 60 + regDto.getLunchEndTime().getMinute()))
					% (regDto.getPerKioskProcessTime().getHour() * 60 + regDto.getPerKioskProcessTime().getMinute());

			LocalTime currentTime1 = regDto.getCenterStartTime();
			for (int i = 0; i < window1; i++) {
				if (i == (window1 - 1)) {
					LocalTime toTime = currentTime1.plusMinutes(regDto.getPerKioskProcessTime().getMinute())
							.plusMinutes(extraTime1);
					saveAvailability(regDto, sDate, currentTime1, toTime);

				} else {
					LocalTime toTime = currentTime1.plusMinutes(regDto.getPerKioskProcessTime().getMinute());
					saveAvailability(regDto, sDate, currentTime1, toTime);
				}
				currentTime1 = currentTime1.plusMinutes(regDto.getPerKioskProcessTime().getMinute());
			}

			LocalTime currentTime2 = regDto.getLunchEndTime();
			for (int i = 0; i < window2; i++) {
				if (i == (window2 - 1)) {
					LocalTime toTime = currentTime2.plusMinutes(regDto.getPerKioskProcessTime().getMinute())
							.plusMinutes(extraTime2);
					saveAvailability(regDto, sDate, currentTime2, toTime);

				} else {
					LocalTime toTime = currentTime2.plusMinutes(regDto.getPerKioskProcessTime().getMinute());
					saveAvailability(regDto, sDate, currentTime2, toTime);
				}
				currentTime2 = currentTime2.plusMinutes(regDto.getPerKioskProcessTime().getMinute());
			}
		}
	}

	private void workingHoursCalculator(LocalTime fromTime, LocalTime toTime, RegistrationCenterDto regDto,
			LocalDate forDate) {
		log.info("sessionId", "idType", "id", "In workingHoursCalculator method of Availability Util");
		int window = (toTime.getHour() * 60 + toTime.getMinute() - fromTime.getHour() * 60 + fromTime.getMinute())
				/ (regDto.getPerKioskProcessTime().getHour() * 60 + regDto.getPerKioskProcessTime().getMinute());
		int extraTime = (toTime.getHour() * 60 + toTime.getMinute() - fromTime.getHour() * 60 + fromTime.getMinute())
				% (regDto.getPerKioskProcessTime().getHour() * 60 + regDto.getPerKioskProcessTime().getMinute());
		LocalTime slotStartTime = fromTime;
		for (int loop = 0; loop < window; loop++) {
			if (loop == window - 1) {
				LocalTime slotsEndTime = slotStartTime.plusMinutes(regDto.getPerKioskProcessTime().getMinute())
						.plusMinutes(extraTime);
				saveAvailability(regDto, forDate, slotStartTime, slotsEndTime);
			} else {
				LocalTime slotsEndTime = slotStartTime.plusMinutes(regDto.getPerKioskProcessTime().getMinute());
				saveAvailability(regDto, forDate, slotStartTime, slotsEndTime);
			}
			slotStartTime = slotStartTime.plusMinutes(regDto.getPerKioskProcessTime().getMinute());

		}
	}

	private void saveAvailability(RegistrationCenterDto regDto, LocalDate date, LocalTime currentTime,
			LocalTime toTime) {
		log.info("sessionId", "idType", "id", "In saveAvailability method of Availability Util");
		AvailibityEntity avaEntity = new AvailibityEntity();
		avaEntity.setRegDate(date);
		avaEntity.setRegcntrId(regDto.getId());
		avaEntity.setFromTime(currentTime);
		avaEntity.setToTime(toTime);
		avaEntity.setCrBy(auditUsername);
		avaEntity.setCrDate(DateUtils.parseDateToLocalDateTime(new Date()));
		if (isNull(regDto.getContactPerson())) {
			avaEntity.setCrBy(auditUsername);
		} else {
			avaEntity.setCrBy(regDto.getContactPerson());
		}
		if (currentTime.equals(toTime)) {
			avaEntity.setAvailableKiosks(0);
		} else {
			avaEntity.setAvailableKiosks(regDto.getNumberOfKiosks());
		}
		batchServiceDAO.saveAvailability(avaEntity);
	}

	/**
	 * This method is used as Null checker for different input keys.
	 *
	 * @param key pass the key
	 * @return true if key not null and return false if key is null.
	 */

	public boolean isNull(Object key) {
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
	 * @param registrationBookingEntity
	 * @throws JsonProcessingException
	 */
	public boolean sendNotification(RegistrationBookingEntity registrationBookingEntity, HttpHeaders headers,
			List<String> notificationsFailedList)
			throws JsonProcessingException {
		try {
		log.info("sessionId", "idType", "id", "In sendNotification method of AvailabilityUtil");
		NotificationDTO notification = new NotificationDTO();
		notification.setAppointmentDate(registrationBookingEntity.getRegDate().toString());
		notification.setPreRegistrationId(registrationBookingEntity.getPreregistrationId());
		String time = LocalTime
				.parse(registrationBookingEntity.getSlotFromTime().toString(), DateTimeFormatter.ofPattern("HH:mm"))
				.format(DateTimeFormatter.ofPattern("hh:mm a"));
		notification.setAppointmentTime(time);
		notification.setAdditionalRecipient(false);
		notification.setIsBatch(true);
		emailNotification(notification, langCode, headers);
		} catch (Exception ex) {
			log.error("Exception in cancel ", ex);
			log.error("sessionId", "idType", "id",
					"Exception in sendNotification method of Availability Util - ", ex);
			notificationsFailedList.add(registrationBookingEntity.getPreregistrationId());
			return false;
		}
		return true;
	}

	/**
	 * 
	 * @param notificationDTO
	 * @param langCode
	 * @return NotificationResponseDTO
	 * @throws JsonProcessingException
	 */
	public void emailNotification(NotificationDTO notificationDTO, String langCode, HttpHeaders headers)
			throws JsonProcessingException {
		String emailResourseUrl = notificationResourseurl;
		MainRequestDTO<NotificationDTO> request = new MainRequestDTO<>();
		mapper.setTimeZone(TimeZone.getDefault());
		try {
			request.setRequest(notificationDTO);
			request.setId("mosip.pre-registration.notification.notify");
			request.setVersion("1.0");
			request.setRequesttime(new Date());
			LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
			map.add("NotificationRequestDTO", mapper.writeValueAsString(request));
			map.add("langCode", langCode);
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
			HttpEntity<LinkedMultiValueMap<String, Object>> httpEntity = new HttpEntity<LinkedMultiValueMap<String, Object>>(
					map, headers);
			log.info("sessionId", "idType", "id",
					"In emailNotification method of NotificationUtil service emailResourseUrl: " + emailResourseUrl);
			
			ResponseEntity<MainResponseDTO<NotificationResponseDTO>> respEntity = restTemplate.exchange(
					emailResourseUrl, HttpMethod.POST, httpEntity,
					new ParameterizedTypeReference<MainResponseDTO<NotificationResponseDTO>>() {
					});
			MainResponseDTO<NotificationResponseDTO> body = respEntity.getBody();
			if (body != null) {
				if (body.getErrors() != null) {
					List<ServiceError> validationErrorList = ExceptionUtils.getServiceErrorList(body.toString());
					if (validationErrorList != null && !validationErrorList.isEmpty()) {
						throw new NotificationException(validationErrorList, null);
					}
				}
			}	
			 	
			
		} catch (Exception ex) {
			log.error("sessionId", "idType", "id",
					"Exception in emailNotification method of Booking Service Util for HttpClientErrorException- ", ex);
			throw new RestCallException(ErrorCodes.PRG_PAM_BAT_012.getCode(),
					ErrorMessages.NOTIFICATION_CALL_FAILED.getMessage());

		}
	}

	public String getCurrentResponseTime() {
		log.info("sessionId", "idType", "id", "In getCurrentResponseTime method of AvailabilityUtil");
		return DateUtils.formatDate(new Date(System.currentTimeMillis()), utcDateTimePattern);
	}

	/**
	 * This method is used to audit all the booking events
	 * 
	 * @param eventId
	 * @param eventName
	 * @param eventType
	 * @param description
	 * @param idType
	 */
	public void setAuditValues(String eventId, String eventName, String eventType, String description, String idType,
			String userId, String userName, String ref_id, HttpHeaders headers) {
		AuditRequestDto auditRequestDto = new AuditRequestDto();
		auditRequestDto.setEventId(eventId);
		auditRequestDto.setEventName(eventName);
		auditRequestDto.setEventType(eventType);
		auditRequestDto.setDescription(description);
		auditRequestDto.setId(idType);
		auditRequestDto.setSessionUserId(userId);
		auditRequestDto.setSessionUserName(userName);
		auditRequestDto.setModuleId(AuditLogVariables.BOOK.toString());
		auditRequestDto.setModuleName(AuditLogVariables.BOOKING_SERVICE.toString());
		auditRequestDto.setId(ref_id);
		auditLogUtil.saveAuditDetails(auditRequestDto, headers);
	}

}
