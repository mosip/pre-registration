package io.mosip.preregistration.captcha.serviceimpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.preregistration.captcha.constants.CaptchaErrorCode;
import io.mosip.preregistration.captcha.dto.CaptchaRequestDTO;
import io.mosip.preregistration.captcha.dto.CaptchaResposneDTO;
import io.mosip.preregistration.captcha.dto.GoogleCaptchaDTO;
import io.mosip.preregistration.captcha.exception.CaptchaException;
import io.mosip.preregistration.captcha.exception.InvalidRequestCaptchaException;
import io.mosip.preregistration.captcha.service.CaptchaService;
import io.mosip.preregistration.core.common.dto.ExceptionJSONInfoDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.config.LoggerConfiguration;

@Service
public class CaptchaServiceImpl implements CaptchaService {

	@Value("${mosip.preregistration.captcha.secretkey}")
	public String recaptchaSecret;

	@Value("${mosip.preregistration.captcha.recaptcha.verify.url}")
	public String recaptchaVerifyUrl;

	@Value("${mosip.preregistration.captcha.id.validate}")
	public String mosipcaptchaValidateId;

	@Value("${version}")
	private String version;

	@Autowired
	@Qualifier(value = "restTemplateBean")
	private RestTemplate restTemplate;

	private final String CAPTCHA_SUCCESS = " Captcha successfully verified";

	private Logger log = LoggerConfiguration.logConfig(CaptchaServiceImpl.class);

	@Override
	public Object validateCaptcha(Object captchaRequest) {

		log.info("sessionId", "idType", "id", "In pre-registration captcha service to validate the token request"
				+ ((CaptchaRequestDTO) captchaRequest).getCaptchaToken());

		validateCaptchaRequest((CaptchaRequestDTO) captchaRequest);

		MainResponseDTO<CaptchaResposneDTO> mainResponse = new MainResponseDTO<>();

		MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
		param.add("secret", recaptchaSecret);
		param.add("response", ((CaptchaRequestDTO) captchaRequest).getCaptchaToken().trim());

		GoogleCaptchaDTO captchaResponse = null;

		try {
			log.info("sessionId", "idType", "id",
					"In pre-registration captcha service try block to validate the token request via a google verify site rest call"
							+ ((CaptchaRequestDTO) captchaRequest).getCaptchaToken() + "  " + recaptchaVerifyUrl);
			
			captchaResponse = this.restTemplate.postForObject(recaptchaVerifyUrl, param, GoogleCaptchaDTO.class);
			if (captchaResponse != null) {
				log.debug("sessionId", "idType", "id", captchaResponse.toString());
			}
		} catch (RestClientException ex) {
			log.error("sessionId", "idType", "id",
					"In pre-registration captcha service to validate the token request via a google verify site rest call has failed --->"
							+ ((CaptchaRequestDTO) captchaRequest).getCaptchaToken() + "  " + recaptchaVerifyUrl + "  "
							+ ex);
			if (captchaResponse != null && captchaResponse.getErrorCodes() !=null) {
			throw new CaptchaException(captchaResponse.getErrorCodes().get(0).getErrorCode(),
					captchaResponse.getErrorCodes().get(0).getMessage());
			}
		}

		if (captchaResponse!=null && captchaResponse.isSuccess()) {
			log.info("sessionId", "idType", "id",
					"In pre-registration captcha service token request has been successfully verified --->"
							+ captchaResponse.isSuccess());
			mainResponse.setId(mosipcaptchaValidateId);
			mainResponse.setResponsetime(captchaResponse.getChallengeTs());
			mainResponse.setVersion(version);
			CaptchaResposneDTO response = new CaptchaResposneDTO();
			response.setMessage(CAPTCHA_SUCCESS);
			response.setSuccess(captchaResponse.isSuccess());
			mainResponse.setResponse(response);
		} else {
			log.error("sessionId", "idType", "id",
					"In pre-registration captcha service token request has failed --->" + captchaResponse.isSuccess());
			mainResponse.setId(mosipcaptchaValidateId);
			mainResponse.setResponsetime(getCurrentResponseTime());
			mainResponse.setVersion(version);
			mainResponse.setResponse(null);
			ExceptionJSONInfoDTO error = new ExceptionJSONInfoDTO(CaptchaErrorCode.INVALID_CAPTCHA_CODE.getErrorCode(),
					CaptchaErrorCode.INVALID_CAPTCHA_CODE.getErrorMessage());
			List<ExceptionJSONInfoDTO> errorList = new ArrayList<ExceptionJSONInfoDTO>();
			errorList.add(error);
			mainResponse.setErrors(errorList);

		}
		return mainResponse;

	}

	private void validateCaptchaRequest(CaptchaRequestDTO captchaRequest) {

	 if (captchaRequest.getCaptchaToken() == null || captchaRequest.getCaptchaToken().trim().length() == 0) {
		 	log.debug("sessionId", "idType", "id", captchaRequest.toString());
			throw new InvalidRequestCaptchaException(CaptchaErrorCode.INVALID_CAPTCHA_REQUEST.getErrorCode(),
					CaptchaErrorCode.INVALID_CAPTCHA_REQUEST.getErrorMessage());
		}
	}

	private String getCurrentResponseTime() {
		String dateTimeFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
		return DateUtils.formatDate(new Date(System.currentTimeMillis()), dateTimeFormat);
	}

}
