package io.mosip.preregistration.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import io.mosip.preregistration.config.PreregOpenApiConfig.SwaggerMount;

/**
 * Unit tests for legacy per-service Swagger mounts ({@link PreregOpenApiConfig}).
 */
public class PreregOpenApiConfigTest {

	private MockMvc mockMvc;

	@Before
	public void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(new PreregSwaggerUiController()).build();
	}

	@Test
	public void mountsUseLegacyCatalogPrefixes() {
		assertEquals(4, PreregOpenApiConfig.SWAGGER_MOUNTS.size());
		SwaggerMount application = PreregOpenApiConfig.SWAGGER_MOUNTS.get(0);
		assertEquals("/application-service", application.uiBasePath());
		assertEquals("/application-service/swagger-ui/index.html", application.swaggerUiIndexPath());
		assertEquals("/application-service/v3/api-docs", application.apiDocsPath());
		assertEquals("/v3/api-docs/application", application.springDocGroupPath());
		assertEquals("/sync/datasync-service", PreregOpenApiConfig.SWAGGER_MOUNTS.get(1).uiBasePath());
		assertEquals("/captcha-service", PreregOpenApiConfig.SWAGGER_MOUNTS.get(2).uiBasePath());
		assertEquals("/appointment/booking-service", PreregOpenApiConfig.SWAGGER_MOUNTS.get(3).uiBasePath());
	}

	@Test
	public void applicationIndexUsesRelativeAssetsAndContextDocsUrl() throws Exception {
		String html = mockMvc
				.perform(get("/preregistration/v1/application-service/swagger-ui/index.html")
						.contextPath("/preregistration/v1"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith("text/html"))
				.andReturn().getResponse().getContentAsString();
		assertTrue(html.contains("href=\"swagger-ui.css\""));
		assertTrue(html.contains("/preregistration/v1/application-service/v3/api-docs"));
		assertTrue(html.contains("/preregistration/v1/appointment/booking-service/v3/api-docs"));
		assertTrue(html.contains("queryConfigEnabled: false"));
		assertTrue(!html.contains("/swagger-ui/swagger-ui/index.html"));
	}

	@Test
	public void bookingIndexSelectsBookingDefinition() throws Exception {
		String html = mockMvc
				.perform(get("/preregistration/v1/appointment/booking-service/swagger-ui/index.html")
						.contextPath("/preregistration/v1"))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		assertTrue(html.contains("'urls.primaryName': 'MOSIP Pre-Registration Booking'"));
	}

	@Test
	public void datasyncSwaggerHtmlRedirectsToIndex() throws Exception {
		mockMvc.perform(get("/preregistration/v1/sync/datasync-service/swagger-ui.html")
				.contextPath("/preregistration/v1"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/preregistration/v1/sync/datasync-service/swagger-ui/index.html"));
	}

	@Test
	public void rootSwaggerHtmlStaysOnSwaggerUiPrefix() throws Exception {
		mockMvc.perform(get("/preregistration/v1/swagger-ui.html").contextPath("/preregistration/v1"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/preregistration/v1/swagger-ui/index.html"));
	}
}
