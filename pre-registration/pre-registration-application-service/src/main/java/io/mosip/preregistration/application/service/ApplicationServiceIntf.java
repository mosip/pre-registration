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
	 * Gives application details for the given applicationId.
	 *
	 * <p>The returned entity is sanitised for external consumption: the ownership columns hold a
	 * canonical id or nothing at all, never the applicant's raw identifier. Callers that need the
	 * stored value — for example to recover a real contact address — must use
	 * {@link #getApplicationInfoInternal(String)} instead.
	 *
	 * @param applicationId
	 * @return
	 */
	MainResponseDTO<ApplicationEntity> getApplicationInfo(String applicationId);

	/**
	 * Gives application details with the ownership columns exactly as stored.
	 *
	 * <p><b>Never return this from a controller.</b> {@code cr_by}, {@code upd_by} and
	 * {@code contact_info} may still hold the applicant's raw email or phone on records the identity
	 * migration has not reached, which is precisely what {@link #getApplicationInfo(String)} strips.
	 * This exists for internal callers that need the stored value to do their job — notification
	 * recovery being the one that does.
	 *
	 * @param applicationId
	 * @return
	 */
	MainResponseDTO<ApplicationEntity> getApplicationInfoInternal(String applicationId);
	
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
	 * @param appointmentFromDate
	 * @param appointmentToDate
	 * @return
	 */
	MainResponseDTO<List<ApplicationDetailResponseDTO>> getBookingsForRegCenter(String regCenterId,
			String appointmentFromDate, String appointmentToDate);

	/**
	 * This method is used to create the a new application with booking type as
	 * UPDATE_REGISTRATION or LOST_FORGOTTEN_UIN
	 * 
	 * @param request
	 * @param bookingType
	 * @return MainResponseDTO<ApplicationResponseDTO>
	 */
	MainResponseDTO<ApplicationResponseDTO> addLostOrUpdateApplication(MainRequestDTO<ApplicationRequestDTO> request,
			String bookingType);

	/**
	 * This method is used to delete the application with booking type as
	 * UPDATE_REGISTRATION or LOST_FORGOTTEN_UIN
	 * 
	 * @param applicationId
	 * @param bookingType   UPDATE_REGISTRATION or LOST_FORGOTTEN_UIN
	 * @return MainResponseDTO<DeleteApplicationDTO>
	 */
	MainResponseDTO<DeleteApplicationDTO> deleteLostOrUpdateApplication(String applicationId, String bookingType);
}
