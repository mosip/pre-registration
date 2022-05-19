package io.mosip.preregistration.application.service;

import java.util.List;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.preregistration.application.dto.ApplicationDetailResponseDTO;
import io.mosip.preregistration.application.dto.ApplicationRequestDTO;
import io.mosip.preregistration.application.dto.ApplicationResponseDTO;
import io.mosip.preregistration.application.dto.ApplicationsListDTO;
import io.mosip.preregistration.application.dto.DeleteApplicationDTO;
import io.mosip.preregistration.application.dto.UIAuditRequest;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.entity.ApplicationEntity;

public interface ApplicationServiceIntf {

	AuthUserDetails authUserDetails();

	MainResponseDTO<String> saveUIEventAudit(UIAuditRequest auditRequest);

	/**
	 * Gives application details for the given applicationId
	 * 
	 * @param applicationId
	 * @return
	 */
	MainResponseDTO<ApplicationEntity> getApplicationInfo(String applicationId);
	
	/**
	 * This Method is used to fetch status of particular application
	 * 
	 *  @param applicationId
	 * @return response status of the application
	 */
	MainResponseDTO<String> getApplicationStatus(String applicationId);

	/**
	 * Gives all the application details for the logged in user.
	 * 
	 * @return
	 */
	MainResponseDTO<ApplicationsListDTO> getAllApplicationsForUser();
	
	/**
	 * Gives all the application details for the logged in user for the given type.
	 * 
	 * @return
	 */
	MainResponseDTO<ApplicationsListDTO> getAllApplicationsForUserForBookingType(String type);

	/**
	 * Get all bookings for the given regCenterId in the given appointmentDate
	 * 
	 * @param regCenterId
	 * @param appointmentDate
	 * @return
	 */
	MainResponseDTO<List<ApplicationDetailResponseDTO>> getBookingsForRegCenter(String regCenterId,
			String appointmentDate);

	/**
	 * This method is used to create the a new application with booking type as
	 * UPDATE_REGISTRATION or LOST_FORGOTTEN_UIN or MISCELLANEOUS_PURPOSE
	 * 
	 * @param request
	 * @param bookingType
	 * @return MainResponseDTO<ApplicationResponseDTO>
	 */
	MainResponseDTO<ApplicationResponseDTO> addLostOrUpdateOrMiscellaneousApplication(MainRequestDTO<? extends ApplicationRequestDTO> request,
			String bookingType);

	/**
	 * This method is used to delete the application with booking type as
	 * UPDATE_REGISTRATION or LOST_FORGOTTEN_UIN or MISCELLANEOUS_PURPOSE
	 * 
	 * @param applicationId
	 * @param bookingType   UPDATE_REGISTRATION or LOST_FORGOTTEN_UIN or MISCELLANEOUS_PURPOSE
	 * @return MainResponseDTO<DeleteApplicationDTO>
	 */
	MainResponseDTO<DeleteApplicationDTO> deleteLostOrUpdateOrMiscellaneousApplication(String applicationId, String bookingType);
}
