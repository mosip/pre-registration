package io.mosip.preregistration.application.test.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.preregistration.application.controller.GenerateQRcodeController;
import io.mosip.preregistration.application.dto.QRCodeResponseDTO;
import io.mosip.preregistration.application.service.GenerateQRcodeService;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.dto.NotificationDTO;
import io.mosip.preregistration.core.util.RequestValidator;
import io.mosip.preregistration.core.util.ValidationUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(GenerateQRcodeController.class)
@Import(GenerateQRcodeController.class)
public class GenerateQRcodeControllerTest {

	/**
	 * Autowired reference for {@link #MockMvc}
	 */
	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	private ObjectMapper mapper;

	/**
	 * /** Creating Mock Bean for NotificationService
	 */
	@MockBean
	private GenerateQRcodeService service;

	@MockBean
	private ValidationUtil serviceUtil;

	@MockBean
	private RequestValidator requestValidator;

	@Mock
	private GenerateQRcodeController generateQRcodeController;
	
	@Value("${mosip.pre-registration.qrcode.generate.id}")
	private String generateId;
	
	MainResponseDTO<QRCodeResponseDTO> responseDTO = new MainResponseDTO<QRCodeResponseDTO>();
	MainRequestDTO<String> requestdata = new MainRequestDTO<>();

	@Before
	public void setUp() {

		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		Mockito.when(requestValidator.supports(Mockito.any())).thenReturn(true);
		
		requestdata.setRequest("123456789");
		requestdata.setId(generateId);
		requestdata.setVersion("1.0");
		requestdata.setRequesttime(new Date());
		
		QRCodeResponseDTO qrCodeResponseDTO = new QRCodeResponseDTO();
		qrCodeResponseDTO.setQrcode("123456789".getBytes());
		responseDTO.setResponse(qrCodeResponseDTO);
		responseDTO.setResponsetime(serviceUtil.getCurrentResponseTime());

	}

	/**
	 * This test method is for success qrCodeGeneration
	 * 
	 * @throws Exception
	 */
	@WithMockUser(username = "individual", authorities = { "INDIVIDUAL", "REGISTRATION_OFFICER" })
	@Test
	public void qrCodeGenerationTest() throws Exception {



		//MainResponseDTO<QRCodeResponseDTO> response = new MainResponseDTO<>();

		String stringjson = mapper.writeValueAsString(requestdata);

		Mockito.when(service.generateQRCode(requestdata)).thenReturn(responseDTO);

		mockMvc.perform(post("/qrCode/generate").contentType(MediaType.APPLICATION_JSON).content(stringjson))
				.andExpect(status().isOk());

	}
}
