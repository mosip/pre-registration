package io.mosip.preregistration.datasync.config;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;

public class RestInterceptor implements ClientHttpRequestInterceptor {

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		addHeadersToRequest(request, body);
		return execution.execute(request, body);
	}

	// TODO need to add support for vertx context
	private void addHeadersToRequest(HttpRequest httpRequest, byte[] bytes) {
		HttpHeaders headers = httpRequest.getHeaders();
		AuthUserDetails authUserDetails = getAuthUserDetails();
		if (authUserDetails != null)
			headers.set(HttpHeaders.COOKIE, "Authorization=" + authUserDetails.getToken());
	}

	private AuthUserDetails getAuthUserDetails() {
		AuthUserDetails authUserDetails = null;
		if (SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication() != null
				&& SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof AuthUserDetails)

			authUserDetails = (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return authUserDetails;
	}

}
