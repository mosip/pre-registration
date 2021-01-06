package io.mosip.preregistration.proxymasterdataservice.service.test;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import io.mosip.preregistration.proxymasterdataservice.service.util.ProxyMasterdataServiceUtil;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ProxyMasterDataServiceTest.class })
public class ProxyMasterDataServiceTest {

	@Mock
	private ProxyMasterdataServiceUtil util;

	@Mock
	RestTemplate restTemplate = new RestTemplate();

	@Value("${mosip.base.url}")
	private String baseUrl;

	MockHttpServletRequest request = new MockHttpServletRequest();

	ResponseEntity<String> responseEntity = Mockito.mock(ResponseEntity.class);

	@Before
	public void setup() {

		request.setServerName(baseUrl);
		request.setContextPath("preregistration/v1/");
		request.setRequestURI("proxy/masterdata/gendertypes");
		request.setQueryString(null);
		request.setMethod("GET");
	}

	@Test
	public void getMasterDataResponseTest() {
		URI uri = util.getUrl(request);
		HttpMethod method = util.getHttpMethodType(request);

		HttpHeaders header = new HttpHeaders();
		header.setContentType(MediaType.APPLICATION_JSON);
		header.set("Cookie", util.getAuthToken());

		HttpEntity<?> entity = new HttpEntity<>(header);

		Mockito.when(restTemplate.exchange(uri, method, entity, String.class)).thenReturn(responseEntity);

	}

}
