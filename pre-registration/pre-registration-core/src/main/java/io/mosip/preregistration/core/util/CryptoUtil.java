package io.mosip.preregistration.core.util;

import static io.mosip.preregistration.core.constant.PreRegCoreConstant.LOGGER_ID;
import static io.mosip.preregistration.core.constant.PreRegCoreConstant.LOGGER_IDTYPE;
import static io.mosip.preregistration.core.constant.PreRegCoreConstant.LOGGER_SESSIONID;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.core.common.dto.CryptoManagerRequestDTO;
import io.mosip.preregistration.core.common.dto.CryptoManagerResponseDTO;
import io.mosip.preregistration.core.common.dto.RequestWrapper;
import io.mosip.preregistration.core.common.dto.ResponseWrapper;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.exception.EncryptionFailedException;

/**
 * @author Tapaswini Behera
 * @since 1.0.0
 *
 */
@Service
public class CryptoUtil {

	/**
	 * Performs log config for MOSIP Pre-Registration.
	 *
	 * @return log config result
	 */
	private Logger log = LoggerConfiguration.logConfig(CryptoUtil.class);

	/**
	 * Autowired reference for {@link #restTemplateBuilder}
	 */
	@Qualifier("selfTokenRestTemplate")
	@Autowired
	RestTemplate restTemplate;

	@Value("${cryptoResource.url}")
	/**
	 * crypto resource url used by this Pre-Registration type.
	 */
	public String cryptoResourceUrl;

	@Value("${preregistration.crypto.applicationId}")
	/**
	 * crypto applcation id used by this Pre-Registration type.
	 */
	public String cryptoApplcationId;

	@Value("${preregistration.crypto.referenceId}")
	/**
	 * crypto reference id used by this Pre-Registration type.
	 */
	public String cryptoReferenceId;

	@Value("${preregistration.crypto.PrependThumbprint}")
	/**
	 * crypto prepend thumbprint used by this Pre-Registration type.
	 */
	public boolean cryptoPrependThumbprint;

	/**
	 * Performs encrypt for MOSIP Pre-Registration.
	 *
	 * @param originalInput original input used by encrypt
	 * @param localDateTime local date time used by encrypt
	 * @return encrypt result
	 */
	public byte[] encrypt(byte[] originalInput, LocalDateTime localDateTime) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "In encrypt method of CryptoUtil service ");

		ResponseEntity<ResponseWrapper<CryptoManagerResponseDTO>> response = null;
		byte[] encryptedBytes = null;
		try {
			String encodedBytes = io.mosip.kernel.core.util.CryptoUtil.encodeToURLSafeBase64(originalInput);
			CryptoManagerRequestDTO dto = new CryptoManagerRequestDTO();
			dto.setApplicationId(cryptoApplcationId);
			dto.setData(encodedBytes);
			dto.setReferenceId(cryptoReferenceId);
			dto.setTimeStamp(localDateTime == null ? null : localDateTime.truncatedTo(ChronoUnit.MILLIS));
			dto.setPrependThumbprint(cryptoPrependThumbprint);
			RequestWrapper<CryptoManagerRequestDTO> requestKernel = new RequestWrapper<>();
			requestKernel.setRequest(dto);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<RequestWrapper<CryptoManagerRequestDTO>> request = new HttpEntity<>(requestKernel, headers);
			log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"In encrypt method of CryptoUtil service cryptoResourceUrl: " + cryptoResourceUrl + "/encrypt");
			response = restTemplate.exchange(cryptoResourceUrl + "/encrypt", HttpMethod.POST, request,
					new ParameterizedTypeReference<ResponseWrapper<CryptoManagerResponseDTO>>() {
					});
			log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "encrypt response of " + response);
			ResponseWrapper<CryptoManagerResponseDTO> body = response.getBody();
			if (body != null) {
				if (!(body.getErrors() == null || body.getErrors().isEmpty())) {
					throw new EncryptionFailedException(body.getErrors(), null);
				}
				if (body.getResponse() != null) {
					encryptedBytes = body.getResponse().getData().getBytes();
				}
			}
		} catch (Exception ex) {
			log.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, ExceptionUtils.getStackTrace(ex));
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"In encrypt method of CryptoUtil Util for Exception- " + ex.getMessage());
			throw ex;
		}
		return encryptedBytes;

	}

	/**
	 * Performs decrypt for MOSIP Pre-Registration.
	 *
	 * @param originalInput original input used by decrypt
	 * @param localDateTime local date time used by decrypt
	 * @return decrypt result
	 */
	public byte[] decrypt(byte[] originalInput, LocalDateTime localDateTime) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "In decrypt method of CryptoUtil service ");
		ResponseEntity<ResponseWrapper<CryptoManagerResponseDTO>> response = null;
		byte[] decodedBytes = null;
		try {

			CryptoManagerRequestDTO dto = new CryptoManagerRequestDTO();
			dto.setApplicationId(cryptoApplcationId);
			dto.setData(new String(originalInput, StandardCharsets.UTF_8));
			dto.setReferenceId(cryptoReferenceId);
			dto.setTimeStamp(localDateTime == null ? null : localDateTime.truncatedTo(ChronoUnit.MILLIS));
			dto.setPrependThumbprint(cryptoPrependThumbprint);
			RequestWrapper<CryptoManagerRequestDTO> requestKernel = new RequestWrapper<>();
			requestKernel.setRequest(dto);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<RequestWrapper<CryptoManagerRequestDTO>> request = new HttpEntity<>(requestKernel, headers);
			log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"In decrypt method of CryptoUtil service cryptoResourceUrl: " + cryptoResourceUrl + "/decrypt");
			response = restTemplate.exchange(cryptoResourceUrl + "/decrypt", HttpMethod.POST, request,
					new ParameterizedTypeReference<ResponseWrapper<CryptoManagerResponseDTO>>() {
					});
			ResponseWrapper<CryptoManagerResponseDTO> body = response.getBody();
			if (body != null) {
				if (!(body.getErrors() == null || body.getErrors().isEmpty())) {
					throw new EncryptionFailedException(body.getErrors(), null);
				}
				if (body.getResponse() != null) {
					decodedBytes = Base64.decodeBase64(body.getResponse().getData().getBytes());
				}
			}
		} catch (Exception ex) {
			log.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, ExceptionUtils.getStackTrace(ex));
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"In decrypt method of CryptoUtil Util for Exception- " + ex.getMessage());
			throw ex;
		}
		return decodedBytes;
	}
}
