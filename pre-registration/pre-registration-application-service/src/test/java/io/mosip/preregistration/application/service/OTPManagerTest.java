package io.mosip.preregistration.application.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.preregistration.application.constant.PreRegLoginConstant;
import io.mosip.preregistration.application.dto.OtpRequestDTO;
import io.mosip.preregistration.application.entity.OtpTransaction;
import io.mosip.preregistration.application.exception.PreRegLoginException;
import io.mosip.preregistration.application.repository.OtpTxnRepository;
import io.mosip.preregistration.application.service.util.NotificationServiceUtil;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import junit.framework.Assert;

@RunWith(JUnit4.class)
@SpringBootTest
@ContextConfiguration(classes = { OTPManager.class })
public class OTPManagerTest {

	@InjectMocks
	private OTPManager otpManager;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);	
		ReflectionTestUtils.setField(otpManager, "sendOtpResourceUrl", "home");
		
	}
	
	@Value("${sendOtp.resource.url}")
	private String sendOtpResourceUrl;
	
	@Value("${secretKey}")
	private String secretKey;

	@Value("${clientId}")
	private String clientId;
	
	@Value("${appId}")
	private String appId;
	
	@Value("${version}")
	private String version;
	
	@Mock
	NotificationServiceUtil notification;

	@Mock
	@Qualifier("restTemplateConfig")
	RestTemplate restTemplate;
	
	@Mock
	private Environment environment;

	@Mock
	private OtpTxnRepository otpRepo;
	
	@Test(expected = PreRegLoginException.class)
	public void testsendOtpPreRegLoginException() throws IOException {
		MainRequestDTO<OtpRequestDTO> requestDTO =new  MainRequestDTO<OtpRequestDTO>();
		OtpRequestDTO request=new OtpRequestDTO();
		request.setUserId("");
		requestDTO.setRequest(request);
		String channelType = null;
		String language = null;
		otpManager.sendOtp(requestDTO,channelType,language);
	}
	
	@Test(expected=PreRegLoginException.class)
	public void testsendOtpPreRegLoginException2() throws IOException {
		MainRequestDTO<OtpRequestDTO> requestDTO =new  MainRequestDTO<OtpRequestDTO>();
		OtpRequestDTO request=new OtpRequestDTO();
		request.setUserId("");
		requestDTO.setRequest(request);
		String channelType = null;
		String language = null;
		HttpHeaders headers = new HttpHeaders();
		headers.add("set-cookie","abcd");
		
		ResponseEntity<String> responseEntity = new ResponseEntity<String>("{\r\n" + 
				"  \"response\":{\r\n" + 
				"  \"status\":\"Success\"\r\n" + 
				"  }\r\n" + 
				"}",headers, HttpStatus.ACCEPTED);

		Mockito.when(restTemplate.exchange(ArgumentMatchers.anyString(),
                ArgumentMatchers.any(HttpMethod.class),
                ArgumentMatchers.any(),
                ArgumentMatchers.<Class<String>>any()))
           .thenReturn(responseEntity);
		otpManager.sendOtp(requestDTO,channelType,language);
	}
	
	@Test(expected=PreRegLoginException.class)
	public void testsendOtpPreRegLoginException3() throws IOException {
		MainRequestDTO<OtpRequestDTO> requestDTO =new  MainRequestDTO<OtpRequestDTO>();
		OtpRequestDTO request=new OtpRequestDTO();
		request.setUserId("");
		requestDTO.setRequest(request);
		String channelType = null;
		String language = null;
		HttpHeaders headers = new HttpHeaders();
		headers.add("set-cookie","abcd;");
		
		ResponseEntity<String> responseEntity = new ResponseEntity<String>("{\r\n" + 
				"  \"response\":{\r\n" + 
				"  \"status\":\"Success\"\r\n" + 
				"  }\r\n" + 
				"}",headers, HttpStatus.ACCEPTED);

		Mockito.when(restTemplate.exchange( Mockito.eq("home/authenticate/clientidsecretkey"),
                ArgumentMatchers.any(HttpMethod.class),
                ArgumentMatchers.any(),
                Mockito.eq(String.class)
                ))
           .thenReturn(responseEntity);
		
		Map<String, String> map=new HashMap<String, String>();
		map.put("status", "USER_BLOCKED");
		ResponseWrapper<Map<String, String>> responseMap=new ResponseWrapper<>();
		
		responseMap.setResponse(map);
		ResponseEntity<ResponseWrapper> response = new ResponseEntity<>(responseMap, HttpStatus.ACCEPTED);
		Mockito.when(environment.getProperty(Mockito.any())).thenReturn("https://dev.mosip.net/v1/otpmanager/otp/generate");
		Mockito.when(restTemplate.exchange(ArgumentMatchers.anyString(),
                ArgumentMatchers.any(HttpMethod.class),
                ArgumentMatchers.any(),
                Mockito.eq(ResponseWrapper.class)))
           .thenReturn(response);
		otpManager.sendOtp(requestDTO,channelType,language);
	}

	@Test
	public void testsendOtpSuccessPhone() throws IOException {
		MainRequestDTO<OtpRequestDTO> requestDTO =new  MainRequestDTO<OtpRequestDTO>();
		OtpRequestDTO request=new OtpRequestDTO();
		request.setUserId("");
		requestDTO.setRequest(request);
		String channelType = "phone";
		String language = null;
		HttpHeaders headers = new HttpHeaders();
		headers.add("set-cookie","abcd;");
		
		ResponseEntity<String> responseEntity = new ResponseEntity<String>("{\r\n" + 
				"  \"response\":{\r\n" + 
				"  \"status\":\"Success\"\r\n" + 
				"  }\r\n" + 
				"}",headers, HttpStatus.ACCEPTED);		
		
		Mockito.when(restTemplate.exchange( Mockito.eq("home/authenticate/clientidsecretkey"),
                ArgumentMatchers.any(HttpMethod.class),
                ArgumentMatchers.any(),
                Mockito.eq(String.class)
                ))
           .thenReturn(responseEntity);
		
		Map<String, String> map=new HashMap<String, String>();
		map.put("status", "USER_NOT_BLOCKED");
		ResponseWrapper<Map<String, String>> responseMap=new ResponseWrapper<>();
		
		responseMap.setResponse(map);
		ResponseEntity<ResponseWrapper> response = new ResponseEntity<>(responseMap, HttpStatus.ACCEPTED);
		Mockito.when(environment.getProperty(Mockito.any())).thenReturn("https://dev.mosip.net/v1/otpmanager/otp/generate");

		Mockito.when(restTemplate.exchange(ArgumentMatchers.anyString(),
                ArgumentMatchers.any(HttpMethod.class),
                ArgumentMatchers.any(),
                Mockito.eq(ResponseWrapper.class)))
           .thenReturn(response);
		Mockito.when(environment.getProperty(PreRegLoginConstant.MOSIP_KERNEL_OTP_EXPIRY_TIME, Long.class)).thenReturn(1800L);
		Mockito.when(environment.getProperty(PreRegLoginConstant.MOSIP_KERNEL_OTP_EXPIRY_TIME, Integer.class)).thenReturn(60);
		Mockito.when(environment.getProperty("mosip.notification.timezone")).thenReturn("GMT+05:30");
		assertTrue(otpManager.sendOtp(requestDTO,channelType,language));
	}
	
	@Test
	public void testsendOtpSuccessEmail2() throws IOException {
		MainRequestDTO<OtpRequestDTO> requestDTO =new  MainRequestDTO<OtpRequestDTO>();
		OtpRequestDTO request=new OtpRequestDTO();
		request.setUserId("");
		requestDTO.setRequest(request);
		String channelType = "email";
		String language = null;
		HttpHeaders headers = new HttpHeaders();
		headers.add("set-cookie","abcd;");
		
		ResponseEntity<String> responseEntity = new ResponseEntity<String>("{\r\n" + 
				"  \"response\":{\r\n" + 
				"  \"status\":\"Success\"\r\n" + 
				"  }\r\n" + 
				"}",headers, HttpStatus.ACCEPTED);
		Mockito.when(otpRepo.existsByOtpHashAndStatusCode(Mockito.any(), Mockito.any()))
        .thenReturn(true);
		OtpTransaction otpTxn = new OtpTransaction();
		Mockito.when(otpRepo.findTopByOtpHashAndStatusCode(Mockito.any(), Mockito.any()))
        .thenReturn(otpTxn);
		
		Mockito.when(restTemplate.exchange( Mockito.eq("home/authenticate/clientidsecretkey"),
                ArgumentMatchers.any(HttpMethod.class),
                ArgumentMatchers.any(),
                Mockito.eq(String.class)
                ))
           .thenReturn(responseEntity);
		
		Map<String, String> map=new HashMap<String, String>();
		map.put("status", "USER_NOT_BLOCKED");
		ResponseWrapper<Map<String, String>> responseMap=new ResponseWrapper<>();
		
		responseMap.setResponse(map);
		ResponseEntity<ResponseWrapper> response = new ResponseEntity<>(responseMap, HttpStatus.ACCEPTED);
		Mockito.when(environment.getProperty(Mockito.any())).thenReturn("https://dev.mosip.net/v1/otpmanager/otp/generate");

		Mockito.when(restTemplate.exchange(ArgumentMatchers.anyString(),
                ArgumentMatchers.any(HttpMethod.class),
                ArgumentMatchers.any(),
                Mockito.eq(ResponseWrapper.class)))
           .thenReturn(response);

		Mockito.when(environment.getProperty(PreRegLoginConstant.MOSIP_KERNEL_OTP_EXPIRY_TIME, Long.class)).thenReturn(1800L);
		Mockito.when(environment.getProperty(PreRegLoginConstant.MOSIP_KERNEL_OTP_EXPIRY_TIME, Integer.class)).thenReturn(60);
		Mockito.when(environment.getProperty("mosip.notification.timezone")).thenReturn("GMT+05:30");
		assertTrue(otpManager.sendOtp(requestDTO,channelType,language));
	}
	
	@Test
	public void testsendOtpSuccessEmail() throws IOException {
		MainRequestDTO<OtpRequestDTO> requestDTO =new  MainRequestDTO<OtpRequestDTO>();
		OtpRequestDTO request=new OtpRequestDTO();
		request.setUserId("");
		requestDTO.setRequest(request);
		String channelType = "email";
		String language = null;
		HttpHeaders headers = new HttpHeaders();
		headers.add("set-cookie","abcd;");
		
		ResponseEntity<String> responseEntity = new ResponseEntity<String>("{\r\n" + 
				"  \"response\":{\r\n" + 
				"  \"status\":\"Success\"\r\n" + 
				"  }\r\n" + 
				"}",headers, HttpStatus.ACCEPTED);
//		String tokenUrl = "home" + "/authenticate/clientidsecretkey";
//		
//		
//		HttpHeaders headers = new HttpHeaders();
//		headers.setContentType(MediaType.APPLICATION_JSON);
//		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
//		JSONObject jsonObject = new JSONObject();
//		jsonObject.put("id", tokenUrl);
//		jsonObject.put("metadata", new JSONObject());
//		JSONObject jsonObject1 = new JSONObject();
//		jsonObject1.put("clientId", clientId);
//		jsonObject1.put("secretKey", secretKey);
//		jsonObject1.put("appId", appId);
//		jsonObject.put("requesttime", LocalDateTime.now().toString());
//		jsonObject.put("version", version);
//		jsonObject.put("request", jsonObject1);
//
//		HttpEntity<String> entity = new HttpEntity<String>(jsonObject.toString(), headers);
//		
		
		Mockito.when(restTemplate.exchange( Mockito.eq("home/authenticate/clientidsecretkey"),
                ArgumentMatchers.any(HttpMethod.class),
                ArgumentMatchers.any(),
                Mockito.eq(String.class)
//                ArgumentMatchers.<Class<String>>any()
                ))
           .thenReturn(responseEntity);
		
		Map<String, String> map=new HashMap<String, String>();
		map.put("status", "USER_NOT_BLOCKED");
		ResponseWrapper<Map<String, String>> responseMap=new ResponseWrapper<>();
		
		responseMap.setResponse(map);
		ResponseEntity<ResponseWrapper> response = new ResponseEntity<>(responseMap, HttpStatus.ACCEPTED);
		Mockito.when(environment.getProperty(Mockito.any())).thenReturn("https://dev.mosip.net/v1/otpmanager/otp/generate");

		Mockito.when(restTemplate.exchange(ArgumentMatchers.anyString(),
                ArgumentMatchers.any(HttpMethod.class),
                ArgumentMatchers.any(),
                Mockito.eq(ResponseWrapper.class)))
           .thenReturn(response);
//		Mockito.when(restTemplate.exchange(ArgumentMatchers.anyString(),
//                ArgumentMatchers.any(HttpMethod.class),
//                ArgumentMatchers.any(),
//                ArgumentMatchers.<Class<ResponseWrapper>>any())
//				.getBody())
//           .thenReturn(responseMap);

		Mockito.when(environment.getProperty(PreRegLoginConstant.MOSIP_KERNEL_OTP_EXPIRY_TIME, Long.class)).thenReturn(1800L);
		Mockito.when(environment.getProperty(PreRegLoginConstant.MOSIP_KERNEL_OTP_EXPIRY_TIME, Integer.class)).thenReturn(60);
		Mockito.when(environment.getProperty("mosip.notification.timezone")).thenReturn("GMT+05:30");
		assertTrue(otpManager.sendOtp(requestDTO,channelType,language));
		}
	
	@Test(expected=PreRegLoginException.class)
	public void testvalidateOtpPreRegLoginException(){
		Mockito.when(otpRepo.existsByOtpHashAndStatusCode(Mockito.any(), Mockito.any())).thenReturn(true);
		OtpTransaction otpTxn = new OtpTransaction();
		otpTxn.setExpiryDtimes(DateUtils.getUTCCurrentDateTime());
		Mockito.when(otpRepo.findTopByOtpHashAndStatusCode(Mockito.any(), Mockito.any()))
        .thenReturn(otpTxn);
		otpManager.validateOtp("111111","42456");
	}
	
	@Test
	public void testvalidateOtpSuccess2(){
		Mockito.when(otpRepo.existsByOtpHashAndStatusCode(Mockito.any(), Mockito.any())).thenReturn(true);
		OtpTransaction otpTxn = new OtpTransaction();
		LocalDateTime a = LocalDateTime.of(2028, 2, 13, 15, 56);    
	    
		otpTxn.setExpiryDtimes(a);
		Mockito.when(otpRepo.findTopByOtpHashAndStatusCode(Mockito.any(), Mockito.any()))
        .thenReturn(otpTxn);
		assertTrue(otpManager.validateOtp("111111","42456"));
	}
	
	@Test (expected = PreRegLoginException.class)
	public void testvalidateOtpExceededException(){
		Mockito.when(otpRepo.existsByOtpHashAndStatusCode(Mockito.any(), Mockito.any())).thenReturn(false);
		OtpTransaction otpTxn = new OtpTransaction();
		LocalDateTime a = LocalDateTime.of(2028, 2, 13, 15, 56);    
		otpTxn.setExpiryDtimes(a);
		Mockito.when(otpRepo.findByRefIdAndStatusCode(Mockito.any(), Mockito.any()))
        .thenReturn(otpTxn);
		otpManager.validateOtp("111111","42456");
	}
}
