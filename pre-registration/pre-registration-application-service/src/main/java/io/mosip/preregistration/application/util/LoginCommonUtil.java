package io.mosip.preregistration.application.util;

import java.io.IOException;
import java.io.StringReader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.application.constant.PreRegLoginErrorConstants;
import io.mosip.preregistration.application.dto.CaptchaRequestDTO;
import io.mosip.preregistration.application.dto.CaptchaResposneDTO;
import io.mosip.preregistration.application.dto.MosipUserDTO;
import io.mosip.preregistration.application.dto.User;
import io.mosip.preregistration.application.errorcodes.LoginErrorCodes;
import io.mosip.preregistration.application.errorcodes.LoginErrorMessages;
import io.mosip.preregistration.application.exception.LanguagePropertiesException;
import io.mosip.preregistration.application.exception.PreRegLoginException;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.dto.ResponseWrapper;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.exception.InvalidRequestException;
import io.mosip.preregistration.core.exception.util.ParseResponseException;
import io.mosip.preregistration.core.util.ValidationUtil;

/**
 * 
 * @author Akshay Jain
 * @since 1.0.0
 */
@Component
public class LoginCommonUtil {

	/**
	 * Environment instance
	 */
	@Autowired
	private Environment env;

	@Autowired
	@Qualifier(value = "restTemplateConfig")
	private RestTemplate restTemplate;

	@Autowired
	private ValidationUtil validationUtil;

	/**
	 * Logger instance
	 */
	private Logger log = LoggerConfiguration.logConfig(LoginCommonUtil.class);

	@Autowired
	private ObjectMapper objectMapper;

	@Value("${otpChannel.mobile}")
	private String mobileChannel;

	@Value("${otpChannel.email}")
	private String emailChannel;

	@Value("${sendOtp.resource.url}")
	private String sendOtpResourceUrl;

	@Value("${prereg.auth.jwt.audience}")
	private String jwtAudience;

	@Value("${mosip.kernel.otp.expiry-time}")
	private int otpExpiryTime;


	@Value("${mosip.preregistration.login.service.version}")
	private String version;

	@Value("${mosip.preregistration.captcha.resourse.url}")
	private String captchaUrl;

	@Value("${mosip.preregistration.captcha.id.validate}")
	private String captchaRequestId;

	private static final String MOSIP_MANDATORY_LANGUAGE = "mosip.mandatory-languages";
	private static final String MOSIP_OPTIONAL_LANGUAGE = "mosip.optional-languages";
	private static final String MOSIP_MIN_LANGUAGE_COUNT = "mosip.min-languages.count";
	private static final String MOSIP_MAX_LANGUAGE_COUNT = "mosip.max-languages.count";

	/**
	 * This method will return the MainResponseDTO with id and version
	 * 
	 * @param mainRequestDto
	 * @return MainResponseDTO<?>
	 */
	public MainResponseDTO<?> getMainResponseDto(MainRequestDTO<?> mainRequestDto) {
		log.info("In getMainResponseDTO method of Login Common Util");
		MainResponseDTO<?> response = new MainResponseDTO<>();
		response.setId(mainRequestDto.getId());
		response.setVersion(mainRequestDto.getVersion());

		return response;
	}

	/**
	 * This method return ResponseEntity for the rest call made to the designated
	 * url
	 * 
	 * @param url
	 * @param mediaType
	 * @param body
	 * @param responseClass
	 * @return ResponseEntity<?>
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 * @throws RestClientException
	 */

	public ResponseEntity<?> callAuthService(String url, HttpMethod httpMethodType, MediaType mediaType, Object body,
			Map<String, String> headersMap, Class<?> responseClass) {
		ResponseEntity<?> response = null;
		try {
			log.info("In getResponseEntity method of Login Common Util");
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(mediaType);
			HttpEntity<?> request = null;
			if (headersMap != null) {
				headersMap.forEach((k, v) -> headers.add(k, v));
			}
			if (body != null) {
				request = new HttpEntity<>(body, headers);
			} else {
				request = new HttpEntity<>(headers);
			}
			log.info("In call to kernel rest service :{}", url);
			response = getRestTemplate().exchange(url, httpMethodType, request, responseClass);
		} catch (RestClientException | KeyManagementException | NoSuchAlgorithmException | KeyStoreException ex) {
			log.debug("Kernel rest call exception ", ex);
			throw new RestClientException("rest call failed");
		}
		return response;

	}

	/**
	 * This method provides validation of the userid and returns the otpChannel list
	 * 
	 * @param userId
	 * @param langCode
	 * @return List<String>
	 */
	public List<String> validateUserId(String userId) {
		log.info("In validateUserIdandLangCode method of Login Common Util");
		List<String> list = new ArrayList<>();
		if (userId == null || userId.isEmpty()) {
			throw new InvalidRequestException(LoginErrorCodes.PRG_AUTH_008.getCode(),
					LoginErrorMessages.INVALID_REQUEST_USERID.getMessage(), null);
		}
		if (validationUtil.phoneValidator(userId)) {
			list.add(mobileChannel);
			return list;
		} else if (validationUtil.emailValidator(userId)) {
			list.add(emailChannel);
			return list;
		}

		throw new InvalidRequestException(LoginErrorCodes.PRG_AUTH_008.getCode(),
				LoginErrorMessages.INVALID_REQUEST_USERID.getMessage(), null);
	}

	/**
	 * This method will validate the otp and userid for null values
	 * 
	 * @param user
	 */
	public void validateOtpAndUserid(User user) {
		log.info("In validateOtpAndUserid method of Login Common Util");
		if (user.getUserId() == null) {
			throw new InvalidRequestException(LoginErrorCodes.PRG_AUTH_008.getCode(),
					LoginErrorMessages.INVALID_REQUEST_USERID.getMessage(), null);
		} else if (user.getOtp() == null) {
			throw new InvalidRequestException(LoginErrorCodes.PRG_AUTH_010.getCode(),
					LoginErrorMessages.INVALID_REQUEST_OTP.getMessage(), null);
		}
	}

	/**
	 * This method will read value from response body and covert it into requested
	 * class object
	 * 
	 * @param serviceResponseBody
	 * @return
	 */
	public ResponseWrapper<?> requestBodyExchange(String serviceResponseBody) throws ParseResponseException {
		try {
			return objectMapper.readValue(serviceResponseBody, ResponseWrapper.class);
		} catch (IOException e) {
			throw new ParseResponseException(LoginErrorCodes.PRG_AUTH_011.getCode(),
					LoginErrorMessages.ERROR_WHILE_PARSING.getMessage(), null);

		}
	}

	/**
	 * This method is used to parse string to required object
	 * 
	 * @param serviceResponseBody
	 * @param responseClass
	 * @return
	 * @throws ParseResponseException
	 */
	public Object requestBodyExchangeObject(String serviceResponseBody, Class<?> responseClass)
			throws ParseResponseException {
		try {
			objectMapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			return objectMapper.readValue(serviceResponseBody, responseClass);
		} catch (IOException e) {
			throw new ParseResponseException(LoginErrorCodes.PRG_AUTH_011.getCode(),
					LoginErrorMessages.ERROR_WHILE_PARSING.getMessage(), null);

		}
	}

	/**
	 * This method is used for parse object to string
	 * 
	 * @param response
	 * @return
	 */
	public String responseToString(Object response) {
		try {
			return objectMapper.writeValueAsString(response);
		} catch (JsonProcessingException e) {

			throw new ParseResponseException("", "", null);
		}
	}

	/**
	 * This method is used for parsing string to properties
	 * 
	 * @param s
	 * @return
	 * @throws IOException
	 */
	public Properties parsePropertiesString(String s) throws IOException {
		final Properties p = new Properties();
		p.load(new StringReader(s));
		return p;
	}

	/**
	 * This method is used config rest call
	 * 
	 * @param filname
	 * @return
	 */
	public String getConfig(String filname) {
		String configServerUri = env.getProperty("spring.cloud.config.uri");
		String configLabel = env.getProperty("spring.cloud.config.label");
		String configProfile = env.getProperty("spring.profiles.active");
		String configAppName = env.getProperty("spring.cloud.config.name");
		StringBuilder uriBuilder = new StringBuilder();

		uriBuilder.append(configServerUri + "/").append(configAppName + "/").append(configProfile + "/")
				.append(configLabel + "/").append(filname);
		log.info(" URL in login service util of configRestCall {} ", uriBuilder);
		return restTemplate.getForObject(uriBuilder.toString(), String.class);

	}

	/**
	 * This method is used for create key value pair from congif file
	 * 
	 * @param prop
	 * @param configParamMap
	 * @param reqParams
	 */
	public void getConfigParams(Properties prop, Map<String, String> configParamMap, List<String> reqParams) {
		for (Entry<Object, Object> e : prop.entrySet()) {
			if (reqParams.contains(String.valueOf(e.getKey()))) {
				configParamMap.put(String.valueOf(e.getKey()), e.getValue().toString());
			}

		}
	}

	/**
	 * This method is used for create request map
	 * 
	 * @param requestDto
	 * @return
	 */
	public Map<String, String> createRequestMap(MainRequestDTO<?> requestDto) {
		log.info("In prepareRequestMap method of Login Service Util");
		Map<String, String> requestMap = new HashMap<>();
		requestMap.put("id", requestDto.getId());
		requestMap.put("version", requestDto.getVersion());
		if (!(requestDto.getRequesttime() == null || requestDto.getRequesttime().toString().isEmpty())) {
			LocalDate date = requestDto.getRequesttime().toInstant().atZone(ZoneId.of("UTC")).toLocalDate();
			requestMap.put("requesttime", date.toString());
		} else {
			requestMap.put("requesttime", null);
		}
		requestMap.put("request", requestDto.getRequest().toString());
		return requestMap;
	}

	public String getUserDetailsFromToken(Map<String, String> authHeader) {
		String url = sendOtpResourceUrl + "/authorize/validateToken";
		ResponseEntity<String> response = (ResponseEntity<String>) callAuthService(url, HttpMethod.POST,
				MediaType.APPLICATION_JSON, null, authHeader, String.class);
		ResponseWrapper<?> responseKernel = requestBodyExchange(response.getBody());
		if (!(responseKernel.getErrors() == null)) {
			log.error("Invalid Token");
			return null;
		}
		MosipUserDTO userDetailsDto = (MosipUserDTO) requestBodyExchangeObject(
				responseToString(responseKernel.getResponse()), MosipUserDTO.class);

		return userDetailsDto.getUserId();
	}

	public void validateLanguageProperties(Map<String, String> configParams) {
		try {
			log.info("In validateLanguageProperties method of  logincommon util");

			List<Object> mandatoryLanguages = Arrays.asList(env.getProperty(MOSIP_MANDATORY_LANGUAGE).split(","))
					.stream().distinct().collect(Collectors.toList());
			List<Object> optionalLanguages = Arrays.asList(env.getProperty(MOSIP_OPTIONAL_LANGUAGE).split(",")).stream()
					.distinct().collect(Collectors.toList());

			int minLanguageCount = tryParsePropertiesToInteger(MOSIP_MIN_LANGUAGE_COUNT);
			int maxLanguageCount = tryParsePropertiesToInteger(MOSIP_MAX_LANGUAGE_COUNT);

			for (Object mandatoryLanguage : mandatoryLanguages) {
				optionalLanguages.removeIf(optionalLang -> optionalLang.equals(mandatoryLanguage));
			}

			log.info("mandatory Language: {} and optional Languages: {} ", mandatoryLanguages, optionalLanguages);
			log.info("min-Language-count: {} and max-Languages-count: {} ", minLanguageCount, maxLanguageCount);

			if (Objects.nonNull(minLanguageCount)) {
				if (minLanguageCount > (mandatoryLanguages.size() + optionalLanguages.size())) {
					log.info(
							"min-language-count is count {} is greater than sum of number of mandatory and optional language {} overriding to sum of mandatpry and optional language",
							minLanguageCount, (mandatoryLanguages.size() + optionalLanguages.size()));
					minLanguageCount = mandatoryLanguages.size() + optionalLanguages.size();
				} else if (mandatoryLanguages.size() > 0 && minLanguageCount < mandatoryLanguages.size()) {
					log.info(
							"min-language-count is count {} is lesser than mandatory-languages {} overidding to mandatory-language size",
							minLanguageCount, mandatoryLanguages.size());
					minLanguageCount = mandatoryLanguages.size();
				} else if (minLanguageCount > 0 && minLanguageCount > maxLanguageCount) {
					log.info(
							"min-language-count is count {} is greater than max-language-count {} overidding to max-language-count",
							minLanguageCount, maxLanguageCount);
					minLanguageCount = maxLanguageCount;
				}
			} else {
				log.info("min-language-count is count null overidding to mandatory-language size {} {}",
						minLanguageCount, mandatoryLanguages.size());
				minLanguageCount = mandatoryLanguages.size() > 0 ? mandatoryLanguages.size() : 1;
			}

			if (Objects.nonNull(maxLanguageCount)) {
				if (maxLanguageCount > (mandatoryLanguages.size() + optionalLanguages.size())) {
					log.info(
							"max-language-count is count {} is greater than sum of number of mandatory and optional language {} overriding to sum of mandatpry and optional language",
							maxLanguageCount, (mandatoryLanguages.size() + optionalLanguages.size()));
					maxLanguageCount = mandatoryLanguages.size() + optionalLanguages.size();
				} else if (maxLanguageCount > 0 && maxLanguageCount < minLanguageCount) {
					log.info(
							"max-language-count is count {} is lesser than min-language-count {} overidding to max-language-count to min-language-count",
							maxLanguageCount, minLanguageCount);
					maxLanguageCount = minLanguageCount;
				} else if (mandatoryLanguages.size() > 0 && maxLanguageCount < mandatoryLanguages.size()) {
					log.info(
							"max-language-count is count {} is lesser than mandatory-languages {} overidding to mandatory-language size",
							maxLanguageCount, mandatoryLanguages.size());
					maxLanguageCount = mandatoryLanguages.size();
				}
			} else {
				log.info("max-language-count is count null overidding to min-language count {} {}", maxLanguageCount,
						minLanguageCount);
				maxLanguageCount = minLanguageCount;
			}
			configParams.put(MOSIP_MAX_LANGUAGE_COUNT, String.valueOf(maxLanguageCount));
			configParams.put(MOSIP_MIN_LANGUAGE_COUNT, String.valueOf(minLanguageCount));
			configParams.put(MOSIP_MANDATORY_LANGUAGE,
					mandatoryLanguages.stream().map(lang -> String.valueOf(lang)).collect(Collectors.joining(",")));
			configParams.put(MOSIP_OPTIONAL_LANGUAGE,
					optionalLanguages.stream().map(lang -> String.valueOf(lang)).collect(Collectors.joining(",")));
		} catch (

		Exception e) {
			log.error("Exception in validateLanguageProperties of logincommonutil ", e);
			throw new LanguagePropertiesException(LoginErrorCodes.PRG_AUTH_015.getCode(),
					LoginErrorMessages.LANGUAGE_PROPERTIES_NOT_FOUND.getMessage());
		}
	}

	Integer tryParsePropertiesToInteger(String prop) {
		try {
			Integer propCount;
			if (prop.equals(MOSIP_MAX_LANGUAGE_COUNT)) {
				propCount = Integer.parseInt(env.getProperty(prop, env.getProperty(MOSIP_MIN_LANGUAGE_COUNT)));
			} else
				propCount = Integer.parseInt(env.getProperty(prop, "1"));
			if (propCount < 1) {
				log.info("{} value is less than 1 {} overriding to 1", prop, propCount);
				return 1;
			} else
				return propCount;
		} catch (NumberFormatException e) {
			if (prop.equals(MOSIP_MAX_LANGUAGE_COUNT)) {
				log.info("{} specified is invaild overriding to default value : {}", prop,
						env.getProperty(MOSIP_MIN_LANGUAGE_COUNT));
				return Integer.parseInt(env.getProperty(MOSIP_MIN_LANGUAGE_COUNT));
			} else {
				log.info("{} specified is invaild overriding to default value : 1", prop);
				return 1;
			}

		}
	}

	public RestTemplate getRestTemplate() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {

		TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

		SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy)
				.build();

		SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

		CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

		requestFactory.setHttpClient(httpClient);
		return new RestTemplate(requestFactory);
	}

	public CaptchaResposneDTO validateCaptchaToken(String captchaToken) {

		if (captchaToken == null || captchaToken.isBlank()) {
			log.error("Validating Captcha token is null or blank");
			throw new PreRegLoginException(PreRegLoginErrorConstants.CAPTCHA_ERROR.getErrorCode(),
					PreRegLoginErrorConstants.CAPTCHA_ERROR.getErrorMessage());
		}

		CaptchaRequestDTO captcha = new CaptchaRequestDTO();
		captcha.setCaptchaToken(captchaToken);
		MainRequestDTO<CaptchaRequestDTO> captchaRequest = new MainRequestDTO<CaptchaRequestDTO>();
		captchaRequest.setRequest(captcha);
		captchaRequest.setRequesttime(new Date());

		captchaRequest.setVersion(version);
		captchaRequest.setId(captchaRequestId);
		HttpHeaders header = new HttpHeaders();
		header.setContentType(MediaType.APPLICATION_JSON_UTF8);
		HttpEntity<?> entity = new HttpEntity<>(captchaRequest, header);
		ResponseEntity<MainResponseDTO<CaptchaResposneDTO>> responseEntity = null;
		try {
			log.debug("Calling captcha service to validate token {}", captchaRequest);

			responseEntity = restTemplate.exchange(captchaUrl, HttpMethod.POST, entity,
					new ParameterizedTypeReference<MainResponseDTO<CaptchaResposneDTO>>() {
					});

			if (responseEntity.getBody().getErrors() != null) {
				log.error("validateCaptchaToken has an error {}", responseEntity.getBody().getErrors());
				throw new PreRegLoginException(responseEntity.getBody().getErrors().get(0).getErrorCode(),
						responseEntity.getBody().getErrors().get(0).getMessage());
			}

		} catch (RestClientException ex) {
			log.error("Error while Calling captcha service to validate token {}", ex);
			throw new PreRegLoginException(PreRegLoginErrorConstants.CAPTCHA_SEVER_ERROR.getErrorCode(),
					PreRegLoginErrorConstants.CAPTCHA_SEVER_ERROR.getErrorMessage());
		}

		return responseEntity.getBody().getResponse();

	}

	public String sendOtpJwtToken(String userId) {
		return Jwts.builder().setIssuedAt(Date.from(Instant.now())).setSubject(userId)
				.setExpiration(Date.from(Instant.now().plusSeconds(otpExpiryTime))).setAudience(jwtAudience).toString();

	}

}
