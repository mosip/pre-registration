package io.mosip.preregistration.proxymasterdataservice.service.util.test;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponentsBuilder;

import io.mosip.preregistration.proxymasterdataservice.service.util.ProxyMasterdataServiceUtil;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ProxyMasterDataServiceUtilTest.class })
public class ProxyMasterDataServiceUtilTest {


	@Value("${mosip.base.url}")
	private String baseUrl;

	@Mock
	private ProxyMasterdataServiceUtil util;

	MockHttpServletRequest request = new MockHttpServletRequest();

	@Before
	public void setup() {

		request.setServerName(baseUrl);
		request.setContextPath("preregistration/v1/");
		request.setRequestURI("proxy/masterdata/gendertypes");
		request.setQueryString(null);
		request.setMethod("GET");
	}

	@Test
	public void getUrlTest() {
		String url = request.getServerName() + request.getContextPath() + request.getRequestURI();
		String httpUrl = url.replace(request.getContextPath() + "/proxy", "").strip().toString();
		URI uri = UriComponentsBuilder.fromHttpUrl(httpUrl).build().toUri();
		Mockito.when(util.getUrl(request)).thenReturn(uri);
	}

	@Test
	public void getHttpMethodTest() {
		HttpMethod method = null;
		if (request.getMethod() == "GET") {
			method = HttpMethod.GET;
		} else if (request.getMethod() == "POST") {
			method = HttpMethod.POST;
		}
		Mockito.when(util.getHttpMethodType(request)).thenReturn(method);
	}

}
