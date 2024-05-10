package io.mosip.preregistration.application.service;

import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.qrcodegenerator.spi.QrCodeGenerator;
import io.mosip.kernel.qrcode.generator.zxing.constant.QrVersion;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.errorcodes.ErrorCodes;
import io.mosip.preregistration.core.errorcodes.ErrorMessages;
import io.mosip.preregistration.core.exception.InvalidRequestException;
import io.mosip.preregistration.core.util.ValidationUtil;
import io.mosip.preregistration.application.dto.QRCodeResponseDTO;
import io.mosip.preregistration.application.exception.util.QRcodeExceptionCatcher;

/**
 * @author Sanober Noor
 * @since 1.0.0
 */
@Service
public class GenerateQRcodeService {

	/**
	 * The reference to {@link GenerateQRcodeServiceUtil}.
	 */
	@Autowired
	private ValidationUtil serviceUtil;
	
	@Autowired
	private ValidationUtil validationUtil;

	private Logger log = LoggerConfiguration.logConfig(GenerateQRcodeService.class);

	@Autowired
	private QrCodeGenerator<QrVersion> qrCodeGenerator;

	Map<String, String> requiredRequestMap = new HashMap<>();

	@Value("${mosip.pre-registration.qrcode.id.generate}")
	private String id;

	@Value("${mosip.pre-registration.qrcode.service.version}")
	private String version;

	@Value("${qrversion}")
	private String qrversion;

	@Value("${mosip.utc-datetime-pattern}")
	private String utcDateTimePattern;

	@PostConstruct
	public void setupBookingService() {
		requiredRequestMap.put("version", version);
		requiredRequestMap.put("id", id);

	}

	/**
	 * This method will generate QR code
	 * 
	 * @param data
	 * @return QRCodeResponseDTO
	 */
	public MainResponseDTO<QRCodeResponseDTO> generateQRCode(MainRequestDTO<String> data) {
		byte[] qrCode = null;

		log.info("sessionId", "idType", "id", "In generateQRCode service of generateQRCode ");
		QRCodeResponseDTO responsedto = null;

		MainResponseDTO<QRCodeResponseDTO> response = new MainResponseDTO<>();

		response.setId(id);
		response.setVersion(version);
		try {
			response.setId(data.getId());
			response.setVersion(data.getVersion());
			if (data.getRequest() == null || data.getRequest().isEmpty()) {
				throw new InvalidRequestException(ErrorCodes.PRG_CORE_REQ_004.getCode(),
						ErrorMessages.INVALID_REQUEST_BODY.getMessage(), null);
			} else if (validationUtil.requestValidator(serviceUtil.prepareRequestMap(data), requiredRequestMap)) {

				qrCode = qrCodeGenerator.generateQrCode(data.getRequest(), QrVersion.valueOf(qrversion));
				responsedto = new QRCodeResponseDTO();
				responsedto.setQrcode(qrCode);
			}

			response.setResponse(responsedto);
			response.setResponsetime(serviceUtil.getCurrentResponseTime());

		} catch (Exception ex) {
			log.error("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id", "In generateQRCode service of generateQRCode " + ex.getMessage());
			new QRcodeExceptionCatcher().handle(ex, response);
		}

		return response;
	}

}
