package io.mosip.preregistration.application.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * This class provides different methods for login called by the controller 
 * 
 * @author M1050360
 * @since 1.0.0
 */

import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.core.code.AuditLogVariables;
import io.mosip.preregistration.core.code.EventId;
import io.mosip.preregistration.core.code.EventName;
import io.mosip.preregistration.core.code.EventType;
import io.mosip.preregistration.core.common.dto.AuditRequestDto;
import io.mosip.preregistration.core.common.dto.AuthNResponse;
import io.mosip.preregistration.core.common.dto.ExceptionJSONInfoDTO;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.dto.RequestWrapper;
import io.mosip.preregistration.core.common.dto.ResponseWrapper;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.util.AuditLogUtil;
import io.mosip.preregistration.core.util.GenericUtil;
import io.mosip.preregistration.application.constant.PreRegLoginConstant;
import io.mosip.preregistration.application.dto.ClientSecretDTO;
import io.mosip.preregistration.application.dto.OtpRequestDTO;
import io.mosip.preregistration.application.dto.OtpUser;
import io.mosip.preregistration.application.dto.User;
import io.mosip.preregistration.application.dto.UserOtp;
import io.mosip.preregistration.application.errorcodes.LoginErrorCodes;
import io.mosip.preregistration.application.errorcodes.LoginErrorMessages;
import io.mosip.preregistration.application.exception.ConfigFileNotFoundException;
import io.mosip.preregistration.application.exception.InvalidOtpOrUseridException;
import io.mosip.preregistration.application.exception.LoginServiceException;
import io.mosip.preregistration.application.exception.NoAuthTokenException;
import io.mosip.preregistration.application.exception.PreRegLoginException;
import io.mosip.preregistration.application.exception.util.LoginExceptionCatcher;
import io.mosip.preregistration.application.util.LoginCommonUtil;

@Service
public class LoginService {

	private Logger log = LoggerConfiguration.logConfig(LoginService.class);

	/**
	 * Autowired reference for {@link #authCommonUtil}
	 */
	@Autowired
	private LoginCommonUtil loginCommonUtil;

	@Value("${global.config.file}")
	private String globalFileName;

	@Value("${pre.reg.config.file}")
	private String preRegFileName;

	@Value("${ui.config.params}")
	private String uiConfigParams;

	@Value("${mosip.preregistration.login.id.invalidatetoken}")
	private String invalidateTokenId;

	@Value("${mosip.preregistration.login.id.config}")
	private String configId;

	@Value("${mosip.preregistration.login.service.version}")
	private String version;
	/**
	 * Reference for ${sendOtp.resource.url} from property file
	 */
	@Value("${sendOtp.resource.url}")
	private String sendOtpResourceUrl;

	@Value("${validationStatus}")
	private String status;

	private List<String> otpChannel;

	@Value("${userIdType}")
	private String useridtype;

	@Value("${appId}")
	private String appId;

	@Value("${context}")
	private String context;

	@Autowired
	AuditLogUtil auditLogUtil;

	@Value("${clientId}")
	private String clientId;

	@Value("${secretKey}")
	private String secretKey;

	@Value("${prereg.auth.jwt.secret}")
	private String jwtSecret;

	@Value("${prereg.auth.jwt.token.expiration}")
	private String jwtTokenExpiryTime;

	@Value("${prereg.auth.jwt.token.roles}")
	private String jwtTokenRoles;

	@Autowired
	OTPManager otpmanager;

	@Autowired
	private RestTemplate restTemplate;

	private String globalConfig;
	private String preregConfig;


	public void setupLoginService() {
		log.info("sessionId", "idType", "id", "In setupLoginService method of login service");
		globalConfig = loginCommonUtil.getConfig(globalFileName);
		preregConfig = loginCommonUtil.getConfig(preRegFileName);
		log.info("sessionId", "idType", "id", "Fetched the globalConfig and preRegconfig from config server");
	}


	/**
	 * It will fetch otp from Kernel auth service and send to the userId provided
	 * 
	 * @param userOtpRequest
	 * @return MainResponseDTO<AuthNResponse>
	 */
	public MainResponseDTO<AuthNResponse> sendOTP(MainRequestDTO<OtpRequestDTO> userOtpRequest, String language) {
		MainResponseDTO<AuthNResponse> response = null;
		String userid = null;
		boolean isSuccess = false;
		log.info("sessionId", "idType", "id", "In callsendOtp method of login service  with request " + userOtpRequest);

		try {
			response = (MainResponseDTO<AuthNResponse>) loginCommonUtil.getMainResponseDto(userOtpRequest);
			log.debug("sessionId", "idType", "id", "response after loginCommonUtil" + response);

			userid = userOtpRequest.getRequest().getUserId();
			otpChannel = loginCommonUtil.validateUserId(userid);
			boolean otpSent = otpmanager.sendOtp(userOtpRequest, otpChannel.get(0), language);
			AuthNResponse authNResponse = null;
			if (otpSent) {
				if (otpChannel.get(0).equalsIgnoreCase(PreRegLoginConstant.PHONE_NUMBER))
					authNResponse = new AuthNResponse(PreRegLoginConstant.SMS_SUCCESS, PreRegLoginConstant.SUCCESS);
				else
					authNResponse = new AuthNResponse(PreRegLoginConstant.EMAIL_SUCCESS, PreRegLoginConstant.SUCCESS);
				response.setResponse(authNResponse);
				isSuccess = true;
			} else
				isSuccess = false;

			response.setResponsetime(GenericUtil.getCurrentResponseTime());
		} catch (HttpServerErrorException | HttpClientErrorException ex) {
			log.debug("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.info("sessionId", "idType", "id",
					"In callsendOtp method of login service- " + ex.getResponseBodyAsString());
			new LoginExceptionCatcher().handle(ex, "sendOtp", response);
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id", "In callsendOtp method of login service- " + ex.getMessage());
			new LoginExceptionCatcher().handle(ex, "sendOtp", response);
		} finally {
			if (isSuccess) {
				setAuditValues(EventId.PRE_410.toString(), EventName.AUTHENTICATION.toString(),
						EventType.BUSINESS.toString(), "Otp send sucessfully", AuditLogVariables.NO_ID.toString(),
						userid, userid);
			} else {
				ExceptionJSONInfoDTO errors = new ExceptionJSONInfoDTO(PreRegLoginConstant.OTP_ERROR_CODE,
						PreRegLoginConstant.OTP_ERROR_MESSAGE);
				List<ExceptionJSONInfoDTO> lst = new ArrayList();
				lst.add(errors);
				response.setErrors(lst);
				response.setResponse(null);
				setAuditValues(EventId.PRE_405.toString(), EventName.EXCEPTION.toString(), EventType.SYSTEM.toString(),
						"Otp fail to send", AuditLogVariables.NO_ID.toString(), userid, userid);
			}
		}
		return response;
	}

	/**
	 * It will validate userId & otp and provide with a access token
	 * 
	 * @param userIdOtpRequest
	 * @return MainResponseDTO<AuthNResponse>
	 */
	public MainResponseDTO<AuthNResponse> validateWithUserIdOtp(MainRequestDTO<User> userIdOtpRequest) {
		log.info("sessionId", "idType", "id", "In calluserIdOtp method of login service ");
		MainResponseDTO<AuthNResponse> response = null;
		response = (MainResponseDTO<AuthNResponse>) loginCommonUtil.getMainResponseDto(userIdOtpRequest);
		String userid = null;
		boolean isSuccess = false;

		try {
			User user = userIdOtpRequest.getRequest();
			userid = user.getUserId().toLowerCase();

			loginCommonUtil.validateOtpAndUserid(user);
			boolean validated = otpmanager.validateOtp(user.getOtp(), user.getUserId());
			AuthNResponse authresponse = new AuthNResponse();
			if (validated) {
				authresponse.setMessage(PreRegLoginConstant.VALIDATION_SUCCESS);
				authresponse.setStatus(PreRegLoginConstant.SUCCESS);

			} else {
				authresponse.setMessage(PreRegLoginConstant.VALIDATION_UNSUCCESS);
				authresponse.setStatus(PreRegLoginConstant.UNSUCCESS);

			}
			response.setResponse(authresponse);
			isSuccess = true;
		} catch (PreRegLoginException ex) {
			log.debug("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id", "In calluserIdOtp method of login service- " + ex.getMessage());
			new LoginExceptionCatcher().handle(ex, "userIdOtp", response);
		} catch (RuntimeException ex) {
			log.debug("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id", "In calluserIdOtp method of login service- " + ex.getMessage());
			new LoginExceptionCatcher().handle(ex, "userIdOtp", response);
		} finally {
			response.setResponsetime(GenericUtil.getCurrentResponseTime());

			if (isSuccess) {
				setAuditValues(EventId.PRE_410.toString(), EventName.AUTHENTICATION.toString(),
						EventType.BUSINESS.toString(), "User sucessfully logged-in", AuditLogVariables.NO_ID.toString(),
						userid, userid);
			} else {
				ExceptionJSONInfoDTO errors = new ExceptionJSONInfoDTO(PreRegLoginConstant.VALIDATE_ERROR_CODE,
						PreRegLoginConstant.VALIDATE_ERROR_MESSAGE);
				List<ExceptionJSONInfoDTO> lst = new ArrayList();
				lst.add(errors);
				response.setErrors(lst);
				response.setResponse(null);
				setAuditValues(EventId.PRE_405.toString(), EventName.EXCEPTION.toString(), EventType.SYSTEM.toString(),
						"User failed to logged-in", AuditLogVariables.NO_ID.toString(), userid, userid);
			}

		}
		return response;
	}

	/**
	 * This method will invalidate the access token
	 * 
	 * @param authHeader
	 * @return AuthNResponse
	 */

	public MainResponseDTO<AuthNResponse> invalidateToken(String authHeader) {
		log.info("sessionId", "idType", "id", "In calluserIdOtp method of login service ");
		ResponseEntity<String> responseEntity = null;
		AuthNResponse authNResponse = null;
		MainResponseDTO<AuthNResponse> response = new MainResponseDTO<>();
		response.setId(invalidateTokenId);
		response.setVersion(version);
		boolean isSuccess = false;
		String userId = null;
		try {

			Map<String, String> headersMap = new HashMap<>();
			if (authHeader != null) {
				headersMap.put("Cookie", authHeader);
			}

			String url = sendOtpResourceUrl + "/logout/user";
			userId = loginCommonUtil.getUserDetailsFromToken(headersMap);
			responseEntity = (ResponseEntity<String>) loginCommonUtil.callAuthService(url, HttpMethod.DELETE,
					MediaType.APPLICATION_JSON, null, headersMap, String.class);
			log.info("sessionId", "idType", "id", "Kernel response: \n" + responseEntity.getBody());
			List<ServiceError> validationErrorList = null;
			validationErrorList = ExceptionUtils.getServiceErrorList(responseEntity.getBody());
			if (!validationErrorList.isEmpty()) {
				throw new LoginServiceException(validationErrorList, response);
			}
			ResponseWrapper<?> responseKernel = loginCommonUtil.requestBodyExchange(responseEntity.getBody());
			authNResponse = (AuthNResponse) loginCommonUtil.requestBodyExchangeObject(
					loginCommonUtil.responseToString(responseKernel.getResponse()), AuthNResponse.class);
			response.setResponse(authNResponse);
			isSuccess = true;
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id",
					"In call invalidateToken method of login service- " + ex.getMessage());
			new LoginExceptionCatcher().handle(ex, "invalidateToken", response);
		} finally {
			response.setResponsetime(GenericUtil.getCurrentResponseTime());
			if (isSuccess) {
				setAuditValues(EventId.PRE_410.toString(), EventName.AUTHENTICATION.toString(),
						EventType.BUSINESS.toString(), "User sucessfully logged-out",
						AuditLogVariables.NO_ID.toString(), userId, userId);
			} else {
				setAuditValues(EventId.PRE_405.toString(), EventName.EXCEPTION.toString(), EventType.SYSTEM.toString(),
						"User failed to logged-out", AuditLogVariables.NO_ID.toString(), userId, userId);
			}

		}
		return response;
	}

	/**
	 * This method is used to audit all the Authentication events
	 * 
	 * @param eventId
	 * @param eventName
	 * @param eventType
	 * @param description
	 * @param idType
	 * @param userName
	 */
	public void setAuditValues(String eventId, String eventName, String eventType, String description, String idType,
			String userId, String userName) {
		try {
			String tokenUrl = sendOtpResourceUrl + "/authenticate/clientidsecretkey";
			ClientSecretDTO clientSecretDto = new ClientSecretDTO(clientId, secretKey, appId);
			RequestWrapper<ClientSecretDTO> requestKernel = new RequestWrapper<>();
			requestKernel.setRequest(clientSecretDto);
			requestKernel.setRequesttime(LocalDateTime.now());
			ResponseEntity<ResponseWrapper<AuthNResponse>> response = (ResponseEntity<ResponseWrapper<AuthNResponse>>) loginCommonUtil
					.callAuthService(tokenUrl, HttpMethod.POST, MediaType.APPLICATION_JSON, requestKernel, null,
							ResponseWrapper.class);
			if (!(response.getBody().getErrors() == null || response.getBody().getErrors().isEmpty())) {
				throw new LoginServiceException(response.getBody().getErrors(), null);
			}
			String token = response.getHeaders().get("Set-Cookie").get(0);
			AuditRequestDto auditRequestDto = new AuditRequestDto();
			auditRequestDto.setEventId(eventId);
			auditRequestDto.setEventName(eventName);
			auditRequestDto.setEventType(eventType);
			auditRequestDto.setDescription(description);
			auditRequestDto.setId(idType);
			auditRequestDto.setSessionUserId(userId);
			auditRequestDto.setSessionUserName(userName);
			auditRequestDto.setModuleId(AuditLogVariables.AUTHENTICATION.toString());
			auditRequestDto.setModuleName(AuditLogVariables.AUTHENTICATION_SERVICE.toString());
			auditLogUtil.saveAuditDetails(auditRequestDto, token);
		} catch (LoginServiceException ex) {
			log.debug("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id",
					"In setAuditvalue of login service:" + StringUtils.join(ex.getValidationErrorList(), ","));
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id", "In setAuditvalue of login service:" + ex.getMessage());
		}
	}

	/**
	 * This will return UI related configurations
	 * 
	 * @return response
	 */
	public MainResponseDTO<Map<String, String>> getConfig() {
		log.info("sessionId", "idType", "id", "In login service of getConfig ");
		MainResponseDTO<Map<String, String>> res = new MainResponseDTO<>();
		res.setId(configId);
		res.setVersion(version);
		List<String> reqParams = new ArrayList<>();
		Map<String, String> configParams = new HashMap<>();
		try {
			String[] uiParams = uiConfigParams.split(",");
			for (int i = 0; i < uiParams.length; i++) {
				reqParams.add(uiParams[i]);
			}
			if (globalFileName != null && preRegFileName != null) {

				Properties prop1 = loginCommonUtil.parsePropertiesString(globalConfig);
				Properties prop2 = loginCommonUtil.parsePropertiesString(preregConfig);
				loginCommonUtil.getConfigParams(prop1, configParams, reqParams);
				loginCommonUtil.getConfigParams(prop2, configParams, reqParams);

			} else {
				throw new ConfigFileNotFoundException(LoginErrorCodes.PRG_AUTH_012.getCode(),
						LoginErrorMessages.CONFIG_FILE_NOT_FOUND_EXCEPTION.getMessage(), res);
			}

		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id", "In login service of getConfig " + ex.getMessage());
			new LoginExceptionCatcher().handle(ex, "config", res);
		}
		res.setResponse(configParams);
		res.setResponsetime(GenericUtil.getCurrentResponseTime());
		return res;
	}

	/**
	 * This will refresh UI related configurations
	 * 
	 * @return response
	 */
	public MainResponseDTO<String> refreshConfig() {
		log.info("sessionId", "idType", "id", "In login service of refreshConfig ");
		MainResponseDTO<String> res = new MainResponseDTO<>();
		res.setId(configId);
		res.setVersion(version);

		try {
			globalConfig = loginCommonUtil.getConfig(globalFileName);
			preregConfig = loginCommonUtil.getConfig(preRegFileName);
		} catch (HttpServerErrorException | HttpClientErrorException ex) {
			log.debug("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id", "In login service of refreshConfig " + ex.getMessage());
			new LoginExceptionCatcher().handle(ex, "refreshConfig", res);
		}
		res.setResponse("success");
		res.setResponsetime(GenericUtil.getCurrentResponseTime());
		return res;
	}

	public String generateJWTToken(String userId, String issuerUrl) {
		log.info("sessionId", "idType", "id", "In generateJWTToken method of loginservice:" + userId + issuerUrl);
		Map<String, Object> claims = new HashMap<String, Object>();
		claims.put("userId", userId);
		claims.put("scope", PreRegLoginConstant.JWT_SCOPE);
		claims.put("user_name", userId);
		claims.put("roles", jwtTokenRoles);

		String jws = Jwts.builder().setClaims(claims).setIssuer(issuerUrl).setIssuedAt(Date.from(Instant.now()))
				.setSubject(userId)
				.setExpiration(Date.from(Instant.now().plusSeconds(Integer.parseInt(jwtTokenExpiryTime))))
				.setAudience(PreRegLoginConstant.JWT_AUDIENCE)
				.signWith(SignatureAlgorithm.HS256, TextCodec.BASE64.decode(jwtSecret)).compact();
		log.info("sessionId", "idType", "id", "Auth token generarted:" + jws);
		return jws;
	}
}
