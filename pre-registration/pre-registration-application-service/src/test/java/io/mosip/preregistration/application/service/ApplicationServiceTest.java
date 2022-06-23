package io.mosip.preregistration.application.service;

import static org.junit.Assert.assertNotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpServerErrorException;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.preregistration.application.dto.ApplicationDetailResponseDTO;
import io.mosip.preregistration.application.dto.ApplicationRequestDTO;
import io.mosip.preregistration.application.dto.ApplicationResponseDTO;
import io.mosip.preregistration.application.dto.ApplicationsListDTO;
import io.mosip.preregistration.application.dto.UIAuditRequest;
import io.mosip.preregistration.application.errorcodes.ApplicationErrorCodes;
import io.mosip.preregistration.application.errorcodes.ApplicationErrorMessages;
import io.mosip.preregistration.application.exception.AuditFailedException;
import io.mosip.preregistration.application.exception.DemographicServiceException;
import io.mosip.preregistration.application.exception.InvalidDateFormatException;
import io.mosip.preregistration.application.exception.RecordNotFoundException;
import io.mosip.preregistration.application.repository.ApplicationRepostiory;
import io.mosip.preregistration.application.service.util.DemographicServiceUtil;
import io.mosip.preregistration.core.code.BookingTypeCodes;
import io.mosip.preregistration.core.code.StatusCodes;
import io.mosip.preregistration.core.common.dto.DemographicResponseDTO;
import io.mosip.preregistration.core.common.dto.DocumentsMetaData;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.entity.ApplicationEntity;
import io.mosip.preregistration.core.exception.InvalidRequestParameterException;
import io.mosip.preregistration.core.exception.PreIdInvalidForUserIdException;
import io.mosip.preregistration.core.exception.PreRegistrationException;
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
	ApplicationResponseDTO applicationResponseDTO;

	@Mock
	DemographicResponseDTO demographicResponse;

	@Mock
	ApplicationRepostiory applicationRepository;

	@Mock
	DocumentServiceIntf documentService;

	@Mock
	ValidationUtil validationUtil;

	@Mock
	DemographicServiceIntf demographicService;

	@Mock
	DocumentsMetaData documentsMetaData;

	@Value("${mosip.utc-datetime-pattern:yyyy-MM-dd'T'hh:mm:ss.SSS'Z'}")
	private String mosipDateTimeFormat;

	@Value("${mosip.preregistration.applications.all.get}")
	private String allApplicationsId;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		AuthUserDetails applicationUser = Mockito.mock(AuthUserDetails.class);
		Authentication authentication = Mockito.mock(Authentication.class);
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(applicationUser);
		ReflectionTestUtils.setField(applicationService, "mosipDateTimeFormat", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		ReflectionTestUtils.setField(applicationService, "allApplicationsId", "preReg");
	}

	@Test(expected = AuditFailedException.class)
	public void testsaveUIEventAuditAuditFailedException() {
		UIAuditRequest auditRequest = new UIAuditRequest();
		applicationService.saveUIEventAudit(auditRequest);
	}

	// @Test
	// public void testgetApplicationInfoSuccess() {
	// ApplicationEntity applicationEntity1 = new ApplicationEntity();
	// applicationEntity1.setCrBy("");
	// Mockito.when(applicationRepository.findByApplicationId(Mockito.any())).thenReturn(applicationEntity1);
	// Mockito.when((AuthUserDetails)
	// SecurityContextHolder.getContext().getAuthentication()
	// .getPrincipal().getUserId()).thenReturn(null);
	// Mockito.when(applicationRepository.findBookingStatusCodeById(Mockito.any())).thenReturn("123456789");
	// MainResponseDTO<ApplicationEntity> response =
	// applicationService.getApplicationInfo("987654321");
	// ApplicationEntity applicationEntity = response.getResponse();
	// Assert.assertEquals(applicationEntity.getApplicationStatusCode(),
	// "123456789");
	//
	// }

	@Test
	public void testsaveUIEventAuditSuccess() {
		UIAuditRequest auditRequest = new UIAuditRequest();
		auditRequest.setActionTimeStamp(LocalDateTime.now(ZoneId.of("UTC")).toString());
		auditRequest.setDescription("{\"template\":\"\",\"description\":\"\",\"url\":\"\"}");
		MainResponseDTO<String> response = applicationService.saveUIEventAudit(auditRequest);
		Assert.assertEquals("Audit Logged Successfully", response.getResponse());
	}

	@Test
	public void testAddLostOrUpdateApplicationSuccess() {
		MainRequestDTO<ApplicationRequestDTO> request = new MainRequestDTO<ApplicationRequestDTO>();
		ApplicationRequestDTO applicationRequestDTO = new ApplicationRequestDTO();
		applicationRequestDTO.setLangCode("eng");
		request.setRequest(applicationRequestDTO);
		String bookingType = null;
		ApplicationEntity applicationEntity = new ApplicationEntity();
		LocalDate localDate = LocalDate.now();
		LocalDateTime localDateTime = LocalDateTime.now();
		LocalTime localTime = LocalTime.now();
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
		Mockito.when(serviceUtil.saveAndUpdateApplicationEntity(Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(applicationEntity);
		MainResponseDTO<ApplicationResponseDTO> mainResponseDTO = new MainResponseDTO<ApplicationResponseDTO>();
		applicationResponseDTO = new ApplicationResponseDTO();
		applicationResponseDTO.setApplicationStatusCode("success");
		mainResponseDTO.setResponse(applicationResponseDTO);
		Mockito.when((MainResponseDTO<ApplicationResponseDTO>) serviceUtil.getMainResponseDto(request))
				.thenReturn(mainResponseDTO);
		MainResponseDTO<ApplicationResponseDTO> response = applicationService.addLostOrUpdateApplication(request,
				bookingType);
		Assert.assertEquals(response.getResponse().getApplicationId(), applicationEntity.getApplicationId());
	}

	@Test(expected = DemographicServiceException.class)
	public void testAddLostOrUpdateHttpServerErrorException() {
		MainRequestDTO<ApplicationRequestDTO> request = new MainRequestDTO<ApplicationRequestDTO>();
		Mockito.when((MainResponseDTO<ApplicationResponseDTO>) serviceUtil.getMainResponseDto(request))
				.thenThrow(new HttpServerErrorException(HttpStatus.ACCEPTED));
		String bookingType = null;
		applicationService.addLostOrUpdateApplication(request, bookingType);
	}

	@Test(expected = InvalidDateFormatException.class)
	public void testAddLostOrUpdateHttpServerErrorException2() {
		MainResponseDTO<ApplicationResponseDTO> mainResponseDTO = new MainResponseDTO<ApplicationResponseDTO>();
		MainRequestDTO<ApplicationRequestDTO> request = new MainRequestDTO<ApplicationRequestDTO>();
		Mockito.when((MainResponseDTO<ApplicationResponseDTO>) serviceUtil.getMainResponseDto(request))
				.thenThrow(new InvalidDateFormatException(ApplicationErrorCodes.PRG_APP_013.getCode(),
						ApplicationErrorMessages.INVAILD_REQUEST_ARGUMENT.getMessage(), mainResponseDTO));
		String bookingType = null;
		applicationService.addLostOrUpdateApplication(request, bookingType);
	}

	// @Test(expected = InvalidDateFormatException.class)
	// public void testgetApplicationsForApplicationIdInvalidDateFormatException() {
	// applicationService.getApplicationsForApplicationId("","");
	// }
	//
	// @Test(expected = RecordNotFoundException.class)
	// public void testgetApplicationsForApplicationIdRecordNotFoundException() {
	// Mockito.when(applicationRepository
	// .findByRegistrationCenterIdAndAppointmentDate(Mockito.any(),
	// Mockito.any())).thenReturn(null);
	// applicationService.getApplicationsForApplicationId("",LocalDate.now().toString());
	// }

	// @Test
	// public void testgetApplicationsForApplicationIdSuccess() {
	// List<ApplicationEntity> entity=new ArrayList<ApplicationEntity>();
	// ApplicationEntity applicationEntity=new ApplicationEntity();
	// applicationEntity.setApplicationId("");
	// applicationEntity.setApplicationStatusCode("");
	// applicationEntity.setAppointmentDate(LocalDate.now());
	// applicationEntity.setBookingDate(LocalDate.now());
	// applicationEntity.setBookingStatusCode("");
	// applicationEntity.setBookingType("");
	// applicationEntity.setContactInfo("");
	// applicationEntity.setCrBy("");
	// applicationEntity.setCrDtime(LocalDateTime.now());
	// applicationEntity.setRegistrationCenterId("");
	// applicationEntity.setSlotFromTime(LocalTime.now());
	// applicationEntity.setSlotToTime(LocalTime.now());
	// applicationEntity.setUpdBy("");
	// applicationEntity.setUpdDtime(LocalDateTime.now());
	// entity.add(applicationEntity);
	// Mockito.when(applicationRepository
	// .findByRegistrationCenterIdAndAppointmentDate(Mockito.any(),
	// Mockito.any())).thenReturn(entity);
	// MainResponseDTO<List<ApplicationDetailResponseDTO>> response =
	// applicationService.getApplicationsForApplicationId("",LocalDate.now().toString());
	// Assert.assertEquals(response.getResponse().get(0).getApplicationId(),
	// applicationEntity.getApplicationId());
	// }
	//

	@Test
	public void getApplicationInfoTest() {
		String applicationId = "9876543210";
		String id = "23465";
		ApplicationEntity applicationEntity = new ApplicationEntity();
		applicationEntity.setApplicationId(applicationId);
		applicationEntity.setAppointmentDate(LocalDate.now());
		applicationEntity.setRegistrationCenterId(id);
		Mockito.when(applicationRepository.findByApplicationId(applicationId)).thenReturn(applicationEntity);
		MainResponseDTO<ApplicationEntity> response = applicationService.getApplicationInfo(applicationId);
		assertNotNull(response.getResponse());
	}

	@Test(expected = InvalidRequestParameterException.class)
	public void testgetApplicationInfoInvalidRequestParameterException() {
		applicationService.getApplicationInfo(null);
	}

	// @Test
	// public void testgetApplicationsStatusForApplicationIdSuccess() {
	// Mockito.when(applicationRepository.findBookingStatusCodeById(Mockito.any())).thenReturn("123456789");
	// MainResponseDTO<String> response =
	// applicationService.getApplicationsStatusForApplicationId("987654321");
	// Assert.assertEquals(response.getResponse(), "123456789");
	// }

	@Test(expected = RecordNotFoundException.class)
	public void testgetApplicationInfoRecordNotFoundException() {
		Mockito.when(applicationRepository.findByApplicationId(Mockito.any())).thenReturn(null);
		MainResponseDTO<ApplicationEntity> response = applicationService.getApplicationInfo("9876543210");
	}

	// @Test(expected=RecordFailedToUpdateException.class)
	// public void
	// testgetApplicationsStatusForApplicationIdRecordNotFoundException() {
	// Mockito.when(applicationRepository.findBookingStatusCodeById(Mockito.any())).thenReturn(null);
	// MainResponseDTO<String> response =
	// applicationService.getApplicationsStatusForApplicationId("987654321");
	// Assert.assertEquals(response.getResponse(), "123456789");
	// }

	@Test
	public void testgetAllApplicationsForUserForBookingType() {
		List<ApplicationEntity> applicationEntities = new ArrayList<ApplicationEntity>();
		ApplicationEntity applicationEntitie = new ApplicationEntity();
		applicationEntitie.setApplicationId("1234567890");
		applicationEntitie.setApplicationStatusCode("Processed");
		applicationEntities.add(applicationEntitie);
		Mockito.when(applicationRepository.findByCreatedByBookingType(Mockito.any(), Mockito.any()))
				.thenReturn(applicationEntities);
		MainResponseDTO<ApplicationsListDTO> response = applicationService
				.getAllApplicationsForUserForBookingType(BookingTypeCodes.NEW_PREREGISTRATION.toString());
		Assert.assertEquals("1234567890", response.getResponse().getAllApplications().get(0).getApplicationId());
	}

	@Test
	public void deleteLostOrUpdateApplicationSuccessTest() {
		String applicationId = "12345";
		ApplicationEntity applicationEntity = new ApplicationEntity();
		applicationEntity.setApplicationId(applicationId);
		applicationEntity.setAppointmentDate(LocalDate.now());
		applicationEntity.setCrBy("4665");
		applicationEntity.setRegistrationCenterId("32544");

		AuthUserDetails applicationUser = Mockito.mock(AuthUserDetails.class);
		Authentication authentication = Mockito.mock(Authentication.class);
		authentication.setAuthenticated(true);
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(applicationUser);

		Mockito.when(serviceUtil.findApplicationById(Mockito.any())).thenReturn(applicationEntity);
		Mockito.when(validationUtil.requstParamValidator(Mockito.any())).thenReturn(true);
		assertNotNull(applicationService.deleteLostOrUpdateApplication(applicationId,
				StatusCodes.BOOKED.getCode().toString()));
	}

	@Test
	public void deleteLostOrUpdateApplicationSuccessTest2() {
		String applicationId = "4665";
		ApplicationEntity applicationEntity = new ApplicationEntity();
		applicationEntity.setApplicationId(applicationId);
		applicationEntity.setAppointmentDate(LocalDate.now());
		applicationEntity.setCrBy("4665");
		applicationEntity.setRegistrationCenterId("32544");

		AuthUserDetails applicationUser = Mockito.mock(AuthUserDetails.class);
		Authentication authentication = Mockito.mock(Authentication.class);
		authentication.setAuthenticated(true);
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(applicationUser);
		Mockito.when(applicationService.authUserDetails().getUserId()).thenReturn(applicationId);

		Mockito.when(serviceUtil.findApplicationById(Mockito.any())).thenReturn(applicationEntity);
		Mockito.when(validationUtil.requstParamValidator(Mockito.any())).thenReturn(true);
		assertNotNull(applicationService.deleteLostOrUpdateApplication(applicationId,
				BookingTypeCodes.LOST_FORGOTTEN_UIN.toString()));
	}

	@Test
	public void deleteLostOrUpdateApplicationSuccessTest3() {
		String applicationId = "4665";
		ApplicationEntity applicationEntity = new ApplicationEntity();
		applicationEntity.setApplicationId(applicationId);
		applicationEntity.setAppointmentDate(LocalDate.now());
		applicationEntity.setCrBy("4665");
		applicationEntity.setRegistrationCenterId("32544");

		AuthUserDetails applicationUser = Mockito.mock(AuthUserDetails.class);
		Authentication authentication = Mockito.mock(Authentication.class);
		authentication.setAuthenticated(true);
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(applicationUser);
		Mockito.when(applicationService.authUserDetails().getUserId()).thenReturn(applicationId);

		Mockito.when(serviceUtil.findApplicationById(Mockito.any())).thenReturn(applicationEntity);
		Mockito.when(validationUtil.requstParamValidator(Mockito.any())).thenReturn(true);
		assertNotNull(applicationService.deleteLostOrUpdateApplication(applicationId,
				BookingTypeCodes.LOST_FORGOTTEN_UIN.toString()));
	}

	@Test(expected = PreIdInvalidForUserIdException.class)
	public void deleteLostOrUpdateApplicationExceptionTest2() {
		String applicationId = "1234";
		ApplicationEntity applicationEntity = new ApplicationEntity();
		applicationEntity.setApplicationId(applicationId);
		applicationEntity.setAppointmentDate(LocalDate.now());
		applicationEntity.setCrBy("4665");
		applicationEntity.setRegistrationCenterId("32544");

		AuthUserDetails applicationUser = Mockito.mock(AuthUserDetails.class);
		Authentication authentication = Mockito.mock(Authentication.class);
		authentication.setAuthenticated(true);
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(applicationUser);
		Mockito.when(applicationService.authUserDetails().getUserId()).thenReturn(applicationId);

		Mockito.when(serviceUtil.findApplicationById(Mockito.any())).thenReturn(applicationEntity);
		Mockito.when(validationUtil.requstParamValidator(Mockito.any())).thenReturn(true);
		assertNotNull(applicationService.deleteLostOrUpdateApplication(applicationId,
				BookingTypeCodes.LOST_FORGOTTEN_UIN.toString()));
	}

	@Test
	public void testDeleteLostOrUpdateApplicationPreRegistrationSuccess() {
		String applicationId = "12345";
		Mockito.when(validationUtil.requstParamValidator(Mockito.any())).thenReturn(true);
		assertNotNull(applicationService.deleteLostOrUpdateApplication(applicationId,
				BookingTypeCodes.UPDATE_REGISTRATION.toString()));
	}

	@Test(expected = PreRegistrationException.class)
	public void testgetAllApplicationsForUserForBookingException() {
		applicationService.getAllApplicationsForUserForBookingType("abcd");
	}

	@Test(expected = PreRegistrationException.class)
	public void testDeleteLostOrUpdateApplicationPreRegistrationException() {
		applicationService.deleteLostOrUpdateApplication(null, BookingTypeCodes.LOST_FORGOTTEN_UIN.toString());
	}

	@Test(expected = PreRegistrationException.class)
	public void testDeleteLostOrUpdateApplicationPreRegistrationException2() {
		applicationService.deleteLostOrUpdateApplication(null, BookingTypeCodes.UPDATE_REGISTRATION.toString());
	}

	@Test(expected = InvalidDateFormatException.class)
	public void testgetBookingsForRegCenterInvalidDateFormatException() {
		applicationService.getBookingsForRegCenter(null, "23-08-2021", null);
	}

	@Test
	public void testgetBookingsForRegCenter() {
		List<ApplicationEntity> entity = new ArrayList<ApplicationEntity>();
		ApplicationEntity applicationEntity = new ApplicationEntity();
		applicationEntity.setApplicationId("");
		applicationEntity.setApplicationStatusCode("");
		applicationEntity.setAppointmentDate(LocalDate.now());
		applicationEntity.setBookingDate(LocalDate.now());
		applicationEntity.setBookingStatusCode("");
		applicationEntity.setBookingType("");
		applicationEntity.setContactInfo("");
		applicationEntity.setCrBy("");
		applicationEntity.setCrDtime(LocalDateTime.now());
		applicationEntity.setRegistrationCenterId("");
		applicationEntity.setSlotFromTime(LocalTime.now());
		applicationEntity.setSlotToTime(LocalTime.now());
		applicationEntity.setUpdBy("");
		applicationEntity.setUpdDtime(LocalDateTime.now());
		entity.add(applicationEntity);
		Mockito.when(applicationRepository.findByRegistrationCenterIdAndBetweenDate(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(entity);

		MainResponseDTO<List<ApplicationDetailResponseDTO>> response = applicationService.getBookingsForRegCenter(null,
				LocalDate.now().toString(), null);
		Assert.assertEquals(response.getResponse().get(0).getApplicationId(), applicationEntity.getApplicationId());

	}

	@Test(expected = RecordNotFoundException.class)
	public void testgetBookingsForRegCenterRecordNotFoundException() {
		Mockito.when(applicationRepository.findByRegistrationCenterIdAndBetweenDate(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(null);

		MainResponseDTO<List<ApplicationDetailResponseDTO>> response = applicationService.getBookingsForRegCenter(null,
				LocalDate.now().toString(), null);

	}

	@Test(expected = InvalidRequestParameterException.class)
	public void testgetBookingsForRegCenterIllegalArgumentException() {
		Mockito.when(applicationRepository.findByRegistrationCenterIdAndBetweenDate(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenThrow(new IllegalArgumentException());

		MainResponseDTO<List<ApplicationDetailResponseDTO>> response = applicationService.getBookingsForRegCenter(null,
				LocalDate.now().toString(),null);
	}

	@Test
	public void testgetAllApplicationsForUser() {
		MainResponseDTO<ApplicationsListDTO> response = applicationService.getAllApplicationsForUser();
		Assert.assertEquals("preReg", response.getId());

	}

	@Test(expected = RecordNotFoundException.class)
	public void testgetAllApplicationsForUserException() {
		Mockito.when(applicationRepository.findByCreatedBy(Mockito.any())).thenThrow(new RecordNotFoundException(
				ApplicationErrorCodes.PRG_APP_013.getCode(), ApplicationErrorMessages.NO_RECORD_FOUND.getMessage()));
		MainResponseDTO<ApplicationsListDTO> response = applicationService.getAllApplicationsForUser();
	}

	@Test(expected = InvalidRequestParameterException.class)
	public void getApplicationStatusExceptionTest() {
		MainResponseDTO<String> response = new MainResponseDTO<String>();
		response.setId("123");
		response.setVersion("11");
		response.setResponsetime(LocalDateTime.now().toString());
		applicationService.getApplicationStatus(allApplicationsId);
	}

	@Test(expected = RecordNotFoundException.class)
	public void getApplicationStatusRecordNotFoundTest() {
		String applicationId = "123";
		MainResponseDTO<String> response = new MainResponseDTO<String>();
		response.setId("123");
		response.setVersion("11");
		response.setResponsetime(LocalDateTime.now().toString());
		applicationService.getApplicationStatus(applicationId);
	}

	@Test
	public void getApplicationStatusTest() {
		String applicationId = "123";
		String applicationBookingStatus = "Success";
		List<ApplicationEntity> entity = new ArrayList<ApplicationEntity>();
		MainResponseDTO<String> responseDTO = new MainResponseDTO<String>();
		ApplicationEntity applicationEntity = new ApplicationEntity();
		responseDTO.setId("123");
		responseDTO.setVersion("11");
		responseDTO.setResponsetime(LocalDateTime.now().toString());
		applicationEntity.setApplicationId(applicationId);
		applicationEntity.setAppointmentDate(LocalDate.now());
		applicationEntity.setBookingStatusCode(applicationBookingStatus);
		applicationEntity.setRegistrationCenterId("123");
		entity.add(applicationEntity);
		responseDTO.setResponse(applicationBookingStatus);
		Mockito.when(applicationRepository.findByApplicationId(Mockito.any())).thenReturn(applicationEntity);
		MainResponseDTO<String> response = applicationService.getApplicationStatus(applicationId);
		Assert.assertEquals(response.getResponse(), "Success");
	}

}
