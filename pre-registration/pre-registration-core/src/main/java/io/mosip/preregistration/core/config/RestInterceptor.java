package io.mosip.preregistration.core.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.preregistration.core.util.AuthTokenUtil;

public class RestInterceptor implements ClientHttpRequestInterceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(RestInterceptor.class);

	@Autowired
	private ClientHttpRequestFactory requestFactory;

	@Autowired(required = false)
	private LoadBalancerClient loadBalancerClient;
	@Lazy
	@Autowired
	private AuthTokenUtil tokenUtil;

	@Override
	public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes,
			ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
		LOGGER.info("Request url: " + httpRequest.getURI());
		if (!httpRequest.getURI().toString().contains("authmanager")) {
			if (httpRequest.getURI().toString().contains("preregistration") && !httpRequest.getURI().toString().contains("sync")) {
				HttpHeaders headers = httpRequest.getHeaders();
				LOGGER.info("Reterving prereg token: ");
				String token = getAuthUserDetails().getToken();
				LOGGER.info("Reterived prereg token: "+token);
				headers.set(HttpHeaders.COOKIE, "Authorization=" + token);
			} else {
				HttpHeaders headers = httpRequest.getHeaders();
				LOGGER.info("Reterving token from keycloak : ");
				String token = tokenUtil.getToken();
				LOGGER.info("Reterived keycloak token: "+token);
				headers.set(HttpHeaders.COOKIE, token);
			}

			LOGGER.info("Header set with cookie for request url: " + httpRequest.getURI());
		}
		httpRequest = resolveServiceId(httpRequest);
		ClientHttpResponse response = clientHttpRequestExecution.execute(httpRequest, bytes);
		return response;
	}

	private AuthUserDetails getAuthUserDetails() {
		AuthUserDetails authUserDetails = null;
		if (SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication() != null
				&& SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof AuthUserDetails)

			authUserDetails = (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return authUserDetails;
	}

	private HttpRequest resolveServiceId(HttpRequest request) {
		try {
			if (loadBalancerClient != null) {
				LOGGER.info("Injected load balancer : {} ", loadBalancerClient.toString());
				ServiceInstance instance = loadBalancerClient.choose(request.getURI().getHost());
				if (instance != null) {
					final ClientHttpRequest newRequest = requestFactory.createRequest(
							loadBalancerClient.reconstructURI(instance, request.getURI()), request.getMethod());
					newRequest.getHeaders().addAll(request.getHeaders());
					return newRequest;
				}
			}
		} catch (Exception ex) {
			LOGGER.warn("Failed to choose service instance : " + ex.getMessage());
			LOGGER.debug("Failed to choose service instance", ex);
		}
		return request;
	}

}
