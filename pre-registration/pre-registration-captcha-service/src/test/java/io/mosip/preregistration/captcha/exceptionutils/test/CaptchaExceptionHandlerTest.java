package io.mosip.preregistration.captcha.exceptionutils.test;

import io.mosip.preregistration.captcha.exception.CaptchaException;
import io.mosip.preregistration.captcha.exception.InvalidRequestCaptchaException;
import io.mosip.preregistration.captcha.exceptionutils.CaptchaExceptionHandler;
import io.mosip.preregistration.core.common.dto.ExceptionJSONInfoDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.exception.InvalidRequestParameterException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

@RunWith(JUnit4.class)
@SpringBootTest
@ContextConfiguration(classes = { CaptchaExceptionHandler.class })
public class CaptchaExceptionHandlerTest {

    @Test
    public void test_handle_invalid_captcha_request_returns_proper_response() {
        CaptchaExceptionHandler exceptionHandler = new CaptchaExceptionHandler();
        ReflectionTestUtils.setField(exceptionHandler, "mosipcaptchaValidateId", "mosip.pre-registration.captcha.validate");
        ReflectionTestUtils.setField(exceptionHandler, "version", "1.0");

        String errorCode = "PRG_CAPT_001";
        String errorMessage = "Invalid Captcha";
        InvalidRequestCaptchaException exception = new InvalidRequestCaptchaException(errorCode, errorMessage);

        MainResponseDTO<?> response = exceptionHandler.handleInvalidCaptchaReqest(exception);

        assertNotNull(response);
        assertEquals("mosip.pre-registration.captcha.validate", response.getId());
        assertEquals("1.0", response.getVersion());
        assertNotNull(response.getResponsetime());
        assertNull(response.getResponse());

        List<ExceptionJSONInfoDTO> errors = response.getErrors();
        assertNotNull(errors);
        assertEquals(1, errors.size());
        assertEquals(errorCode, errors.getFirst().getErrorCode());
        assertEquals(errorMessage, errors.getFirst().getMessage());
    }

    @Test
    public void test_handle_captcha_exception_returns_correct_error_details() {
        CaptchaExceptionHandler handler = new CaptchaExceptionHandler();
        CaptchaException ex = new CaptchaException("ERR001", "Captcha error occurred");
        ReflectionTestUtils.setField(handler, "mosipcaptchaValidateId", "captcha-validate-id");
        ReflectionTestUtils.setField(handler, "version", "1.0");

        MainResponseDTO<?> response = handler.handleCaptchaException(ex);

        assertEquals("captcha-validate-id", response.getId());
        assertEquals("1.0", response.getVersion());
        assertNotNull(response.getResponsetime());
        assertNull(response.getResponse());
        assertEquals(1, response.getErrors().size());
        assertEquals("ERR001", response.getErrors().getFirst().getErrorCode());
        assertEquals("Captcha error occurred", response.getErrors().getFirst().getMessage());
    }

    @Test
    public void test_handle_exception_generic() {
        CaptchaExceptionHandler handler = new CaptchaExceptionHandler();
        handler.mosipcaptchaValidateId = "testId";

        Exception ex = new Exception("Generic error occurred");
        MainResponseDTO<?> response = handler.handleException(ex);

        assertEquals("testId", response.getId());
        assertNotNull(response.getResponsetime());
        assertNull(response.getResponse());
        assertEquals(1, response.getErrors().size());
        assertEquals("", response.getErrors().getFirst().getErrorCode());
        assertEquals("Generic error occurred", response.getErrors().getFirst().getMessage());
    }

    @Test
    public void test_all_exception_handlers_response() {
        CaptchaExceptionHandler handler = new CaptchaExceptionHandler();
        handler.mosipcaptchaValidateId = "testId";

        InvalidRequestCaptchaException invalidCaptchaEx = new InvalidRequestCaptchaException("400", "Invalid captcha");
        MainResponseDTO<?> captchaResponse = handler.handleInvalidCaptchaReqest(invalidCaptchaEx);

        assertEquals("testId", captchaResponse.getId());
        assertNotNull(captchaResponse.getResponsetime());
        assertNull(captchaResponse.getResponse());
        assertEquals(1, captchaResponse.getErrors().size());
        assertEquals("400", captchaResponse.getErrors().getFirst().getErrorCode());
        assertEquals("Invalid captcha", captchaResponse.getErrors().getFirst().getMessage());

        CaptchaException captchaEx = new CaptchaException("500", "Captcha error");
        MainResponseDTO<?> captchaExResponse = handler.handleCaptchaException(captchaEx);

        assertEquals("testId", captchaExResponse.getId());
        assertNotNull(captchaExResponse.getResponsetime());
        assertNull(captchaExResponse.getResponse());
        assertEquals(1, captchaExResponse.getErrors().size());
        assertEquals("500", captchaExResponse.getErrors().getFirst().getErrorCode());
        assertEquals("Captcha error", captchaExResponse.getErrors().getFirst().getMessage());
    }

    @Test
    public void test_handle_invalid_captcha_request_with_empty_error_code() {
        CaptchaExceptionHandler handler = new CaptchaExceptionHandler();
        InvalidRequestCaptchaException exception = new InvalidRequestCaptchaException("", "Error message");

        MainResponseDTO<?> response = handler.handleInvalidCaptchaReqest(exception);

        assertNotNull(response);
        assertEquals("", response.getErrors().getFirst().getErrorCode());
        assertEquals("Error message", response.getErrors().getFirst().getMessage());
    }

    @Test
    public void test_handle_captcha_exception_with_long_error_message() {
        CaptchaExceptionHandler handler = new CaptchaExceptionHandler();
        String longErrorMessage = "This is a very long error message that exceeds normal length expectations.";
        CaptchaException exception = new CaptchaException("ERR001", longErrorMessage);

        MainResponseDTO<?> response = handler.handleCaptchaException(exception);

        assertNotNull(response);
        assertEquals("ERR001", response.getErrors().getFirst().getErrorCode());
        assertEquals(longErrorMessage, response.getErrors().getFirst().getMessage());
    }

    @Test
    public void test_handle_exception_with_empty_error_code() {
        CaptchaExceptionHandler handler = new CaptchaExceptionHandler();
        Exception exception = new Exception("Test exception message");

        MainResponseDTO<?> response = handler.handleException(exception);

        assertNotNull(response);
        assertEquals("", response.getErrors().getFirst().getErrorCode());
        assertEquals("Test exception message", response.getErrors().getFirst().getMessage());
    }

    @Test
    public void test_invalid_request_throws_null_pointer_exception() {
        CaptchaExceptionHandler exceptionHandler = new CaptchaExceptionHandler();

        Map<String, String> mockIdMap = new HashMap<>();
        mockIdMap.put("testOperation", "test.id");

        ReflectionTestUtils.setField(exceptionHandler, "id", mockIdMap);

        List<ExceptionJSONInfoDTO> exceptionList = new ArrayList<>();
        exceptionList.add(new ExceptionJSONInfoDTO("ERR-001", "Test error message"));

        InvalidRequestParameterException exception = new InvalidRequestParameterException(exceptionList, "testOperation", null);

        assertThrows(NullPointerException.class, () -> {
            exceptionHandler.invalidRequest(exception);
        });
    }

}
