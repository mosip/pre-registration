package io.mosip.preregistration.datasync.dto;

import io.mosip.preregistration.core.common.dto.DemographicResponseDTO;
import io.mosip.preregistration.core.common.dto.DocumentsMetaData;
import lombok.Data;

@Data
/**
 * Data transfer object carrying application info metadata across Pre-Registration APIs.
 */
public class ApplicationInfoMetadataDTO {

	/**
	 * demographic response used by this Pre-Registration type.
	 */
	private DemographicResponseDTO demographicResponse;

	/**
	 * documents meta data used by this Pre-Registration type.
	 */
	private DocumentsMetaData documentsMetaData;

}
