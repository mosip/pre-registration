package io.mosip.preregistration.application.test.service.util;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.mosip.preregistration.application.errorcodes.DocumentErrorCodes;
import io.mosip.preregistration.application.errorcodes.DocumentErrorMessages;
import io.mosip.preregistration.application.exception.DemographicGetDetailsException;
import io.mosip.preregistration.application.exception.DocumentNotValidException;
import io.mosip.preregistration.application.exception.DocumentSizeExceedException;
import io.mosip.preregistration.core.code.StatusCodes;
import io.mosip.preregistration.core.common.dto.DemographicResponseDTO;
import io.mosip.preregistration.core.common.dto.ExceptionJSONInfoDTO;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.entity.DemographicEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import io.mosip.commons.khazana.spi.ObjectStoreAdapter;
import io.mosip.kernel.core.virusscanner.exception.VirusScannerException;
import io.mosip.kernel.core.virusscanner.spi.VirusScanner;
import io.mosip.preregistration.application.dto.DocumentRequestDTO;
import io.mosip.preregistration.application.exception.InvalidDocumentIdExcepion;
import io.mosip.preregistration.application.service.DemographicService;
import io.mosip.preregistration.application.service.util.CommonServiceUtil;
import io.mosip.preregistration.application.service.util.DocumentServiceUtil;
import io.mosip.preregistration.core.common.entity.DocumentEntity;
import io.mosip.preregistration.core.exception.InvalidRequestException;
import io.mosip.preregistration.core.util.RequestValidator;
import io.mosip.preregistration.core.util.ValidationUtil;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Sanober Noor
 * @since 1.0.0
 * @author Aiham Hasan
 * @since 1.2.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DocumentServiceUtil.class)
public class DocumentServiceUtilTest {

	@MockBean
	private VirusScanner<Boolean, InputStream> virusScan;

	List<DocumentEntity> docEntity = new ArrayList<>();

	@MockBean(name = "restTemplateConfig")
	RestTemplate restTemplate;

	@MockBean
	private ValidationUtil util;

	@MockBean
	private CommonServiceUtil commonServiceUtil;

	@Autowired
	private DocumentServiceUtil documentServiceUtil;

	private MockMultipartFile mockMultipartFile;

	@MockBean
	private RequestValidator requestValidator;

	@MockBean
	private DemographicService demographicServiceIntf;

	@MockBean(name = "S3Adapter")
	private ObjectStoreAdapter objectStore;

	String preRegistrationId = "48690172097498";
	DocumentRequestDTO documentDto = new DocumentRequestDTO("address", "POA", "ENG", "test");
	File file;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		ReflectionTestUtils.setField(documentServiceUtil, "utcDateTimePattern", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		ReflectionTestUtils.setField(commonServiceUtil, "utcDateTimePattern", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		ReflectionTestUtils.setField(documentServiceUtil, "commonServiceUtil", commonServiceUtil);

		ClassLoader classLoader = getClass().getClassLoader();
		URI uri = new URI(classLoader.getResource("Doc.pdf").getFile().trim().replaceAll("\\u0020", "%20"));
		file = new File(uri.getPath());
		mockMultipartFile = new MockMultipartFile("file", "Doc.pdf", "mixed/multipart", new FileInputStream(file));
	}

	@Test
	public void getDateStringTest() {
		String responseTime = documentServiceUtil.getDateString(new Date());
		assertNotNull(responseTime);
	}

	@Test
	public void parseDocumentIdTest() {
		String validDocumentId = "1234";
		Integer documentId = documentServiceUtil.parseDocumentId(validDocumentId);
		assertEquals(Integer.valueOf(validDocumentId), documentId);
	}

	@Test(expected = InvalidDocumentIdExcepion.class)
	public void parseDocumentIdFailureTest() throws Exception {
		documentServiceUtil.parseDocumentId("1234!@#$&^$$~~~~~~#@!$^%");
	}

	@Test(expected = InvalidRequestException.class)
	public void isValidCatCodeTest() {
		documentServiceUtil.isValidCatCode("13fww");
	}

	@Test(expected = InvalidRequestException.class)
	public void inValidPreIDTest() {
		documentServiceUtil.isValidRequest(documentDto, null);
	}

	@Test(expected = VirusScannerException.class)
	public void virusscannerFailureTest() throws Exception {
		when(virusScan.scanDocument(mockMultipartFile.getBytes())).thenThrow(VirusScannerException.class);
		documentServiceUtil.virusScanCheck(mockMultipartFile);
	}

	@Test
	public void test_valid_json_document_string_parsing() throws Exception {
		DocumentServiceUtil documentServiceUtil = new DocumentServiceUtil();
		ReflectionTestUtils.setField(documentServiceUtil, "utcDateTimePattern", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

		String preRegistrationId = "12345678901234";
		String documentJsonString = "{"
				+ "\"id\": \"mosip.pre-registration.document.upload\","
				+ "\"version\": \"1.0\","
				+ "\"requesttime\": \"2023-01-01T10:00:00.000Z\","
				+ "\"request\": {"
				+ "  \"docCatCode\": \"POA\","
				+ "  \"docTypCode\": \"RNC\","
				+ "  \"langCode\": \"eng\","
				+ "  \"refNumber\": \"REF123456\""
				+ "}}";

		MainRequestDTO<DocumentRequestDTO> result = documentServiceUtil.createUploadDto(documentJsonString, preRegistrationId);

		assertNotNull(result);
		assertEquals("mosip.pre-registration.document.upload", result.getId());
		assertEquals("1.0", result.getVersion());
		assertNotNull(result.getRequesttime());

		DocumentRequestDTO documentDto = result.getRequest();
		assertNotNull(documentDto);
		assertEquals("POA", documentDto.getDocCatCode());
		assertEquals("RNC", documentDto.getDocTypCode());
		assertEquals("eng", documentDto.getLangCode());
		assertEquals("REF123456", documentDto.getRefNumber());
	}

	public void test_null_or_empty_requesttime_handling() throws Exception {
		DocumentServiceUtil documentServiceUtil = new DocumentServiceUtil();
		ReflectionTestUtils.setField(documentServiceUtil, "utcDateTimePattern", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

		String preRegistrationId = "12345678901234";
		String documentJsonString = "{"
				+ "\"id\": \"mosip.pre-registration.document.upload\","
				+ "\"version\": \"1.0\","
				+ "\"requesttime\": \"\","
				+ "\"request\": {"
				+ "  \"docCatCode\": \"POA\","
				+ "  \"docTypCode\": \"RNC\","
				+ "  \"langCode\": \"eng\","
				+ "  \"refNumber\": \"REF123456\""
				+ "}}";

		MainRequestDTO<DocumentRequestDTO> result = documentServiceUtil.createUploadDto(documentJsonString, preRegistrationId);

		assertNotNull(result);
		assertEquals("mosip.pre-registration.document.upload", result.getId());
		assertEquals("1.0", result.getVersion());
		assertNull(result.getRequesttime());

		documentJsonString = "{"
				+ "\"id\": \"mosip.pre-registration.document.upload\","
				+ "\"version\": \"1.0\","
				+ "\"requesttime\": null,"
				+ "\"request\": {"
				+ "  \"docCatCode\": \"POA\","
				+ "  \"docTypCode\": \"RNC\","
				+ "  \"langCode\": \"eng\","
				+ "  \"refNumber\": \"REF123456\""
				+ "}}";

		result = documentServiceUtil.createUploadDto(documentJsonString, preRegistrationId);

		assertNotNull(result);
		assertNull(result.getRequesttime());
	}

	@Test
	public void test_dto_to_entity_creates_document_entity_with_correct_values() {
		DocumentServiceUtil documentServiceUtil = new DocumentServiceUtil();
		DocumentRequestDTO dto = new DocumentRequestDTO();
		dto.setDocCatCode("POA");
		dto.setDocTypCode("Passport");
		dto.setLangCode("eng");
		dto.setRefNumber("REF123456");

		String userId = "testUser";
		String preRegistrationId = "12345678901234";
		DocumentEntity existingEntity = null;

		MultipartFile file = new MockMultipartFile(
				"testFile.pdf",
				"testFile.pdf",
				"application/pdf",
				"test content".getBytes()
		);

		DocumentEntity result = documentServiceUtil.dtoToEntity(file, dto, userId, preRegistrationId, existingEntity);

		assertNotNull(result);
		assertNotNull(result.getDocumentId());
		assertEquals(preRegistrationId + "/" + dto.getDocCatCode() + "_" + result.getDocumentId(), result.getDocId());
		assertEquals(preRegistrationId, result.getDemographicEntity().getPreRegistrationId());
		assertEquals(dto.getDocCatCode(), result.getDocCatCode());
		assertEquals(dto.getDocTypCode(), result.getDocTypeCode());
		assertEquals("pdf", result.getDocFileFormat());
		assertEquals(StatusCodes.DOCUMENT_UPLOADED.getCode(), result.getStatusCode());
		assertEquals(dto.getLangCode(), result.getLangCode());
		assertEquals(userId, result.getCrBy());
		assertEquals(userId, result.getUpdBy());
		assertEquals(dto.getRefNumber(), result.getRefNumber());
		assertNotNull(result.getCrDtime());
		assertNotNull(result.getUpdDtime());
	}

	@Test
	public void test_dto_to_entity_handles_null_values_in_dto() {
		DocumentServiceUtil documentServiceUtil = new DocumentServiceUtil();
		DocumentRequestDTO dto = new DocumentRequestDTO();

		String userId = "testUser";
		String preRegistrationId = "12345678901234";
		DocumentEntity existingEntity = null;

		MultipartFile file = new MockMultipartFile(
				"testFile.jpg",
				"testFile.jpg",
				"image/jpeg",
				"test content".getBytes()
		);

		DocumentEntity result = documentServiceUtil.dtoToEntity(file, dto, userId, preRegistrationId, existingEntity);

		assertNotNull(result);
		assertNotNull(result.getDocumentId());
		assertEquals(preRegistrationId + "/null_" + result.getDocumentId(), result.getDocId());
		assertEquals(preRegistrationId, result.getDemographicEntity().getPreRegistrationId());
		assertNull(result.getDocCatCode());
		assertNull(result.getDocTypeCode());
		assertEquals("jpg", result.getDocFileFormat());
		assertEquals(StatusCodes.DOCUMENT_UPLOADED.getCode(), result.getStatusCode());
		assertNull(result.getLangCode());
		assertEquals(userId, result.getCrBy());
		assertEquals(userId, result.getUpdBy());
		assertNull(result.getRefNumber());
		assertNotNull(result.getCrDtime());
		assertNotNull(result.getUpdDtime());
	}

	@Test
	public void test_file_size_less_than_max_returns_true() {
		DocumentServiceUtil documentServiceUtil = new DocumentServiceUtil();

		ReflectionTestUtils.setField(documentServiceUtil, "maxFileSize", 2);

		long uploadedFileSize = 1 * 1024 * 1024;

		boolean result = documentServiceUtil.fileSizeCheck(uploadedFileSize);

		assertTrue(result, "File size check should return true when file size is less than maximum allowed size");
	}

	@Test
	public void test_file_size_exceeds_max_throws_exception() {
		DocumentServiceUtil documentServiceUtil = new DocumentServiceUtil();

		ReflectionTestUtils.setField(documentServiceUtil, "maxFileSize", 2);

		long uploadedFileSize = 2 * 1024 * 1024;

		DocumentSizeExceedException exception = assertThrows(
				DocumentSizeExceedException.class,
				() -> documentServiceUtil.fileSizeCheck(uploadedFileSize),
				"Expected DocumentSizeExceedException to be thrown when file size equals maximum allowed size"
		);

		assertEquals(DocumentErrorCodes.PRG_PAM_DOC_007.toString(), exception.getErrorCode());
		assertEquals(DocumentErrorMessages.DOCUMENT_EXCEEDING_PREMITTED_SIZE.getMessage(), exception.getErrorText());
	}

	@Test
	public void test_valid_file_extension_returns_true() throws Exception {
		DocumentServiceUtil documentServiceUtil = new DocumentServiceUtil();

		Field fileExtensionField = DocumentServiceUtil.class.getDeclaredField("fileExtension");
		fileExtensionField.setAccessible(true);
		fileExtensionField.set(documentServiceUtil, "PDF, PNG, JPEG");

		MultipartFile mockFile = mock(MultipartFile.class);
		when(mockFile.getOriginalFilename()).thenReturn("document.PDF");

		boolean result = documentServiceUtil.fileExtensionCheck(mockFile);

		assertTrue(result);
	}

	@Test
	public void test_invalid_file_extension_throws_exception() throws Exception {
		DocumentServiceUtil documentServiceUtil = new DocumentServiceUtil();

		Field fileExtensionField = DocumentServiceUtil.class.getDeclaredField("fileExtension");
		fileExtensionField.setAccessible(true);
		fileExtensionField.set(documentServiceUtil, "PDF, PNG, JPEG");

		MultipartFile mockFile = mock(MultipartFile.class);
		when(mockFile.getOriginalFilename()).thenReturn("document.TXT");

		DocumentNotValidException exception = assertThrows(DocumentNotValidException.class, () -> {
			documentServiceUtil.fileExtensionCheck(mockFile);
		});

		assertEquals(DocumentErrorCodes.PRG_PAM_DOC_004.toString(), exception.getErrorCode());
		assertEquals(DocumentErrorMessages.DOCUMENT_INVALID_FORMAT.getMessage(), exception.getErrorText());
	}

	@Test
	public void test_get_pre_reg_info_rest_service_success() {
		String preId = "12345678901234";
		DocumentServiceUtil documentServiceUtil = new DocumentServiceUtil();
		CommonServiceUtil commonServiceUtil = Mockito.mock(CommonServiceUtil.class);
		ReflectionTestUtils.setField(documentServiceUtil, "commonServiceUtil", commonServiceUtil);

		MainResponseDTO<DemographicResponseDTO> mainResponseDTO = new MainResponseDTO<>();
		DemographicResponseDTO demographicResponseDTO = new DemographicResponseDTO();
		demographicResponseDTO.setPreRegistrationId(preId);
		demographicResponseDTO.setCreatedBy("testUser");
		mainResponseDTO.setResponse(demographicResponseDTO);
		mainResponseDTO.setErrors(null);

		Mockito.when(commonServiceUtil.getDemographicData(preId)).thenReturn(mainResponseDTO);

		DemographicResponseDTO result = documentServiceUtil.getPreRegInfoRestService(preId);

		Assertions.assertNotNull(result);
		Assertions.assertEquals(preId, result.getPreRegistrationId());
		Assertions.assertEquals("testUser", result.getCreatedBy());
		Mockito.verify(commonServiceUtil, Mockito.times(1)).getDemographicData(preId);
	}

	@Test
	public void test_get_pre_reg_info_rest_service_with_errors() {
		String preId = "12345678901234";
		DocumentServiceUtil documentServiceUtil = new DocumentServiceUtil();
		CommonServiceUtil commonServiceUtil = Mockito.mock(CommonServiceUtil.class);
		ReflectionTestUtils.setField(documentServiceUtil, "commonServiceUtil", commonServiceUtil);

		MainResponseDTO<DemographicResponseDTO> mainResponseDTO = new MainResponseDTO<>();
		List<ExceptionJSONInfoDTO> errors = new ArrayList<>();
		ExceptionJSONInfoDTO error = new ExceptionJSONInfoDTO();
		error.setErrorCode("PRG_PAM_APP_005");
		error.setMessage("Unable to fetch the pre-registration details");
		errors.add(error);
		mainResponseDTO.setErrors(errors);

		Mockito.when(commonServiceUtil.getDemographicData(preId)).thenReturn(mainResponseDTO);

		DemographicGetDetailsException exception = Assertions.assertThrows(
				DemographicGetDetailsException.class,
				() -> documentServiceUtil.getPreRegInfoRestService(preId)
		);

		Assertions.assertEquals("PRG_PAM_APP_005", exception.getErrorCode());
		Assertions.assertEquals("Unable to fetch the pre-registration details", exception.getErrorText());
		Mockito.verify(commonServiceUtil, Mockito.times(1)).getDemographicData(preId);
	}

	@Test
	public void test_handles_no_document_entities() throws org.json.simple.parser.ParseException {
		DocumentServiceUtil documentServiceUtil = new DocumentServiceUtil();
		DemographicEntity demographicEntity = new DemographicEntity();
		demographicEntity.setPreRegistrationId("12345");
		demographicEntity.setDocumentEntity(new ArrayList<>());

		DocumentServiceUtil spyUtil = Mockito.spy(documentServiceUtil);
		List<String> mandatoryDocs = Arrays.asList("POA", "POI");
		Mockito.doReturn(mandatoryDocs).when(spyUtil).validMandatoryDocuments(demographicEntity);

		boolean result = spyUtil.isMandatoryDocumentDeleted(demographicEntity);

		assertTrue(result);
	}

	@Test
	public void test_non_pdf_files_return_false() throws java.io.IOException {
		DocumentServiceUtil documentServiceUtil = new DocumentServiceUtil();
		ReflectionTestUtils.setField(documentServiceUtil, "fileExtension", "PDF,DOC,DOCX");

		MultipartFile mockFile = mock(MultipartFile.class);
		when(mockFile.getContentType()).thenReturn("application/msword");

		boolean result = documentServiceUtil.isPasswordProtectedFile(mockFile);

		assertFalse(result);
		verify(mockFile, never()).getInputStream();
	}

}