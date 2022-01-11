package io.mosip.preregistration.application.service;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;

import io.mosip.preregistration.application.util.ProxyMasterdataServiceUtil;

@RunWith(JUnit4.class)
@SpringBootTest
@ContextConfiguration(classes = { ProxyMasterDataService.class })
public class ProxyMasterDataServiceTest {

	@InjectMocks
	ProxyMasterDataService proxyMasterDataService;

	@Mock
	private ProxyMasterdataServiceUtil util;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getMasterDataResponseTest() {
		String body = "temp";
		MockHttpServletRequest request = new MockHttpServletRequest();
		Mockito.when(util.masterDataRestCall(util.getUrl(request), body,
				util.getHttpMethodType(request))).thenReturn("temp");
		assertNotNull(proxyMasterDataService.getMasterDataResponse(body, request));
	}

}
