package io.mosip.preregistration.application.service.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.preregistration.application.repository.DemographicRepository;
import io.mosip.preregistration.core.common.dto.DemographicResponseDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.entity.DemographicEntity;
import io.mosip.preregistration.core.common.service.UserDetailsService;
import io.mosip.preregistration.core.util.CryptoUtil;
import io.mosip.preregistration.core.util.HashUtill;
import io.mosip.preregistration.core.util.ValidationUtil;

/**
 * Covers the role gating of {@link CommonServiceUtil#getDemographicData(String)}.
 * <p>
 * ROLE_INDIVIDUAL must gate ownership validation only — it must never gate access to the data
 * itself. A regression previously bound the "record not found" branch to the role check, so any
 * caller without ROLE_INDIVIDUAL was refused the record outright.
 */
@RunWith(JUnit4.class)
public class CommonServiceUtilTest {

	@InjectMocks
	private CommonServiceUtil commonServiceUtil;

	@Mock
	private ValidationUtil validationUtil;

	@Mock
	private CryptoUtil cryptoUtil;

	@Mock
	private DemographicServiceUtil demographicServiceUtil;

	@Mock
	private UserDetailsService userDetailsService;

	@Mock
	private DemographicRepository demographicRepository;

	private DemographicEntity demographicEntity;

	private static final String PRE_REG_ID = "98746563542672";

	private static final String USER_ID = "9988905444";

	private static final String DEMOGRAPHIC_JSON = "{\"identity\":{\"name\":\"abcd\"}}";

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.openMocks(this);

		ReflectionTestUtils.setField(commonServiceUtil, "utcDateTimePattern", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		ReflectionTestUtils.setField(commonServiceUtil, "retrieveDetailsId",
				"mosip.pre-registration.demographic.retrieve.details");
		ReflectionTestUtils.setField(commonServiceUtil, "version", "1.0");
		ReflectionTestUtils.setField(commonServiceUtil, "piiBackwardCompatibility", true);

		byte[] applicantDetailJson = DEMOGRAPHIC_JSON.getBytes();

		demographicEntity = new DemographicEntity();
		demographicEntity.setPreRegistrationId(PRE_REG_ID);
		demographicEntity.setApplicantDetailJson(applicantDetailJson);
		// The service recomputes the hash and compares, so seed the real expected value.
		demographicEntity.setDemogDetailHash(HashUtill.hashUtill(applicantDetailJson));
		demographicEntity.setStatusCode("Pending_Appointment");
		demographicEntity.setLangCode("eng");
		demographicEntity.setCreatedBy(USER_ID);
		demographicEntity.setCreateDateTime(LocalDateTime.now());
		demographicEntity.setUpdatedBy(USER_ID);
		demographicEntity.setUpdateDateTime(LocalDateTime.now());
		demographicEntity.setEncryptedDateTime(LocalDateTime.now());

		Mockito.when(validationUtil.requstParamValidator(Mockito.any())).thenReturn(true);
		Mockito.when(demographicRepository.findBypreRegistrationId(PRE_REG_ID)).thenReturn(demographicEntity);
		Mockito.when(cryptoUtil.decrypt(Mockito.any(), Mockito.any())).thenReturn(applicantDetailJson);
		Mockito.when(userDetailsService.matchesUser(Mockito.any(), Mockito.any(), Mockito.anyBoolean()))
				.thenReturn(true);
	}

	/** The security context is static — leaving one behind would leak into later tests in this fork. */
	@After
	public void tearDown() {
		SecurityContextHolder.clearContext();
	}

	private void authenticateAs(String... roles) {
		AuthUserDetails authUser = Mockito.mock(AuthUserDetails.class);
		Authentication authentication = Mockito.mock(Authentication.class);
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		Mockito.when(authentication.getPrincipal()).thenReturn(authUser);

		Collection<GrantedAuthority> authorities = new ArrayList<>();
		for (String role : roles) {
			authorities.add(new SimpleGrantedAuthority(role));
		}
		Mockito.<Collection<? extends GrantedAuthority>>when(authUser.getAuthorities()).thenReturn(authorities);
		Mockito.when(authUser.getUserId()).thenReturn(USER_ID);
		Mockito.when(authUser.getUsername()).thenReturn(USER_ID);
	}

	/** An individual still gets the record, and ownership is still enforced for that role. */
	@Test
	public void getDemographicDataAsIndividualReturnsRecordAndValidatesOwnership() {
		authenticateAs("ROLE_INDIVIDUAL");

		MainResponseDTO<DemographicResponseDTO> response = commonServiceUtil.getDemographicData(PRE_REG_ID);

		assertNotNull(response.getResponse());
		assertEquals(PRE_REG_ID, response.getResponse().getPreRegistrationId());
		Mockito.verify(userDetailsService).matchesUser(Mockito.eq(USER_ID), Mockito.any(), Mockito.anyBoolean());
	}

	/**
	 * Regression guard: a non-individual caller must still receive the record. Previously the
	 * "record not found" branch was attached to the role check, so this threw
	 * RecordNotFoundException instead of returning data.
	 */
	@Test
	public void getDemographicDataAsNonIndividualReturnsRecord() {
		authenticateAs("ROLE_REGISTRATION_OFFICER");

		MainResponseDTO<DemographicResponseDTO> response = commonServiceUtil.getDemographicData(PRE_REG_ID);

		assertNotNull(response.getResponse());
		assertEquals(PRE_REG_ID, response.getResponse().getPreRegistrationId());
	}

	/** Ownership validation is scoped to individuals — other roles must not be user-matched. */
	@Test
	public void getDemographicDataAsNonIndividualSkipsOwnershipValidation() {
		authenticateAs("ROLE_PREREGISTRATION_ADMIN");

		commonServiceUtil.getDemographicData(PRE_REG_ID);

		Mockito.verify(userDetailsService, Mockito.never()).matchesUser(Mockito.any(), Mockito.any(),
				Mockito.anyBoolean());
	}

	/** A caller holding several roles including ROLE_INDIVIDUAL is still validated as an individual. */
	@Test
	public void getDemographicDataWithMultipleRolesIncludingIndividualValidatesOwnership() {
		authenticateAs("ROLE_REGISTRATION_OFFICER", "ROLE_INDIVIDUAL");

		MainResponseDTO<DemographicResponseDTO> response = commonServiceUtil.getDemographicData(PRE_REG_ID);

		assertNotNull(response.getResponse());
		Mockito.verify(userDetailsService).matchesUser(Mockito.eq(USER_ID), Mockito.any(), Mockito.anyBoolean());
	}

	/** listAuth flattens granted authorities to plain role names. */
	@Test
	public void listAuthReturnsAuthorityNames() {
		List<String> roles = commonServiceUtil.listAuth(
				Arrays.asList(new SimpleGrantedAuthority("ROLE_INDIVIDUAL"), new SimpleGrantedAuthority("ROLE_ADMIN")));

		assertEquals(Arrays.asList("ROLE_INDIVIDUAL", "ROLE_ADMIN"), roles);
	}
}
