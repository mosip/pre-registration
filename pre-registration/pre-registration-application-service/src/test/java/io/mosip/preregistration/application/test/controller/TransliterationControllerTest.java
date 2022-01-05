package io.mosip.preregistration.application.test.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import io.mosip.preregistration.application.controller.TransliterationController;
import io.mosip.preregistration.application.dto.TransliterationResponseDTO;
import io.mosip.preregistration.application.service.TransliterationService;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.util.RequestValidator;

/**
 * 
 * Test class to test the pre-registration transliteration Controller methods
 * 
 * @author Kishan rathore
 * @since 1.0.0
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(controllers = TransliterationController.class)
@Import(TransliterationController.class)
@WithMockUser(username = "individual", authorities = { "INDIVIDUAL", "REGISTRATION_OFFICER" })
public class TransliterationControllerTest {

	/**
	 * Autowired reference for {@link #MockMvc}
	 */
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@MockBean
	private RequestValidator requestValidator;

	/**
	 * Creating Mock Bean for transliteration Service
	 */
	@MockBean
	private TransliterationService serviceImpl;

	/**
	 * @throws FileNotFoundException when file not found
	 * @throws IOException           on input error
	 * @throws ParseException        on json parsing error
	 */
	@Before
	public void setup() throws FileNotFoundException, IOException, ParseException {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		Mockito.when(requestValidator.supports(Mockito.any())).thenReturn(true);

	}

	/**
	 * @throws Exception on error
	 */
	@Test
	public void successTest() throws Exception {

		MainResponseDTO<TransliterationResponseDTO> response = new MainResponseDTO<>();
		TransliterationResponseDTO dto = new TransliterationResponseDTO();
		response.setResponse(dto);

		Mockito.when(serviceImpl.translitratorService(Mockito.any())).thenReturn(response);

		RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/transliteration/transliterate")
				.contentType(MediaType.APPLICATION_JSON_VALUE).characterEncoding("UTF-8")
				.accept(MediaType.APPLICATION_JSON_VALUE).content(
						"{\"id\":\"mosip.pre-registration.transliteration.transliterate\",\"request\":{\"from_field_lang\":\"eng\",\"from_field_value\":\"Ajay\",\"to_field_lang\":\"ara\"},\"version\":\"1.0\",\"requesttime\":\"2021-08-20T11:47:43.190Z\"}");

		mockMvc.perform(requestBuilder).andExpect(status().isOk());
	}

}