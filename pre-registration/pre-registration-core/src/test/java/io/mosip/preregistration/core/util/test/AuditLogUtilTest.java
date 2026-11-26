package io.mosip.preregistration.core.util.test;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.preregistration.core.code.AuditLogVariables;
import io.mosip.preregistration.core.common.dto.AuditRequestDto;
import io.mosip.preregistration.core.common.dto.AuditResponseDto;
import io.mosip.preregistration.core.common.dto.ResponseWrapper;
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
	
	@MockBean(name="selfTokenRestTemplate")
	RestTemplate restTemplate;
	
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

	/** No UserDetailsService bean in this context: a login id must be masked, not throw. */
	@Test
	public void should_maskLoginId_when_userDetailsServiceIsAbsent() {
		auditRequestDto.setSessionUserId("resident@example.com");
		auditRequestDto.setSessionUserName("resident@example.com");
		Mockito.doReturn(true).when(auditLogUtilSpy).callAuditManager(Mockito.any());

		auditLogUtilSpy.saveAuditDetails(auditRequestDto);

		ArgumentCaptor<AuditRequestDto> sent = ArgumentCaptor.forClass(AuditRequestDto.class);
		Mockito.verify(auditLogUtilSpy).callAuditManager(sent.capture());
		assertEquals("r***@example.com", sent.getValue().getSessionUserId());
		assertEquals("r***@example.com", sent.getValue().getSessionUserName());
	}
}