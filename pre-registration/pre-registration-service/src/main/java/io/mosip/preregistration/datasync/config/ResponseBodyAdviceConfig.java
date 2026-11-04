package io.mosip.preregistration.datasync.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.util.DateUtils2;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.util.ResponseFilter;
import io.mosip.preregistration.datasync.errorcodes.ErrorCodes;
import io.mosip.preregistration.datasync.errorcodes.ErrorMessages;
import io.mosip.preregistration.datasync.exception.ParseResponseException;
import io.mosip.preregistration.datasync.service.util.DataSyncServiceUtil;

@RestControllerAdvice
/**
 * Spring configuration for response body advice config in Pre-Registration.
 */
public class ResponseBodyAdviceConfig implements ResponseBodyAdvice<MainResponseDTO<?>> {

	@Autowired
	/**
	 * object mapper used by this Pre-Registration type.
	 */
	private ObjectMapper objectMapper;

	@Autowired
	@Lazy
	DataSyncServiceUtil serviceUtil;

	@Override
	/**
	 * Performs supports for MOSIP Pre-Registration.
	 *
	 * @param returnType return type used by supports
	 * @param converterType converter type used by supports
	 * @return supports result
	 */
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		return returnType.hasMethodAnnotation(ResponseFilter.class);
	}

	@Override
	/**
	 * Performs before body write for MOSIP Pre-Registration.
	 *
	 * @param body body used by before body write
	 * @param returnType return type used by before body write
	 * @param selectedContentType selected content type used by before body write
	 * @param selectedConverterType selected converter type used by before body write
	 * @param request request used by before body write
	 * @param response response used by before body write
	 * @return before body write result
	 */
	public MainResponseDTO<?> beforeBodyWrite(MainResponseDTO<?> body, MethodParameter returnType,
			MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType,
			ServerHttpRequest request, ServerHttpResponse response) {
		if (body != null) {
			try {
				String timestamp = DateUtils2.getUTCCurrentDateTimeString();
				body.setResponsetime(timestamp);
				String signedResponse = serviceUtil.signData(objectMapper.writeValueAsString(body));
				response.getHeaders().add("Response-Signature", signedResponse);
			} catch (JsonProcessingException e) {
				throw new ParseResponseException(ErrorCodes.PRG_DATA_SYNC_017.toString(),
						ErrorMessages.ERROR_WHILE_PARSING.getMessage(), body);
			}
		}
		return body;
	}

}
