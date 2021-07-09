package io.mosip.preregistration.core.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import io.mosip.preregistration.core.util.AuthTokenUtil;

public class DataSyncRestInterceptor implements ClientHttpRequestInterceptor {

	@Autowired
	@Lazy
	AuthTokenUtil tokenUtil;

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		addHeadersToRequest(request, body);
		return execution.execute(request, body);
	}

	private void addHeadersToRequest(HttpRequest httpRequest, byte[] bytes) {
		HttpHeaders headers = httpRequest.getHeaders();
		String token = tokenUtil.getToken();
		if (token != null)
			headers.set(HttpHeaders.COOKIE, "Authorization=" + token);
	}

}
