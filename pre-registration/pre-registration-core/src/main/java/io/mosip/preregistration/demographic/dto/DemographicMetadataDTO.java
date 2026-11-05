package io.mosip.preregistration.demographic.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * List of preregistration response DTO
 * 
 * @author Jagadishwari
 * @since 1.0.0
 */

@Data
@NoArgsConstructor
@ToString
public class DemographicMetadataDTO implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 6126886610955736452L;

	/**
	 * basic details used by this Pre-Registration type.
	 */
	private List<DemographicViewDTO> basicDetails;

	/**
	 * total records used by this Pre-Registration type.
	 */
	private String totalRecords;

	/**
	 * no of records used by this Pre-Registration type.
	 */
	private String noOfRecords;

	/**
	 * page index used by this Pre-Registration type.
	 */
	private String pageIndex;
}
