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

import io.mosip.kernel.core.util.DateUtils;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.util.ResponseFilter;
import io.mosip.preregistration.datasync.errorcodes.ErrorCodes;
import io.mosip.preregistration.datasync.errorcodes.ErrorMessages;
import io.mosip.preregistration.datasync.exception.ParseResponseException;
import io.mosip.preregistration.datasync.service.util.DataSyncServiceUtil;

@RestControllerAdvice
public class ResponseBodyAdviceConfig implements ResponseBodyAdvice<MainResponseDTO<?>> {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	@Lazy
	DataSyncServiceUtil serviceUtil;

	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		return returnType.hasMethodAnnotation(ResponseFilter.class);
	}

	@Override
	public MainResponseDTO<?> beforeBodyWrite(MainResponseDTO<?> body, MethodParameter returnType,
			MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType,
			ServerHttpRequest request, ServerHttpResponse response) {
		if (body != null) {
			try {
				String timestamp = DateUtils.getUTCCurrentDateTimeString();
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
