package io.mosip.preregistration.application.service;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.junit.Assert;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import io.mosip.analytics.event.anonymous.util.AnonymousProfileUtil;
import io.mosip.preregistration.application.repository.ApplicationRepostiory;
import io.mosip.preregistration.application.service.util.AppointmentUtil;
import io.mosip.preregistration.booking.dto.AvailabilityDto;
import io.mosip.preregistration.booking.dto.DateTimeDto;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import static org.mockito.ArgumentMatchers.any;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.junit.runners.JUnit4;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.MockitoAnnotations;

@RunWith(JUnit4.class)
@ImportAutoConfiguration(RefreshAutoConfiguration.class)
@ContextConfiguration(classes = { AppointmentServiceImpl.class })
public class AppointmentServiceImplTest {

	@InjectMocks
	AppointmentServiceImpl appointmentServiceImpl;

	@Mock
	private AppointmentUtil appointmentUtils;

	@Mock
	private DemographicService demographicService;
	
	@Mock
	private DocumentService documentService;
	
	/**
	 * Autowired reference for {@link #AnonymousProfileUtil}
	 */
	@Mock
	AnonymousProfileUtil anonymousProfileUtil;

	@Value("${version}")
	private String version;

	@Value("${mosip.utc-datetime-pattern:yyyy-MM-dd'T'hh:mm:ss.SSS'Z'}")
	private String mosipDateTimeFormat;

	@Value("${mosip.preregistration.booking.fetch.availability.id}")
	private String availablityFetchId;

	@Value("${mosip.preregistration.booking.fetch.booking.id}")
	private String appointmentDetailsFetchId;

	@Value("${mosip.preregistration.booking.book.id}")
	private String appointmentBookId;

	@Value("${mosip.preregistration.booking.cancel.id}")
	private String appointmentCancelId;

	@Value("${mosip.preregistration.booking.delete.id}")
	private String appointmentDeletelId;

	@Mock
	private ApplicationRepostiory applicationRepostiory;


	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		ReflectionTestUtils.setField(appointmentServiceImpl, "mosipDateTimeFormat", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		
	}
	
	@Test
	public void getSlotAvailablityTest() {

		String regCenterId = "10001";

		MainResponseDTO<AvailabilityDto> response = new MainResponseDTO<AvailabilityDto>();
		response.setResponsetime(LocalDateTime.now().toString());
		response.setId("");
		response.setVersion("1.0");
		AvailabilityDto availabilityDto = new AvailabilityDto();
		availabilityDto.setRegCenterId("10001");

		List<DateTimeDto> list = new ArrayList<>();
		DateTimeDto dtd = new DateTimeDto();
		dtd.setDate(LocalDate.now().toString());
		dtd.setHoliday(false);
		dtd.setTimeSlots(null);

		availabilityDto.setCenterDetails(list);

		response.setResponse(availabilityDto);

		Mockito.when(appointmentUtils.getSlotAvailablityByRegCenterId(any())).thenReturn(availabilityDto);
		MainResponseDTO<AvailabilityDto> obj=appointmentServiceImpl.getSlotAvailablity(regCenterId);
		Assert.assertEquals(obj.getResponse(), availabilityDto);
	}
}
