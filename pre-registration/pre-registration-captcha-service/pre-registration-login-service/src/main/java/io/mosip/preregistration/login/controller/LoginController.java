package io.mosip.preregistration.login.controller;

import java.net.HttpCookie;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.core.common.dto.AuthNResponse;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.dto.ResponseWrapper;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.util.DataValidationUtil;
import io.mosip.preregistration.core.util.RequestValidator;
import io.mosip.preregistration.login.dto.OtpRequestDTO;
import io.mosip.preregistration.login.dto.User;
import io.mosip.preregistration.login.service.LoginService;
import io.mosip.preregistration.login.util.LoginCommonUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

/**
 * This class provides different api to perform operation for login 
 * 
 * @author Akshay Jain
 * @since 1.0.0
 *
 */
@RestController
@Api(tags = "PreAuth")
public class LoginController {

	/** Autowired reference for {@link #authService}. */
	@Autowired
	private LoginService loginService;
	@Autowired
	private LoginCommonUtil loginCommonUtil;
	
	@Autowired
	private RequestValidator loginValidator;
	
	/** The Constant SENDOTP. */
	private static final String SENDOTP = "sendotp";
	
	
	/** The Constant VALIDATEOTP. */
	private static final String VALIDATEOTP = "validateotp";
	
	/**
	 * Inits the binder.
	 *
	 * @param binder the binder
	 */
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.addValidators(loginValidator);
	}
	
	
	private Logger log = LoggerConfiguration.logConfig(LoginController.class);
	
	/**
	 * This Post api use to send otp to the user by email or sms
	 * @param userOtpRequest
	 * @param errors
	 * @return AuthNResponse
	 */
	@PostMapping(value = "/sendOtp",produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Send Otp to UserId")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<MainResponseDTO<AuthNResponse>> sendOTP(@Validated @RequestBody MainRequestDTO<OtpRequestDTO> userOtpRequest, @ApiIgnore Errors errors ){
		log.info("sessionId", "idType", "id",
				"In sendOtp method of Login controller for sending Otp ");
		loginValidator.validateId(SENDOTP, userOtpRequest.getId(), errors);
		DataValidationUtil.validate(errors,SENDOTP);
		return ResponseEntity.status(HttpStatus.OK).body(loginService.sendOTP(userOtpRequest));
		}
	
	/**
	 * This Post api use to validate userid and otp
	 * @param userIdOtpRequest
	 * @param errors
	 * @return AuthNResponse
	 */
	@PostMapping(value="/validateOtp",produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Validate UserId and Otp")
	@ResponseStatus(value=HttpStatus.OK)
	public ResponseEntity<MainResponseDTO<AuthNResponse>> validateWithUserIdOtp(@Validated @RequestBody MainRequestDTO<User> userIdOtpRequest,@ApiIgnore Errors errors,HttpServletResponse res){
		log.info("sessionId", "idType", "id",
				"In validateWithUserIdotp method of Login controller for validating user and Otp and providing the access token ");
		loginValidator.validateId(VALIDATEOTP, userIdOtpRequest.getId(), errors);
		DataValidationUtil.validate(errors,VALIDATEOTP);
		MainResponseDTO<ResponseEntity<String>> serviceResponse=loginService.validateWithUserIdOtp(userIdOtpRequest);
		MainResponseDTO<AuthNResponse> responseBody=new MainResponseDTO<>();
		responseBody.setId(serviceResponse.getId());
		responseBody.setResponsetime(serviceResponse.getResponsetime());
		responseBody.setVersion(serviceResponse.getVersion());
		ResponseEntity<String> response=serviceResponse.getResponse();
		ResponseWrapper<?> responseWrapped=loginCommonUtil.requestBodyExchange(response.getBody());
		responseBody.setResponse((AuthNResponse) loginCommonUtil.requestBodyExchangeObject(loginCommonUtil.responseToString(responseWrapped.getResponse()),AuthNResponse.class));
		HttpHeaders headers=response.getHeaders();
		String content=headers.get("Set-Cookie").get(0);
		List<HttpCookie> httpCookies=HttpCookie.parse(content);
		httpCookies.stream().forEach(httpCookie->res.addCookie(createCookie(httpCookie)));
		return ResponseEntity.status(HttpStatus.OK).body(responseBody);
	}
	
	/**
	 * This Post api use to invalidate the token for logout.
	 * @param req
	 * @return AuthNResponse
	 */
	@PostMapping(value="/invalidateToken",produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Invalidate the token")
	@ResponseStatus(value=HttpStatus.OK)
	public ResponseEntity<MainResponseDTO<AuthNResponse>> invalidateToken(HttpServletRequest req){
		log.info("sessionId", "idType", "id",
				"In invalidateToken method of Login controller for invalidating access token ");
		String authHeader=req.getHeader("Cookie");
		return ResponseEntity.status(HttpStatus.OK).body(loginService.invalidateToken(authHeader));
		
	}
	/**
	 * This method is used to create a cookie
	 * @param cookie
	 * @return cookie
	 */
	private Cookie createCookie(final HttpCookie cookie) {
        final Cookie responseCookie = new Cookie(cookie.getName(),cookie.getValue());
        responseCookie.setMaxAge((int) cookie.getMaxAge());
        responseCookie.setHttpOnly(cookie.isHttpOnly());
        responseCookie.setSecure(cookie.getSecure());
        responseCookie.setPath(cookie.getPath());
        return responseCookie;
  }
	
	/**
	 * This get api is use to load the configuration data
	 *  
	 * @return the response entity
	 */
	@GetMapping(path="/config" ,produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get global and Pre-Registration config data")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "global and Pre-Registration config data successfully retrieved") })
	public ResponseEntity<MainResponseDTO<Map<String,String>>> configParams() {
		log.info("sessionId", "idType", "id",
				"In Login controller for getting config values ");
		return  new ResponseEntity<>( loginService.getConfig(),HttpStatus.OK);
		
	}
	
	
	/**
	 * This get api is use to refresh the configuration  data
	 * 
	 * @return the response entity
	 */
	@GetMapping(path="/refreshconfig" ,produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Refresh global and Pre-Registration config data")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "global and Pre-Registration config data successfully updated") })
	public ResponseEntity<MainResponseDTO<String>> refreshConfigParams() {
		log.info("sessionId", "idType", "id",
				"In Login controller for updating config values ");
		return  new ResponseEntity<>( loginService.refreshConfig(),HttpStatus.OK);
		
	}


}
