package io.mosip.preregistration.application.test.service.util;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import io.mosip.preregistration.application.dto.UISpecResponseDTO;
import io.mosip.preregistration.application.exception.UISpecException;
import io.mosip.preregistration.application.service.util.UISpecServiceUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.jackson2.autoconfigure.Jackson2AutoConfiguration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = UISpecServiceUtil.class)
@ImportAutoConfiguration(Jackson2AutoConfiguration.class)
@TestPropertySource(properties = "masterdata.resource.url=http://masterdata.test")
public class LatestUISpecDecodingTest {

    @Autowired
    private UISpecServiceUtil util;

    @MockitoBean(name = "selfTokenRestTemplate")
    private RestTemplate restTemplate;

    @Test
    public void should_decodeUtcTimestamps_when_latestUiSpecReturnsJson() {
        mockResponse("""
                {
                  "responsetime": "2026-11-20T10:15:00.123Z",
                  "response": [{
                    "effectiveFrom": "2026-11-20T10:00:00Z",
                    "createdOn": "2026-11-19T09:00:00Z",
                    "updatedOn": "2026-11-20T10:10:00Z"
                  }]
                }
                """.getBytes(UTF_8));

        List<UISpecResponseDTO> result = util.getUISchema(0.0, 0.1);

        assertEquals(1, result.size());
        UISpecResponseDTO spec = result.get(0);
        assertEquals(LocalDateTime.of(2026, 11, 20, 10, 0), spec.getEffectiveFrom());
        assertEquals(LocalDateTime.of(2026, 11, 19, 9, 0), spec.getCreatedOn());
        assertEquals(LocalDateTime.of(2026, 11, 20, 10, 10), spec.getUpdatedOn());
    }

    @Test
    public void should_throwUiSpecException_when_latestUiSpecResponseHasNoBody() {
        mockResponse(null);

        UISpecException error = assertThrows(UISpecException.class,
                () -> util.getUISchema(0.0, 0.1));

        assertEquals("PRG_APP_003", error.getErrorCode());
    }

    @Test
    public void should_throwUiSpecException_when_latestUiSpecResponseIsMalformed() {
        mockResponse("{".getBytes(UTF_8));

        UISpecException error = assertThrows(UISpecException.class,
                () -> util.getUISchema(0.0, 0.1));

        assertEquals("PRG_APP_003", error.getErrorCode());
    }

    private void mockResponse(byte[] body) {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(ResponseEntity.ok(body));
    }
}
