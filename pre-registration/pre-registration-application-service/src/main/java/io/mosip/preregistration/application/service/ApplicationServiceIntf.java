package io.mosip.preregistration.application.service;

import java.util.List;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.preregistration.application.dto.ApplicationDetailResponseDTO;
import io.mosip.preregistration.application.dto.ApplicationInfoMetadataDTO;
import io.mosip.preregistration.application.dto.ApplicationRequestDTO;
import io.mosip.preregistration.application.dto.ApplicationResponseDTO;
import io.mosip.preregistration.application.dto.UIAuditRequest;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;

public interface ApplicationServiceIntf {
	
	AuthUserDetails authUserDetails();

	public MainResponseDTO<ApplicationInfoMetadataDTO> getPregistrationInfo(String prid);

	MainResponseDTO<String> saveUIEventAudit(UIAuditRequest auditRequest);

	MainResponseDTO<String> getApplicationsStatusForApplicationId(String applicationId);

	MainResponseDTO<List<ApplicationDetailResponseDTO>> getApplicationsForApplicationId(String regCenterId,
			String appointmentDate);

	/*
	 * This method is used to create the a new application with booking type as
	 * UPDATE_REGISTRATION_DETAILS or LOST_FORGOTTEN_UIN
	 * 
	 * 
	 * @param request pass application request
	 * 
	 * @param bookingType UPDATE_REGISTRATION_DETAILS or LOST_FORGOTTEN_UIN
	 * 
	 * @return MainResponseDTO<ApplicationResponseDTO>
	 */
	MainResponseDTO<ApplicationResponseDTO> addLostOrUpdateApplication(MainRequestDTO<ApplicationRequestDTO> request,
			String bookingType);

}
