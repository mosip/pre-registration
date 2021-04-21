package io.mosip.preregistration.application.dto;

import io.mosip.preregistration.core.common.dto.DemographicResponseDTO;
import io.mosip.preregistration.core.common.dto.DocumentsMetaData;
import lombok.Data;

@Data
public class ApplicationInfoMetadataDTO {

	private DemographicResponseDTO demographicResponse;

	private DocumentsMetaData documentsMetaData;

}
