package io.mosip.preregistration.application.test.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.templatemanager.spi.TemplateManager;
import io.mosip.preregistration.application.code.NotificationRequestCodes;
import io.mosip.preregistration.application.controller.NotificationController;
import io.mosip.preregistration.application.errorcodes.NotificationErrorCodes;
import io.mosip.preregistration.application.exception.util.PreRegistrationExceptionHandler;
import io.mosip.preregistration.application.service.ApplicationServiceIntf;
import io.mosip.preregistration.application.service.DemographicServiceIntf;
import io.mosip.preregistration.application.service.NotificationService;
import io.mosip.preregistration.application.service.util.NotificationServiceUtil;
import io.mosip.preregistration.core.code.BookingTypeCodes;
import io.mosip.preregistration.core.common.dto.BookingRegistrationDTO;
import io.mosip.preregistration.core.common.dto.DemographicResponseDTO;
import io.mosip.preregistration.core.common.dto.KeyValuePairDto;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.dto.NotificationDTO;
import io.mosip.preregistration.core.common.entity.ApplicationEntity;
import io.mosip.preregistration.core.common.service.UserDetailsService;
import io.mosip.preregistration.core.util.AuditLogUtil;
import io.mosip.preregistration.core.util.NotificationUtil;
import io.mosip.preregistration.core.util.ValidationUtil;

/**
 * MOSIP-38854: component tests at the HTTP boundary. The controller, NotificationService and
 * NotificationServiceUtil run for real; only the services they call out to are mocked, so these tests
 * show what a caller of the notification API sees when the identity lacks a configured name field.
 */
@RunWith(SpringRunner.class)
@WebMvcTest(NotificationController.class)
@Import({ NotificationController.class, NotificationService.class, NotificationServiceUtil.class,
		PreRegistrationExceptionHandler.class })
@TestPropertySource(properties = "preregistration.notification.nameFormat=firstName,fullName")
public class NotificationNameFieldComponentTest {

	private static final String PRE_ID = "20180396713560";

	/** The id map the application configuration normally provides to PreRegistrationExceptionHandler. */
	@TestConfiguration
	static class IdMapConfig {
		@Bean(name = "id")
		Map<String, String> id() {
			return new HashMap<>();
		}
	}

	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext webAppContext;

	@Autowired
	private ObjectMapper mapper;

	@SpyBean
	private NotificationServiceUtil serviceUtil;

	@MockBean
	private NotificationUtil notificationUtil;

	@MockBean
	private DemographicServiceIntf demographicServiceIntf;

	@MockBean
	private ApplicationServiceIntf applicationServiceIntf;

	@MockBean
	private UserDetailsService userDetailsService;

	@MockBean
	private AuditLogUtil auditLogUtil;

	@MockBean
	private ValidationUtil validationUtil;

	@MockBean
	private TemplateManager templateManager;

	@MockBean(name = "selfTokenRestTemplate")
	private RestTemplate restTemplate;

	@Before
	public void setUp() throws Exception {
		mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext).build();
		AuthUserDetails user = Mockito.mock(AuthUserDetails.class);
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null,
				AuthorityUtils.createAuthorityList("ROLE_INDIVIDUAL")));
		Mockito.when(validationUtil.requestValidator(Mockito.any(), Mockito.any())).thenReturn(true);
		Mockito.doAnswer(invocation -> invocation.getArgument(0)).when(serviceUtil)
				.modifyCenterNameAndAddress(Mockito.any(), Mockito.any(), Mockito.any());
	}

	@After
	public void tearDown() {
		SecurityContextHolder.clearContext();
	}

	/** AC-1: a configured name field missing from the identity is skipped and the notification is sent. */
	@Test
	public void notifySkipsNameFieldMissingFromIdentity() throws Exception {
		Mockito.when(demographicServiceIntf.getDemographicData(PRE_ID)).thenReturn(identityWithNameFields("fullName"));
		Mockito.when(notificationUtil.getAppointmentDetails(PRE_ID)).thenReturn(booking());

		JsonNode body = post("/notification/notify", legacyRequest(false));

		assertNoErrors(body);
		assertEquals(NotificationRequestCodes.MESSAGE.getCode(), body.path("response").path("message").asText());
		NotificationDTO sent = sentNotification();
		assertEquals(1, sent.getFullName().size());
		assertEquals("Sanober", sent.getFullName().get(0).getValue());
	}

	/** AC-2: no configured name field present -> the caller gets PRG_PAM_ACK_008, not PRG_PAM_ACK_002. */
	@Test
	public void notifyReturnsFullNameValidationErrorWhenNoConfiguredNameFieldPresent() throws Exception {
		Mockito.when(demographicServiceIntf.getDemographicData(PRE_ID)).thenReturn(identityWithNameFields());

		JsonNode body = post("/notification/notify", legacyRequest(false));

		assertEquals(NotificationErrorCodes.PRG_PAM_ACK_008.getCode(),
				body.path("errors").path(0).path("errorCode").asText());
		assertTrue(body.path("response").isMissingNode() || body.path("response").isNull());
		Mockito.verify(notificationUtil, Mockito.never()).notify(Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any());
	}

	/** AC-3: legacy flow with no name field in the identity still notifies, with the requested name. */
	@Test
	public void notifyStillSendsWhenIdentityHasNoNameField() throws Exception {
		Mockito.when(demographicServiceIntf.getDemographicData(PRE_ID)).thenReturn(identityWithNameFields());

		JsonNode body = post("/notification/notify", legacyRequest(true));

		assertNoErrors(body);
		assertEquals(NotificationRequestCodes.MESSAGE.getCode(), body.path("response").path("message").asText());
		NotificationDTO sent = sentNotification();
		assertEquals(1, sent.getFullName().size());
		assertEquals("eng", sent.getFullName().get(0).getKey());
		assertEquals("Sanober", sent.getFullName().get(0).getValue());
	}

	/** AC-3: a request without a name, for an identity without a name field, is named by the pre-registration id. */
	@Test
	public void notifyNamesNotificationByPreRegistrationIdWhenNoNameIsAvailable() throws Exception {
		Mockito.when(demographicServiceIntf.getDemographicData(PRE_ID)).thenReturn(identityWithNameFields());

		JsonNode body = post("/notification/notify", envelope(notification(null, true)));

		assertNoErrors(body);
		assertEquals(NotificationRequestCodes.MESSAGE.getCode(), body.path("response").path("message").asText());
		NotificationDTO sent = sentNotification();
		assertEquals(1, sent.getFullName().size());
		assertEquals("eng", sent.getFullName().get(0).getKey());
		assertEquals(PRE_ID, sent.getFullName().get(0).getValue());
	}

	/**
	 * AC-1 + AC-3: non-batch request whose name validates against firstName while the identity has no
	 * preregistration.identity.name field; the notification is sent with the requested name.
	 */
	@Test
	public void notifyNonBatchSendsWhenIdentityHasOnlyFirstName() throws Exception {
		Mockito.when(demographicServiceIntf.getDemographicData(PRE_ID)).thenReturn(identityWithNameFields("firstName"));
		Mockito.when(notificationUtil.getAppointmentDetails(PRE_ID)).thenReturn(booking());

		JsonNode body = post("/notification/notify", legacyRequest(false));

		assertNoErrors(body);
		assertEquals(NotificationRequestCodes.MESSAGE.getCode(), body.path("response").path("message").asText());
		NotificationDTO sent = sentNotification();
		assertEquals(1, sent.getFullName().size());
		assertEquals("eng", sent.getFullName().get(0).getKey());
		assertEquals("Sanober", sent.getFullName().get(0).getValue());
	}

	/** AC-4: v2 flow with no name field in the identity still notifies and keeps the per-language names. */
	@Test
	public void notifyV2KeepsRequestNamesWhenIdentityHasNoNameField() throws Exception {
		Mockito.when(applicationServiceIntf.getApplicationInfoInternal(PRE_ID)).thenReturn(newPreRegistration());
		Mockito.when(demographicServiceIntf.getDemographicData(PRE_ID)).thenReturn(identityWithNameFields());

		JsonNode body = post("/notification/v2", multilingualRequest());

		assertNoErrors(body);
		assertEquals(NotificationRequestCodes.MESSAGE.getCode(), body.path("response").path("message").asText());
		Set<String> languages = new HashSet<>();
		for (KeyValuePairDto<String, String> name : sentNotification().getFullName()) {
			languages.add(name.getKey());
			assertEquals("Sanober " + name.getKey(), name.getValue());
		}
		assertEquals(new HashSet<>(Arrays.asList("eng", "ara")), languages);
	}

	private JsonNode post(String path, String requestJson) throws Exception {
		MvcResult result = mockMvc
				.perform(MockMvcRequestBuilders.multipart(path)
						.file(new MockMultipartFile("NotificationRequestDTO", "", "application/json",
								requestJson.getBytes(StandardCharsets.UTF_8)))
						.file(new MockMultipartFile("langCode", "", "application/json",
								"eng".getBytes(StandardCharsets.UTF_8))))
				.andExpect(status().isOk()).andReturn();
		return mapper.readTree(result.getResponse().getContentAsString());
	}

	private void assertNoErrors(JsonNode body) {
		JsonNode errors = body.get("errors");
		assertTrue("unexpected errors " + errors, errors == null || errors.isNull() || errors.size() == 0);
	}

	private NotificationDTO sentNotification() throws Exception {
		ArgumentCaptor<NotificationDTO> sent = ArgumentCaptor.forClass(NotificationDTO.class);
		Mockito.verify(notificationUtil).notify(Mockito.eq(NotificationRequestCodes.EMAIL.getCode()), sent.capture(),
				Mockito.any(), Mockito.eq(BookingTypeCodes.NEW_PREREGISTRATION.toString()));
		return sent.getValue();
	}

	private Map<String, Object> notification(String name, boolean isBatch) {
		Map<String, Object> dto = new LinkedHashMap<>();
		dto.put("name", name);
		dto.put("preRegistrationId", PRE_ID);
		dto.put("appointmentDate", "2019-01-22");
		// formatted like NotificationService does, so the check holds in any JVM locale
		dto.put("appointmentTime", LocalTime.of(9, 0).format(DateTimeFormatter.ofPattern("hh:mm a")));
		dto.put("additionalRecipient", false);
		dto.put("isBatch", isBatch);
		return dto;
	}

	private String envelope(Object request) throws Exception {
		Map<String, Object> envelope = new LinkedHashMap<>();
		envelope.put("id", "mosip.pre-registration.notification.notify");
		envelope.put("version", "1.0");
		envelope.put("requesttime", "");
		envelope.put("request", request);
		return mapper.writeValueAsString(envelope);
	}

	private String legacyRequest(boolean isBatch) throws Exception {
		return envelope(notification("Sanober", isBatch));
	}

	private String multilingualRequest() throws Exception {
		Map<String, Object> request = new LinkedHashMap<>();
		request.put("eng", notification("Sanober eng", true));
		request.put("ara", notification("Sanober ara", true));
		return envelope(request);
	}

	@SuppressWarnings("unchecked")
	private MainResponseDTO<DemographicResponseDTO> identityWithNameFields(String... nameFields) {
		JSONObject identity = new JSONObject();
		for (String nameField : nameFields) {
			JSONObject name = new JSONObject();
			name.put("language", "eng");
			name.put("value", "Sanober");
			JSONArray names = new JSONArray();
			names.add(name);
			identity.put(nameField, names);
		}
		identity.put("email", "sanober@gmail.com");
		identity.put("phone", "9480548558");
		JSONObject demographicDetails = new JSONObject();
		demographicDetails.put("identity", identity);
		DemographicResponseDTO demographic = new DemographicResponseDTO();
		demographic.setPreRegistrationId(PRE_ID);
		demographic.setDemographicDetails(demographicDetails);
		MainResponseDTO<DemographicResponseDTO> response = new MainResponseDTO<>();
		response.setResponse(demographic);
		return response;
	}

	private MainResponseDTO<BookingRegistrationDTO> booking() {
		BookingRegistrationDTO booking = new BookingRegistrationDTO();
		booking.setRegDate("2019-01-22");
		booking.setRegistrationCenterId("10001");
		booking.setSlotFromTime("09:00");
		booking.setSlotToTime("09:15");
		MainResponseDTO<BookingRegistrationDTO> response = new MainResponseDTO<>();
		response.setResponse(booking);
		return response;
	}

	private MainResponseDTO<ApplicationEntity> newPreRegistration() {
		ApplicationEntity application = new ApplicationEntity();
		application.setApplicationId(PRE_ID);
		application.setBookingType(BookingTypeCodes.NEW_PREREGISTRATION.toString());
		MainResponseDTO<ApplicationEntity> response = new MainResponseDTO<>();
		response.setResponse(application);
		return response;
	}
}
