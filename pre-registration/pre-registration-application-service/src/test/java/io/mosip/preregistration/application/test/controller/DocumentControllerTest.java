package io.mosip.preregistration.application.test.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import io.mosip.preregistration.application.controller.DocumentController;
import io.mosip.preregistration.application.dto.DocumentRequestDTO;
import io.mosip.preregistration.application.dto.DocumentResponseDTO;
import io.mosip.preregistration.application.service.DocumentServiceIntf;
import io.mosip.preregistration.core.common.dto.DocumentDeleteResponseDTO;
import io.mosip.preregistration.core.common.dto.DocumentsMetaData;
//import io.mosip.preregistration.booking.service.BookingServiceIntf;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.entity.DocumentEntity;
import io.mosip.preregistration.core.util.RequestValidator;

/**
 * Test class to test the DocumentUploader Controller methods
 * 
 * @author Sanober Noor
 * @author Rajath KR
 * @author Tapaswini Bahera
 * @author Jagadishwari S
 * @since 1.0.0
 * 
 */
@RunWith(SpringRunner.class)
@WebMvcTest(DocumentController.class)
@Import(DocumentController.class)
@WithMockUser(username = "individual", authorities = { "INDIVIDUAL", "REGISTRATION_OFFICER" })
public class DocumentControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@MockBean
	private RequestValidator requestValidator;

	private MockMultipartFile mockMultipartFile;

	/**
	 * Creating Mock Bean for DocumentUploadService
	 */

	@MockBean
	private DocumentServiceIntf service;

	List<DocumentEntity> DocumentList = new ArrayList<>();
	Map<String, String> response = null;

	String documentId;
	boolean flag;

	String docJson = "";

	Map<String, String> map = new HashMap<>();
	MainResponseDTO<DocumentResponseDTO> responseCopy = new MainResponseDTO<>();
	MainResponseDTO<String> res = new MainResponseDTO<String>();
	MainResponseDTO<DocumentsMetaData> responseAllDoc = new MainResponseDTO<>();
	MainResponseDTO<DocumentDeleteResponseDTO> responseDelete = new MainResponseDTO<>();
	MainResponseDTO<io.mosip.preregistration.application.dto.DocumentResponseDTO> responseMain = new MainResponseDTO<>();
	DocumentRequestDTO documentDto = null;
	List<DocumentResponseDTO> docResponseDtos = new ArrayList<>();

	/**
	 * @throws IOException
	 */
	@Before
	public void setUp() throws IOException {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		documentDto = new DocumentRequestDTO("POA", "address", "ENG", "Test");
		// "59276903416082",
		docJson = "{\"id\": \"mosip.pre-registration.document.upload\",\"version\" : \"1.0\","
				+ "\"requesttime\" : \"2018-12-28T05:23:08.019Z\",\"request\" :" + "{\"docCatCode\" "
				+ ": \"POA\",\"docTypCode\" : \"address\",\"langCode\":\"ENG\"}}";

		response = new HashMap<String, String>();
		response.put("DocumentId", "1");
		response.put("Status", "Pending_Appoinment");
		documentId = response.get("DocumentId");
		flag = true;

		DocumentResponseDTO responseDto = new DocumentResponseDTO();
		responseDto.setDocCatCode("POA");
		responseDto.setDocId("12345");
		responseDto.setPreRegistrationId("123546987412563");

		responseMain.setResponse(responseDto);
	}

	@Test
	public void successFileupload() throws Exception {
		String preRegistrationId = "123546987412563";

		MockMultipartFile jsonMultiPart = new MockMultipartFile("Document request", "docJson", "application/json",
				docJson.getBytes());

		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("Doc.pdf").getFile());
		mockMultipartFile = new MockMultipartFile("file", "Doc.pdf", "mixed/multipart", new FileInputStream(file));

		Mockito.when(service.uploadDocument(mockMultipartFile, docJson, preRegistrationId)).thenReturn(responseMain);

		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.multipart("/documents/{preRegistrationId}", preRegistrationId).file(jsonMultiPart)
				.file(mockMultipartFile).contentType(MediaType.MULTIPART_FORM_DATA);
		mockMvc.perform(requestBuilder).andExpect(status().isOk());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void successDelete() throws Exception {
		String preRegistrationId = "1234567847847";
		String documentId = "2ebbd74e-55e3-11e9-a7b4-b1f3d4442a79";
		Mockito.when(service.deleteDocument(documentId, preRegistrationId)).thenReturn(responseDelete);
		RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/documents/{documentId}", documentId)
				.contentType(MediaType.APPLICATION_JSON_VALUE).characterEncoding("UTF-8")
				.accept(MediaType.APPLICATION_JSON_VALUE).param("preRegistrationId", preRegistrationId);
		mockMvc.perform(requestBuilder).andExpect(status().isOk());
	}

	/**
	 * @throws Exception
	 */

	@Test
	public void getAllDocumentforPreidTest() throws Exception {
		String preRegistrationId = "48690172097498";
		Mockito.when(service.getAllDocumentForPreId("48690172097498")).thenReturn(responseAllDoc);

		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.get("/documents/preregistration/{preRegistrationId}", preRegistrationId)
				.contentType(MediaType.APPLICATION_JSON_VALUE);
		mockMvc.perform(requestBuilder).andExpect(status().isOk());
	}

	/**
	 * @throws Exception
	 */

	@Test
	public void getAllDocumentforDocidTest() throws Exception {
		String preRegistrationId = "1234567847847";
		String documentId = "2ebbd74e-55e3-11e9-a7b4-b1f3d4442a79";
		Mockito.when(service.deleteDocument(documentId, preRegistrationId)).thenReturn(responseDelete);
		RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/documents/{documentId}", documentId)
				.contentType(MediaType.APPLICATION_JSON_VALUE).characterEncoding("UTF-8")
				.accept(MediaType.APPLICATION_JSON_VALUE).param("preRegistrationId", preRegistrationId);
		mockMvc.perform(requestBuilder).andExpect(status().isOk());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void updateDocRefIdTest() throws Exception {
		String preRegistrationId = "48690172097498";
		String documentId = "4564";
		String docRefId = "564";
		Mockito.when(service.updateDocRefId(documentId, preRegistrationId, docRefId)).thenReturn(res);

		RequestBuilder requestBuilder = MockMvcRequestBuilders.put("/documents/document/{documentId}", documentId)
				.contentType(MediaType.APPLICATION_JSON_VALUE).characterEncoding("UTF-8")
				.accept(MediaType.APPLICATION_JSON_VALUE).param("preRegistrationId", preRegistrationId)
				.param("refNumber", docRefId);
		mockMvc.perform(requestBuilder).andExpect(status().isOk());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void deletetAllDocumentByPreidTest() throws Exception {
		String preRegistrationId = "48690172097498";
		Mockito.when(service.deleteAllByPreId("48690172097498")).thenReturn(responseDelete);

		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.delete("/documents/preregistration/{preRegistrationId}", preRegistrationId)
				.contentType(MediaType.APPLICATION_JSON_VALUE).characterEncoding("UTF-8")
				.accept(MediaType.APPLICATION_JSON_VALUE);
		mockMvc.perform(requestBuilder).andExpect(status().isOk());
	}

	/**
	 * @throws Exception
	 */

	@Test
	public void copyDocumentTest() throws Exception {
		Mockito.when(service.copyDocument("POA", "48690172097498", "1234567891")).thenReturn(responseCopy);

		String preRegistrationId = "1232462566658";
		RequestBuilder requestBuilder = MockMvcRequestBuilders.put("/documents/{preRegistrationId}", preRegistrationId)
				.contentType(MediaType.APPLICATION_JSON_VALUE).characterEncoding("UTF-8")
				.accept(MediaType.APPLICATION_JSON_VALUE).param("catCode", "POA")
				.param("sourcePreId", "48690172097498");
		mockMvc.perform(requestBuilder).andExpect(status().isOk());
	}

}
