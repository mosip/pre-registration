package io.mosip.preregistration.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.HttpServerErrorException;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.preregistration.application.dto.ApplicationRequestDTO;
import io.mosip.preregistration.application.dto.ApplicationResponseDTO;
import io.mosip.preregistration.application.exception.DemographicServiceException;
import io.mosip.preregistration.application.service.util.DemographicServiceUtil;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.entity.ApplicationEntity;
import io.mosip.preregistration.core.util.AuditLogUtil;
import io.mosip.preregistration.core.util.ValidationUtil;


@RunWith(JUnit4.class)
@SpringBootTest
@ContextConfiguration(classes = { ApplicationService.class })
public class ApplicationServiceTest {

	@InjectMocks
	private ApplicationService applicationService;

	@Mock
	AuditLogUtil auditUtil;

	@Mock
	private DemographicServiceUtil serviceUtil;

	@Mock
	ApplicationResponseDTO  applicationResponseDTO;

	@Mock
	ValidationUtil validationUtil;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);	
		AuthUserDetails applicationUser = Mockito.mock(AuthUserDetails.class);
		Authentication authentication = Mockito.mock(Authentication.class);
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);

		Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(applicationUser);

	}

	@Test
	public void testAddLostOrUpdateApplicationSuccess() {
		MainRequestDTO<ApplicationRequestDTO> request = new MainRequestDTO<ApplicationRequestDTO>();
		ApplicationRequestDTO applicationRequestDTO = new  ApplicationRequestDTO();
		applicationRequestDTO.setLangCode("eng");
		request.setRequest(applicationRequestDTO );
		String bookingType = null;
		ApplicationEntity applicationEntity=new ApplicationEntity();
		LocalDate localDate=LocalDate.now();
		LocalDateTime localDateTime=LocalDateTime.now();
		LocalTime localTime=LocalTime.now();
		applicationEntity.setApplicationId("");
		applicationEntity.setApplicationStatusCode("");
		applicationEntity.setAppointmentDate(localDate);
		applicationEntity.setBookingDate(localDate);
		applicationEntity.setBookingStatusCode("");
		applicationEntity.setBookingType("");
		applicationEntity.setContactInfo("");
		applicationEntity.setCrBy("");
		applicationEntity.setCrDtime(localDateTime);
		applicationEntity.setRegistrationCenterId("");

		applicationEntity.setSlotFromTime(localTime);
		applicationEntity.setSlotToTime(localTime);
		applicationEntity.setUpdBy("");
		applicationEntity.setUpdDtime(localDateTime);

		Mockito.when(serviceUtil.saveAndUpdateApplicationEntity(Mockito.any(), Mockito.any(),
				Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(applicationEntity);

		MainResponseDTO<ApplicationResponseDTO> mainResponseDTO = new MainResponseDTO<ApplicationResponseDTO>();


		applicationResponseDTO = new ApplicationResponseDTO();
		applicationResponseDTO.setApplicationStatusCode("success");
		mainResponseDTO.setResponse(applicationResponseDTO);

		Mockito.when((MainResponseDTO<ApplicationResponseDTO>) serviceUtil.getMainResponseDto(request)).thenReturn(mainResponseDTO);
		MainResponseDTO<ApplicationResponseDTO> response = applicationService.addLostOrUpdateApplication(request, bookingType);
		Assert.assertEquals(response.getResponse().getApplicationId(), applicationEntity.getApplicationId());

	}

	@Test(expected = DemographicServiceException.class)
	public void testAddLostOrUpdateHttpServerErrorException() {

		MainRequestDTO<ApplicationRequestDTO> request = new MainRequestDTO<ApplicationRequestDTO>();
		Mockito.when((MainResponseDTO<ApplicationResponseDTO>) serviceUtil.getMainResponseDto(request)).thenThrow(new HttpServerErrorException(HttpStatus.ACCEPTED));
		String bookingType=null;
		applicationService.addLostOrUpdateApplication(request, bookingType);
	}
}
