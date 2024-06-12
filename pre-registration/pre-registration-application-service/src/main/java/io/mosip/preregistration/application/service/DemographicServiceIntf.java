package io.mosip.preregistration.application.service;

import java.util.Map;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.preregistration.application.dto.ApplicationInfoMetadataDTO;
import io.mosip.preregistration.application.dto.DeletePreRegistartionDTO;
import io.mosip.preregistration.application.dto.DemographicCreateResponseDTO;
import io.mosip.preregistration.application.dto.DemographicMetadataDTO;
import io.mosip.preregistration.application.dto.DemographicRequestDTO;
import io.mosip.preregistration.application.dto.DemographicUpdateResponseDTO;
import io.mosip.preregistration.core.common.dto.DemographicResponseDTO;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.dto.PreRegIdsByRegCenterIdDTO;
import io.mosip.preregistration.core.common.dto.PreRegistartionStatusDTO;

public interface DemographicServiceIntf {

	AuthUserDetails authUserDetails();

	/*
	 * This method is used to create the demographic data by generating the unique
	 * PreId
	 * 
	 * 
	 * @param request pass demographic request
	 * 
	 * @return MainResponseDTO<DemographicCreateResponseDTO>
	 */
	MainResponseDTO<DemographicCreateResponseDTO> addPreRegistration(MainRequestDTO<DemographicRequestDTO> request);

	/*
	 * This method is used to update the demographic data by PreId
	 * 
	 * @see
	 * io.mosip.registration.service.RegistrationService#addPreRegistration(java.
	 * lang.Object, java.lang.String)
	 * 
	 * @param demographicRequest pass demographic request
	 * 
	 * @return responseDTO
	 */
	MainResponseDTO<DemographicUpdateResponseDTO> updatePreRegistration(MainRequestDTO<DemographicRequestDTO> request,
			String preRegistrationId, String userId);

	/**
	 * This Method is used to fetch all the applications created by User
	 * 
	 * @param userId pass a userId through which user has logged in which can be
	 *               either email Id or phone number
	 * @return List of groupIds
	 * 
	 */
	MainResponseDTO<DemographicMetadataDTO> getAllApplicationDetails(String userId, String pageIdx);

	/**
	 * This Method is used to fetch status of particular preId
	 * 
	 * @param preRegId pass preRegId of the user
	 * @return response status of the preRegId
	 * 
	 * 
	 */
	MainResponseDTO<PreRegistartionStatusDTO> getApplicationStatus(String preRegId, String userId);

	/**
	 * This Method is used to delete the Individual Application and documents
	 * associated with it
	 * 
	 * @param preregId pass the preregId of individual
	 * @return response
	 * 
	 */
	MainResponseDTO<DeletePreRegistartionDTO> deleteIndividual(String preregId, String userId);
	
	/**
	 * This Method is used to retrieve the demographic
	 * 
	 * @param preRegId pass the preregId
	 * @return response DemographicData of preRegId
	 */
	MainResponseDTO<DemographicResponseDTO> getDemographicData(String preRegId);

	/**
	 * This Method is used to update status of particular preId
	 * 
	 * @param preRegId pass the preregId of individual
	 * @param status   pass the status of individual
	 * @return response
	 * 
	 * 
	 */
	MainResponseDTO<String> updatePreRegistrationStatus(String preRegId, String status, String userId);

	MainResponseDTO<Map<String, String>> getUpdatedDateTimeForPreIds(
			PreRegIdsByRegCenterIdDTO preRegIdsByRegCenterIdDTO);

	
	public MainResponseDTO<ApplicationInfoMetadataDTO> getPregistrationInfo(String prid);


	/**
	 * This method acts as a post constructor to initialize the required request
	 * parameters.
	 */
	void setup();

}