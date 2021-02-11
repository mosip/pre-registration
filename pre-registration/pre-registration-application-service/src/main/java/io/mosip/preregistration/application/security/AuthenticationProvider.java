package io.mosip.preregistration.application.security;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.impl.TextCodec;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.authmanager.authadapter.model.MosipUserDto;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;

@Component
public class AuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationProvider.class);

	@Autowired
	private RestTemplate restTemplate;

	@Value("${prereg.auth.jwt.secret}")
	private String jwtSecret;

	@Value("${auth.server.admin.validate.url:http://localhost:8091/v1/authmanager/authorize/admin/validateToken}")
	private String adminValidateUrl;

	@Autowired
	private ObjectMapper objectMapper;

	@Override
	protected void additionalAuthenticationChecks(UserDetails userDetails,
			UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {
	}

	@Override
	protected UserDetails retrieveUser(String userName,
			UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {
		Object token = usernamePasswordAuthenticationToken.getCredentials();
		LOGGER.info("In retriveUser method of AuthenticationProvider class" + token);
		MosipUserDto mosipUserDto = new MosipUserDto();
		byte[] secret = TextCodec.BASE64.decode(jwtSecret);
		try {
			
			Jws<Claims> clamis = Jwts.parser().setSigningKey(secret).parseClaimsJws(token.toString());
			mosipUserDto.setUserId(clamis.getBody().get("userId").toString());
			mosipUserDto.setName(clamis.getBody().get("user_name").toString());
			mosipUserDto.setToken(token.toString());
			mosipUserDto.setRole(clamis.getBody().get("roles").toString());
			LOGGER.info("extracted token details" + mosipUserDto);

		} catch (SignatureException | IllegalArgumentException ex) {
			System.out.println(ex.getMessage());
			ResponseEntity<String> response = null;
			response = getKeycloakValidatedUserResponse(token.toString());
			System.out.println(response);
			List<ServiceError> validationErrorsList = ExceptionUtils.getServiceErrorList(response.getBody());
			if (!validationErrorsList.isEmpty()) {
				throw new AccessDeniedException(validationErrorsList.toString());
			}
			try {
				ResponseWrapper<?> responseObject = objectMapper.readValue(response.getBody(), ResponseWrapper.class);
				mosipUserDto = objectMapper.readValue(objectMapper.writeValueAsString(responseObject.getResponse()),
						MosipUserDto.class);
			} catch (Exception e) {
				throw new AccessDeniedException(String.valueOf(HttpStatus.UNAUTHORIZED.value()));
			}
		} catch (JwtException e) {
			LOGGER.error("exception while parsing the token", ExceptionUtils.getStackTrace(e));
			throw new UsernameNotFoundException("Cannot find user with authentication token=" + token);
		}

		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, token.toString());
		List<GrantedAuthority> grantedAuthorities = AuthorityUtils
				.commaSeparatedStringToAuthorityList(mosipUserDto.getRole());
		authUserDetails.setAuthorities(grantedAuthorities);
		return authUserDetails;
	}

	private ResponseEntity<String> getKeycloakValidatedUserResponse(String token) {
		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.COOKIE, "Authorization=" + token);
		HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
		ResponseEntity<String> response = null;
		try {
			LOGGER.info("validate token url" + adminValidateUrl);
			response = restTemplate.exchange(adminValidateUrl, HttpMethod.GET, entity, String.class);
		} catch (RestClientException e) {
			LOGGER.error("validate token exception", ExceptionUtils.getStackTrace(e));
			throw new AccessDeniedException(e.getMessage());
		}
		return response;
	}
}
