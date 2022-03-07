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
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.application.constant.PreRegLoginConstant;
import io.mosip.preregistration.application.dto.CaptchaResposneDTO;
import io.mosip.preregistration.application.dto.OTPRequestWithLangCodeAndCaptchaToken;
import io.mosip.preregistration.application.dto.OtpRequestDTO;
import io.mosip.preregistration.application.dto.User;
import io.mosip.preregistration.application.errorcodes.LoginErrorCodes;
import io.mosip.preregistration.application.errorcodes.LoginErrorMessages;
import io.mosip.preregistration.application.exception.InvalidOtpOrUseridException;
import io.mosip.preregistration.application.exception.LoginServiceException;
import io.mosip.preregistration.application.exception.PreRegLoginException;
import io.mosip.preregistration.application.exception.util.LoginExceptionCatcher;
import io.mosip.preregistration.application.util.LoginCommonUtil;
import io.mosip.preregistration.core.code.AuditLogVariables;
import io.mosip.preregistration.core.code.EventId;
import io.mosip.preregistration.core.code.EventName;
import io.mosip.preregistration.core.code.EventType;
import io.mosip.preregistration.core.common.dto.AuditRequestDto;
import io.mosip.preregistration.core.common.dto.AuthNResponse;
import io.mosip.preregistration.core.common.dto.ExceptionJSONInfoDTO;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.util.AuditLogUtil;
import io.mosip.preregistration.core.util.GenericUtil;

@Service
public class LoginService {

	private Logger log = LoggerConfiguration.logConfig(LoginService.class);

	/**
	 * Autowired reference for {@link #authCommonUtil}
	 */
	@Autowired
	private LoginCommonUtil loginCommonUtil;


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

	@Value("${context}")
	private String context;

	@Autowired
	AuditLogUtil auditLogUtil;

	@Value("${prereg.auth.jwt.secret}")
	private String jwtSecret;

	@Value("${prereg.auth.jwt.token.expiration}")
	private String jwtTokenExpiryTime;

	@Value("${prereg.auth.jwt.token.roles}")
	private String jwtTokenRoles;

	@Value("${prereg.auth.jwt.scope}")
	private String jwtScope;

	@Value("${prereg.auth.jwt.audience}")
	private String jwtAudience;

	@Value("${mosip.kernel.otp.expiry-time}")
	private int otpExpiryTime;

	@Value("${mosip.preregistration.captcha.enable}")
	private boolean isCaptchaEnabled;

	@Autowired
	OTPManager otpmanager;

	@Autowired
    private Environment env;
	/**
	 * It will fetch otp from Kernel auth service and send to the userId provided
	 * 
	 * @param userOtpRequest
	 * @return MainResponseDTO<AuthNResponse>
	 */
	@SuppressWarnings("unchecked")
	public MainResponseDTO<AuthNResponse> sendOTP(MainRequestDTO<OtpRequestDTO> userOtpRequest, String language) {
		MainResponseDTO<AuthNResponse> response = null;
		String userid = null;
		boolean isSuccess = false;

		log.info("In callsendOtp method of login service  with userID: {} and langCode",
				userOtpRequest.getRequest().getUserId(), language);

		try {
			response = (MainResponseDTO<AuthNResponse>) loginCommonUtil.getMainResponseDto(userOtpRequest);
			log.info("Response after loginCommonUtil {}", response);

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
			log.error("In callsendOtp method of login service- ", ex.getResponseBodyAsString());
			new LoginExceptionCatcher().handle(ex, "sendOtp", response);
		} catch (Exception ex) {
			log.error("In callsendOtp method of login service- ", ex);
			new LoginExceptionCatcher().handle(ex, "sendOtp", response);
		} finally {
			if (isSuccess) {
				setAuditValues(EventId.PRE_410.toString(), EventName.AUTHENTICATION.toString(),
						EventType.BUSINESS.toString(), "Otp send sucessfully", AuditLogVariables.NO_ID.toString(),
						userid, userid);
			} else {

				ExceptionJSONInfoDTO errors = new ExceptionJSONInfoDTO(LoginErrorCodes.PRG_AUTH_001.getCode(),
						LoginErrorMessages.SEND_OTP_FAILED.getMessage());
				List<ExceptionJSONInfoDTO> lst = new ArrayList<>();
				lst.add(errors);
				response.setErrors(lst);
				response.setResponse(null);
				setAuditValues(EventId.PRE_405.toString(), EventName.EXCEPTION.toString(), EventType.SYSTEM.toString(),
						"Otp fail to send", AuditLogVariables.NO_ID.toString(), userid, userid);
			}
		}
		return response;
	}

	@SuppressWarnings({ "unchecked" })
	public MainResponseDTO<AuthNResponse> validateCaptchaAndSendOtp(
			MainRequestDTO<OTPRequestWithLangCodeAndCaptchaToken> request) {

		log.info("In validateCaptchaAndSendOtp method with userId and langCode{}", request.getRequest().getUserId(),
				request.getRequest().getUserId());
		MainResponseDTO<AuthNResponse> response = (MainResponseDTO<AuthNResponse>) loginCommonUtil
				.getMainResponseDto(request);

		String captchaToken = request.getRequest().getCaptchaToken();
		String langCode = request.getRequest().getLangCode();
		String userId = request.getRequest().getUserId();

		OtpRequestDTO otpRequest = new OtpRequestDTO();
		otpRequest.setUserId(userId);
		MainRequestDTO<OtpRequestDTO> userOtpRequest = new MainRequestDTO<OtpRequestDTO>();
		userOtpRequest.setRequest(otpRequest);
		CaptchaResposneDTO captchaResponse = null;
		AuthNResponse authRes = new AuthNResponse();
		try {
			if (isCaptchaEnabled) {
				captchaResponse = this.loginCommonUtil.validateCaptchaToken(captchaToken);
				authRes.setMessage(captchaResponse.getMessage().concat(" and "));
			}

			MainResponseDTO<AuthNResponse> sendOtpResponse = this.sendOTP(userOtpRequest, langCode);

			if (sendOtpResponse.getErrors() != null) {
				throw new PreRegLoginException(sendOtpResponse.getErrors().get(0).getErrorCode(),
						sendOtpResponse.getErrors().get(0).getMessage());
			}

			if (Objects.isNull(authRes.getMessage()))
				authRes.setMessage(sendOtpResponse.getResponse().getMessage());
			else
				authRes.setMessage(authRes.getMessage().concat(sendOtpResponse.getResponse().getMessage()));
			authRes.setStatus(PreRegLoginConstant.SUCCESS);
			response.setResponse(authRes);

		} catch (PreRegLoginException ex) {

			log.error("In validateCaptchaAndSendOtp method of login service- ", ex);
			new LoginExceptionCatcher().handle(ex, "sendOtp", response);
		} catch (Exception ex) {
			log.error("In validateCaptchaAndSendOtp method of login service- ", ex);
			new LoginExceptionCatcher().handle(ex, "sendOtp", response);
		}
		response.setResponsetime(LocalDateTime.now().toString());
		return response;
	}

	/**
	 * It will validate userId & otp and provide with a access token
	 * 
	 * @param userIdOtpRequest
	 * @return MainResponseDTO<AuthNResponse>
	 */
	@SuppressWarnings("unchecked")
	public MainResponseDTO<AuthNResponse> validateWithUserIdOtp(MainRequestDTO<User> userIdOtpRequest) {
		log.info("In calluserIdOtp method of login service ");
		MainResponseDTO<AuthNResponse> response = null;
		response = (MainResponseDTO<AuthNResponse>) loginCommonUtil.getMainResponseDto(userIdOtpRequest);
		String userid = null;
		boolean isSuccess = false;

		try {
			User user = userIdOtpRequest.getRequest();
			userid = user.getUserId().toString();

			loginCommonUtil.validateOtpAndUserid(user);
			boolean validated = otpmanager.validateOtp(user.getOtp(), user.getUserId());
			AuthNResponse authresponse = new AuthNResponse();
			if (validated) {
				authresponse.setMessage(PreRegLoginConstant.VALIDATION_SUCCESS);
				authresponse.setStatus(PreRegLoginConstant.SUCCESS);

			} else {
				throw new InvalidOtpOrUseridException(LoginErrorCodes.PRG_AUTH_013.getCode(),
						PreRegLoginConstant.VALIDATION_UNSUCCESS, response);

			}
			response.setResponse(authresponse);
			isSuccess = true;
		} catch (PreRegLoginException ex) {
			log.error("In calluserIdOtp method of login service- ", ex);
			new LoginExceptionCatcher().handle(ex, "userIdOtp", response);
		} catch (RuntimeException ex) {
			log.error("In calluserIdOtp method of login service- ", ex);
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
				List<ExceptionJSONInfoDTO> lst = new ArrayList<>();
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
	public MainResponseDTO<String> invalidateToken(String token) {
		log.info("In calluserIdOtp method of login service ");
		MainResponseDTO<String> response = new MainResponseDTO<>();
		response.setId(invalidateTokenId);
		response.setVersion(version);
		String userId = null;
		boolean isSuccess = false;
		try {
			byte[] secret = TextCodec.BASE64.decode(jwtSecret);
			String jwtToken = token.replace("Authorization=", "").split(";")[0];

			log.info("Token to be reset {}", jwtToken);
			Jws<Claims> clamis = Jwts.parser().setSigningKey(secret).parseClaimsJws(jwtToken);
			userId = clamis.getBody().get("userId").toString();
			response.setResponse("Loggedout successfully");
			isSuccess = true;
		} catch (JwtException e) {
			log.error("Failed logout:", e);
			MainResponseDTO<String> res = new MainResponseDTO<String>();
			res.setResponse("Failed to invalidate the auth token");
			new LoginExceptionCatcher().handle(e, null, res);
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error("In call invalidateToken method of login service- ", ex);
			new LoginExceptionCatcher().handle(ex, "invalidateToken", response);
		} finally {
			System.out.println(response);
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
			auditLogUtil.saveAuditDetails(auditRequestDto);
		} catch (LoginServiceException ex) {
			log.error("In setAuditvalue of login service:", StringUtils.join(ex.getValidationErrorList(), ","));
		} catch (Exception ex) {
			log.error("In setAuditvalue of login service:", ex);
		}
	}

	/**
	 * This will return UI related configurations
	 * 
	 * @return response
	 */
	@Cacheable(value = "login-cache", key = "'configCache'")
	public MainResponseDTO<Map<String, String>> getConfig() {
		log.info("In login service of getConfig ");
		MainResponseDTO<Map<String, String>> res = new MainResponseDTO<>();
		res.setId(configId);
		res.setVersion(version);
		Map<String, String> responseParamsMap = new HashMap<>();
		try {
			loginCommonUtil.validateLanguageProperties(responseParamsMap);
			String[] uiParams = uiConfigParams.split(",");
			for (String uiParam: uiParams) {           
			    responseParamsMap.put(uiParam, env.getProperty(uiParam));
			}			
		} catch (Exception ex) {
			log.error("In login service of getConfig ", ex);
			new LoginExceptionCatcher().handle(ex, "config", res);
		}
		res.setResponse(responseParamsMap);
		res.setResponsetime(GenericUtil.getCurrentResponseTime());
		return res;
	}

	private String generateJWTToken(String userId, String issuerUrl, String jwtTokenExpiryTime) {
		log.info("In generateJWTToken method of loginservice:{} {}", userId, issuerUrl);
		Map<String, Object> claims = new HashMap<String, Object>();
		claims.put("userId", userId);
		claims.put("scope", jwtScope);
		claims.put("user_name", userId);
		claims.put("roles", jwtTokenRoles);

		String jws = null;
		if (jwtTokenExpiryTime != null) {
			jws = Jwts.builder().setClaims(claims).setIssuer(issuerUrl).setIssuedAt(Date.from(Instant.now()))
					.setSubject(userId)
					.setExpiration(Date.from(Instant.now().plusSeconds(Integer.parseInt(jwtTokenExpiryTime))))
					.setAudience(jwtAudience).signWith(SignatureAlgorithm.HS256, TextCodec.BASE64.decode(jwtSecret))
					.compact();
			log.info("Auth token generarted");
		} else {
			jws = Jwts.builder().setClaims(claims).setIssuer(issuerUrl).setIssuedAt(Date.from(Instant.now()))
					.setSubject(userId).setExpiration(Date.from(Instant.now().plusSeconds(Integer.parseInt("0"))))
					.setAudience(jwtAudience).signWith(SignatureAlgorithm.HS256, TextCodec.BASE64.decode(jwtSecret))
					.compact();
			log.info("Auth token generarted:");
		}

		return jws;
	}

	public String getLoginToken(String userId, String issuerUrl) {
		return this.generateJWTToken(userId, issuerUrl, jwtTokenExpiryTime);
	}

	public String getLogoutToken(String token) {
		byte[] secret = TextCodec.BASE64.decode(jwtSecret);
		String jwtToken = token.replace("Authorization=", "").split(";")[0];
		String userId = null;
		String issuer = null;
		try {
			Jws<Claims> clamis = Jwts.parser().setSigningKey(secret).parseClaimsJws(jwtToken);
			userId = clamis.getBody().get("userId").toString();
			issuer = clamis.getBody().getIssuer();
		} catch (JwtException e) {
			log.error("Failed to generate logout token:", e);
			MainResponseDTO<String> res = new MainResponseDTO<String>();
			res.setResponse("Failed to generate logout token");
			new LoginExceptionCatcher().handle(e, null, res);
		}
		return this.generateJWTToken(userId, issuer, null);
	}

	public String sendOTPSuccessJwtToken(String userId) {
		return this.loginCommonUtil.sendOtpJwtToken(userId);
	}
}
