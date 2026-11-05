package io.mosip.preregistration.captcha.serviceimpl;

import static io.mosip.preregistration.core.constant.PreRegCoreConstant.LOGGER_ID;
import static io.mosip.preregistration.core.constant.PreRegCoreConstant.LOGGER_IDTYPE;
import static io.mosip.preregistration.core.constant.PreRegCoreConstant.LOGGER_SESSIONID;

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
import io.mosip.kernel.core.util.DateUtils2;
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
/**
 * Business service for captcha service impl in MOSIP Pre-Registration.
 */
public class CaptchaServiceImpl implements CaptchaService {

	@Value("${mosip.preregistration.captcha.secretkey}")
	/**
	 * recaptcha secret used by this Pre-Registration type.
	 */
	public String recaptchaSecret;

	@Value("${mosip.preregistration.captcha.recaptcha.verify.url}")
	/**
	 * recaptcha verify url used by this Pre-Registration type.
	 */
	public String recaptchaVerifyUrl;

	@Value("${mosip.preregistration.captcha.id.validate}")
	/**
	 * mosipcaptcha validate id used by this Pre-Registration type.
	 */
	public String mosipcaptchaValidateId;

	@Value("${version}")
	/**
	 * version used by this Pre-Registration type.
	 */
	private String version;

	@Autowired
	@Qualifier(value = "restTemplateBean")
	/**
	 * rest template used by this Pre-Registration type.
	 */
	private RestTemplate restTemplate;

	/**
	 * captcha success used by this Pre-Registration type.
	 */
	private final String CAPTCHA_SUCCESS = " Captcha successfully verified";

	/**
	 * Performs log config for MOSIP Pre-Registration.
	 *
	 * @return log config result
	 */
	private Logger log = LoggerConfiguration.logConfig(CaptchaServiceImpl.class);

	@Override
	/**
	 * Performs validate captcha for MOSIP Pre-Registration.
	 *
	 * @param captchaRequest captcha request used by validate captcha
	 * @return validate captcha result
	 */
	public Object validateCaptcha(Object captchaRequest) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"In pre-registration captcha service to validate the token request"
						+ ((CaptchaRequestDTO) captchaRequest).getCaptchaToken());

		validateCaptchaRequest((CaptchaRequestDTO) captchaRequest);

		MainResponseDTO<CaptchaResposneDTO> mainResponse = new MainResponseDTO<>();

		MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
		param.add("secret", recaptchaSecret);
		param.add("response", ((CaptchaRequestDTO) captchaRequest).getCaptchaToken().trim());

		GoogleCaptchaDTO captchaResponse = null;

		try {
			log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"In pre-registration captcha service try block to validate the token request via a google verify site rest call"
							+ ((CaptchaRequestDTO) captchaRequest).getCaptchaToken() + "  " + recaptchaVerifyUrl);

			captchaResponse = this.restTemplate.postForObject(recaptchaVerifyUrl, param, GoogleCaptchaDTO.class);
			if (captchaResponse != null) {
				log.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, captchaResponse.toString());
			}
		} catch (RestClientException ex) {
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"In pre-registration captcha service to validate the token request via a google verify site rest call has failed --->"
							+ ((CaptchaRequestDTO) captchaRequest).getCaptchaToken() + "  " + recaptchaVerifyUrl + "  "
							+ ex);
			if (captchaResponse != null && captchaResponse.getErrorCodes() != null) {
				throw new CaptchaException(captchaResponse.getErrorCodes().get(0).getErrorCode(),
						captchaResponse.getErrorCodes().get(0).getMessage());
			}
		}

		if (captchaResponse != null && captchaResponse.isSuccess()) {
			log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
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
			if (captchaResponse != null) {
				log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
						"In pre-registration captcha service token request has failed --->"
								+ captchaResponse.isSuccess());
			}
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

	/**
	 * Performs validate captcha request for MOSIP Pre-Registration.
	 *
	 * @param captchaRequest captcha request used by validate captcha request
	 */
	private void validateCaptchaRequest(CaptchaRequestDTO captchaRequest) {
		if (captchaRequest.getCaptchaToken() == null || captchaRequest.getCaptchaToken().trim().length() == 0) {
			log.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, captchaRequest.toString());
			throw new InvalidRequestCaptchaException(CaptchaErrorCode.INVALID_CAPTCHA_REQUEST.getErrorCode(),
					CaptchaErrorCode.INVALID_CAPTCHA_REQUEST.getErrorMessage());
		}
	}

	/**
	 * Returns current response time.
	 *
	 * @return get current response time result
	 */
	private String getCurrentResponseTime() {
		String dateTimeFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
		return DateUtils2.formatDate(new Date(System.currentTimeMillis()), dateTimeFormat);
	}
}
