
package io.mosip.preregistration.application.test.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.context.WebApplicationContext;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.idgenerator.spi.PridGenerator;
import io.mosip.preregistration.application.controller.DemographicController;
import io.mosip.preregistration.application.dto.ApplicationInfoMetadataDTO;
import io.mosip.preregistration.application.dto.DeleteApplicationDTO;
import io.mosip.preregistration.application.dto.DeletePreRegistartionDTO;
import io.mosip.preregistration.application.dto.DemographicCreateResponseDTO;
import io.mosip.preregistration.application.dto.DemographicMetadataDTO;
import io.mosip.preregistration.application.dto.DemographicRequestDTO;
import io.mosip.preregistration.application.dto.DemographicUpdateResponseDTO;
import io.mosip.preregistration.application.dto.DemographicViewDTO;
import io.mosip.preregistration.application.service.DemographicServiceIntf;
import io.mosip.preregistration.core.common.dto.DemographicResponseDTO;
import io.mosip.preregistration.core.common.dto.DocumentsMetaData;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.dto.PreRegIdsByRegCenterIdDTO;
import io.mosip.preregistration.core.common.dto.PreRegistartionStatusDTO;
import io.mosip.preregistration.core.util.RequestValidator;
import net.minidev.json.parser.ParseException;

/**
 * Test class to test the PreRegistration Controller methods
 * 
 * @author Rajath KR
 * @author Sanober Noor
 * @author Tapaswini Bahera
 * @author Jagadishwari S
 * @author Ravi C Balaji
 * @since 1.0.0
 * 
 */

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = DemographicController.class)
@Import(DemographicController.class)
@WithMockUser(username = "individual", authorities = { "INDIVIDUAL", "REGISTRATION_OFFICER" })
public class DemographicControllerTest {

	/**
	 * Autowired reference for {@link #MockMvc}
	 */
	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@MockBean
	private RequestValidator requestValidator;

	String preRegistrationId = "";

	/**
	 * Creating Mock Bean for DemographicService
	 */
	@MockBean
	private DemographicServiceIntf preRegistrationService;

	@MockBean
	private PridGenerator<String> pridGenerator;

	@Mock
	private AuthUserDetails authUserDetails;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Mock
	private DemographicController controller;

	String userId = "";
	MainRequestDTO<DemographicRequestDTO> reqDto = new MainRequestDTO<>();

	/**
	 * @throws FileNotFoundException when file not found
	 * @throws IOException           on input error
	 * @throws ParseException        on json parsing error
	 */
	@Before
	public void setup() throws FileNotFoundException, IOException, ParseException {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		preRegistrationId = "98746563542672";
		userId = "9988905333";
	}

	/**
	 * Test init binder.
	 */
	@Test
	public void testInitBinder() {
		controller.initBinder(Mockito.mock(WebDataBinder.class));
	}

	
	/**
	 * @throws Exception on error
	 */
	@WithMockUser(username = "individual", authorities = { "INDIVIDUAL", "REGISTRATION_OFFICER" })
	@Test
	public void successSave() throws Exception {
		Mockito.when(requestValidator.supports(Mockito.any())).thenReturn(true);
		
		logger.info("----------Successful save of application-------");
		MainResponseDTO<DemographicCreateResponseDTO> response = new MainResponseDTO<>();
		List<DemographicCreateResponseDTO> saveList = new ArrayList<>();
		DemographicCreateResponseDTO createDto = new DemographicCreateResponseDTO();

		MainRequestDTO<DemographicRequestDTO> request = new MainRequestDTO<>();
		DemographicRequestDTO demo = new DemographicRequestDTO();
		request.setRequest(demo);

		createDto.setPreRegistrationId("98746563542672");
		// saveList.add(createDto);
		DemographicRequestDTO req = new DemographicRequestDTO();
		req.setLangCode("eng");
		List<String> requiredFields = new ArrayList<String>();
		requiredFields.add("fullName");
		requiredFields.add("email");
		req.setRequiredFields(requiredFields);
		response.setResponse(createDto);
		reqDto.setId("mosip.pre-registration.demographic.create");
		reqDto.setVersion("1.0");
		reqDto.setRequesttime(new Date());
		reqDto.setRequest(req);
		Mockito.when(preRegistrationService.addPreRegistration(Mockito.any())).thenReturn(response);

		mockMvc.perform(post("/applications/prereg").contentType(MediaType.APPLICATION_JSON_VALUE)
				.content("{\"demographicDetails\":{\"identity\":{\"IDSchemaVersion\":0.1}}}"))
				.andExpect(status().isOk());

	}

	/**
	 * @throws Exception on error
	 */

	@Test
	@WithMockUser(username = "individual", authorities = { "INDIVIDUAL", "REGISTRATION_OFFICER" })
	public void successUpdate() throws Exception {
		Mockito.when(requestValidator.supports(Mockito.any())).thenReturn(true);
		
		logger.info("----------Successful save of application-------");

		MainResponseDTO<DemographicUpdateResponseDTO> response = new MainResponseDTO<>();
		List<DemographicUpdateResponseDTO> saveList = new ArrayList<>();
		DemographicUpdateResponseDTO createDto = new DemographicUpdateResponseDTO();
		createDto.setPreRegistrationId("98746563542672");
		preRegistrationId = "98746563542672";
		// saveList.add(createDto);
		response.setResponse(createDto);

		MainRequestDTO<DemographicRequestDTO> request = new MainRequestDTO<>();
		DemographicRequestDTO demo = new DemographicRequestDTO();
		preRegistrationId = "98746563542672";
		request.setRequest(demo);
		Mockito.when(preRegistrationService.authUserDetails()).thenReturn(authUserDetails);
		Mockito.when(authUserDetails.getUserId()).thenReturn(userId);
		Mockito.when(preRegistrationService.updatePreRegistration(request, preRegistrationId, userId))
				.thenReturn(response);

		DemographicRequestDTO req = new DemographicRequestDTO();
		req.setLangCode("eng");
		response.setResponse(createDto);
		reqDto.setId("mosip.pre-registration.demographic.update");
		reqDto.setVersion("1.0");
		reqDto.setRequesttime(new Date());
		reqDto.setRequest(req);

		mockMvc.perform(put("/applications/prereg/{preRegistrationId}", "98746563542672")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"demographicDetails\":{\"identity\":{\"IDSchemaVersion\":0.1}}}"))
				.andExpect(status().isOk());

	}

	/**
	 * @throws Exception on error
	 */
	@WithMockUser(username = "individual", authorities = { "INDIVIDUAL", "REGISTRATION_OFFICER" })
	@Test
	public void getAllApplicationTest() throws Exception {
		MainResponseDTO<DemographicMetadataDTO> response = new MainResponseDTO<>();
		List<DemographicViewDTO> viewList = new ArrayList<>();
		DemographicMetadataDTO demographicMetadataDTO = new DemographicMetadataDTO();
		DemographicViewDTO viewDto = new DemographicViewDTO();
		viewDto.setPreRegistrationId("1234");
		viewDto.setStatusCode("Pending_Appointment");
		viewList.add(viewDto);
		demographicMetadataDTO.setBasicDetails(viewList);
		response.setResponse(demographicMetadataDTO);
		Mockito.when(preRegistrationService.authUserDetails()).thenReturn(authUserDetails);
		Mockito.when(authUserDetails.getUserId()).thenReturn(userId);
		Mockito.when(preRegistrationService.getAllApplicationDetails(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(response);
		RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/applications/prereg")
				.contentType(MediaType.APPLICATION_JSON_VALUE).characterEncoding("UTF-8")
				.accept(MediaType.APPLICATION_JSON_VALUE);

		mockMvc.perform(requestBuilder).andExpect(status().isOk());

	}

	/**
	 * @throws Exception on error
	 */
	@WithMockUser(username = "individual", authorities = { "INDIVIDUAL", "REGISTRATION_OFFICER" })
	@Test
	public void getApplicationStatusTest() throws Exception {
		String preId = "14532456789";
		MainResponseDTO<PreRegistartionStatusDTO> response = new MainResponseDTO<>();
		List<PreRegistartionStatusDTO> statusList = new ArrayList<PreRegistartionStatusDTO>();
		PreRegistartionStatusDTO statusDto = new PreRegistartionStatusDTO();
		statusDto.setPreRegistartionId(preId);
		statusDto.setStatusCode("Pending_Appointment");
		// statusList.add(statusDto);
		response.setResponse(statusDto);
		Mockito.when(preRegistrationService.authUserDetails()).thenReturn(authUserDetails);
		Mockito.when(authUserDetails.getUserId()).thenReturn(userId);
		Mockito.when(preRegistrationService.getApplicationStatus(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(response);

		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.get("/applications/prereg/status/{preRegistrationId}", preId)
				.contentType(MediaType.APPLICATION_JSON_VALUE).characterEncoding("UTF-8")
				.accept(MediaType.APPLICATION_JSON_VALUE);

		mockMvc.perform(requestBuilder).andExpect(status().isOk());
	}

	/**
	 * @throws Exception on error
	 */
	@WithMockUser(username = "individual", authorities = { "INDIVIDUAL", "REGISTRATION_OFFICER" })
	@Test
	public void discardIndividualTest() throws Exception {
		String preId = "3";
		MainResponseDTO<DeletePreRegistartionDTO> response = new MainResponseDTO<>();
		List<DeleteApplicationDTO> DeleteList = new ArrayList<DeleteApplicationDTO>();
		DeletePreRegistartionDTO deleteDto = new DeletePreRegistartionDTO();

		deleteDto.setPreRegistrationId("3");
		deleteDto.setDeletedBy(userId);
		// DeleteList.add(deleteDto);
		response.setResponse(deleteDto);
		Mockito.when(preRegistrationService.authUserDetails()).thenReturn(authUserDetails);
		Mockito.when(authUserDetails.getUserId()).thenReturn(userId);
		Mockito.when(preRegistrationService.deleteIndividual("3", userId)).thenReturn(response);

		RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/applications/prereg/{preRegistrationId}", preId)
				.contentType(MediaType.APPLICATION_JSON_VALUE).characterEncoding("UTF-8")
				.accept(MediaType.APPLICATION_JSON_VALUE);

		mockMvc.perform(requestBuilder).andExpect(status().isOk());
	}

	/**
	 * @throws Exception on error
	 */
	@WithMockUser(username = "individual", authorities = { "INDIVIDUAL", "REGISTRATION_OFFICER" })
	@Test
	public void getApplicationSuccessTest() throws Exception {
		MainResponseDTO<DemographicResponseDTO> response = new MainResponseDTO<>();
		DemographicResponseDTO createDto = new DemographicResponseDTO();

		createDto.setPreRegistrationId("98746563542672");
		response.setResponse(createDto);
		Mockito.when(preRegistrationService.authUserDetails()).thenReturn(authUserDetails);
		Mockito.when(authUserDetails.getUserId()).thenReturn(userId);
		Mockito.when(preRegistrationService.getDemographicData("98746563542672")).thenReturn(response);

		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.get("/applications/prereg/{preRegistrationId}", createDto.getPreRegistrationId())
				.contentType(MediaType.APPLICATION_JSON_VALUE).characterEncoding("UTF-8")
				.accept(MediaType.APPLICATION_JSON_VALUE);

		mockMvc.perform(requestBuilder).andExpect(status().isOk());
	}

	/**
	 * @throws Exception on error
	 */
	@WithMockUser(username = "individual", authorities = { "INDIVIDUAL", "REGISTRATION_OFFICER",
			"REGISTRATION_PROCESSOR" })
	@Test
	public void updateApplicationStatusTest() throws Exception {
		MainResponseDTO<String> response = new MainResponseDTO<>();
		response.setErrors(null);
		response.setResponse("Status Updated sucessfully");
		Mockito.when(preRegistrationService.authUserDetails()).thenReturn(authUserDetails);
		Mockito.when(authUserDetails.getUserId()).thenReturn(userId);
		Mockito.when(preRegistrationService.updatePreRegistrationStatus("98746563542672", "Booked", userId))
				.thenReturn(response);

		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.put("/applications/prereg/status/{preRegistrationId}", "98746563542672").contentType(MediaType.ALL)
				.characterEncoding("UTF-8").accept(MediaType.ALL).param("statusCode", "Booked");

		mockMvc.perform(requestBuilder).andExpect(status().isOk());
	}

	/**
	 * @throws Exception on error
	 */
	@WithMockUser(username = "individual", authorities = { "INDIVIDUAL", "REGISTRATION_OFFICER" })
	@Test
	public void getUpdatedDateTimeTest() throws Exception {
		Mockito.when(requestValidator.supports(Mockito.any())).thenReturn(true);
		
		MainRequestDTO<PreRegIdsByRegCenterIdDTO> mainRequestDTO = new MainRequestDTO<>();
		List<String> list = new ArrayList<>();
		list.add("98746563542672");
		PreRegIdsByRegCenterIdDTO byRegCenterIdDTO = new PreRegIdsByRegCenterIdDTO();
		byRegCenterIdDTO.setPreRegistrationIds(list);
		mainRequestDTO.setRequest(byRegCenterIdDTO);

		MainResponseDTO<Map<String, String>> response = new MainResponseDTO<>();
		Map<String, String> map = new HashMap<>();
		map.put("98746563542672", LocalDateTime.now().toString());
		response.setResponse(map);
		response.setErrors(null);
		response.setResponsetime(LocalDateTime.now().toString());

		Mockito.when(preRegistrationService.getUpdatedDateTimeForPreIds(byRegCenterIdDTO)).thenReturn(response);

		mockMvc.perform(post("/applications/prereg/updatedTime").contentType(MediaType.APPLICATION_JSON)
				.content("{\"registrationCenterId\":\"regCenterId\",\"preregistrationId\":\"prid\"}"))
				.andExpect(status().isOk());

	}

	@Test
	public void getPreRegDemographicDataTest() throws Exception {
		MainResponseDTO<DemographicResponseDTO> response = new MainResponseDTO<DemographicResponseDTO>();
		String preRegistrationId = "123456";
		Mockito.when(preRegistrationService.getDemographicData(preRegistrationId)).thenReturn(response);
		RequestBuilder request = MockMvcRequestBuilders
				.get("/applications/prereg/{preRegistrationId}", preRegistrationId)
				.param("preRegistrationId", preRegistrationId).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON);
		mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
	}
	
	@Test
	public void getPreRegDemographicAndDocumentData() throws Exception {
		MainResponseDTO<ApplicationInfoMetadataDTO> response = new MainResponseDTO<ApplicationInfoMetadataDTO>();
		ApplicationInfoMetadataDTO applicationInfoMetadataDTO = new ApplicationInfoMetadataDTO();
		String preRegistrationId = "123456";
		DemographicResponseDTO demographicResponseDTO = new DemographicResponseDTO();
		demographicResponseDTO.setPreRegistrationId(preRegistrationId);
		applicationInfoMetadataDTO.setDemographicResponse(demographicResponseDTO);
		DocumentsMetaData documentsMetaData = new DocumentsMetaData();
		applicationInfoMetadataDTO.setDocumentsMetaData(documentsMetaData);
		response.setResponse(applicationInfoMetadataDTO);
		Mockito.when(preRegistrationService.getPregistrationInfo(preRegistrationId)).thenReturn(response);
		RequestBuilder request = MockMvcRequestBuilders
				.get("/applications/prereg/info/{preRegistrationId}", preRegistrationId)
				.param("preRegistrationId", preRegistrationId).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON);
		mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
	}

}
