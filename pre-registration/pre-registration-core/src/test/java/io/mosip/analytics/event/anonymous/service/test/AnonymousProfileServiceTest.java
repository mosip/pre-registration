package io.mosip.analytics.event.anonymous.service.test;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.analytics.event.anonymous.dto.AnonymousProfileRequestDTO;
import io.mosip.analytics.event.anonymous.dto.AnonymousProfileResponseDTO;
import io.mosip.analytics.event.anonymous.entity.AnonymousProfileEntity;
import io.mosip.analytics.event.anonymous.repository.AnonymousProfileRepostiory;
import io.mosip.analytics.event.anonymous.service.AnonymousProfileService;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;

@RunWith(JUnit4.class)
@SpringBootTest
@ContextConfiguration(classes = { AnonymousProfileService.class })
public class AnonymousProfileServiceTest {

	@InjectMocks
	private AnonymousProfileService anonymousProfileService;

	@Mock
	private AnonymousProfileRepostiory anonymousProfileRepostiory;

	@Value("${mosip.utc-datetime-pattern}")
	private String utcDateTimePattern;

	@Value("${mosip.preregistration.anonymous-profile-username}")
    private String anonymousProfileUsername;

	AnonymousProfileResponseDTO responseDto = new AnonymousProfileResponseDTO();

	LocalDateTime currentDateTime = LocalDateTime.now(ZoneId.of("UTC"));

	AnonymousProfileEntity requestEntity = new AnonymousProfileEntity();

	AnonymousProfileRequestDTO requestDto = new AnonymousProfileRequestDTO();

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		ReflectionTestUtils.setField(anonymousProfileService, "utcDateTimePattern", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

		AuthUserDetails applicationUser = Mockito.mock(AuthUserDetails.class);
		Authentication authentication = Mockito.mock(Authentication.class);
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(applicationUser);
	}

	@Test
	public void saveAnonymousProfileTest() {
		String profile = "Demo";
		String id = "1234";

		requestEntity.setId(id);
		requestEntity.setProfile(requestDto.getProfileDetails());
		requestEntity.setCreatedBy(anonymousProfileUsername);
		requestEntity.setCreateDateTime(currentDateTime);
		requestEntity.setUpdatedBy(anonymousProfileUsername);
		requestEntity.setUpdateDateTime(currentDateTime);
		requestEntity.setIsDeleted(false);
		requestDto.setProfileDetails(profile);

		AnonymousProfileEntity responseEntity = new AnonymousProfileEntity();
		Mockito.when(anonymousProfileRepostiory.save(Mockito.any())).thenReturn(requestEntity);
		responseDto.setProfile(responseEntity.getProfile());
		responseDto.setId(id);
		responseDto.setCreatedBy(responseEntity.getCreatedBy());
		responseDto.setCreatedDateTime(anonymousProfileService.getLocalDateString(currentDateTime));
		responseDto.setUpdatedBy(responseEntity.getUpdatedBy());
		responseDto.setUpdatedDateTime(anonymousProfileService.getLocalDateString(LocalDateTime.now()));
		assertNotNull(anonymousProfileService.saveAnonymousProfile(requestDto));
	}

	@Test
	public void saveAnonymousProfileExceptionTest() throws Exception {
		String profile = "Demo";
		String id = "1234";

		requestEntity.setId(id);
		requestEntity.setProfile(requestDto.getProfileDetails());
		requestEntity.setCreatedBy(anonymousProfileUsername);
		requestEntity.setCreateDateTime(currentDateTime);
		requestEntity.setUpdatedBy(anonymousProfileUsername);
		requestEntity.setUpdateDateTime(currentDateTime);
		requestEntity.setIsDeleted(false);
		requestDto.setProfileDetails(profile);

		AnonymousProfileEntity responseEntity = new AnonymousProfileEntity();
		Mockito.when(anonymousProfileRepostiory.save(Mockito.any())).thenReturn(responseEntity);
		assertNotNull(anonymousProfileService.saveAnonymousProfile(requestDto));
	}

}
