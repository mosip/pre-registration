package io.mosip.preregistration.booking.test.service.util;

import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.mosip.preregistration.booking.exception.*;
import io.mosip.preregistration.core.exception.UserLookupException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.util.DateUtils2;
import io.mosip.preregistration.booking.codes.RequestCodes;
import io.mosip.preregistration.booking.dto.BookingRequestDTO;
import io.mosip.preregistration.booking.dto.CancelBookingDTO;
import io.mosip.preregistration.booking.dto.DateTimeDto;
import io.mosip.preregistration.booking.dto.RegistrationCenterDto;
import io.mosip.preregistration.booking.dto.RegistrationCenterResponseDto;
import io.mosip.preregistration.booking.dto.SlotDto;
import io.mosip.preregistration.booking.entity.AvailibityEntity;
import io.mosip.preregistration.booking.repository.BookingAvailabilityRepository;
import io.mosip.preregistration.booking.repository.RegistrationBookingRepository;
import io.mosip.preregistration.booking.repository.impl.BookingDAO;
import io.mosip.preregistration.booking.service.util.BookingServiceUtil;
import io.mosip.preregistration.core.common.dto.BookingRegistrationDTO;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.NotificationDTO;
import io.mosip.preregistration.core.common.dto.ResponseWrapper;
import io.mosip.preregistration.core.common.entity.ApplicationEntity;
import io.mosip.preregistration.core.common.entity.RegistrationBookingEntity;
import io.mosip.preregistration.core.common.repository.ApplicationRepostiory;
import io.mosip.preregistration.core.common.service.UserDetailsService;
import io.mosip.preregistration.core.exception.MasterDataNotAvailableException;
import io.mosip.preregistration.core.exception.PreIdInvalidForUserIdException;
import io.mosip.preregistration.core.exception.RestCallException;
import io.mosip.preregistration.core.util.RequestValidator;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BookingServiceUtil.class })
@TestPropertySource(properties = {
		"preregistration.timespan.cancel=24",
		"preregistration.timespan.rebook=24",
		"regCenter.url=http://localhost/v1/masterdata/registrationcenters",
		"holiday.url=http://localhost/v1/masterdata/getregistrationcenterholidays/",
		"notification.url=http://localhost/preregistration/v1/internal/notification",
		"demographic.resource.url=http://localhost/preregistration/v1",
		"mosip.utc-datetime-pattern=yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
		"mosip.notification.timezone=GMT+05:30",
		"mosip.prereg.pii.backward.compatibility=true"
})
public class BookingServiceUtilTest {

	@MockitoBean
	private BookingAvailabilityRepository bookingAvailabilityRepository;

	@MockitoBean
	private RegistrationBookingRepository registrationBookingRepository;

	@MockitoBean(name = "selfTokenRestTemplate")
	RestTemplate restTemplate;

	@MockitoBean
	private RequestValidator requestValidator;

	@MockitoBean
	ObjectMapper mapper;

	@Autowired
	private BookingServiceUtil serviceUtil;

	@MockitoBean
	private BookingDAO bookingDAO;

	@MockitoBean
	private UserDetailsService userDetailsService;

	@MockitoBean
	private ApplicationRepostiory applicationRepostiory;

	@MockitoBean
	private io.mosip.preregistration.pipeline.InProcessApplicationClient applicationClient;

	@Mock
	private AuthUserDetails authUserDetails;

	@Mock
	SecurityContextHolder securityContextHolder;
	RegistrationCenterResponseDto regCenDto = new RegistrationCenterResponseDto();
	RegistrationCenterDto centerDto = new RegistrationCenterDto();
	List<RegistrationCenterDto> centerList = new ArrayList<>();

	LocalTime startTime;
	LocalTime endTime;
	LocalTime perKioskTime;
	LocalTime LunchStartTime;
	LocalTime LunchEndTime;
	LocalTime localTime1;
	LocalTime localTime2;
	DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	@Before
	public void setup() throws Exception {

		String date3 = "2016-11-09 00:20:00";
		String date4 = "2016-11-09 13:00:00";
		String date5 = "2016-11-09 14:20:00";
		String date1 = "2016-11-09 09:00:00";
		String date2 = "2016-11-09 09:20:00";
		LocalDateTime localDateTime1 = LocalDateTime.parse(date1, format);
		LocalDateTime localDateTime2 = LocalDateTime.parse(date2, format);
		localTime1 = localDateTime1.toLocalTime();
		localTime2 = localDateTime2.toLocalTime();
		LocalDateTime localDateTime3 = LocalDateTime.parse(date3, format);
		startTime = localDateTime1.toLocalTime();
		endTime = localDateTime2.toLocalTime();
		perKioskTime = localDateTime3.toLocalTime();
		LunchStartTime = LocalDateTime.parse(date4, format).toLocalTime();
		LunchEndTime = LocalDateTime.parse(date5, format).toLocalTime();
		AuthUserDetails applicationUser = Mockito.mock(AuthUserDetails.class);
		Authentication authentication = Mockito.mock(Authentication.class);
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(applicationUser);
		Mockito.when(applicationUser.getUserId()).thenReturn("test-user");
		Mockito.when(userDetailsService.getOrCreateInternalUserId("test-user"))
				.thenReturn("00000000-0000-0000-0000-000000000001");
		centerDto.setId("10001");
		centerDto.setLangCode("eng");
		centerDto.setCenterStartTime(startTime);
		centerDto.setCenterEndTime(endTime);
		centerDto.setPerKioskProcessTime(perKioskTime);
		centerDto.setLunchStartTime(LunchStartTime);
		centerDto.setLunchEndTime(LunchEndTime);
		centerDto.setNumberOfKiosks((short) 4);
		centerList.add(centerDto);
		regCenDto.setRegistrationCenters(centerList);
	}

	@Test
	public void mandatoryParameterCheckTest() {
		String preRegistrationId = "23587986034785";
		BookingRequestDTO bookingRequestDTO = new BookingRequestDTO();
		bookingRequestDTO.setRegistrationCenterId("1");
		bookingRequestDTO.setSlotFromTime("09:00");
		bookingRequestDTO.setSlotToTime("09:13");
		bookingRequestDTO.setRegDate("2018-12-06");

		boolean flag = serviceUtil.mandatoryParameterCheck(preRegistrationId, bookingRequestDTO);
		assertEquals(true, flag);
	}

	@Test(expected = BookingPreIdNotFoundException.class)
	public void regIdNullCheckTest() {
		String preRegistrationId = null;
		BookingRequestDTO bookingRequestDTO = new BookingRequestDTO();
		bookingRequestDTO.setRegistrationCenterId("1");
		bookingRequestDTO.setSlotFromTime("09:00");
		bookingRequestDTO.setSlotToTime("09:13");
		bookingRequestDTO.setRegDate("2018-12-06");

		serviceUtil.mandatoryParameterCheck(preRegistrationId, bookingRequestDTO);

	}

	@Test(expected = BookingRegistrationCenterIdNotFoundException.class)
	public void regCenterNullinOldBookingCheckTest() {
		String preRegistrationId = "23587986034785";
		BookingRequestDTO bookingRequestDTO = new BookingRequestDTO();
		bookingRequestDTO.setRegistrationCenterId(null);
		bookingRequestDTO.setSlotFromTime("09:00");
		bookingRequestDTO.setSlotToTime("09:13");
		bookingRequestDTO.setRegDate("2018-12-06");

		serviceUtil.mandatoryParameterCheck(preRegistrationId, bookingRequestDTO);

	}

	@Test(expected = BookingTimeSlotNotSeletectedException.class)
	public void slotNullinOldBookingCheckTest() {
		String preRegistrationId = "23587986034785";
		BookingRequestDTO bookingRequestDTO = new BookingRequestDTO();
		bookingRequestDTO.setRegistrationCenterId("1");
		bookingRequestDTO.setSlotFromTime(null);
		bookingRequestDTO.setSlotToTime(null);
		bookingRequestDTO.setRegDate("2018-12-06");

		serviceUtil.mandatoryParameterCheck(preRegistrationId, bookingRequestDTO);

	}

	@Test(expected = BookingDateNotSeletectedException.class)
	public void regDateNullinOldBookingCheckTest() {
		String preRegistrationId = "23587986034785";
		BookingRequestDTO bookingRequestDTO = new BookingRequestDTO();
		bookingRequestDTO.setRegistrationCenterId("1");
		bookingRequestDTO.setSlotFromTime("09:00");
		bookingRequestDTO.setSlotToTime("09:13");
		bookingRequestDTO.setRegDate(null);

		serviceUtil.mandatoryParameterCheck(preRegistrationId, bookingRequestDTO);

	}

	@Test(expected = BookingRegistrationCenterIdNotFoundException.class)
	public void regCenterNullinNewBookingCheckTest() {
		String preRegistrationId = "23587986034785";

		BookingRequestDTO bookingRequestDTO = new BookingRequestDTO();
		bookingRequestDTO.setRegistrationCenterId(null);
		bookingRequestDTO.setSlotFromTime("09:00");
		bookingRequestDTO.setSlotToTime("09:13");
		bookingRequestDTO.setRegDate("2018-12-06");

		serviceUtil.mandatoryParameterCheck(preRegistrationId, bookingRequestDTO);

	}

	@Test(expected = BookingTimeSlotNotSeletectedException.class)
	public void slotNullinNewBookingCheckTest() {
		String preRegistrationId = "23587986034785";
		BookingRequestDTO bookingRequestDTO = new BookingRequestDTO();
		bookingRequestDTO.setRegistrationCenterId("10");
		bookingRequestDTO.setSlotFromTime(null);
		bookingRequestDTO.setSlotToTime(null);
		bookingRequestDTO.setRegDate("2018-12-06");

		serviceUtil.mandatoryParameterCheck(preRegistrationId, bookingRequestDTO);

	}

	@Test(expected = BookingDateNotSeletectedException.class)
	public void bookingDateNotSeletectedExceptionTest() {
		String preRegistrationId = "23587986034785";
		BookingRequestDTO bookingRequestDTO = new BookingRequestDTO();
		bookingRequestDTO.setRegistrationCenterId("10");
		bookingRequestDTO.setSlotFromTime("09:00");
		bookingRequestDTO.setSlotToTime("09:13");
		bookingRequestDTO.setRegDate(null);

		serviceUtil.mandatoryParameterCheck(preRegistrationId, bookingRequestDTO);

	}

	@Test(expected = AppointmentReBookingFailedException.class)
	public void appointmentReBookingFailedExceptionTest() {
		BookingRegistrationDTO oldBooking = new BookingRegistrationDTO();
		oldBooking.setRegistrationCenterId("1");
		oldBooking.setSlotFromTime("09:00");
		oldBooking.setSlotToTime("09:13");
		oldBooking.setRegDate("2018-12-06");
		BookingRegistrationDTO newBooking = new BookingRegistrationDTO();
		newBooking.setRegistrationCenterId("1");
		newBooking.setSlotFromTime("09:00");
		newBooking.setSlotToTime("09:13");
		newBooking.setRegDate("2018-12-06");

		serviceUtil.isNotDuplicate(oldBooking, newBooking);

	}

	@Test
	public void cancelBookingDtoSetterTest() {
		String preRegistrationId = "23587986034785";
		BookingRegistrationDTO bookingRegistrationDTO = new BookingRegistrationDTO();
		bookingRegistrationDTO.setRegistrationCenterId("1");
		bookingRegistrationDTO.setSlotFromTime("09:00");
		bookingRegistrationDTO.setSlotToTime("09:13");
		bookingRegistrationDTO.setRegDate("2018-12-06");

		CancelBookingDTO cancelBookingDTO = serviceUtil.cancelBookingDtoSetter(preRegistrationId,
				bookingRegistrationDTO);
		assertEquals("1", bookingRegistrationDTO.getRegistrationCenterId());
	}

	@Test
	public void validateAppointmentDateTest() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, 1);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Map<String, String> dateMap = new HashMap<>();
		dateMap.put(RequestCodes.REG_DATE.getCode(), sdf.format(cal.getTime()));
		dateMap.put(RequestCodes.FROM_SLOT_TIME.getCode(), LocalTime.now().toString());
		boolean isValid = serviceUtil.validateAppointmentDate(dateMap);
		assertEquals(Boolean.TRUE, isValid);
	}

	@Ignore
	@Test(expected = InvalidDateTimeFormatException.class)
	public void validateAppointmentDateExceptionTest() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Map<String, String> dateMap = new HashMap<>();
		dateMap.put(RequestCodes.REG_DATE.getCode(), sdf.format(cal.getTime()));
		dateMap.put(RequestCodes.FROM_SLOT_TIME.getCode(), LocalTime.now().toString());
		boolean isValid = serviceUtil.validateAppointmentDate(dateMap);
		assertEquals(Boolean.FALSE, isValid);
	}

	@Test(expected = InvalidDateTimeFormatException.class)
	@Ignore
	public void validateAppointmentTimeExceptionTest() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Map<String, String> dateMap = new HashMap<>();
		dateMap.put(RequestCodes.REG_DATE.getCode(), sdf.format(cal.getTime()));
		dateMap.put(RequestCodes.FROM_SLOT_TIME.getCode(), LocalTime.now().minusMinutes(120).toString());
		boolean isValid = serviceUtil.validateAppointmentDate(dateMap);
		assertEquals(Boolean.FALSE, isValid);
	}

	@Test(expected = AvailablityNotFoundException.class)
	public void AvailablityNotFoundException() {
		AvailibityEntity availableEntity = new AvailibityEntity();
		availableEntity.setAvailableKiosks(0);
		availableEntity.setRegcntrId("1");
		availableEntity.setRegDate(LocalDate.parse("2018-12-04"));
		availableEntity.setCrBy("987654321");
		availableEntity.setCrDate(DateUtils2.parseDateToLocalDateTime(new Date()));
		availableEntity.setDeleted(false);
		serviceUtil.isKiosksAvailable(availableEntity);
	}

	@Test(expected = BookingPreIdNotFoundException.class)
	public void bookingPreIdNotFoundExceptionforCancelTest() {

		CancelBookingDTO cancelBookingDTO = new CancelBookingDTO();
		serviceUtil.mandatoryParameterCheckforCancel(null);

	}

	/*
	 * @Test(expected=BookingRegistrationCenterIdNotFoundException.class) public
	 * void bookingRegistrationCenterIdNotFoundExceptionforCancelTest() {
	 * 
	 * CancelBookingDTO cancelBookingDTO = new CancelBookingDTO();
	 * cancelBookingDTO.setRegistrationCenterId(null);
	 * serviceUtil.mandatoryParameterCheckforCancel("23587986034785");
	 * 
	 * }
	 */

	/*
	 * @Test(expected=BookingDateNotSeletectedException.class) public void
	 * bookingDateNotSeletectedExceptionforCancelTest() {
	 * 
	 * CancelBookingDTO cancelBookingDTO = new CancelBookingDTO();
	 * cancelBookingDTO.setRegistrationCenterId("1");
	 * cancelBookingDTO.setSlotFromTime("09:00");
	 * cancelBookingDTO.setSlotToTime("09:13"); cancelBookingDTO.setRegDate(null);
	 * serviceUtil.mandatoryParameterCheckforCancel("23587986034785");
	 * 
	 * }
	 */

	/*
	 * @Test(expected=BookingTimeSlotNotSeletectedException.class) public void
	 * bookingTimeSlotNotSeletectedExceptionforCancelTest() {
	 * 
	 * CancelBookingDTO cancelBookingDTO = new CancelBookingDTO();
	 * cancelBookingDTO.setRegistrationCenterId("1");
	 * cancelBookingDTO.setRegDate("2018-12-06");
	 * cancelBookingDTO.setSlotFromTime(null); cancelBookingDTO.setSlotToTime(null);
	 * serviceUtil.mandatoryParameterCheckforCancel("23587986034785");
	 * 
	 * }
	 */

	@Test(expected = MasterDataNotAvailableException.class)
	public void callRegCenterDateRestServiceTest() {

		RegistrationCenterResponseDto preRegResponse = new RegistrationCenterResponseDto();
		preRegResponse.setRegistrationCenters(null);
		ResponseWrapper<RegistrationCenterResponseDto> resp = new ResponseWrapper<>();
		resp.setResponse(preRegResponse);
		ResponseEntity<ResponseWrapper<RegistrationCenterResponseDto>> res = new ResponseEntity<>(resp, HttpStatus.OK);
		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.eq(HttpMethod.GET), Mockito.any(),
				Mockito.eq(new ParameterizedTypeReference<ResponseWrapper<RegistrationCenterResponseDto>>() {
				}))).thenReturn(res);

		serviceUtil.getRegCenterMasterData("10001");

	}

	@Test
	public void getRegCenterMasterDataPropagatesKerMsd215() {
		ResponseWrapper<RegistrationCenterResponseDto> resp = new ResponseWrapper<>();
		resp.setResponse(null);
		resp.setErrors(List.of(new io.mosip.kernel.core.exception.ServiceError("KER-MSD-215",
				"Registration Center not found")));
		ResponseEntity<ResponseWrapper<RegistrationCenterResponseDto>> res = new ResponseEntity<>(resp, HttpStatus.OK);
		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.eq(HttpMethod.GET), Mockito.any(),
				Mockito.eq(new ParameterizedTypeReference<ResponseWrapper<RegistrationCenterResponseDto>>() {
				}))).thenReturn(res);
		try {
			serviceUtil.getRegCenterMasterData("@#@$W#");
			org.junit.Assert.fail("expected MasterDataNotAvailableException");
		} catch (MasterDataNotAvailableException ex) {
			assertEquals("KER-MSD-215", ex.getErrorCode());
		}
		org.mockito.ArgumentCaptor<String> uriCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
		Mockito.verify(restTemplate).exchange(uriCaptor.capture(), Mockito.eq(HttpMethod.GET), Mockito.any(),
				Mockito.eq(new ParameterizedTypeReference<ResponseWrapper<RegistrationCenterResponseDto>>() {
				}));
		String uri = uriCaptor.getValue();
		org.junit.Assert.assertFalse("center id '#' must not be treated as a URI fragment", uri.contains("#"));
		org.junit.Assert.assertTrue(uri.contains("%23"));
		org.junit.Assert.assertTrue(uri.endsWith("/all"));
	}

	@Test
	public void getRegCenterMasterDataHttpErrorPropagatesKerMsd215() {
		String body = "{\"errors\":[{\"errorCode\":\"KER-MSD-215\",\"message\":\"Registration Center not found\"}]}";
		HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found",
				HttpHeaders.EMPTY, body.getBytes(java.nio.charset.StandardCharsets.UTF_8),
				java.nio.charset.StandardCharsets.UTF_8);
		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.eq(HttpMethod.GET), Mockito.any(),
				Mockito.eq(new ParameterizedTypeReference<ResponseWrapper<RegistrationCenterResponseDto>>() {
				}))).thenThrow(ex);
		try {
			serviceUtil.getRegCenterMasterData("@#@$W#");
			org.junit.Assert.fail("expected MasterDataNotAvailableException");
		} catch (MasterDataNotAvailableException thrown) {
			assertEquals("KER-MSD-215", thrown.getErrorCode());
		}
	}

	@Ignore
	@Test(expected = RestCallException.class)
	public void HttpClientErrorExceptionTest() {
		HttpClientErrorException ex = new HttpClientErrorException(HttpStatus.OK);
		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.eq(HttpMethod.GET), Mockito.any(),
				Mockito.eq(new ParameterizedTypeReference<ResponseWrapper<RegistrationCenterResponseDto>>() {
				}))).thenThrow(ex);

		serviceUtil.getRegCenterMasterData("10001");

	}

	/*
	 * @Test(expected = DemographicStatusUpdationException.class) public void
	 * callUpdateStatusRestServiceTest() {
	 * 
	 * Map<String, Object> map = new HashMap<>(); map.put("pre_rgistration_id",
	 * "23587986034785");
	 * 
	 * RestClientException ex = new RestClientException(null);
	 * MainResponseDTO<String> preRegResponse = new MainResponseDTO<>();
	 * preRegResponse.setResponse(null); ExceptionJSONInfoDTO err = new
	 * ExceptionJSONInfoDTO(); List<ExceptionJSONInfoDTO> errList = new
	 * ArrayList<>(); err.setErrorCode(ErrorCodes.PRG_BOOK_RCI_011.name());
	 * err.setMessage(ErrorMessages.DEMOGRAPHIC_STATUS_UPDATION_FAILED.getMessage())
	 * ; errList.add(err); preRegResponse.setErrors(errList);
	 * preRegResponse.setResponsetime(serviceUtil.getCurrentResponseTime());
	 * MainResponseDTO<String> updatePreRegistrationStatus = new
	 * MainResponseDTO<>(); updatePreRegistrationStatus.setErrors(errList);
	 * Mockito.when(serviceUtil.updatePreRegistrationStatus("23587986034785",
	 * "Pending_Appointment")) .thenReturn(updatePreRegistrationStatus);
	 * serviceUtil.updateDemographicStatus("23587986034785", "Pending_Appointment");
	 * 
	 * }
	 * 
	 * @SuppressWarnings("unchecked")
	 * 
	 * @Test(expected = BookingDataNotFoundException.class) public void
	 * callGetStatusRestServiceforCancelTest() {
	 * 
	 * List<PreRegistartionStatusDTO> statusList = new ArrayList<>();
	 * PreRegistartionStatusDTO preRegistartionStatusDTO = new
	 * PreRegistartionStatusDTO();
	 * 
	 * @SuppressWarnings("rawtypes") MainResponseDTO preRegResponse = new
	 * MainResponseDTO();
	 * preRegistartionStatusDTO.setStatusCode(StatusCodes.PENDING_APPOINTMENT.
	 * getCode()); preRegistartionStatusDTO.setPreRegistartionId("23587986034785");
	 * statusList.add(preRegistartionStatusDTO);
	 * 
	 * preRegResponse.setResponse(preRegistartionStatusDTO);
	 * preRegResponse.setErrors(null);
	 * preRegResponse.setResponsetime(serviceUtil.getCurrentResponseTime());
	 * Mockito.when(serviceUtil.getApplicationStatus("23587986034785")).thenReturn(
	 * preRegResponse); serviceUtil.getDemographicStatusForCancel("23587986034785");
	 * 
	 * }
	 */
	/*
	 * @Test(expected = DemographicGetStatusException.class) public void
	 * callGetStatusRestServiceforCancel1Test() {
	 * 
	 * @SuppressWarnings("rawtypes") MainResponseDTO preRegResponse = new
	 * MainResponseDTO(); ExceptionJSONInfoDTO err = new ExceptionJSONInfoDTO();
	 * err.setErrorCode(ErrorCodes.PRG_BOOK_RCI_011.name());
	 * err.setMessage(ErrorMessages.DEMOGRAPHIC_STATUS_UPDATION_FAILED.getMessage())
	 * ; List<ExceptionJSONInfoDTO> list = new ArrayList<>(); list.add(err);
	 * preRegResponse.setErrors(list);
	 * preRegResponse.setResponsetime(serviceUtil.getCurrentResponseTime());
	 * 
	 * @SuppressWarnings("unchecked")
	 * ResponseEntity<MainResponseDTO<PreRegistartionStatusDTO>> res = new
	 * ResponseEntity<>(preRegResponse, HttpStatus.OK);
	 * Mockito.when(restTemplate.exchange(Mockito.anyString(),
	 * Mockito.eq(HttpMethod.GET), Mockito.any(), Mockito.eq(new
	 * ParameterizedTypeReference<MainResponseDTO<PreRegistartionStatusDTO>>() { }),
	 * Mockito.anyMap())).thenReturn(res);
	 * serviceUtil.getDemographicStatusForCancel("23587986034785");
	 * 
	 * }
	 */

	/*
	 * @Test(expected = DemographicGetStatusException.class) public void
	 * callGetStatusRestService1Test() { MainResponseDTO<PreRegistartionStatusDTO>
	 * getApplicationStatus = new MainResponseDTO<>(); List<ExceptionJSONInfoDTO> ex
	 * = new ArrayList<ExceptionJSONInfoDTO>(); ExceptionJSONInfoDTO err = new
	 * ExceptionJSONInfoDTO(); err.setErrorCode(ErrorCodes.PRG_BOOK_RCI_011.name());
	 * err.setMessage(ErrorMessages.DEMOGRAPHIC_STATUS_UPDATION_FAILED.getMessage())
	 * ; ex.add(err); getApplicationStatus.setErrors(ex);
	 * Mockito.when(serviceUtil.getApplicationStatus("23587986034785")).thenReturn(
	 * getApplicationStatus); serviceUtil.getDemographicStatus("23587986034785");
	 * 
	 * }
	 */

	@Test
	public void isValidRegCenterTest() {
		ResponseWrapper<RegistrationCenterResponseDto> resp = new ResponseWrapper<>();
		resp.setResponse(regCenDto);
		ResponseEntity<ResponseWrapper<RegistrationCenterResponseDto>> res = new ResponseEntity<>(resp, HttpStatus.OK);
		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.eq(HttpMethod.GET), Mockito.any(),
				Mockito.eq(new ParameterizedTypeReference<ResponseWrapper<RegistrationCenterResponseDto>>() {
				}))).thenReturn(res);
		serviceUtil.isValidRegCenter("10001");
	}

	@Test(expected = RecordNotFoundException.class)
	public void isValidRegCenterTestException() {
		ResponseWrapper<RegistrationCenterResponseDto> resp = new ResponseWrapper<>();
		regCenDto.getRegistrationCenters().get(0).setId("1");
		resp.setResponse(regCenDto);
		ResponseEntity<ResponseWrapper<RegistrationCenterResponseDto>> res = new ResponseEntity<>(resp, HttpStatus.OK);
		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.eq(HttpMethod.GET), Mockito.any(),
				Mockito.eq(new ParameterizedTypeReference<ResponseWrapper<RegistrationCenterResponseDto>>() {
				}))).thenReturn(res);
		serviceUtil.isValidRegCenter("10001");
	}

	@Test
	public void validateFromDateAndToDateTest() {
		String format = "yyyy-MM-dd";
		String todate = "2019-11-15";
		String fromdate = "2019-11-05";
		serviceUtil.validateFromDateAndToDate(fromdate, todate, format);
	}

	@Test(expected = InvalidDateTimeFormatException.class)
	public void validateFromDateAndToDateTestEx1() {
		String format = "yyyy-MM-dd";
		String todate = "2019-11-15";
		String fromdate = new Date().toString();
		serviceUtil.validateFromDateAndToDate(fromdate, todate, format);
	}

	@Test(expected = InvalidDateTimeFormatException.class)
	public void validateFromDateAndToDateTestEx2() {
		String format = "yyyy-MM-dd";
		String todate = "2019-11-15";
		String fromdate = "";
		serviceUtil.validateFromDateAndToDate(fromdate, todate, format);
	}

	@Test(expected = InvalidDateTimeFormatException.class)
	public void validateFromDateAndToDateTestEx3() {
		String format = "yyyy-MM-dd";
		String todate = "2019-11-05";
		String fromdate = "2019-11-15";
		serviceUtil.validateFromDateAndToDate(fromdate, todate, format);
	}

	@Test(expected = NullPointerException.class)
	public void emailNotificationTest() throws JsonProcessingException {
		NotificationDTO dto = new NotificationDTO();
		dto.setEmailID("rajath@gmail.com");
		dto.setMobNum("9480548558");
		dto.setAdditionalRecipient(false);
		MultiValueMap<Object, Object> emailMap = new LinkedMultiValueMap<>();
		emailMap.add("NotificationRequestDTO", mapper.writeValueAsString(dto));
		emailMap.add("langCode", "eng");
		HttpHeaders headers = new HttpHeaders();
		HttpEntity<MultiValueMap<Object, Object>> httpEntity = new HttpEntity<>(emailMap, headers);
		ResponseEntity<String> res = new ResponseEntity<>("Success", HttpStatus.OK);
		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.eq(HttpMethod.POST), Mockito.any(),
				Mockito.eq(new ParameterizedTypeReference<String>() {
				}))).thenReturn(res);
		serviceUtil.emailNotification(dto, "eng");
	}

	@Test
	public void bookingEntitySetterTest() {
		BookingRequestDTO bookingRequestDTO = new BookingRequestDTO();
		bookingRequestDTO.setRegistrationCenterId("1");
		bookingRequestDTO.setSlotFromTime("09:00");
		bookingRequestDTO.setSlotToTime("09:13");
		bookingRequestDTO.setRegDate("2018-12-06");
		serviceUtil.bookingEntitySetter("1234568687844744", bookingRequestDTO);
	}

	@Test
	public void bookingEntitySetterLegacyCrByTest() {
		BookingRequestDTO bookingRequestDTO = new BookingRequestDTO();
		bookingRequestDTO.setRegistrationCenterId("1");
		bookingRequestDTO.setSlotFromTime("09:00");
		bookingRequestDTO.setSlotToTime("09:13");
		bookingRequestDTO.setRegDate("2018-12-06");
		RegistrationBookingEntity entity = serviceUtil.bookingEntitySetter("1234568687844744", bookingRequestDTO);
		assertEquals("00000000-0000-0000-0000-000000000001", entity.getCrBy());
	}

	@Test(expected = AppointmentBookingFailedException.class)
	public void bookingEntitySetterThrowsOnUuidResolutionFailureTest() {
		Mockito.when(userDetailsService.getOrCreateInternalUserId("test-user"))
				.thenThrow(new UserLookupException("PRG_CORE_REQ_024", "Failed to resolve internal user ID"));
		BookingRequestDTO bookingRequestDTO = new BookingRequestDTO();
		bookingRequestDTO.setRegistrationCenterId("1");
		bookingRequestDTO.setSlotFromTime("09:00");
		bookingRequestDTO.setSlotToTime("09:13");
		bookingRequestDTO.setRegDate("2018-12-06");
		serviceUtil.bookingEntitySetter("1234568687844744", bookingRequestDTO);
	}

	/**
	 * An absent authenticated user id must fail as an identity error rather than
	 * flowing through as a null crBy and tripping the NOT NULL constraint, which
	 * would surface to the caller as an opaque "table not accessible" error.
	 */
	@Test(expected = AppointmentBookingFailedException.class)
	public void bookingEntitySetter_authenticatedUserIdAbsent_throwsAppointmentBookingFailed() {
		AuthUserDetails principal = (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		Mockito.when(principal.getUserId()).thenReturn(null);
		BookingRequestDTO bookingRequestDTO = new BookingRequestDTO();
		bookingRequestDTO.setRegistrationCenterId("1");
		bookingRequestDTO.setSlotFromTime("09:00");
		bookingRequestDTO.setSlotToTime("09:13");
		bookingRequestDTO.setRegDate("2018-12-06");
		serviceUtil.bookingEntitySetter("1234568687844744", bookingRequestDTO);
	}

	@Test
	public void prepareRequestMapTest() {
		MainRequestDTO<String> requestDto = new MainRequestDTO<>();
		requestDto.setId("mosip.io.booking");
		requestDto.setVersion("1.0");
		requestDto.setRequest("98463784586348");
		Date date = new Date();
		requestDto.setRequesttime(date);
		serviceUtil.prepareRequestMap(requestDto);
	}

	@Test
	public void slotSetter() {
		List<DateTimeDto> dateList = new ArrayList<>();
		SlotDto slots = new SlotDto();
		DateTimeDto dateDto = new DateTimeDto();
		List<SlotDto> slotsList = new ArrayList<>();
		List<AvailibityEntity> availabilityList = new ArrayList<>();
		AvailibityEntity availableEntity = new AvailibityEntity();
		String date1 = "2016-11-09 09:00:00";
		String date2 = "2016-11-09 09:20:00";
		LocalDateTime localDateTime1 = LocalDateTime.parse(date1, format);
		LocalDateTime localDateTime2 = LocalDateTime.parse(date2, format);
		localTime1 = localDateTime1.toLocalTime();
		localTime2 = localDateTime2.toLocalTime();
		slots.setAvailability(4);
		slots.setFromTime(localTime1);
		slots.setToTime(localTime2);
		slotsList.add(slots);
		dateDto.setDate("2018-12-04");
		dateDto.setHoliday(false);
		dateDto.setTimeSlots(slotsList);
		dateList.add(dateDto);
		availableEntity.setAvailableKiosks(4);
		availableEntity.setRegcntrId("1");
		availableEntity.setRegDate(LocalDate.parse("2019-12-04"));
		availableEntity.setToTime(localTime2);
		availableEntity.setFromTime(localTime1);
		availableEntity.setCrBy("987654321");
		availableEntity.setCrDate(DateUtils2.parseDateToLocalDateTime(new Date()));
		availableEntity.setDeleted(false);
		availabilityList.add(availableEntity);
		serviceUtil.slotSetter(availableEntity.getRegDate(), dateList, dateDto, availabilityList);
	}

	@Test(expected = TimeSpanException.class)
	public void timeSpanCheckForCancel() {
		LocalDateTime time = LocalDateTime.now();
		serviceUtil.timeSpanCheckForCancle(time);
	}

	@Test
	public void timeSpanCheckForCancel1() {
		LocalDateTime time = LocalDateTime.now().plusDays(3);
		serviceUtil.timeSpanCheckForCancle(time);
	}

	@Test(expected = TimeSpanException.class)
	public void timeSpanCheckForRebook() {
		LocalDateTime time = LocalDateTime.now();
		serviceUtil.timeSpanCheckForRebook(time, new Date());
	}

	@Test
	public void timeSpanCheckForRebook1() {
		LocalDateTime time = LocalDateTime.now().plusDays(3);
		serviceUtil.timeSpanCheckForRebook(time, new Date());
	}

	@Test
	public void assertAppointmentOwnerSkipsWhenNotIndividual() {
		serviceUtil.assertAppointmentOwner("23587986034785");
		Mockito.verify(applicationRepostiory, Mockito.never()).findByApplicationId(Mockito.anyString());
	}

	@Test
	public void assertAppointmentOwnerAllowsMatchingIndividual() {
		AuthUserDetails user = (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Mockito.doReturn(List.of(new SimpleGrantedAuthority("ROLE_INDIVIDUAL"))).when(user).getAuthorities();
		ApplicationEntity applicationEntity = new ApplicationEntity();
		applicationEntity.setApplicationId("23587986034785");
		applicationEntity.setCrBy("00000000-0000-0000-0000-000000000001");
		Mockito.when(applicationRepostiory.findByApplicationId("23587986034785")).thenReturn(applicationEntity);
		Mockito.when(userDetailsService.matchesUser(Mockito.eq("test-user"), Mockito.anyString(), Mockito.eq(true)))
				.thenReturn(true);

		serviceUtil.assertAppointmentOwner("23587986034785");
	}

	@Test
	public void assertAppointmentOwnerUnknownApplicationReturnsApp013() {
		AuthUserDetails user = (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Mockito.doReturn(List.of(new SimpleGrantedAuthority("ROLE_INDIVIDUAL"))).when(user).getAuthorities();
		Mockito.when(applicationRepostiory.findByApplicationId("ggfr3567ugg")).thenReturn(null);

		try {
			serviceUtil.assertAppointmentOwner("ggfr3567ugg");
			throw new AssertionError("expected RecordNotFoundException");
		} catch (RecordNotFoundException ex) {
			assertEquals("PRG_APP_013", ex.getErrorCode());
		}
	}

	@Test(expected = PreIdInvalidForUserIdException.class)
	public void assertAppointmentOwnerRejectsMismatchedIndividual() {
		AuthUserDetails user = (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Mockito.doReturn(List.of(new SimpleGrantedAuthority("ROLE_INDIVIDUAL"))).when(user).getAuthorities();
		ApplicationEntity applicationEntity = new ApplicationEntity();
		applicationEntity.setApplicationId("23587986034785");
		applicationEntity.setCrBy("someone-else");
		Mockito.when(applicationRepostiory.findByApplicationId("23587986034785")).thenReturn(applicationEntity);
		Mockito.when(userDetailsService.matchesUser(Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean()))
				.thenReturn(false);

		serviceUtil.assertAppointmentOwner("23587986034785");
	}

}


