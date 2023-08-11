package io.mosip.preregistration.application.service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.xml.bind.DatatypeConverter;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.HMACUtils2;
import io.mosip.preregistration.application.constant.PreRegLoginConstant;
import io.mosip.preregistration.application.constant.PreRegLoginErrorConstants;
import io.mosip.preregistration.application.dto.OTPGenerateRequestDTO;
import io.mosip.preregistration.application.dto.OtpRequestDTO;
import io.mosip.preregistration.application.dto.RequestDTO;
import io.mosip.preregistration.application.entity.OtpTransaction;
import io.mosip.preregistration.application.exception.PreRegLoginException;
import io.mosip.preregistration.application.repository.OtpTxnRepository;
import io.mosip.preregistration.application.service.util.NotificationServiceUtil;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.config.LoggerConfiguration;

/**
 * OTPManager handling with OTP-Generation and OTP-Validation.
 * 
 *
 */
@Component
public class OTPManager {

	/** The Constant OTP_EXPIRED. */
	private static final String OTP_EXPIRED = "OTP_EXPIRED";

	/** The Constant OTP_ALREADY_SENT. */
	private static final String OTP_ALREADY_SENT = "OTP_ALREADY_SENT";

	/** The Constant USER_BLOCKED. */
	private static final String USER_BLOCKED = "USER_BLOCKED";
	
	/** The Constant OTP_ATTEMPT_EXCEEDED. */
	private static final String OTP_ATTEMPT_EXCEEDED = "OTP_ATTEMPT_EXCEEDED";

	@Value("${secretKey}")
	private String secretKey;

	@Value("${clientId}")
	private String clientId;

	@Value("${version}")
	private String version;

	@Value("${sendOtp.resource.url}")
	private String sendOtpResourceUrl;
	
	@Value("${pre.reg.login.otp.validation-attempt-threshold}")
	private int otpValidationThreshold;

	@Value("${appId}")
	private String appId;

	private static final String OTP = "otp";

	@Autowired
	private Environment environment;

	@Autowired
	@Qualifier("selfTokenRestTemplate")
	RestTemplate restTemplate;

	@Autowired
	private OtpTxnRepository otpRepo;

	/** The logger. */
	private Logger logger = LoggerConfiguration.logConfig(OTPManager.class);

	@Autowired
	NotificationServiceUtil notification;

	/**
	 * Generate OTP with information of {@link MediaType } and OTP generation
	 * time-out.
	 *
	 * @param otpRequestDTO the otp request DTO
	 * @param uin           the uin
	 * @param valueMap      the value map
	 * @return String(otp)
	 * @throws IOException
	 * @throws PreRegLoginException
	 */
	public boolean sendOtp(MainRequestDTO<OtpRequestDTO> requestDTO, String channelType, String language)
			throws PreRegLoginException, IOException {
		logger.info("sessionId", "idType", "id", "In sendOtp method of otpmanager service ");
		String userId = requestDTO.getRequest().getUserId();

		String refId = hash(userId);

		if ((otpRepo.checkotpsent(refId, PreRegLoginConstant.ACTIVE_STATUS, DateUtils.getUTCCurrentDateTime()) > 0)) {
			logger.error(PreRegLoginConstant.SESSION_ID, this.getClass().getSimpleName(),
					PreRegLoginErrorConstants.OTP_ALREADY_SENT.getErrorCode(), OTP_ALREADY_SENT);
			throw new PreRegLoginException(PreRegLoginErrorConstants.OTP_ALREADY_SENT.getErrorCode(),
					PreRegLoginErrorConstants.OTP_ALREADY_SENT.getErrorMessage());
		}

		String otp = generateOTP(requestDTO);
		logger.info("sessionId", "idType", "id", "In generateOTP method of otpmanager service OTP generated");
		String otpHash = digestAsPlainText(
				(userId + environment.getProperty(PreRegLoginConstant.KEY_SPLITTER) + otp).getBytes());

		if (otpRepo.existsByOtpHashAndStatusCode(otpHash, PreRegLoginConstant.ACTIVE_STATUS)) {
			OtpTransaction otpTxn = otpRepo.findTopByOtpHashAndStatusCode(otpHash, PreRegLoginConstant.ACTIVE_STATUS); 
			otpTxn.setOtpHash(otpHash);
			otpTxn.setUpdBy(environment.getProperty(PreRegLoginConstant.MOSIP_PRE_REG_CLIENTID));
			otpTxn.setUpdDTimes(DateUtils.getUTCCurrentDateTime());
			otpTxn.setExpiryDtimes(DateUtils.getUTCCurrentDateTime().plusSeconds(
					environment.getProperty(PreRegLoginConstant.MOSIP_KERNEL_OTP_EXPIRY_TIME, Long.class)));
			otpTxn.setStatusCode(PreRegLoginConstant.ACTIVE_STATUS);
			otpRepo.save(otpTxn);
		} else {
			OtpTransaction txn = new OtpTransaction();
			txn.setId(UUID.randomUUID().toString());
			txn.setRefId(hash(userId));
			txn.setOtpHash(otpHash);
			txn.setCrBy(environment.getProperty(PreRegLoginConstant.MOSIP_PRE_REG_CLIENTID));
			txn.setCrDtimes(DateUtils.getUTCCurrentDateTime());
			txn.setGeneratedDtimes(DateUtils.getUTCCurrentDateTime());
			txn.setExpiryDtimes(DateUtils.getUTCCurrentDateTime().plusSeconds(
					environment.getProperty(PreRegLoginConstant.MOSIP_KERNEL_OTP_EXPIRY_TIME, Long.class)));
			txn.setStatusCode(PreRegLoginConstant.ACTIVE_STATUS);
			otpRepo.save(txn);
		}
		Map<String, Object> mp = new HashMap<>();

		Integer validTime = environment.getProperty(PreRegLoginConstant.MOSIP_KERNEL_OTP_EXPIRY_TIME, Integer.class)
				/ 60;
		LocalDateTime dateTime = LocalDateTime.now(ZoneId.of(environment.getProperty("mosip.notification.timezone")));

		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

		mp.put("otp", otp);
		mp.put("date", dateFormatter.format(dateTime));
		mp.put("validTime", validTime);
		mp.put("name", userId);
		mp.put("username", userId);
		mp.put("time", timeFormatter.format(dateTime));

		if (channelType.equalsIgnoreCase(PreRegLoginConstant.PHONE_NUMBER)) {
			logger.info("sessionId", "idType", "id",
					"In generateOTP method of otpmanager service invoking sms notification");
			notification.invokeSmsNotification(mp, userId, requestDTO, language);
		}
		if (channelType.equalsIgnoreCase(PreRegLoginConstant.EMAIL)) {
			logger.info("sessionId", "idType", "id",
					"In generateOTP method of otpmanager service invoking email notification");
			notification.invokeEmailNotification(mp, userId, requestDTO, language);
		}
		return true;
	}

	private String generateOTP(MainRequestDTO<OtpRequestDTO> requestDTO) throws PreRegLoginException {
		logger.info("sessionId", "idType", "id", "In generateOTP method of otpmanager service ");
		try {
			OTPGenerateRequestDTO otpRequestDTO = new OTPGenerateRequestDTO();
			otpRequestDTO.setId(requestDTO.getId());
			otpRequestDTO.setRequesttime(requestDTO.getRequesttime());
			otpRequestDTO.setVersion(requestDTO.getVersion());
			RequestDTO req = new RequestDTO(requestDTO.getRequest().getUserId());

			otpRequestDTO.setRequest(req);

			HttpHeaders headers1 = new HttpHeaders();
			headers1.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
			headers1.setContentType(MediaType.APPLICATION_JSON_UTF8);
			headers1.add("user-agent",
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
			HttpEntity<OTPGenerateRequestDTO> entity1 = new HttpEntity<OTPGenerateRequestDTO>(otpRequestDTO, headers1);
			ResponseWrapper<Map<String, String>> response = restTemplate
					.exchange(environment.getProperty("otp-generate.rest.uri"), HttpMethod.POST, entity1,
							ResponseWrapper.class)
					.getBody();
			String otp = null;
			if (response != null) {
				Map<String, String> res = response.getResponse();
				if (res != null) {
					if (res.get("status").equals(USER_BLOCKED)) {
						logger.error(PreRegLoginConstant.SESSION_ID, this.getClass().getSimpleName(),
								PreRegLoginErrorConstants.BLOCKED_OTP_VALIDATE.getErrorCode(), USER_BLOCKED);
						throw new PreRegLoginException(PreRegLoginErrorConstants.BLOCKED_OTP_VALIDATE.getErrorCode(),
								PreRegLoginErrorConstants.BLOCKED_OTP_VALIDATE.getErrorMessage());
					} else {
						otp = res.get(OTP);
					}
				}
			}
			return otp;
		} catch (PreRegLoginException e) {
			logger.error(PreRegLoginConstant.SESSION_ID, this.getClass().getSimpleName(), "generateOTP",
					e.getMessage());
			throw new PreRegLoginException(PreRegLoginErrorConstants.UNABLE_TO_PROCESS.getErrorCode(),
					PreRegLoginErrorConstants.UNABLE_TO_PROCESS.getErrorMessage());
		} catch (Exception e) {
			logger.error(PreRegLoginConstant.SESSION_ID, this.getClass().getSimpleName(),
					PreRegLoginErrorConstants.SERVER_ERROR.getErrorCode(),
					PreRegLoginErrorConstants.SERVER_ERROR.getErrorMessage());
			throw new PreRegLoginException(PreRegLoginErrorConstants.SERVER_ERROR.getErrorCode(),
					PreRegLoginErrorConstants.SERVER_ERROR.getErrorMessage());
		}
	}

	/**
	 * Validate method for OTP Validation.
	 *
	 * @param pinValue the pin value
	 * @param otpKey   the otp key
	 * @return true, if successful
	 * @throws PreRegLoginException the id authentication business exception
	 */
	public boolean validateOtp(String otp, String userId) throws PreRegLoginException {
		logger.info("sessionId", "idType", "id", "In validateOtp method of otpmanager service ");
		String otpHash;
		String refId = hash(userId);
		otpHash = digestAsPlainText(
				(userId + environment.getProperty(PreRegLoginConstant.KEY_SPLITTER) + otp).getBytes());
		if (!otpRepo.existsByOtpHashAndStatusCode(otpHash, PreRegLoginConstant.ACTIVE_STATUS)) {
			OtpTransaction otpTn = otpRepo.findByRefIdAndStatusCode(refId, PreRegLoginConstant.ACTIVE_STATUS);
			if (otpTn.getValidationRetryCount() == null) {
				otpTn.setValidationRetryCount(1);
			} else {
				int otpCount = 0;
				otpCount = otpTn.getValidationRetryCount() + 1;
				otpTn.setValidationRetryCount(otpCount);
			}
			if (otpTn.getValidationRetryCount() > otpValidationThreshold) {
				otpTn.setStatusCode(PreRegLoginConstant.USED_STATUS);
				otpRepo.save(otpTn);
				logger.error(PreRegLoginConstant.SESSION_ID, this.getClass().getSimpleName(),
						PreRegLoginErrorConstants.OTP_ATTEMPT_EXCEEDED.getErrorCode(), OTP_ATTEMPT_EXCEEDED);
				throw new PreRegLoginException(PreRegLoginErrorConstants.OTP_ATTEMPT_EXCEEDED.getErrorCode(),
						PreRegLoginErrorConstants.OTP_ATTEMPT_EXCEEDED.getErrorMessage());
			}
			otpRepo.save(otpTn);
			return false;
		}
		OtpTransaction otpTxn = otpRepo.findTopByOtpHashAndStatusCode(otpHash, PreRegLoginConstant.ACTIVE_STATUS);
		otpTxn.setStatusCode(PreRegLoginConstant.USED_STATUS);
		otpRepo.save(otpTxn);
		if (!(otpTxn.getExpiryDtimes().isAfter(DateUtils.getUTCCurrentDateTime()))) {
			logger.error(PreRegLoginConstant.SESSION_ID, this.getClass().getSimpleName(),
					PreRegLoginErrorConstants.EXPIRED_OTP.getErrorCode(), OTP_EXPIRED);
			throw new PreRegLoginException(PreRegLoginErrorConstants.EXPIRED_OTP.getErrorCode(),
					PreRegLoginErrorConstants.EXPIRED_OTP.getErrorMessage());
		}
		return true;
	}

	private static String digestAsPlainText(byte[] data) {
		return DatatypeConverter.printHexBinary(data).toUpperCase();
	}

    private String hash(String id) throws PreRegLoginException {
        String idHash = null;
        try {
            idHash = HMACUtils2.digestAsPlainText(id.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new PreRegLoginException(PreRegLoginErrorConstants.UNABLE_TO_PROCESS.getErrorCode(), e.getMessage());
        }
        return idHash;
    }

}