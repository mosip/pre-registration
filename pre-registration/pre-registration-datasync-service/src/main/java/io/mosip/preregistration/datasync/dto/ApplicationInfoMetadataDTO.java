package io.mosip.preregistration.datasync.dto;

import io.mosip.preregistration.core.common.dto.DemographicResponseDTO;
import io.mosip.preregistration.core.common.dto.DocumentsMetaData;
import lombok.Data;

@Data
public class ApplicationInfoMetadataDTO {

	private DemographicResponseDTO demographicResponse;

	private DocumentsMetaData documentsMetaData;

}
