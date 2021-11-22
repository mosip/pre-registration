package io.mosip.preregistration.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

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
import io.mosip.preregistration.application.dto.ApplicationInfoMetadataDTO;
import io.mosip.preregistration.application.dto.ApplicationRequestDTO;
import io.mosip.preregistration.application.dto.ApplicationResponseDTO;
import io.mosip.preregistration.application.dto.UIAuditRequest;
import io.mosip.preregistration.application.errorcodes.ApplicationErrorCodes;
import io.mosip.preregistration.application.errorcodes.ApplicationErrorMessages;
import io.mosip.preregistration.application.exception.AuditFailedException;
import io.mosip.preregistration.application.exception.DemographicServiceException;
import io.mosip.preregistration.application.exception.DocumentNotFoundException;
import io.mosip.preregistration.application.exception.InvalidDateFormatException;
import io.mosip.preregistration.application.exception.RecordFailedToUpdateException;
import io.mosip.preregistration.application.exception.RecordNotFoundException;
import io.mosip.preregistration.application.repository.ApplicationRepostiory;
import io.mosip.preregistration.application.service.util.DemographicServiceUtil;
import io.mosip.preregistration.core.common.dto.DemographicResponseDTO;
import io.mosip.preregistration.core.common.dto.DocumentsMetaData;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.entity.ApplicationEntity;
import io.mosip.preregistration.core.exception.InvalidRequestParameterException;
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
	}


//	@Test(expected = InvalidRequestParameterException.class)
//	public void testgetPregistrationInfoInvalidRequestParameterException() {
//		applicationService.getPregistrationInfo(null);
//	}

//	@Test
//	public void testgetPregistrationInfoPreRegistrationException() {
//		MainResponseDTO<DemographicResponseDTO> reponseDto=new MainResponseDTO<DemographicResponseDTO>();
//		reponseDto.setResponse(demographicResponse);
//		MainResponseDTO<DocumentsMetaData>  mainDocumentsMetaData=new MainResponseDTO<DocumentsMetaData>();
//		mainDocumentsMetaData.setResponse(documentsMetaData);
//		Mockito.when(demographicService.getDemographicData(Mockito.any(), Mockito.any())).thenReturn(reponseDto);
//		Mockito.when(documentService.getAllDocumentForPreId(Mockito.any())).thenThrow(new DocumentNotFoundException());
//		MainResponseDTO<ApplicationInfoMetadataDTO> response = applicationService.getPregistrationInfo("987654321");
//		Assert.assertEquals(response.getResponse().getDemographicResponse(), demographicResponse);
//	}

//	@Test(expected = InvalidDateFormatException.class)
//	public void testgetApplicationsForApplicationIdInvalidDateFormatException() {
//		applicationService.getApplicationsForApplicationId("","");
//	}
//
//	@Test(expected = RecordNotFoundException.class)
//	public void testgetApplicationsForApplicationIdRecordNotFoundException() {
//		Mockito.when(applicationRepository
//				.findByRegistrationCenterIdAndAppointmentDate(Mockito.any(), Mockito.any())).thenReturn(null);
//		applicationService.getApplicationsForApplicationId("",LocalDate.now().toString());
//	}

//	@Test
//	public void testgetApplicationsForApplicationIdSuccess() {
//		List<ApplicationEntity>  entity=new ArrayList<ApplicationEntity>();
//		ApplicationEntity applicationEntity=new ApplicationEntity();
//		applicationEntity.setApplicationId("");
//		applicationEntity.setApplicationStatusCode("");
//		applicationEntity.setAppointmentDate(LocalDate.now());
//		applicationEntity.setBookingDate(LocalDate.now());
//		applicationEntity.setBookingStatusCode("");
//		applicationEntity.setBookingType("");
//		applicationEntity.setContactInfo("");
//		applicationEntity.setCrBy("");
//		applicationEntity.setCrDtime(LocalDateTime.now());
//		applicationEntity.setRegistrationCenterId("");
//		applicationEntity.setSlotFromTime(LocalTime.now());
//		applicationEntity.setSlotToTime(LocalTime.now());
//		applicationEntity.setUpdBy("");
//		applicationEntity.setUpdDtime(LocalDateTime.now());
//		entity.add(applicationEntity);
//		Mockito.when(applicationRepository
//				.findByRegistrationCenterIdAndAppointmentDate(Mockito.any(), Mockito.any())).thenReturn(entity);
//		MainResponseDTO<List<ApplicationDetailResponseDTO>> response = applicationService.getApplicationsForApplicationId("",LocalDate.now().toString());
//		Assert.assertEquals(response.getResponse().get(0).getApplicationId(), applicationEntity.getApplicationId());
//	}
//
//	@Test(expected=InvalidRequestParameterException.class)
//	public void testgetApplicationsForApplicationIdInvalidRequestParameterException() {
//		Mockito.when(applicationRepository
//				.findByRegistrationCenterIdAndAppointmentDate(Mockito.any(), Mockito.any())).thenThrow(new IllegalArgumentException());
//		applicationService.getApplicationsForApplicationId("",LocalDate.now().toString());
//	}


//	@Test(expected = InvalidRequestParameterException.class)
//	public void testgetPregistrationInfoInvalidRequestParameterException() {
//		demographicService.getPregistrationInfo(null);
//	}

//	@Test
//	public void testgetPregistrationInfoPreRegistrationException() {
//		MainResponseDTO<DemographicResponseDTO> reponseDto=new MainResponseDTO<DemographicResponseDTO>();
//		reponseDto.setResponse(demographicResponse);
//		MainResponseDTO<DocumentsMetaData>  mainDocumentsMetaData=new MainResponseDTO<DocumentsMetaData>();
//		mainDocumentsMetaData.setResponse(documentsMetaData);
//		Mockito.when(demographicService.getDemographicData(Mockito.any(), Mockito.any())).thenReturn(reponseDto);
//		Mockito.when(documentService.getAllDocumentForPreId(Mockito.any())).thenThrow(new DocumentNotFoundException());
//
//		MainResponseDTO<ApplicationInfoMetadataDTO> response = demographicService.getPregistrationInfo("987654321");
//		Assert.assertEquals(response.getResponse().getDemographicResponse(), demographicResponse);
//
//	}


	@Test(expected = AuditFailedException.class)
	public void testsaveUIEventAuditAuditFailedException() {
		UIAuditRequest auditRequest=new UIAuditRequest();
		applicationService.saveUIEventAudit(auditRequest);
	}


//	@Test
//	public void testgetApplicationsStatusForApplicationIdSuccess() {
//		Mockito.when(applicationRepository.findBookingStatusCodeById(Mockito.any())).thenReturn("123456789");
//		MainResponseDTO<String> response = applicationService.getApplicationsStatusForApplicationId("987654321");
//		Assert.assertEquals(response.getResponse(), "123456789");
//	}

//	@Test(expected=RecordFailedToUpdateException.class)
//	public void testgetApplicationsStatusForApplicationIdInvalidRequestParameterException() {
//		MainResponseDTO<String> response = applicationService.getApplicationsStatusForApplicationId(null);
//	}
//
//	@Test(expected=RecordFailedToUpdateException.class)
//	public void testgetApplicationsStatusForApplicationIdRecordNotFoundException() {
//		Mockito.when(applicationRepository.findBookingStatusCodeById(Mockito.any())).thenReturn(null);
//		MainResponseDTO<String> response = applicationService.getApplicationsStatusForApplicationId("987654321");
//		Assert.assertEquals(response.getResponse(), "123456789");
//	}



//	@Test
//	public void testgetApplicationsStatusForApplicationIdSuccess() {
//		Mockito.when(applicationRepository.findBookingStatusCodeById(Mockito.any())).thenReturn("123456789");
//		MainResponseDTO<ApplicationEntity> response = applicationService.getApplicationInfo("987654321");
//		ApplicationEntity applicationEntity = response.getResponse();
//		Assert.assertEquals(applicationEntity.getApplicationStatusCode(), "123456789");
//
//	}

	@Test
	public void testsaveUIEventAuditSuccess() {
		UIAuditRequest auditRequest=new UIAuditRequest();
		auditRequest.setActionTimeStamp(LocalDateTime.now(ZoneId.of("UTC")).toString());
		auditRequest.setDescription("{\"template\":\"\",\"description\":\"\",\"url\":\"\"}");
		MainResponseDTO<String> response = applicationService.saveUIEventAudit(auditRequest);
		Assert.assertEquals(response.getResponse(), "Audit Logged Successfully");
	}

//	@Test
//	public void testgetPregistrationInfoSuccess() {
//		MainResponseDTO<DemographicResponseDTO> reponseDto=new MainResponseDTO<DemographicResponseDTO>();
//		reponseDto.setResponse(demographicResponse);
//		MainResponseDTO<DocumentsMetaData>  mainDocumentsMetaData=new MainResponseDTO<DocumentsMetaData>();
//		mainDocumentsMetaData.setResponse(documentsMetaData);
//		Mockito.when(demographicService.getDemographicData(Mockito.any(), Mockito.any())).thenReturn(reponseDto);
//		Mockito.when(documentService.getAllDocumentForPreId(Mockito.any())).thenReturn(mainDocumentsMetaData);
//		MainResponseDTO<ApplicationInfoMetadataDTO> response = applicationService.getPregistrationInfo("987654321");
//		Assert.assertEquals(response.getResponse().getDocumentsMetaData(), documentsMetaData);
//		Assert.assertEquals(response.getResponse().getDemographicResponse(), demographicResponse);
//	}


//	@Test
//	public void testgetPregistrationInfoSuccess() {
//		MainResponseDTO<DemographicResponseDTO> reponseDto=new MainResponseDTO<DemographicResponseDTO>();
//		reponseDto.setResponse(demographicResponse);
//		MainResponseDTO<DocumentsMetaData>  mainDocumentsMetaData=new MainResponseDTO<DocumentsMetaData>();
//		mainDocumentsMetaData.setResponse(documentsMetaData);
//		Mockito.when(demographicService.getDemographicData(Mockito.any(), Mockito.any())).thenReturn(reponseDto);
//		Mockito.when(documentService.getAllDocumentForPreId(Mockito.any())).thenReturn(mainDocumentsMetaData);
//		MainResponseDTO<ApplicationInfoMetadataDTO> response = demographicService.getPregistrationInfo("987654321");
//		Assert.assertEquals(response.getResponse().getDocumentsMetaData(), documentsMetaData);
//		Assert.assertEquals(response.getResponse().getDemographicResponse(), demographicResponse);
//
//	}

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

	@Test(expected = InvalidDateFormatException.class)
	public void testAddLostOrUpdateHttpServerErrorException2() {
		MainResponseDTO<ApplicationResponseDTO> mainResponseDTO = new MainResponseDTO<ApplicationResponseDTO>();
		MainRequestDTO<ApplicationRequestDTO> request = new MainRequestDTO<ApplicationRequestDTO>();
		Mockito.when((MainResponseDTO<ApplicationResponseDTO>) serviceUtil.getMainResponseDto(request)).thenThrow(
				new InvalidDateFormatException(
						ApplicationErrorCodes.PRG_APP_013.getCode()
						,ApplicationErrorMessages.INVAILD_REQUEST_ARGUMENT.getMessage(), mainResponseDTO));
		String bookingType=null;
		applicationService.addLostOrUpdateApplication(request, bookingType);
	}
}
