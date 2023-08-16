package io.mosip.preregistration.application.test.service.util;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import io.mosip.commons.khazana.spi.ObjectStoreAdapter;
import io.mosip.kernel.core.virusscanner.exception.VirusScannerException;
import io.mosip.kernel.core.virusscanner.spi.VirusScanner;
import io.mosip.preregistration.application.dto.DocumentRequestDTO;
import io.mosip.preregistration.application.exception.InvalidDocumentIdExcepion;
import io.mosip.preregistration.application.service.DemographicService;
import io.mosip.preregistration.application.service.util.DocumentServiceUtil;
import io.mosip.preregistration.core.common.entity.DocumentEntity;
import io.mosip.preregistration.core.exception.InvalidRequestException;
import io.mosip.preregistration.core.util.RequestValidator;
import io.mosip.preregistration.core.util.ValidationUtil;

/**
 * @author Sanober Noor
 * @since 1.0.0
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

	@Autowired
	private DocumentServiceUtil serviceUtil;

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
		ClassLoader classLoader = getClass().getClassLoader();
		URI uri = new URI(classLoader.getResource("Doc.pdf").getFile().trim().replaceAll("\\u0020", "%20"));
		file = new File(uri.getPath());
		mockMultipartFile = new MockMultipartFile("file", "Doc.pdf", "mixed/multipart", new FileInputStream(file));
	}

	@Test
	public void getDateStringTest() {
		serviceUtil.getDateString(new Date());
	}

	@Test
	public void parseDocumentIdTest() {
		serviceUtil.parseDocumentId("1234");
	}

	@Test(expected = InvalidDocumentIdExcepion.class)
	public void parseDocumentIdFailureTest() throws Exception {
		serviceUtil.parseDocumentId("1234!@#$&^$$~~~~~~#@!$^%");
	}

	@Test(expected = InvalidRequestException.class)
	public void isValidCatCodeTest() throws Exception {
		serviceUtil.isValidCatCode("13fww");
	}

	@Test(expected = InvalidRequestException.class)
	public void inValidPreIDTest() throws Exception {
		serviceUtil.isValidRequest(documentDto, null);
	}

	@Test(expected = VirusScannerException.class)
	public void virusscannerFailureTest() throws Exception {
		Mockito.when(virusScan.scanDocument(mockMultipartFile.getBytes())).thenThrow(VirusScannerException.class);
		serviceUtil.virusScanCheck(mockMultipartFile);
	}

}
