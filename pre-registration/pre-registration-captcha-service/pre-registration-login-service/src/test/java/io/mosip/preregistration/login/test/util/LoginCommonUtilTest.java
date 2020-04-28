package io.mosip.preregistration.login.test.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.exception.InvalidRequestException;
import io.mosip.preregistration.core.exception.util.ParseResponseException;
import io.mosip.preregistration.core.util.ValidationUtil;
import io.mosip.preregistration.login.dto.User;
import io.mosip.preregistration.login.util.LoginCommonUtil;
import net.minidev.json.JSONObject;


@RunWith(SpringRunner.class)
@SpringBootTest
public class LoginCommonUtilTest {

	@Value("${mosip.id.validation.identity.phone}")
	private static String phoneRegex;

	@MockBean
	private RestTemplate restTemplate;
	
	
	@InjectMocks
	private LoginCommonUtil commonUtil;
	
	@Mock
	private ValidationUtil validationutil;
	
	@Mock
	private ObjectMapper objectMapper;
	
	MainRequestDTO<String> mainRequestDTO=new MainRequestDTO<>();
	
	Map<String, String> authHeader = new HashMap<String, String>();
	
	List<String> reqParams = new ArrayList<>();
	
	@Before
	public void setUp() throws Exception {
		
		mainRequestDTO.setId("String");
		mainRequestDTO.setVersion("v1");
		mainRequestDTO.setRequesttime(new Date());
		mainRequestDTO.setRequest("xyz");
	}
	
	@Test
	public void getMainResponseDtoTest() {
		commonUtil.getMainResponseDto(mainRequestDTO);
	}
	
	@Test(expected=InvalidRequestException.class)
	public void validateUserIdNullSuccessTest() {
		commonUtil.validateUserId(null);
	}
	
	@Test
	public void phoneValidatorSuccessTest() {
		Mockito.when(validationutil.phoneValidator(Mockito.anyString())).thenReturn(true);
		commonUtil.validateUserId("9938738987");
	}
	
	@Test
	public void emailValidatorSuccessTest() {
		Mockito.when(validationutil.emailValidator(Mockito.anyString())).thenReturn(true);
		commonUtil.validateUserId("test@gmail.com");
	}
	
	@Test(expected=InvalidRequestException.class)
	public void validateUserIdFailureTest() {
		commonUtil.validateUserId("1234");
	}
	
	@Test(expected=InvalidRequestException.class)
	public void validateUserIdNullTest() {
		User user = new User();
		user.setUserId(null);
		user.setOtp("1234");
		commonUtil.validateOtpAndUserid(user);
	}
	
	@Test(expected=InvalidRequestException.class)
	public void validateOtpNullTest() {
		User user = new User();
		user.setUserId("123456789");
		user.setOtp(null);
		commonUtil.validateOtpAndUserid(user);
	}
	
		
	@Test
	public void createRequestMapTest() {
		commonUtil.createRequestMap(mainRequestDTO);
	}
	
	@Test
	public void requestTimeNullCheckTest() {
		mainRequestDTO.setRequesttime(null);
		commonUtil.createRequestMap(mainRequestDTO);
	}
	
	
	@Test
	public void requestBodyExchangeSuccessTest() {
		commonUtil.requestBodyExchange("String");
	}
	
	@Test
	public void responseToStringSuccessTest() {
		Object response=new Object();
		commonUtil.responseToString(response);
	}
	
	@Test(expected=ParseResponseException.class)
	public void responseToStringFailureTest() throws JsonProcessingException {
		Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenThrow(JsonProcessingException.class);
		commonUtil.responseToString(null);
	}
	
	@Test
	public void requestBodyExchangeObjectTest() {
		commonUtil.requestBodyExchangeObject("String", JSONObject.class);
	}
	
//	@Test(expected=ParseResponseException.class)
//	public void requestBodyExchangeObjectFailureTest() throws JsonParseException, JsonMappingException, IOException {
//		ObjectMapper mapper=new ObjectMapper();
//		Mockito.when(objectMapper.readValue("{}", JSONObject.class )).thenThrow(IOException.class);
//		commonUtil.requestBodyExchangeObject("", JSONObject.class);
//	}
	
	@Test(expected=RestClientException.class)
	public void getUserDetailsFromTokenTest() {
		ResponseEntity<?> respEntity = null;
		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.eq(HttpMethod.POST), Mockito.any(),
				Mockito.eq(Class.class))).thenReturn((ResponseEntity<Class>) respEntity);
		commonUtil.getUserDetailsFromToken(authHeader);
	}
	
	
	@Test
	public void getConfigParamsTest() throws IOException {
		reqParams.add("String");
		commonUtil.getConfigParams(commonUtil.parsePropertiesString("String"), authHeader, reqParams);
	}
	
	
}
