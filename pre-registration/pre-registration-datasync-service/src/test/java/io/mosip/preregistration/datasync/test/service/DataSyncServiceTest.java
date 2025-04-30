package io.mosip.preregistration.datasync.test.service;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.mosip.preregistration.core.util.ValidationUtil;
import io.mosip.preregistration.datasync.dto.ApplicationInfoMetadataDTO;
import io.mosip.preregistration.datasync.dto.DataSyncRequestDTO;
import io.mosip.preregistration.datasync.dto.PreRegArchiveDTO;
import io.mosip.preregistration.datasync.dto.PreRegistrationIdsDTO;
import io.mosip.preregistration.datasync.dto.ReverseDataSyncRequestDTO;
import io.mosip.preregistration.datasync.dto.ReverseDatasyncReponseDTO;
import io.mosip.preregistration.datasync.dto.ApplicationsDTO;
import io.mosip.preregistration.datasync.dto.ApplicationDetailResponseDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.WebApplicationContext;

import io.mosip.analytics.event.anonymous.util.AnonymousProfileUtil;
import io.mosip.kernel.clientcrypto.service.spi.ClientCryptoManagerService;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.exception.ParseException;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.preregistration.application.config.Config;
import io.mosip.preregistration.core.common.dto.BookingDataByRegIdDto;
import io.mosip.preregistration.core.common.dto.BookingRegistrationDTO;
import io.mosip.preregistration.core.common.dto.DemographicResponseDTO;
import io.mosip.preregistration.core.common.dto.DocumentDTO;
import io.mosip.preregistration.core.common.dto.DocumentMultipartResponseDTO;
import io.mosip.preregistration.core.common.dto.DocumentsMetaData;
import io.mosip.preregistration.core.common.dto.ExceptionJSONInfoDTO;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.dto.SlotTimeDto;
import io.mosip.preregistration.core.exception.InvalidRequestParameterException;
import io.mosip.preregistration.core.util.AuditLogUtil;
import io.mosip.preregistration.datasync.DataSyncApplicationTest;
import io.mosip.preregistration.datasync.errorcodes.ErrorCodes;
import io.mosip.preregistration.datasync.errorcodes.ErrorMessages;
import io.mosip.preregistration.datasync.exception.DemographicGetDetailsException;
import io.mosip.preregistration.datasync.repository.InterfaceDataSyncRepo;
import io.mosip.preregistration.datasync.repository.ProcessedDataSyncRepo;
import io.mosip.preregistration.datasync.service.DataSyncService;
import io.mosip.preregistration.datasync.service.util.DataSyncServiceUtil;
import io.mosip.preregistration.datasync.test.config.TestConfig;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {Config.class, TestConfig.class, TestContext.class, WebApplicationContext.class})
@SpringBootTest(classes = { DataSyncApplicationTest.class })
public class DataSyncServiceTest {

	@Mock
	private InterfaceDataSyncRepo interfaceDataSyncRepo;

	@Mock
	private ProcessedDataSyncRepo processedDataSyncRepo;

	@Autowired
	@InjectMocks
	private DataSyncService dataSyncService;

	@SpyBean
	private DataSyncService spyDataSyncService;

	@MockBean
	RestTemplateBuilder restTemplateBuilder;

	@MockBean
	AnonymousProfileUtil profileUtil;

	/**
	 * Autowired reference for $link{DataSyncServiceUtil}
	 */
	@MockBean
	DataSyncServiceUtil serviceUtil;

	@MockBean
	ClientCryptoManagerService clientCryptoManagerService;

	@MockBean
	AuditLogUtil auditLogUtil;

	@Mock
	ValidationUtil validationUtil;

	String preid = "61720179614289";
	ExceptionJSONInfoDTO errlist = new ExceptionJSONInfoDTO();
	ExceptionJSONInfoDTO exceptionJSONInfo = new ExceptionJSONInfoDTO("", "");
	MainResponseDTO<PreRegistrationIdsDTO> dataSyncResponseDTO = new MainResponseDTO<>();
	MainResponseDTO<String> storeResponseDTO = new MainResponseDTO<>();

	byte[] pFile = null;

	/**
	 * Reference for ${mosip.id.preregistration.datasync.fetch.ids} from property
	 * file
	 */
	@Value("${mosip.id.preregistration.datasync.fetch.ids}")
	private String fetchAllId;

	/**
	 * Reference for ${mosip.id.preregistration.datasync.store} from property file
	 */
	@Value("${mosip.id.preregistration.datasync.store}")
	private String storeId;

	/**
	 * Reference for ${mosip.id.preregistration.datasync.fetch} from property file
	 */
	@Value("${mosip.id.preregistration.datasync.fetch}")
	private String fetchId;

	/**
	 * Reference for ${ver} from property file
	 */
	@Value("${version}")
	private String version;

	// new
	String preId = "23587986034785";
	String machineId = "12345";
	String fromDate = "2018-01-17 00:00:00";
	String toDate = "2019-01-17 00:00:00";
	DocumentMultipartResponseDTO multipartResponseDTOs = new DocumentMultipartResponseDTO();
	List<DocumentMultipartResponseDTO> list2 = new ArrayList<>();
	DocumentsMetaData documentsMetaData = new DocumentsMetaData();
	DocumentDTO documentDTO = new DocumentDTO();
	BookingRegistrationDTO bookingRegistrationDTO = new BookingRegistrationDTO();
	DemographicResponseDTO demography = new DemographicResponseDTO();
	PreRegArchiveDTO archiveDTO = new PreRegArchiveDTO();
	MainResponseDTO<PreRegArchiveDTO> mainResponseDTO = new MainResponseDTO<>();

	BookingDataByRegIdDto preRegIdsByRegCenterIdResponseDTO = new BookingDataByRegIdDto();
	MainRequestDTO<DataSyncRequestDTO> datasyncReqDto = new MainRequestDTO<>();
	DataSyncRequestDTO dataSyncRequestDTO = new DataSyncRequestDTO();
	PreRegistrationIdsDTO preRegistrationIdsDTO = new PreRegistrationIdsDTO();
	Map<String, String> requestMap = new HashMap<>();
	Map<String, String> requiredRequestMap = new HashMap<>();

	private String dateTimeFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	String resTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date());

	MainRequestDTO<ReverseDataSyncRequestDTO> reverseRequestDTO = new MainRequestDTO<>();
	MainResponseDTO<ReverseDatasyncReponseDTO> reverseResponseDTO = new MainResponseDTO<>();
	ReverseDataSyncRequestDTO reverseDataSyncRequestDTO = new ReverseDataSyncRequestDTO();
	ReverseDatasyncReponseDTO reverseDatasyncReponse = new ReverseDatasyncReponseDTO();
	ApplicationInfoMetadataDTO preRegInfo = new ApplicationInfoMetadataDTO();

	@Before
	public void setUp() throws ParseException{

		List<String> preRegIds = new ArrayList<String>();
		preRegIds.add("23587986034785");

		MockitoAnnotations.initMocks(this);

		Map<String, Map<LocalDate, SlotTimeDto>> idsWithAppointmentDate = new HashMap<>();
		Map<LocalDate, SlotTimeDto> appointDateWithFromTime = new HashMap<>();
		SlotTimeDto timeDto = new SlotTimeDto();
		timeDto.setFromTime(LocalTime.now().minusMinutes(-15));
		timeDto.setToTime(LocalTime.now().plusMinutes(120));
		appointDateWithFromTime.put(LocalDate.now(), timeDto);
		idsWithAppointmentDate.put("23587986034785", appointDateWithFromTime);

		AuthUserDetails applicationUser = Mockito.mock(AuthUserDetails.class);
		Authentication authentication = Mockito.mock(Authentication.class);
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(applicationUser);

		requiredRequestMap.put("version", version);

		multipartResponseDTOs.setDocName("Address.pdf");
		multipartResponseDTOs.setDocumentId("1234");
		multipartResponseDTOs.setDocCatCode("POA");
		multipartResponseDTOs.setLangCode("ENG");
		multipartResponseDTOs.setDocTypCode("RNC");
		list2.add(multipartResponseDTOs);
		documentsMetaData.setDocumentsMetaData(list2);

		bookingRegistrationDTO.setRegDate("2018-01-17 00:00:00");
		bookingRegistrationDTO.setRegistrationCenterId("1005");
		bookingRegistrationDTO.setSlotFromTime(fromDate);
		bookingRegistrationDTO.setSlotToTime(toDate);

		demography.setPreRegistrationId(preId);
		demography.setCreatedBy("Rajath");
		demography.setStatusCode("SAVE");
		demography.setLangCode("12L");
		demography.setPreRegistrationId(preid);

		byte[] demographicDetails = { 1, 0, 1, 0, 1, 0 };

		demography.setDemographicDetails(null);

		archiveDTO.setZipBytes(demographicDetails);
		archiveDTO.setFileName(demography.getPreRegistrationId().toString());

		mainResponseDTO.setResponsetime(resTime);
		mainResponseDTO.setResponse(archiveDTO);
		List<ExceptionJSONInfoDTO> exceptionJSONInfoDTOs = new ArrayList<>();
		exceptionJSONInfoDTOs.add(errlist);
		mainResponseDTO.setErrors(exceptionJSONInfoDTOs);

		preRegIds.add(preId);

		preRegIdsByRegCenterIdResponseDTO.setIdsWithAppointmentDate(idsWithAppointmentDate);
		preRegIdsByRegCenterIdResponseDTO.setRegistrationCenterId("1005");

		dataSyncRequestDTO.setRegistrationCenterId("1005");
		dataSyncRequestDTO.setFromDate("2018-01-17 00:00:00");
		dataSyncRequestDTO.setToDate("2018-12-17 00:00:00");

		datasyncReqDto.setId(fetchAllId);
		datasyncReqDto.setVersion(version);
		datasyncReqDto.setRequesttime(new Timestamp(System.currentTimeMillis()));
		datasyncReqDto.setRequest(dataSyncRequestDTO);

		Map<String, String> list = new HashMap<>();
		list.put(preId, "2018-12-28T13:04:53.117Z");
		preRegistrationIdsDTO.setPreRegistrationIds(list);
		preRegistrationIdsDTO.setTransactionId("1111");
		preRegistrationIdsDTO.setCountOfPreRegIds("1");

		dataSyncResponseDTO.setResponse(preRegistrationIdsDTO);
		dataSyncResponseDTO.setErrors(null);
		dataSyncResponseDTO.setResponsetime("2019-02-12T10:54:53.131Z");

		Date date = new Timestamp(System.currentTimeMillis());

		requestMap.put("id", datasyncReqDto.getId());
		requestMap.put("version", datasyncReqDto.getVersion());
		requestMap.put("requesttime", DateUtils.formatDate(date, dateTimeFormat));
		requestMap.put("request", datasyncReqDto.getRequest().toString());

		List<String> preRegistrationIds = new ArrayList<>();
		preRegistrationIds.add(preid);
		reverseDataSyncRequestDTO.setPreRegistrationIds(preRegistrationIds);

		reverseRequestDTO.setRequest(reverseDataSyncRequestDTO);
		reverseRequestDTO.setRequesttime(new Timestamp(System.currentTimeMillis()));
		reverseRequestDTO.setId(storeId);
		reverseRequestDTO.setVersion(version);

		reverseDatasyncReponse.setTransactionId("1111");
		List<String> preids = new ArrayList<>();
		preids.add("23587986034785");
		reverseDatasyncReponse.setPreRegistrationIds(preids);
		reverseDatasyncReponse.setCountOfStoredPreRegIds("1");
	}

	@Test
	public void successGetPreRegistrationTest() throws Exception {

		when(serviceUtil.getPreRegistrationData(Mockito.anyString())).thenReturn(demography);
		when(serviceUtil.getDocDetails(Mockito.anyString())).thenReturn(documentsMetaData);
		when(serviceUtil.getDocBytesDetails(Mockito.anyString(), Mockito.anyString())).thenReturn(documentDTO);
		when(serviceUtil.getAppointmentDetails(Mockito.anyString())).thenReturn(bookingRegistrationDTO);
		when(serviceUtil.archivingFiles(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(archiveDTO);
		Mockito.doNothing().when(spyDataSyncService).setAuditValues(Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		MainResponseDTO<PreRegArchiveDTO> response = dataSyncService.getPreRegistrationData(preId);

		assertEquals(response.getResponse().getPreRegistrationId(), archiveDTO.getPreRegistrationId());
	}

	@Test(expected = DemographicGetDetailsException.class)
	public void GetPreRegistrationTest1() throws Exception {
		DemographicGetDetailsException ex = new DemographicGetDetailsException(ErrorCodes.PRG_DATA_SYNC_007.toString(),
				ErrorMessages.DEMOGRAPHIC_GET_RECORD_FAILED.toString(), null);
		when(serviceUtil.getPreRegistrationData(Mockito.anyString())).thenThrow(ex);
		dataSyncService.getPreRegistrationData(preId);
	}

	@Test
	public void successRetrieveAllPreRegIdTest() throws Exception {
		when(serviceUtil.validateDataSyncRequest(Mockito.any(), Mockito.any())).thenReturn(true);
		when(serviceUtil.getBookedPreIdsByDateAndRegCenterIdRestService(Mockito.any(), Mockito.any(),
				Mockito.anyString())).thenReturn(preRegIdsByRegCenterIdResponseDTO);
		when(serviceUtil.getLastUpdateTimeStamp(Mockito.any())).thenReturn(preRegistrationIdsDTO);
		Mockito.doNothing().when(spyDataSyncService).setAuditValues(Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		MainResponseDTO<PreRegistrationIdsDTO> response = dataSyncService.retrieveAllPreRegIds(datasyncReqDto);

		assertEquals(preRegistrationIdsDTO.getCountOfPreRegIds().length(),
				response.getResponse().getCountOfPreRegIds().length());
	}

	@Test(expected = InvalidRequestParameterException.class)
	public void RetrieveAllPreRegIdTest1() throws Exception {
		InvalidRequestParameterException ex = new InvalidRequestParameterException(
				ErrorCodes.PRG_DATA_SYNC_009.toString(), ErrorMessages.INVALID_REGISTRATION_CENTER_ID.toString(), null);
		when(serviceUtil.validateDataSyncRequest(Mockito.any(), Mockito.any())).thenThrow(ex);
		Mockito.doNothing().when(spyDataSyncService).setAuditValues(Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		dataSyncService.retrieveAllPreRegIds(datasyncReqDto);
	}

	@Test
	public void successStoreConsumedPreRegistrationsTest() throws Exception {
		when(serviceUtil.validateReverseDataSyncRequest(Mockito.any(), Mockito.any())).thenReturn(true);
		when(serviceUtil.reverseDateSyncSave(Mockito.any(), Mockito.any(), Mockito.anyString()))
				.thenReturn(reverseDatasyncReponse);
		Mockito.doNothing().when(spyDataSyncService).setAuditValues(Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		reverseResponseDTO = dataSyncService.storeConsumedPreRegistrations(reverseRequestDTO);

		assertEquals(reverseDatasyncReponse.getPreRegistrationIds().size(),
				reverseResponseDTO.getResponse().getPreRegistrationIds().size());
	}

	@Test
	public void fetchPreRegistrationDataTest() {
		mainResponseDTO.setId(fetchId);
		mainResponseDTO.setVersion(version);
		mainResponseDTO.setResponsetime(serviceUtil.getCurrentResponseTime());
		mainResponseDTO.setResponse(archiveDTO);
		preRegInfo.setDocumentsMetaData(documentsMetaData);
		when(serviceUtil.getPreRegistrationInfo(Mockito.any())).thenReturn(preRegInfo);
		when(serviceUtil.getAppointmentDetails(Mockito.any())).thenReturn(bookingRegistrationDTO);
		when(serviceUtil.archivingFiles(demography, bookingRegistrationDTO, documentsMetaData, machineId))
				.thenReturn(archiveDTO);
		MainResponseDTO<PreRegArchiveDTO> response = dataSyncService.fetchPreRegistrationData(preid, machineId);
		assertEquals(mainResponseDTO.getId().length(), response.getId().length());
	}

	@Test
	public void fetchPreRegistrationDataPendingStatusTest() {
		String preregId = "12345";
		mainResponseDTO.setId(fetchId);
		mainResponseDTO.setVersion(version);
		mainResponseDTO.setResponsetime(serviceUtil.getCurrentResponseTime());
		mainResponseDTO.setResponse(archiveDTO);
		preRegInfo.setDocumentsMetaData(documentsMetaData);
		demography.setPreRegistrationId(preregId);
		demography.setStatusCode("Pending");
		demography.setCreatedDateTime(fromDate);
		preRegInfo.setDemographicResponse(demography);
		when(serviceUtil.getPreRegistrationInfo(Mockito.any())).thenReturn(preRegInfo);
		when(serviceUtil.getAppointmentDetails(Mockito.any())).thenReturn(bookingRegistrationDTO);
		when(serviceUtil.archivingFiles(demography, bookingRegistrationDTO, documentsMetaData, machineId))
				.thenReturn(archiveDTO);
		MainResponseDTO<PreRegArchiveDTO> response = dataSyncService.fetchPreRegistrationData(preregId, machineId);
		assertEquals(mainResponseDTO.getId().length(), response.getId().length());
	}

	@Test
	public void test_retrieve_appointments_for_registration_center_between_dates() {
		DataSyncService dataSyncService = new DataSyncService();
		ReflectionTestUtils.setField(dataSyncService, "auditLogUtil", auditLogUtil);

		MainRequestDTO<DataSyncRequestDTO> dataSyncRequest = new MainRequestDTO<>();
		DataSyncRequestDTO dataSyncRequestDTO = new DataSyncRequestDTO();
		dataSyncRequestDTO.setRegistrationCenterId("10001");
		dataSyncRequestDTO.setFromDate("2023-01-01");
		dataSyncRequestDTO.setToDate("2023-01-31");
		dataSyncRequest.setRequest(dataSyncRequestDTO);
		dataSyncRequest.setId("mosip.pre-registration.datasync.fetch.ids");
		dataSyncRequest.setVersion("1.0");
		dataSyncRequest.setRequesttime(new Date());

		Authentication authentication = Mockito.mock(Authentication.class);
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		AuthUserDetails userDetails = Mockito.mock(AuthUserDetails.class);
		when(authentication.getPrincipal()).thenReturn(userDetails);
		when(userDetails.getUserId()).thenReturn("testUser");
		when(userDetails.getUsername()).thenReturn("testUsername");

		MainResponseDTO<ApplicationsDTO> response = dataSyncService.retrieveAllAppointmentsSyncV2(dataSyncRequest);

		assertNotNull(response);
	}

	@Test
	public void test_invalid_request_parameters() {
		DataSyncService dataSyncService = new DataSyncService();
		ReflectionTestUtils.setField(dataSyncService, "auditLogUtil", auditLogUtil);
		ReflectionTestUtils.setField(dataSyncService, "validationUtil", validationUtil);
		ReflectionTestUtils.setField(dataSyncService, "serviceUtil", serviceUtil);

		MainRequestDTO<DataSyncRequestDTO> dataSyncRequest = new MainRequestDTO<>();
		DataSyncRequestDTO dataSyncRequestDTO = new DataSyncRequestDTO();
		dataSyncRequestDTO.setRegistrationCenterId(null); // Invalid registration center ID
		dataSyncRequestDTO.setFromDate("2023-13-01"); // Invalid date format
		dataSyncRequest.setRequest(dataSyncRequestDTO);
		dataSyncRequest.setId("mosip.pre-registration.datasync.fetch.ids");
		dataSyncRequest.setVersion("1.0");
		dataSyncRequest.setRequesttime(new Date());

		InvalidRequestParameterException expectedEx = new InvalidRequestParameterException(
                "PRE-16000", "Invalid Registration Center Id", null);

		when(validationUtil.requestValidator(dataSyncRequest))
				.thenThrow(expectedEx);

		Authentication authentication = Mockito.mock(Authentication.class);
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		AuthUserDetails userDetails = Mockito.mock(AuthUserDetails.class);
		when(authentication.getPrincipal()).thenReturn(userDetails);
		when(userDetails.getUserId()).thenReturn("testUser");
		when(userDetails.getUsername()).thenReturn("testUsername");

		ReflectionTestUtils.setField(dataSyncService, "requiredRequestMap", new HashMap<String, String>());
		ReflectionTestUtils.setField(dataSyncService, "fetchAllId", "mosip.pre-registration.datasync.fetch.ids");
		ReflectionTestUtils.setField(dataSyncService, "version", "1.0");

		InvalidRequestParameterException exception = assertThrows(InvalidRequestParameterException.class, () -> {
			dataSyncService.retrieveAllAppointmentsSyncV2(dataSyncRequest);
		});

		assertEquals( "PRE-16000 --> Invalid Registration Center Id", exception.getMessage());
	}

	@Test
	public void test_validate_data_sync_request_parameters() {
		DataSyncService dataSyncService = new DataSyncService();
		ReflectionTestUtils.setField(dataSyncService, "auditLogUtil", auditLogUtil);
		ReflectionTestUtils.setField(dataSyncService, "validationUtil", validationUtil);
		ReflectionTestUtils.setField(dataSyncService, "serviceUtil", serviceUtil);

		Map<String, String> requiredRequestMap = new HashMap<>();
		requiredRequestMap.put("id", "testId");
		ReflectionTestUtils.setField(dataSyncService, "requiredRequestMap", requiredRequestMap);
		ReflectionTestUtils.setField(dataSyncService, "fetchAllId", "testId");
		ReflectionTestUtils.setField(dataSyncService, "version", "1.0");

		MainRequestDTO<DataSyncRequestDTO> request = new MainRequestDTO<>();
		DataSyncRequestDTO dataSyncRequestDTO = new DataSyncRequestDTO();
		dataSyncRequestDTO.setRegistrationCenterId("center123");
		dataSyncRequestDTO.setFromDate("2023-10-01");
		dataSyncRequestDTO.setToDate("2023-10-10");
		request.setRequest(dataSyncRequestDTO);
		request.setId("testId");
		request.setVersion("1.0");
		request.setRequesttime(new Date());

		Mockito.when(validationUtil.requestValidator(request)).thenReturn(true);

		Map<String, String> requestMap = new HashMap<>();
		requestMap.put("id", "testId");
		Mockito.when(serviceUtil.prepareRequestMap(request)).thenReturn(requestMap);
		Mockito.when(validationUtil.requestValidator(Mockito.anyMap(), Mockito.anyMap())).thenReturn(true);
		Mockito.when(serviceUtil.isNull(Mockito.anyString())).thenReturn(false);

		List<ApplicationDetailResponseDTO> emptyList = new ArrayList<>();
		Mockito.when(serviceUtil.getAllBookedApplicationIds(
						Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(emptyList);

		Date responseTime = new Date();
		Mockito.when(serviceUtil.getCurrentResponseTime()).thenReturn(String.valueOf(responseTime));

		Authentication authentication = Mockito.mock(Authentication.class);
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		AuthUserDetails userDetails = Mockito.mock(AuthUserDetails.class);
		Mockito.when(authentication.getPrincipal()).thenReturn(userDetails);
		Mockito.when(userDetails.getUserId()).thenReturn("testUser");
		Mockito.when(userDetails.getUsername()).thenReturn("testUsername");

		MainResponseDTO<ApplicationsDTO> response = dataSyncService.retrieveAllAppointmentsSyncV2(request);

		assertNotNull(response);
		assertEquals("testId", response.getId());
		assertEquals("1.0", response.getVersion());

		Mockito.verify(validationUtil).requestValidator(request);
		Mockito.verify(serviceUtil).prepareRequestMap(request);
		Mockito.verify(validationUtil).requestValidator(requestMap, requiredRequestMap);
		Mockito.verify(serviceUtil).validateDataSyncRequest(dataSyncRequestDTO, response);
		Mockito.verify(serviceUtil).getAllBookedApplicationIds("2023-10-01", "2023-10-10", "center123");
		Mockito.verify(serviceUtil).getCurrentResponseTime();
	}

	@Test
	public void test_set_to_date_when_null() {
		DataSyncService dataSyncService = new DataSyncService();
		ReflectionTestUtils.setField(dataSyncService, "auditLogUtil", auditLogUtil);
		ReflectionTestUtils.setField(dataSyncService, "validationUtil", validationUtil);
		ReflectionTestUtils.setField(dataSyncService, "serviceUtil", serviceUtil);

		ReflectionTestUtils.setField(dataSyncService, "requiredRequestMap", new HashMap<String, String>());
		ReflectionTestUtils.setField(dataSyncService, "fetchAllId", "mosip.pre-registration.datasync.fetch.ids");
		ReflectionTestUtils.setField(dataSyncService, "version", "1.0");

		MainRequestDTO<DataSyncRequestDTO> request = new MainRequestDTO<>();
		DataSyncRequestDTO dataSyncRequestDTO = new DataSyncRequestDTO();
		dataSyncRequestDTO.setRegistrationCenterId("center123");
		dataSyncRequestDTO.setFromDate("2023-10-01");
		dataSyncRequestDTO.setToDate(null);
		request.setRequest(dataSyncRequestDTO);
		request.setId("testId");
		request.setVersion("1.0");
		request.setRequesttime(new Date());

		Mockito.when(validationUtil.requestValidator(request)).thenReturn(true);
		Mockito.when(validationUtil.requestValidator(Mockito.anyMap(), Mockito.anyMap())).thenReturn(true);
		Mockito.when(serviceUtil.prepareRequestMap(request)).thenReturn(new HashMap<>());

		Mockito.when(serviceUtil.isNull(dataSyncRequestDTO.getToDate())).thenReturn(true);

		List<ApplicationDetailResponseDTO> emptyList = new ArrayList<>();
		Mockito.when(serviceUtil.getAllBookedApplicationIds(
						Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(emptyList);

		Mockito.when(serviceUtil.getCurrentResponseTime()).thenReturn(String.valueOf(new Date()));

		Authentication authentication = Mockito.mock(Authentication.class);
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		AuthUserDetails userDetails = Mockito.mock(AuthUserDetails.class);
		Mockito.when(authentication.getPrincipal()).thenReturn(userDetails);
		Mockito.when(userDetails.getUserId()).thenReturn("testUser");
		Mockito.when(userDetails.getUsername()).thenReturn("testUsername");

		MainResponseDTO<ApplicationsDTO> response = dataSyncService.retrieveAllAppointmentsSyncV2(request);

		assertNotNull(response);
		assertEquals("2023-10-01", dataSyncRequestDTO.getToDate());

		Mockito.verify(serviceUtil).isNull(null);
	}

	@Test
	public void test_fetch_booked_application_ids() {
		DataSyncService dataSyncService = new DataSyncService();
		ReflectionTestUtils.setField(dataSyncService, "auditLogUtil", auditLogUtil);
		ReflectionTestUtils.setField(dataSyncService, "validationUtil", validationUtil);
		ReflectionTestUtils.setField(dataSyncService, "serviceUtil", serviceUtil);

		ReflectionTestUtils.setField(dataSyncService, "requiredRequestMap", new HashMap<String, String>());
		ReflectionTestUtils.setField(dataSyncService, "fetchAllId", "mosip.pre-registration.datasync.fetch.ids");
		ReflectionTestUtils.setField(dataSyncService, "version", "1.0");

		MainRequestDTO<DataSyncRequestDTO> request = new MainRequestDTO<>();
		DataSyncRequestDTO dataSyncRequestDTO = new DataSyncRequestDTO();
		dataSyncRequestDTO.setRegistrationCenterId("center123");
		dataSyncRequestDTO.setFromDate("2023-10-01");
		dataSyncRequestDTO.setToDate("2023-10-10");
		request.setRequest(dataSyncRequestDTO);
		request.setId("testId");
		request.setVersion("1.0");
		request.setRequesttime(new Date());

		when(validationUtil.requestValidator(request)).thenReturn(true);
		when(validationUtil.requestValidator(Mockito.anyMap(), Mockito.anyMap())).thenReturn(true);
		when(serviceUtil.prepareRequestMap(request)).thenReturn(new HashMap<>());
		when(serviceUtil.isNull(Mockito.anyString())).thenReturn(false);

		Authentication authentication = Mockito.mock(Authentication.class);
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		AuthUserDetails userDetails = Mockito.mock(AuthUserDetails.class);
		when(authentication.getPrincipal()).thenReturn(userDetails);
		when(userDetails.getUserId()).thenReturn("testUser");
		when(userDetails.getUsername()).thenReturn("testUsername");

		List<ApplicationDetailResponseDTO> applicationDetailsList = new ArrayList<>();
		ApplicationDetailResponseDTO applicationDetail = new ApplicationDetailResponseDTO();
		applicationDetail.setApplicationId("app123");
		applicationDetail.setAppointmentDate(LocalDate.now().toString());
		applicationDetail.setSlotFromTime(LocalTime.now().toString());
		applicationDetail.setBookingType("NEW");
		applicationDetailsList.add(applicationDetail);

		when(serviceUtil.getAllBookedApplicationIds(
						Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(applicationDetailsList);

		try {
			Method getUTCTimeStampMethod = DataSyncService.class.getDeclaredMethod("getUTCTimeStamp", String.class, String.class);
			getUTCTimeStampMethod.setAccessible(true);
			Mockito.mockStatic(DataSyncService.class);
			when(getUTCTimeStampMethod.invoke(dataSyncService, Mockito.anyString(), Mockito.anyString())).thenReturn("2023-10-01T10:00:00Z");
		} catch (Exception e) {
			when(serviceUtil.getCurrentResponseTime()).thenReturn(String.valueOf(new Date()));
		}

		MainResponseDTO<ApplicationsDTO> response = dataSyncService.retrieveAllAppointmentsSyncV2(request);

		assertNotNull(response);
		assertNotNull(response.getResponse());
		assertFalse(response.getResponse().getApplications().isEmpty());
		assertEquals(1, response.getResponse().getApplications().size());
		assertEquals("app123", response.getResponse().getApplications().get(0).getApplicationId());
	}

}