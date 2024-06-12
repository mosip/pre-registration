package io.mosip.preregistration.application.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.commons.khazana.spi.ObjectStoreAdapter;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.exception.IOException;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.exception.JsonMappingException;
import io.mosip.kernel.core.util.exception.JsonParseException;
import io.mosip.preregistration.application.dto.DocumentRequestDTO;
import io.mosip.preregistration.application.dto.DocumentResponseDTO;
import io.mosip.preregistration.application.exception.DocumentFailedToCopyException;
import io.mosip.preregistration.application.exception.DocumentNotFoundException;
import io.mosip.preregistration.application.exception.FSServerException;
import io.mosip.preregistration.application.exception.InvalidDocumentIdExcepion;
import io.mosip.preregistration.application.exception.RecordFailedToUpdateException;
import io.mosip.preregistration.application.exception.RecordNotFoundException;
import io.mosip.preregistration.application.repository.DocumentDAO;
import io.mosip.preregistration.application.service.util.DocumentServiceUtil;
import io.mosip.preregistration.core.code.RequestCodes;
import io.mosip.preregistration.core.common.dto.DemographicResponseDTO;
import io.mosip.preregistration.core.common.dto.DocumentDTO;
import io.mosip.preregistration.core.common.dto.DocumentDeleteResponseDTO;
import io.mosip.preregistration.core.common.dto.DocumentMultipartResponseDTO;
import io.mosip.preregistration.core.common.dto.DocumentsMetaData;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.entity.DemographicEntity;
import io.mosip.preregistration.core.common.entity.DocumentEntity;
import io.mosip.preregistration.core.exception.InvalidRequestException;
import io.mosip.preregistration.core.exception.PreRegistrationException;
import io.mosip.preregistration.core.util.AuditLogUtil;
import io.mosip.preregistration.core.util.CryptoUtil;
import io.mosip.preregistration.core.util.HashUtill;
import io.mosip.preregistration.core.util.ValidationUtil;

@RunWith(JUnit4.class)
@SpringBootTest
@ContextConfiguration(classes = { DocumentService.class })
public class DocumentServiceTest {

	@InjectMocks
	private DocumentService documentUploadService;

	@Mock
	private DocumentServiceUtil serviceUtil;

	@Mock
	private DocumentDAO documnetDAO;

	@Mock
	private ValidationUtil validationutil;

	@Mock
	private AuditLogUtil auditLogUtil;

	@Mock
	private CryptoUtil cryptoUtil;

	@Mock
	private ObjectStoreAdapter objectStore;

	@Value("${mosip.preregistration.document.scan}")
	private Boolean scanDocument;

	@Value("${mosip.kernel.objectstore.account-name}")
	private String objectStoreAccountName;

	private DocumentEntity documentEntity;

	private File file;

	private DemographicEntity demographicEntity;

	String preRegistrationId = "48690172097498";

	private MockMultipartFile mockMultipartFile;

	// MultipartFile multipartFile;
	private MockMultipartFile multipartFile;

	String docJson;

	DocumentResponseDTO docResp = new DocumentResponseDTO();
	DocumentRequestDTO document = new DocumentRequestDTO();
	DemographicResponseDTO demographicResponseDTO = new DemographicResponseDTO();
	MainResponseDTO<DocumentResponseDTO> responseUpload = new MainResponseDTO<>();

	DocumentRequestDTO documentRequestDTO = new DocumentRequestDTO("RNC", "POA", "eng", "123");
	MainRequestDTO<DocumentRequestDTO> documentRequestDTOList = new MainRequestDTO<DocumentRequestDTO>();

	String documentId = "1";

	MainResponseDTO<DocumentDeleteResponseDTO> responsedelete = new MainResponseDTO<>();

	@Before
	public void setUp() throws URISyntaxException, FileNotFoundException, java.io.IOException {
		MockitoAnnotations.initMocks(this);

		ClassLoader classLoader = getClass().getClassLoader();
		URI uri = new URI(classLoader.getResource("Doc.pdf").getFile().trim().replaceAll("\\u0020", "%20"));
		file = new File(uri.getPath());
		InputStream sourceFile = new FileInputStream(file);

		byte[] cephBytes = IOUtils.toByteArray(sourceFile);

		demographicEntity = new DemographicEntity();

		demographicEntity.setCreateDateTime(LocalDateTime.now());
		demographicEntity.setCreatedBy("Jagadishwari");
		demographicEntity.setStatusCode("Pending_Appointment");
		demographicEntity.setUpdateDateTime(LocalDateTime.now());
		demographicEntity.setPreRegistrationId(preRegistrationId);

		documentEntity = new DocumentEntity(demographicEntity, "1", "Doc.pdf", "POA", "RNC", "PDF",
				"Pending_Appointment", "eng", "Jagadishwari", DateUtils.parseDateToLocalDateTime(new Date()),
				"Jagadishwari", DateUtils.parseDateToLocalDateTime(new Date()),
				DateUtils.parseDateToLocalDateTime(new Date()), "1", new String(HashUtill.hashUtill(cephBytes)), "123");

		AuthUserDetails applicationUser = Mockito.mock(AuthUserDetails.class);
		Authentication authentication = Mockito.mock(Authentication.class);
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(applicationUser);

		Date date = new Date();
		SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		String presentDate = dateformat.format(date);

		docJson = "{\"id\": \"mosip.pre-registration.document.upload\",\"version\" : \"1.0\"," + "\"requesttime\" : \""
				+ presentDate + "\",\"request\" :" + "{\"docCatCode\" "
				+ ": \"POA\",\"docTypCode\" : \"RNC\",\"langCode\":\"eng\"}}";

		mockMultipartFile = new MockMultipartFile("file", "Doc.pdf", "mixed/multipart", new FileInputStream(file));

		ReflectionTestUtils.setField(documentUploadService, "scanDocument", true);

		ReflectionTestUtils.setField(documentUploadService, "objectStoreAccountName", "abcd");

		multipartFile = new MockMultipartFile("file", "Doc.pdf", "mixed/multipart", new FileInputStream(file));

	}

	@Test
	public void getAllDocumentForPreIdSuccessTest() throws Exception {
		List<DocumentMultipartResponseDTO> documentGetAllDtos = new ArrayList<>();

		List<DocumentEntity> documentEntities = new ArrayList<>();
		documentEntities.add(documentEntity);
		DocumentsMetaData metadata = new DocumentsMetaData();
		DocumentMultipartResponseDTO allDocDto = new DocumentMultipartResponseDTO();
		allDocDto.setDocCatCode(documentEntity.getDocCatCode());
		allDocDto.setDocName(documentEntity.getDocName());
		allDocDto.setDocumentId(documentEntity.getDocumentId());
		allDocDto.setDocTypCode(documentEntity.getDocTypeCode());
		documentGetAllDtos.add(allDocDto);

		MainResponseDTO<DocumentsMetaData> responseDto = new MainResponseDTO<>();
		metadata.setDocumentsMetaData(documentGetAllDtos);
		responseDto.setResponse(metadata);

		Mockito.when(validationutil.requstParamValidator(Mockito.any())).thenReturn(true);
		DemographicResponseDTO obj = new DemographicResponseDTO();
		Mockito.when(serviceUtil.getPreRegInfoRestService(Mockito.any())).thenReturn(obj);
		Mockito.when(documnetDAO.findBypreregId(Mockito.any())).thenReturn(documentEntities);
		MainResponseDTO<DocumentsMetaData> serviceResponseDto = documentUploadService
				.getAllDocumentForPreId("48690172097498");
		assertEquals(serviceResponseDto.getResponse().getDocumentsMetaData().get(0).getDocumentId(),
				responseDto.getResponse().getDocumentsMetaData().get(0).getDocumentId());
	}

	@Test(expected = DocumentNotFoundException.class)
	public void getAllDocumentDocumentNotFoundExceptionTest() throws Exception {
		List<DocumentMultipartResponseDTO> documentGetAllDtos = new ArrayList<>();

		List<DocumentEntity> documentEntities = new ArrayList<>();
		documentEntities.add(documentEntity);
		DocumentsMetaData metadata = new DocumentsMetaData();
		DocumentMultipartResponseDTO allDocDto = new DocumentMultipartResponseDTO();
		allDocDto.setDocCatCode(documentEntity.getDocCatCode());
		allDocDto.setDocName(documentEntity.getDocName());
		allDocDto.setDocumentId(documentEntity.getDocumentId());
		allDocDto.setDocTypCode(documentEntity.getDocTypeCode());
		documentGetAllDtos.add(allDocDto);

		MainResponseDTO<DocumentsMetaData> responseDto = new MainResponseDTO<>();
		metadata.setDocumentsMetaData(documentGetAllDtos);
		responseDto.setResponse(metadata);

		Mockito.when(validationutil.requstParamValidator(Mockito.any())).thenReturn(true);
		DemographicResponseDTO obj = new DemographicResponseDTO();
		Mockito.when(serviceUtil.getPreRegInfoRestService(Mockito.any())).thenReturn(obj);
		Mockito.when(documnetDAO.findBypreregId(Mockito.any()))
				.thenThrow(new DocumentNotFoundException("ErrCode", "failed"));
		MainResponseDTO<DocumentsMetaData> serviceResponseDto = documentUploadService
				.getAllDocumentForPreId("48690172097498");
	}

	@Test(expected = DocumentNotFoundException.class)
	public void copyDocumentDocumentNotFoundExceptionTest() throws Exception {

		Mockito.when(serviceUtil.isValidCatCode(Mockito.any())).thenReturn(true);

		documentUploadService.copyDocument("POA", "987654321", "48690172097499");
	}

	@Test
	public void copyDocumentSuccesssTest() throws Exception {
		docResp.setDocName("Doc.pdf");

		Mockito.when(serviceUtil.isValidCatCode(Mockito.any())).thenReturn(true);
		Mockito.when(documnetDAO.findSingleDocument(Mockito.any(), Mockito.any())).thenReturn(documentEntity);

		Mockito.when(serviceUtil.getPreRegInfoRestService(Mockito.any())).thenReturn(demographicResponseDTO);
		Mockito.when(serviceUtil.getPreRegInfoRestService(Mockito.any())).thenReturn(demographicResponseDTO);

		Mockito.when(
				documnetDAO.saveDocument(serviceUtil.documentEntitySetter(Mockito.any(), Mockito.any(), Mockito.any())))
				.thenReturn(documentEntity);
		Mockito.when(objectStore.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any())).thenReturn(true);
		MainResponseDTO<DocumentResponseDTO> responseDto = documentUploadService.copyDocument("POA", "987654321",
				"48690172097499");
		assertEquals(docResp.getDocName(), responseDto.getResponse().getDocName());

	}

	@Test(expected = InvalidRequestException.class)
	public void InvalidRequestParameterExceptionTest1() throws Exception {
		documentUploadService.copyDocument("POA", "", "48690172097499");
	}

	@Test(expected = InvalidRequestException.class)
	public void InvalidRequestParameterExceptionTest2() throws Exception {
		documentUploadService.copyDocument("POA", "48690172097499", "");
	}

	@Test(expected = InvalidRequestException.class)
	public void InvalidRequestParameterExceptionTest() throws Exception {
		documentUploadService.copyDocument(null, null, null);
	}

	// @Test
	// public void uploadDocumentSuccessTest() throws JSONException,
	// JsonParseException, JsonMappingException, IOException, ParseException {
	// documentRequestDTOList.setRequest(documentRequestDTO);
	// documentRequestDTOList.setId("mosip.Doc");
	// documentRequestDTOList.setVersion("0.1");
	// docResp.setDocCatCode("POA");
	// docResp.setDocTypCode("RNC");
	// responseUpload.setResponse(docResp);
	// Map<String, String> map = new HashMap<>();
	// Mockito.when(serviceUtil.createUploadDto(Mockito.any(),
	// Mockito.any())).thenReturn(documentRequestDTOList);
	// Mockito.when(validationutil.requestValidator(Mockito.any(),
	// Mockito.any())).thenReturn(true);
	// Mockito.when(serviceUtil.fileExtensionCheck(Mockito.any())).thenReturn(true);
	// Mockito.when(serviceUtil.fileSizeCheck(Mockito.any())).thenReturn(true);
	//
	// MainResponseDTO<DocumentResponseDTO> responseDto =
	// documentUploadService.uploadDocument(mockMultipartFile,
	// docJson, preRegistrationId);
	// assertEquals(responseUpload.getResponse().getDocCatCode(),
	// responseDto.getResponse().getDocCatCode());
	// }

	@Test(expected = RecordFailedToUpdateException.class)
	public void createDocRecordFailedToUpdateExceptionTest() throws Exception {
		Mockito.when(serviceUtil.getPreRegInfoRestService(Mockito.any())).thenReturn(demographicResponseDTO);
		Mockito.when(documnetDAO.findSingleDocument(Mockito.any(), Mockito.any())).thenReturn(documentEntity);
		Mockito.when(validationutil.isStatusBookedOrExpired(documentEntity.getDemographicEntity().getStatusCode()))
				.thenReturn(true);
		documentUploadService.createDoc(document, multipartFile, preRegistrationId);
	}

	@Test(expected = FSServerException.class)
	public void createDocFSServerExceptionTest() throws Exception {
		Mockito.when(serviceUtil.getPreRegInfoRestService(Mockito.any())).thenReturn(demographicResponseDTO);
		Mockito.when(documnetDAO.findSingleDocument(Mockito.any(), Mockito.any())).thenReturn(documentEntity);
		Mockito.when(validationutil.isStatusBookedOrExpired(documentEntity.getDemographicEntity().getStatusCode()))
				.thenReturn(false);
		Mockito.when(serviceUtil.dtoToEntity(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(documentEntity);

		InputStream sourceFile = new FileInputStream(file);

		byte[] cephBytes = IOUtils.toByteArray(sourceFile);
		Mockito.when(cryptoUtil.encrypt(Mockito.any(), Mockito.any())).thenReturn(cephBytes);

		Mockito.when(documnetDAO.saveDocument(Mockito.any())).thenReturn(documentEntity);

		Mockito.when(objectStore.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any())).thenReturn(false);

		documentUploadService.createDoc(document, multipartFile, preRegistrationId);
	}

	@Test
	public void createDocTest() throws Exception {

		docResp.setDocName("Doc.pdf");
		Mockito.when(serviceUtil.getPreRegInfoRestService(Mockito.any())).thenReturn(demographicResponseDTO);
		Mockito.when(documnetDAO.findSingleDocument(Mockito.any(), Mockito.any())).thenReturn(documentEntity);
		Mockito.when(validationutil.isStatusBookedOrExpired(documentEntity.getDemographicEntity().getStatusCode()))
				.thenReturn(false);
		Mockito.when(serviceUtil.dtoToEntity(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(documentEntity);

		InputStream sourceFile = new FileInputStream(file);

		byte[] cephBytes = IOUtils.toByteArray(sourceFile);
		Mockito.when(cryptoUtil.encrypt(Mockito.any(), Mockito.any())).thenReturn(cephBytes);

		Mockito.when(documnetDAO.saveDocument(Mockito.any())).thenReturn(documentEntity);

		Mockito.when(objectStore.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any())).thenReturn(true);

		DocumentResponseDTO responseDto = documentUploadService.createDoc(document, multipartFile, preRegistrationId);

		assertEquals(docResp.getDocName(), responseDto.getDocName());

	}

	@Test(expected = FSServerException.class)
	public void copyFileFSServerExceptionTest() throws Exception {
		DocumentEntity copyDocumentEntity = new DocumentEntity();
		DemographicEntity demographicEntity = new DemographicEntity();
		demographicEntity.setPreRegistrationId(preRegistrationId);
		copyDocumentEntity.setDocCatCode("POA");
		copyDocumentEntity.setDocId("1");
		copyDocumentEntity.setDemographicEntity(demographicEntity);
		documentUploadService.copyFile(copyDocumentEntity, "sourseName", "key");
	}

	@Test(expected = DocumentFailedToCopyException.class)
	public void copyFileDocumentFailedToCopyExceptionTest() throws Exception {
		documentUploadService.copyFile(null, "sourseName", "key");
	}

	@Test
	public void copyFileTest() throws Exception {
		DocumentEntity copyDocumentEntity = new DocumentEntity();
		DemographicEntity demographicEntity = new DemographicEntity();
		demographicEntity.setPreRegistrationId(preRegistrationId);
		copyDocumentEntity.setDocCatCode("POA");
		copyDocumentEntity.setDocId("1");
		copyDocumentEntity.setDemographicEntity(demographicEntity);

		Mockito.when(objectStore.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any())).thenReturn(true);

		DocumentService documentUploadService = Mockito.mock(DocumentService.class);
		Mockito.doNothing().when(documentUploadService).copyFile(Mockito.isA(DocumentEntity.class),
				Mockito.isA(String.class), Mockito.isA(String.class));
		documentUploadService.copyFile(copyDocumentEntity, "sourseName", "key");
		Mockito.verify(documentUploadService, Mockito.times(1)).copyFile(copyDocumentEntity, "sourseName", "key");
	}

	@Test(expected = InvalidDocumentIdExcepion.class)
	public void invalidDocumentIdExcepionTest() {

		Mockito.when(serviceUtil.getPreRegInfoRestService(Mockito.any())).thenReturn(demographicResponseDTO);
		Mockito.when(validationutil.requstParamValidator(Mockito.any())).thenReturn(true);
		Mockito.when(documnetDAO.findBydocumentId(Mockito.any())).thenReturn(documentEntity);
		MainResponseDTO<DocumentDeleteResponseDTO> responseDto = documentUploadService.deleteDocument(documentId,
				"1234567890");
		assertEquals(responseDto.getResponse().getMessage(), responsedelete.getResponse().getMessage());
	}

	@Test(expected = FSServerException.class)
	public void deleteDocumentFSServerExceptionTest() {
		demographicResponseDTO.setStatusCode("Pending_Appointment");
		Mockito.when(serviceUtil.getPreRegInfoRestService(Mockito.any())).thenReturn(demographicResponseDTO);
		Mockito.when(validationutil.requstParamValidator(Mockito.any())).thenReturn(true);
		Mockito.when(documnetDAO.findBydocumentId(Mockito.any())).thenReturn(documentEntity);
		Mockito.when(documnetDAO.deleteAllBydocumentId(documentId)).thenReturn(1);
		Mockito.when(documnetDAO.getDemographicEntityForPrid(preRegistrationId))
				.thenThrow(new DocumentNotFoundException());
		MainResponseDTO<DocumentDeleteResponseDTO> responseDto = documentUploadService.deleteDocument(documentId,
				"48690172097498");

	}

	@Test
	public void deleteDocumentSuccessTest() throws org.json.simple.parser.ParseException {
		demographicResponseDTO.setStatusCode("Pending_Appointment");
		List<String> doc = new ArrayList<String>();
		doc.add("1");
		DocumentDeleteResponseDTO response = new DocumentDeleteResponseDTO();
		response.setMessage("Document successfully deleted");
		responsedelete.setResponse(response);
		Mockito.when(
				objectStore.deleteObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(true);
		Mockito.when(serviceUtil.validMandatoryDocuments(Mockito.any())).thenReturn(doc);
		Mockito.when(serviceUtil.getPreRegInfoRestService(Mockito.any())).thenReturn(demographicResponseDTO);
		Mockito.when(validationutil.requstParamValidator(Mockito.any())).thenReturn(true);
		Mockito.when(documnetDAO.findBydocumentId(Mockito.any())).thenReturn(documentEntity);
		Mockito.when(documnetDAO.deleteAllBydocumentId(documentId)).thenReturn(1);
		Mockito.when(documnetDAO.getDemographicEntityForPrid(preRegistrationId)).thenReturn(demographicEntity);
		MainResponseDTO<DocumentDeleteResponseDTO> responseDto = documentUploadService.deleteDocument(documentId,
				"48690172097498");

		assertEquals(responseDto.getResponse().getMessage(), responsedelete.getResponse().getMessage());
	}

	@Test(expected = InvalidDocumentIdExcepion.class)
	public void getDocumentForDocIdInvalidDocumentIdExcepionTest() {

		Mockito.when(validationutil.requstParamValidator(Mockito.any())).thenReturn(true);
		DemographicResponseDTO obj = new DemographicResponseDTO();
		Mockito.when(serviceUtil.getPreRegInfoRestService(Mockito.any())).thenReturn(obj);
		Mockito.when(documnetDAO.findBydocumentId(Mockito.any())).thenReturn(documentEntity);

		documentUploadService.getDocumentForDocId("", "");
	}

	@Test(expected = FSServerException.class)
	public void getDocumentForDocIdFSServerExceptionTest() {

		Mockito.when(validationutil.requstParamValidator(Mockito.any())).thenReturn(true);
		DemographicResponseDTO obj = new DemographicResponseDTO();
		Mockito.when(serviceUtil.getPreRegInfoRestService(Mockito.any())).thenReturn(obj);
		Mockito.when(documnetDAO.findBydocumentId(Mockito.any())).thenReturn(documentEntity);

		documentUploadService.getDocumentForDocId("", "48690172097498");
	}

	@Test
	public void getDocumentForDocIdSuccessTest() throws FileNotFoundException {

		Mockito.when(validationutil.requstParamValidator(Mockito.any())).thenReturn(true);
		DemographicResponseDTO obj = new DemographicResponseDTO();
		Mockito.when(serviceUtil.getPreRegInfoRestService(Mockito.any())).thenReturn(obj);
		Mockito.when(documnetDAO.findBydocumentId(Mockito.any())).thenReturn(documentEntity);
		InputStream sourceFile = new FileInputStream(file);

		Mockito.when(objectStore.getObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(sourceFile);
		MainResponseDTO<DocumentDTO> responseDTO = documentUploadService.getDocumentForDocId("", "48690172097498");
		assertNotNull(responseDTO.getResponse());
	}

	@Test(expected = PreRegistrationException.class)
	public void getDocumentForDocIdPreRegistrationExceptionTest() throws FileNotFoundException {
		documentEntity.setDocHash("123");
		Mockito.when(validationutil.requstParamValidator(Mockito.any())).thenReturn(true);
		DemographicResponseDTO obj = new DemographicResponseDTO();
		Mockito.when(serviceUtil.getPreRegInfoRestService(Mockito.any())).thenReturn(obj);
		Mockito.when(documnetDAO.findBydocumentId(Mockito.any())).thenReturn(documentEntity);
		InputStream sourceFile = new FileInputStream(file);
		Mockito.when(objectStore.getObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(sourceFile);
		// Mockito.when(HashUtill.hashUtill(Mockito.any())).thenReturn("123");
		Mockito.when(cryptoUtil.decrypt(Mockito.any(), Mockito.any())).thenReturn("123".getBytes());

		MainResponseDTO<DocumentDTO> responseDTO = documentUploadService.getDocumentForDocId("", "48690172097498");
	}

	@Test(expected = RecordNotFoundException.class)
	public void updateDocRefIdExceptionTest() {
		String preId = "1234";
		MainResponseDTO<String> response = new MainResponseDTO<>();
		Map<String, String> requestParamMap = new HashMap<>();
		response.setResponsetime(serviceUtil.getCurrentResponseTime());
		response.setId("1234");
		response.setVersion("2");
		requestParamMap.put(RequestCodes.PRE_REGISTRATION_ID, preId);
		documentUploadService.updateDocRefId(documentId, preRegistrationId, docJson);
	}

	@Test
	public void prepareRequestParamMapTest() {
		Map<String, String> inputValidation = new HashMap<>();
		documentRequestDTOList.setId(documentId);
		documentRequestDTOList.setVersion("12");
		documentRequestDTOList.setRequesttime(Date.from(Instant.now()));
		documentRequestDTOList.setRequest(documentRequestDTO);
		inputValidation.put(RequestCodes.ID, documentRequestDTOList.getId());
		inputValidation.put(RequestCodes.VER, documentRequestDTOList.getVersion());
		Map<String, String> response = documentUploadService.prepareRequestParamMap(documentRequestDTOList);
		assertEquals(documentRequestDTOList.getId(), "1");
	}

	@Test
	public void deleteAllByPreIdSuccessTest() {
		MainResponseDTO<DocumentDeleteResponseDTO> deleteRes = new MainResponseDTO<>();
		deleteRes.setId("1");
		deleteRes.setVersion("12");
		Mockito.when(validationutil.requstParamValidator(Mockito.any())).thenReturn(true);
		Mockito.when(serviceUtil.getPreRegInfoRestService(Mockito.any())).thenReturn(demographicResponseDTO);
		assertNotNull(documentUploadService.deleteAllByPreId(preRegistrationId));
	}

	@Test
	public void deleteFileTest() {
		List<DocumentEntity> documentEntityList = new ArrayList<DocumentEntity>();
		String docId = "12";
		String name = "Demo";
		documentEntity.setDocId(docId);
		documentEntity.setDocName(name);
		documentEntity.setCrDtime(LocalDateTime.now());
		assertNotNull(documentUploadService.deleteFile(documentEntityList, preRegistrationId));
	}

	@Test
	public void uploadDocument1Test()
			throws JsonParseException, JsonMappingException, IOException, JSONException, ParseException {
		Map<String, String> requiredRequestMap = new HashMap<>();
		documentRequestDTOList.setId(documentId);
		documentRequestDTOList.setVersion("2");
		documentRequestDTOList.setRequest(documentRequestDTO);
		responseUpload.setId(documentId);
		responseUpload.setVersion("12");
		responseUpload.setResponsetime(LocalDateTime.now().toString());
		responseUpload.setResponse(docResp);
		requiredRequestMap.put("id", "123");
		Mockito.doReturn(documentRequestDTOList).when(serviceUtil).createUploadDto(docJson, documentId);
		assertNotNull(documentUploadService.uploadDocument(mockMultipartFile, documentId, preRegistrationId));
	}

	@Test
	public void uploadDocument2Test()
			throws JsonParseException, JsonMappingException, IOException, JSONException, ParseException {
		Map<String, String> requiredRequestMap = new HashMap<>();
		documentRequestDTOList.setId(documentId);
		documentRequestDTOList.setVersion("2");
		documentRequestDTOList.setRequest(documentRequestDTO);
		responseUpload.setId(documentId);
		responseUpload.setVersion("12");
		responseUpload.setResponsetime(LocalDateTime.now().toString());
		responseUpload.setResponse(docResp);
		requiredRequestMap.put("id", "123");
		Mockito.when(serviceUtil.createUploadDto(Mockito.any(), Mockito.any())).thenReturn(documentRequestDTOList);
		Mockito.when(validationutil.requestValidator(Mockito.any(), Mockito.any())).thenReturn(true);
		Mockito.when(serviceUtil.fileSizeCheck(multipartFile.getSize())).thenReturn(true);
		Mockito.when(serviceUtil.fileExtensionCheck(Mockito.any())).thenReturn(true);
		assertNotNull(documentUploadService.uploadDocument(mockMultipartFile, documentId, preRegistrationId));
	}

	@Test
	public void setupTest() {
		documentUploadService.setup();
	}
}