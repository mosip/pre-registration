package io.mosip.preregistration.application.security;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.util.ContentCachingRequestWrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.preregistration.core.common.dto.ResponseWrapper;

public class AuthenticationFilter extends AbstractAuthenticationProcessingFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationFilter.class);

	AuthenticationFilter(final RequestMatcher requiresAuth) {
		super(requiresAuth);
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) throws AuthenticationException, IOException, ServletException {
		LOGGER.info(
				"In attemptAuthentication method of AuthenticationFilter class:" + httpServletRequest.getPathInfo());
		String token = null;
		Cookie[] cookies = null;
		try {
			cookies = httpServletRequest.getCookies();
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if (cookie.getName().contains("Authorization")) {
						System.out.println(cookie.getValue());
						token = cookie.getValue();
						LOGGER.info("Token from the cookie:" + token);
					}
				}
			}
		} catch (Exception e) {

			LOGGER.error("extract token from cookie failed for request " + httpServletRequest.getRequestURI());
			e.printStackTrace();
		}

		if (token == null) {
			ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
			ServiceError error = new ServiceError("KER-ATH-401", "Authentication Failed");
			errorResponse.getErrors().add(error);
			httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
			httpServletResponse.setContentType("application/json");
			httpServletResponse.setCharacterEncoding("UTF-8");
			httpServletResponse.getWriter().write(convertObjectToJson(errorResponse));
			LOGGER.error("\n\n Exception : Authorization token not present > " + httpServletRequest.getRequestURL()
					+ "\n\n");
			return null;
		}
		Authentication requestAuthentication = new UsernamePasswordAuthenticationToken(token, token);
		return getAuthenticationManager().authenticate(requestAuthentication);

	}

	@Override
	protected void successfulAuthentication(final HttpServletRequest request, final HttpServletResponse response,
			final FilterChain chain, final Authentication authResult) throws IOException, ServletException {
		SecurityContextHolder.getContext().setAuthentication(authResult);
		chain.doFilter(request, response);
	}

	private ResponseWrapper<ServiceError> setErrors(HttpServletRequest httpServletRequest) throws IOException {
		ResponseWrapper<ServiceError> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponsetime(LocalDateTime.now(ZoneId.of("UTC")));
		String requestBody = null;
		if (httpServletRequest instanceof ContentCachingRequestWrapper) {
			requestBody = new String(((ContentCachingRequestWrapper) httpServletRequest).getContentAsByteArray());
		}
		if (EmptyCheckUtils.isNullEmpty(requestBody)) {
			return responseWrapper;
		}
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		JsonNode reqNode = objectMapper.readTree(requestBody);
		responseWrapper.setId(reqNode.path("id").asText());
		responseWrapper.setVersion(reqNode.path("version").asText());
		return responseWrapper;
	}

	private String convertObjectToJson(Object object) throws JsonProcessingException {
		if (object == null) {
			return null;
		}
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		return mapper.writeValueAsString(object);
	}
}