package io.mosip.preregistration.application.test.service.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import io.mosip.preregistration.application.dto.PageDTO;
import io.mosip.preregistration.application.dto.UISpecKeyValuePair;
import io.mosip.preregistration.application.dto.UISpecResponseDTO;
import io.mosip.preregistration.application.service.util.UISpecServiceUtil;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = UISpecServiceUtil.class)
public class UISpecUtilTest {

	@MockBean
	private UISpecServiceUtil util;

	@MockBean(name = "restTemplateConfig")
	private RestTemplate restTemplate;

	@Test
	public void getUISchema() {

		List<UISpecResponseDTO> res = new ArrayList<>();
		UISpecKeyValuePair spec = new UISpecKeyValuePair();
		spec.setSpec(null);
		List<UISpecKeyValuePair> uispec = new ArrayList<>();
		uispec.add(spec);
		UISpecResponseDTO uiSpecRes = new UISpecResponseDTO();
		uiSpecRes.setDomain("pre-registration");
		uiSpecRes.setIdentitySchemaId("10001");
		uiSpecRes.setDescription("UI SPEC");
		uiSpecRes.setJsonSpec(uispec);
		
		Mockito.when(util.getUISchema(0.0, 0.1)).thenReturn(res);

	}
	
	@Test
	public void getAllUISchema() {

		PageDTO<UISpecResponseDTO> res = new PageDTO<>();
		UISpecKeyValuePair spec = new UISpecKeyValuePair();
		spec.setSpec(null);
		List<UISpecKeyValuePair> uispec = new ArrayList<>();
		uispec.add(spec);
		UISpecResponseDTO uiSpecRes = new UISpecResponseDTO();
		uiSpecRes.setDomain("pre-registration");
		uiSpecRes.setIdentitySchemaId("10001");
		uiSpecRes.setDescription("UI SPEC");
		uiSpecRes.setJsonSpec(uispec);
		
		Mockito.when(util.getAllUISchema(0, 0)).thenReturn(res);

	}

}
