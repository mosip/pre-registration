package io.mosip.preregistration.core.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.preregistration.core.util.AuthTokenUtil;

public class DataSyncRestInterceptor implements ClientHttpRequestInterceptor {

	@Lazy
	@Autowired
	private AuthTokenUtil tokenUtil;

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		addHeadersToRequest(request, body);
		return execution.execute(request, body);
	}

	private void addHeadersToRequest(HttpRequest httpRequest, byte[] bytes) {
		HttpHeaders headers = httpRequest.getHeaders();
		if (httpRequest.getURI().toString().contains("preregistration")) {
			AuthUserDetails authUserDetails = getAuthUserDetails();
			if (authUserDetails != null)
				headers.set(HttpHeaders.COOKIE, "Authorization=" + authUserDetails.getToken());
		} else {
			String token = tokenUtil.getToken();
			headers.set(HttpHeaders.COOKIE, token);
		}

	}

	private AuthUserDetails getAuthUserDetails() {
		AuthUserDetails authUserDetails = null;
		if (SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication() != null
				&& SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof AuthUserDetails)

			authUserDetails = (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return authUserDetails;
	}

}
