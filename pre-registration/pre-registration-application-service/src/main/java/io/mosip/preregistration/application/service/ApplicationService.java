package io.mosip.preregistration.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.preregistration.application.dto.ApplicationInfoMetadataDTO;
import io.mosip.preregistration.application.exception.DocumentNotFoundException;
import io.mosip.preregistration.application.exception.util.DemographicExceptionCatcher;
import io.mosip.preregistration.core.common.dto.DemographicResponseDTO;
import io.mosip.preregistration.core.common.dto.DocumentsMetaData;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.exception.PreRegistrationException;

@Service
public class ApplicationService {

	@Value("${version}")
	private String version;

	@Autowired
	DemographicService demographicService;

	@Autowired
	DocumentService documentService;

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

}
