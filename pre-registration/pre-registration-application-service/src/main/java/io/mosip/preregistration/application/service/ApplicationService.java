package io.mosip.preregistration.application.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.HMACUtils2;
import io.mosip.preregistration.application.dto.ApplicationInfoMetadataDTO;
import io.mosip.preregistration.application.dto.UIAuditRequest;
import io.mosip.preregistration.application.errorcodes.ApplicationErrorCodes;
import io.mosip.preregistration.application.errorcodes.ApplicationErrorMessages;
import io.mosip.preregistration.application.exception.AuditFailedException;
import io.mosip.preregistration.application.exception.DocumentNotFoundException;
import io.mosip.preregistration.application.exception.util.DemographicExceptionCatcher;
import io.mosip.preregistration.core.code.AuditLogVariables;
import io.mosip.preregistration.core.code.EventId;
import io.mosip.preregistration.core.code.EventType;
import io.mosip.preregistration.core.common.dto.AuditRequestDto;
import io.mosip.preregistration.core.common.dto.DemographicResponseDTO;
import io.mosip.preregistration.core.common.dto.DocumentsMetaData;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.exception.PreRegistrationException;
import io.mosip.preregistration.core.util.AuditLogUtil;

@Service
public class ApplicationService {

	@Value("${version}")
	private String version;

	@Autowired
	DemographicService demographicService;

	@Autowired
	DocumentService documentService;

	@Autowired
	AuditLogUtil auditUtil;

	/**
	 * logger instance
	 */
	private Logger log = LoggerConfiguration.logConfig(ApplicationService.class);

	public MainResponseDTO<ApplicationInfoMetadataDTO> getPregistrationInfo(String prid) {
		log.info("In getPregistrationInfo method of Application service for prid {}", prid);
		MainResponseDTO<ApplicationInfoMetadataDTO> response = new MainResponseDTO<ApplicationInfoMetadataDTO>();
		response.setVersion(version);
		response.setResponsetime(DateUtils.getUTCCurrentDateTime().toString());
		ApplicationInfoMetadataDTO applicationInfo = new ApplicationInfoMetadataDTO();
		DocumentsMetaData documentsMetaData = null;
		DemographicResponseDTO demographicResponse = null;
		try {
			log.info("In getPregistrationInfo method of Application service fetching demographic for prid {}", prid);
			demographicResponse = demographicService.getDemographicData(prid.trim()).getResponse();
			applicationInfo.setDemographicResponse(demographicResponse);
			response.setResponse(applicationInfo);
			try {
				log.info("In getPregistrationInfo method of Application service fetching documents for prid {}", prid);
				documentsMetaData = documentService.getAllDocumentForPreId(prid.trim()).getResponse();
				applicationInfo.setDocumentsMetaData(documentsMetaData);
			} catch (PreRegistrationException | DocumentNotFoundException ex) {
				log.error("Exception occured while fetching documents for prid {}", prid);
				log.error("{}", ex);
				applicationInfo.setDocumentsMetaData(documentsMetaData);
			}
		} catch (Exception ex) {
			log.error("Exception occured while fetching demographic for prid {}", prid);
			log.error("{}", ex);
			new DemographicExceptionCatcher().handle(ex, response);
		}
		return response;
	}

	public MainResponseDTO<String> saveUIEventAudit(UIAuditRequest auditRequest) {
		log.info("In saveUIEventAudit method");
		MainResponseDTO<String> response = new MainResponseDTO<String>();
		response.setResponsetime(LocalDateTime.now().toString());
		response.setVersion(version);
		try {
			String description = auditRequest.getDescription();
			JsonNode node = new ObjectMapper().readTree(description);
			String template = node.get("template").toString();
			String hashedDescription = node.get("description").asText().concat("  ")
					.concat("Request_url :" + node.get("url").asText()).concat("  ")
					.concat("Consent_Text: " + HMACUtils2.digestAsPlainText(template.getBytes()));
			auditRequest.setDescription(hashedDescription);
			AuditRequestDto auditRequestDto = setAuditValues(auditRequest);
			log.info("In saveUIEventAudit method saving audit  details {}", auditRequestDto);
			auditUtil.saveAuditDetails(auditRequestDto);
			log.info("Request audit logged successfully");
			response.setResponse("Audit Logged Successfully");
		} catch (Exception ex) {
			log.error("Exception error occured while saving audit Request {}", ex);
			throw new AuditFailedException(ApplicationErrorCodes.PRG_APP_007.getCode(),
					ApplicationErrorMessages.AUDIT_FAILED.getMessage());
		}

		return response;
	}

	private AuditRequestDto setAuditValues(UIAuditRequest auditRequest) {
		AuditRequestDto auditRequestDto = new AuditRequestDto();
		auditRequestDto.setEventId(EventId.PRE_414.toString());
		auditRequestDto.setEventName(auditRequest.getEventName());
		auditRequestDto.setEventType(EventType.BUSINESS.toString());
		auditRequestDto.setDescription(auditRequest.getDescription());
		auditRequestDto.setId(AuditLogVariables.NO_ID.toString());
		auditRequestDto.setSessionUserId(auditRequest.getActionUserId());
		auditRequestDto.setSessionUserName(auditRequest.getActionUserId());
		auditRequestDto.setModuleId(auditRequest.getModuleId());
		auditRequestDto.setModuleName(auditRequest.getModuleName());
		auditRequestDto.setActionTimeStamp(LocalDateTime.parse(auditRequest.getActionTimeStamp()));
		return auditRequestDto;
	}

}
