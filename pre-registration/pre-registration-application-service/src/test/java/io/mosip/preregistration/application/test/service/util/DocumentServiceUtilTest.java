package io.mosip.preregistration.application.test.service.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
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
		Mockito.when(virusScan.scanDocument(mockMultipartFile.getBytes())).thenThrow(VirusScannerException.class);
		documentServiceUtil.virusScanCheck(mockMultipartFile);
	}
}