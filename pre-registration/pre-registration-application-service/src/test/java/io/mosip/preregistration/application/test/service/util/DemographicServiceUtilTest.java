package io.mosip.preregistration.application.test.service.util;

import java.io.File;
import java.io.FileReader;

import io.mosip.preregistration.core.common.service.UserDetailsService;
import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import io.mosip.preregistration.application.exception.OperationNotAllowedException;
import io.mosip.preregistration.application.repository.ApplicationRepostiory;
import io.mosip.preregistration.application.repository.DocumentDAO;
import io.mosip.preregistration.application.service.AppointmentService;
import io.mosip.preregistration.application.service.UISpecService;
import io.mosip.preregistration.application.service.util.DemographicServiceUtil;
import io.mosip.preregistration.core.common.entity.ApplicationEntity;
import io.mosip.preregistration.core.code.StatusCodes;
import io.mosip.preregistration.core.common.entity.DemographicEntity;
import io.mosip.preregistration.core.common.entity.DocumentEntity;
import io.mosip.preregistration.core.util.AuditLogUtil;
import io.mosip.preregistration.core.util.CryptoUtil;
import io.mosip.preregistration.core.util.RequestValidator;
import io.mosip.preregistration.application.dto.DemographicRequestDTO;
import io.mosip.preregistration.demographic.exception.system.DateParseException;
import io.mosip.preregistration.demographic.exception.system.JsonParseException;

/**
 * Test class to test the PreRegistration Service util methods
 * 
 * @author Ravi C Balaji
 * @since 1.0.0
 * 
 */
@RunWith(SpringRunner.class)
@ImportAutoConfiguration(RefreshAutoConfiguration.class)
@SpringBootTest(classes = DemographicServiceUtil.class, properties = "spring.cloud.config.enabled=false")
public class DemographicServiceUtilTest {

	/**
	 * Autowired reference for $link{DemographicServiceUtil}
	 */
	@Autowired
	private DemographicServiceUtil demographicServiceUtil;

	@MockBean(name = "selfTokenRestTemplate")
	RestTemplate restTemplate;

	@MockBean
	private AppointmentService appointmentService;

	@MockBean
	private ApplicationRepostiory applicationRepostiory;

	@MockBean
	private DocumentDAO documentDAO;

	@MockBean
	private RequestValidator requestValidator;
	
	@MockBean
	private UISpecService uiSpecService;

	private DemographicRequestDTO saveDemographicRequest = null;
	private DemographicRequestDTO updateDemographicRequest = null;
	private DemographicEntity demographicEntity = null;
	private String requestId = null;
	private JSONObject jsonObject;
	private JSONParser parser = null;

	@MockBean
	private AuditLogUtil auditLogUtil;

	@MockBean
	private CryptoUtil cryptoUtil;

	@MockBean
	private UserDetailsService userDetailsService;

	/**
	 * @throws Exception on Any Exception
	 */
	@Before
	public void setUp() throws Exception {
		requestId = "mosip.preregistration";
		parser = new JSONParser();

		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("pre-registration.json").getFile());
		jsonObject = (JSONObject) parser.parse(new FileReader(file));

		saveDemographicRequest = new DemographicRequestDTO();
		saveDemographicRequest.setLangCode("ENG");
		saveDemographicRequest.setDemographicDetails(jsonObject);

		updateDemographicRequest = new DemographicRequestDTO();
		updateDemographicRequest.setLangCode("ENG");
		updateDemographicRequest.setDemographicDetails(jsonObject);

		demographicEntity = new DemographicEntity();
		demographicEntity.setPreRegistrationId("35760478648170");
		demographicEntity.setApplicantDetailJson((jsonObject.toJSONString() + "623744").getBytes());
		Mockito.when(userDetailsService.resolveCanonicalUserIdOrIdentifier(Mockito.anyString()))
				.thenReturn("00000000-0000-0000-0000-000000000001");
	}

	@Test(expected = JsonParseException.class)
	public void setterForCreateDTOFailureTest() {
		Mockito.when(cryptoUtil.decrypt(Mockito.any(), Mockito.any()))
				.thenReturn(Base64.decodeBase64(jsonObject.toString().getBytes()));
		Mockito.when(demographicServiceUtil.setterForCreateDTO(demographicEntity)).thenThrow(JsonParseException.class);
	}

	@Test(expected = OperationNotAllowedException.class)
	public void checkStatusForDeletionFailureTest() {
		Mockito.when(demographicServiceUtil.checkStatusForDeletion(StatusCodes.EXPIRED.getCode()))
				.thenThrow(OperationNotAllowedException.class);
	}

	@Test(expected = DateParseException.class)
	public void getDateFromStringFailureTest() throws Exception {
		demographicServiceUtil.getDateFromString("abc");
	}

	@Test
	public void prepareDemographicEntityForUpdateMigratesDemographicAndDocumentOwnershipToUuid() {
		ApplicationEntity applicationEntity = new ApplicationEntity();
		applicationEntity.setApplicationId("35760478648170");
		applicationEntity.setBookingType("NEW");
		applicationEntity.setApplicationStatusCode("DRAFT");
		applicationEntity.setBookingStatusCode(StatusCodes.APPLICATION_INCOMPLETE.getCode());
		applicationEntity.setCrBy("legacy-user");

		DocumentEntity documentEntity = new DocumentEntity();
		documentEntity.setCrBy("legacy-user");
		documentEntity.setUpdBy("legacy-user");
		demographicEntity.setDocumentEntity(java.util.List.of(documentEntity));

		Mockito.when(applicationRepostiory.findByApplicationId("35760478648170")).thenReturn(applicationEntity);
		Mockito.when(applicationRepostiory.save(Mockito.any(ApplicationEntity.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));
		Mockito.when(cryptoUtil.encrypt(Mockito.any(), Mockito.any())).thenReturn("encrypted".getBytes());
		Mockito.when(documentDAO.updateDocument(Mockito.any(DocumentEntity.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		DemographicEntity updatedEntity = demographicServiceUtil.prepareDemographicEntityForUpdate(demographicEntity,
				updateDemographicRequest, StatusCodes.APPLICATION_INCOMPLETE.getCode(), "legacy-user", "35760478648170");

		org.junit.Assert.assertEquals("00000000-0000-0000-0000-000000000001", updatedEntity.getCrAppuserId());
		org.junit.Assert.assertEquals("00000000-0000-0000-0000-000000000001", updatedEntity.getCreatedBy());
		org.junit.Assert.assertEquals("00000000-0000-0000-0000-000000000001", updatedEntity.getUpdatedBy());
		org.junit.Assert.assertEquals("00000000-0000-0000-0000-000000000001", documentEntity.getCrBy());
		org.junit.Assert.assertEquals("00000000-0000-0000-0000-000000000001", documentEntity.getUpdBy());
		Mockito.verify(documentDAO).updateDocument(documentEntity);
	}

}
