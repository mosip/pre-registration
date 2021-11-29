package io.mosip.preregistration.application.service;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.JsonNode;

import io.mosip.preregistration.application.dto.PageDTO;
import io.mosip.preregistration.application.dto.UISpecKeyValuePair;
import io.mosip.preregistration.application.dto.UISpecMetaDataDTO;
import io.mosip.preregistration.application.dto.UISpecResponseDTO;
import io.mosip.preregistration.application.exception.UISpecException;
import io.mosip.preregistration.application.service.util.UISpecServiceUtil;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;

@RunWith(JUnit4.class)
@SpringBootTest
@ContextConfiguration(classes = { UISpecService.class })
public class UISpecServiceTest {

	@InjectMocks
	private UISpecService uISpecService;

	@Value("${mosip.utc-datetime-pattern}")
	private String mosipDateTimeFormat;

	@Mock
	UISpecServiceUtil serviceUtil;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		ReflectionTestUtils.setField(uISpecService, "mosipDateTimeFormat", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");


	}

	@Test
	public void testSuccessGetAllUISchema() {
		PageDTO<UISpecResponseDTO> res=new PageDTO<UISpecResponseDTO>();
		res.setPageNo(1);
		res.setPageSize(1);
		Sort sort = null;
		res.setSort(sort);
		res.setTotalItems(1);
		res.setTotalPages(1);
		List<UISpecResponseDTO> data=new ArrayList<UISpecResponseDTO>();
		UISpecResponseDTO uISpecResponseDTO=new UISpecResponseDTO();
		uISpecResponseDTO.setTitle("abc");
		uISpecResponseDTO.setDomain("pre-registration");

		List<UISpecKeyValuePair> jsonSpec=new ArrayList<UISpecKeyValuePair>();
		JsonNode jsonNode=null;
		UISpecKeyValuePair e=new UISpecKeyValuePair("", jsonNode);
		jsonSpec.add(e);
		uISpecResponseDTO.setJsonSpec(jsonSpec);
		data.add(uISpecResponseDTO);

		res.setData(data);
		Mockito.when(serviceUtil.getAllUISchema(1,1)).thenReturn(res);
		MainResponseDTO<PageDTO<UISpecMetaDataDTO>> response = uISpecService.getAllUISpec(1, 1);
		assertEquals(response.getResponse().getPageNo(),1);

	}

	@Test
	public void testGetAllUISchemaException() {
		Mockito.when(serviceUtil.getAllUISchema(1,1)).thenThrow(new UISpecException("ErrorCode","exception"));
		MainResponseDTO<PageDTO<UISpecMetaDataDTO>> response = uISpecService.getAllUISpec(1, 1);
		assertEquals(response.getErrors().get(0).getMessage(),"ErrorCode --> exception");
	}

	@Test
	public void testgetLatestUISpecTest() {
		List<UISpecResponseDTO> uiSchemas = new ArrayList<UISpecResponseDTO>();
		UISpecResponseDTO uiSchema= new UISpecResponseDTO();
		uiSchema.setStatus("PUBLISHED");
		List<UISpecKeyValuePair> jsonSpec=new ArrayList<UISpecKeyValuePair>();
		JsonNode jsonNode=null;
		UISpecKeyValuePair e=new UISpecKeyValuePair("", jsonNode);
		jsonSpec.add(e);
		uiSchema.setJsonSpec(jsonSpec);
		uiSchemas.add(uiSchema);
		Mockito.when(serviceUtil.getUISchema(Mockito.any(), Mockito.any())).thenReturn(uiSchemas);
		MainResponseDTO<UISpecMetaDataDTO> response = uISpecService.getLatestUISpec(0, 0.0);
		assertEquals(response.getResponse().getStatus(),"PUBLISHED");

	}

	@Test
	public void testgetLatestUISpecExceptionTest() {
		List<UISpecResponseDTO> uiSchemas = new ArrayList<UISpecResponseDTO>();
		UISpecResponseDTO uiSchema= new UISpecResponseDTO();
		uiSchema.setStatus("PUBLISHED");
		List<UISpecKeyValuePair> jsonSpec=new ArrayList<UISpecKeyValuePair>();
		JsonNode jsonNode=null;
		UISpecKeyValuePair e=new UISpecKeyValuePair("", jsonNode);
		jsonSpec.add(e);
		uiSchema.setJsonSpec(jsonSpec);
		uiSchemas.add(uiSchema);
		Mockito.when(serviceUtil.getUISchema(Mockito.any(), Mockito.any())).thenThrow(new UISpecException("ErrorCode","exception"));
		MainResponseDTO<UISpecMetaDataDTO> response = uISpecService.getLatestUISpec(0, 0.0);
		assertEquals(response.getErrors().get(0).getMessage(),"ErrorCode --> exception");

	}
}
