package io.mosip.preregistration.application.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * This class is a filter for giving Access Headers to solve CORS
 * 
 * @author Mindtree Ltd.
 *
 */
public class CorsFilter extends OncePerRequestFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(CorsFilter.class);

	private List<String> origins;

	public CorsFilter(String origins) {
		this.origins = Arrays.asList(origins.split("( )*,( )*"));
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String origin = request.getHeader("Origin");
		LOGGER.info("origins {} ", origins);
		if (origin == null || origin.isEmpty()) {
			LOGGER.info("origin  {}", origin);
			LOGGER.info("requesturl  {}", request.getRequestURL().toString());
		} else if (origins != null && !origins.isEmpty() && origins.contains(origin)) {
			response.setHeader("Access-Control-Allow-Origin", origin);
		}
		response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT, PATCH");
		response.setHeader("Access-Control-Allow-Headers",
				"Origin,Date, Content-Type, Accept, X-Requested-With, Authorization, From, X-Auth-Token, Request-Id");
		response.setHeader("Access-Control-Expose-Headers", "Set-Cookie");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		if (!"OPTIONS".equalsIgnoreCase(request.getMethod())) {
			filterChain.doFilter(request, response);
		}
	}

}