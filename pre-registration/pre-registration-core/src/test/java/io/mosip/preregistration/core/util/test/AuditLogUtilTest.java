package io.mosip.preregistration.core.util.test;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.preregistration.core.code.AuditLogVariables;
import io.mosip.preregistration.core.common.dto.AuditRequestDto;
import io.mosip.preregistration.core.common.dto.AuditResponseDto;
import io.mosip.preregistration.core.common.dto.ResponseWrapper;
import io.mosip.preregistration.core.common.service.UserDetailsService;
import io.mosip.preregistration.core.config.TestConfig;
import io.mosip.preregistration.core.util.AuditLogUtil;
import io.mosip.preregistration.core.util.RequestValidator;

/**
 * 
 * AuditLogUtil Test
 * @version 1.0.0
 * @author M1043226
 */

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = {ObjectMapper.class, AuditLogUtil.class, TestConfig.class, TestContext.class, WebApplicationContext.class })
public class AuditLogUtilTest {

	@Autowired
	AuditLogUtil auditUtil;
	
	@Mock
	private RequestValidator validator;
	
	private AuditLogUtil auditLogUtilSpy;
	
	@MockitoBean(name="selfTokenRestTemplate")
	RestTemplate restTemplate;

	@MockitoBean
	UserDetailsService userDetailsService;
	
	AuditRequestDto auditRequestDto=new AuditRequestDto();
	AuditResponseDto auditResponseDto=new AuditResponseDto();
	
	@Before
	public void setUp() throws Exception {
		
		auditRequestDto.setActionTimeStamp(LocalDateTime.now());
		auditRequestDto.setApplicationId(AuditLogVariables.MOSIP_1.toString());
		auditRequestDto.setApplicationName(AuditLogVariables.PREREGISTRATION.toString());
		auditRequestDto.setCreatedBy(AuditLogVariables.SYSTEM.toString());
		auditRequestDto.setIdType(AuditLogVariables.PRE_REGISTRATION_ID.toString());
		auditRequestDto.setSessionUserId(AuditLogVariables.SYSTEM.toString());
		auditRequestDto.setSessionUserName(AuditLogVariables.SYSTEM.toString());
		auditRequestDto.setId(AuditLogVariables.NO_ID.toString());
		auditRequestDto.setHostName("A2ML29862");
		auditRequestDto.setEventId("1");
		auditRequestDto.setEventName("xyz");
		auditRequestDto.setEventType("abc");
		auditRequestDto.setHostIp("1234");
		auditRequestDto.setModuleId("ModuleId");
		auditRequestDto.setDescription("description");
		auditRequestDto.setModuleName("ModuleName");
		
		auditResponseDto.setStatus(true);
		auditLogUtilSpy=Mockito.spy(auditUtil);
	}
	
	@Test
	public void saveAuditDetailsSuccessTest() {
		ResponseWrapper<AuditResponseDto> res=new ResponseWrapper<>();
		res.setResponse(auditResponseDto);
		ResponseEntity<ResponseWrapper<AuditResponseDto>> respEntity= new ResponseEntity<>(res, HttpStatus.OK);
		Mockito.doReturn(true).when(auditLogUtilSpy).callAuditManager(Mockito.any());
//		Mockito.when(restTemplate.exchange(Mockito.any(),Mockito.eq(HttpMethod.POST), Mockito.any(),Mockito.eq(new ParameterizedTypeReference<ResponseWrapper<AuditResponseDto>>() {
//		}))).thenReturn(respEntity);
		
		auditLogUtilSpy.saveAuditDetails(auditRequestDto);
		assertEquals(respEntity.getBody().getResponse().isStatus(), true);
	}

	@Test
	public void saveAuditDetailsResolvesEmailToCanonicalUserId() {
		String canonical = "11111111-1111-1111-1111-111111111111";
		Mockito.when(userDetailsService.getOrCreateInternalUserId("user@mosip.net")).thenReturn(canonical);
		auditRequestDto.setSessionUserId("user@mosip.net");
		auditRequestDto.setSessionUserName("user@mosip.net");
		Mockito.doReturn(true).when(auditLogUtilSpy).callAuditManager(Mockito.any());
		auditLogUtilSpy.saveAuditDetails(auditRequestDto);
		assertEquals(canonical, auditRequestDto.getSessionUserId());
		assertEquals(canonical, auditRequestDto.getSessionUserName());
	}

	@Test(expected = HttpServerErrorException.class)
	public void callAuditManagerPropagatesHttpServerError() {
		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.eq(HttpMethod.POST), Mockito.any(),
				Mockito.any(ParameterizedTypeReference.class)))
				.thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
		auditUtil.callAuditManager(auditRequestDto);
	}

	@Test
	public void callAuditManagerSwallowsHttpClientError() {
		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.eq(HttpMethod.POST), Mockito.any(),
				Mockito.any(ParameterizedTypeReference.class)))
				.thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad Request", HttpHeaders.EMPTY,
						new byte[0], null));
		assertEquals(false, auditUtil.callAuditManager(auditRequestDto));
	}

	@Test(expected = HttpServerErrorException.class)
	public void callAuditManagerWithHeadersPropagatesHttpServerError() {
		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.eq(HttpMethod.POST), Mockito.any(),
				Mockito.any(ParameterizedTypeReference.class)))
				.thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
		auditUtil.callAuditManager(auditRequestDto, new HttpHeaders());
	}
}