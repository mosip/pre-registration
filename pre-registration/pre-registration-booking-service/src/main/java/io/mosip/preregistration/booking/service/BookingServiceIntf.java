package io.mosip.preregistration.booking.service;


import org.springframework.stereotype.Service;

import io.mosip.preregistration.booking.dto.AvailabilityDto;
import io.mosip.preregistration.booking.dto.BookingRequestDTO;
import io.mosip.preregistration.booking.dto.BookingStatus;
import io.mosip.preregistration.booking.dto.BookingStatusDTO;
import io.mosip.preregistration.booking.dto.MultiBookingRequest;
import io.mosip.preregistration.core.common.dto.BookingRegistrationDTO;
import io.mosip.preregistration.core.common.dto.CancelBookingResponseDTO;
import io.mosip.preregistration.core.common.dto.DeleteBookingDTO;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.dto.PreRegIdsByRegCenterIdResponseDTO;

@Service
public interface BookingServiceIntf {

	/**
	 * Gives the availability details based on registrationId
	 * 
	 * @param regID pass the RegistartionId
	 * @return AvailabilityDto return the registration details
	 */
	MainResponseDTO<AvailabilityDto> getAvailability(String regID);


	/**
	 * This method use to book the appointment.
	 * 
	 * @param bookingRequestDTO pass the booking details
	 * @return BookingStatusDTO response with booking status code 
	 */
	MainResponseDTO<BookingStatusDTO> bookAppointment(MainRequestDTO<BookingRequestDTO> bookingRequestDTOs,
			String preRegistrationId);

	/**
	 * This method will book the multiple appointments.
	 * 
	 * @param bookingRequestDTOs pass the list of booking details
	 * @return BookingStatus response with list of booking status code
	 */
	MainResponseDTO<BookingStatus> bookMultiAppointment(MainRequestDTO<MultiBookingRequest> bookingRequestDTOs);

	/**
	 * This method is for getting appointment details for given preRegistrationId.
	 * 
	 * @param preRegID pass the preRegId
	 * @return BookingRegistrationDTO return the appointment details for given preRegId
	 */
	MainResponseDTO<BookingRegistrationDTO> getAppointmentDetails(String preRegID);

	/**
	 * This method will cancel the appointment based on the preRegistrationId.
	 * 
	 * @param preRegistrationId pass the preRegistrationId
	 * @return CancelBookingResponseDTO return the status code for cancel booking 
	 */
	MainResponseDTO<CancelBookingResponseDTO> cancelAppointment(String preRegistrationId);

	/**
	 * This method will cancel the appointment using batch.
	 *
	 * @param preRegistrationId pass the preRegistartionId
	 * @return CancelBookingResponseDTO return the status code for cancel booking 
	 */
	MainResponseDTO<CancelBookingResponseDTO> cancelAppointmentBatch(String preRegistrationId);

	/**
	 * 
	 * This booking API will be called by bookAppointment method.
	 * 
	 * @param preRegistrationId pass the peRegistartionId
	 * @param bookingRequestDTO pass the booking details
	 * @return BookingStatusDTO return the status code
	 */
	BookingStatusDTO book(String preRegistrationId, BookingRequestDTO bookingRequestDTO);

	/**
	 * This cancel API will be called by cancelAppointment method.
	 * 
	 * @param preRegistrationId pass the preRegistrationId 
	 * @param isBatchUser   
	 * @return CancelBookingResponseDTO response with status code
	 */
	CancelBookingResponseDTO cancelBooking(String preRegistrationId, boolean isBatchUser);

	/**
	 * This Method is used to delete the Individual Application and documents
	 * associated with it
	 * 
	 * @param preregId
	 *            pass the preregId of individual
	 * @return response
	 * 
	 */
	MainResponseDTO<DeleteBookingDTO> deleteBooking(String preregId);

	/**
	 * This Method is used to check the slot availability
	 * 
	 * @param bookingRequestDTO
	 *            pass the booking details
	 * 
	 */
	void checkSlotAvailability(BookingRequestDTO bookingRequestDTO);

	/**
	 * This Method is used to delete the old booking
	 * 
	 * @param preId
	 *            pass the PreRegistrationId
	 * 
	 */
	boolean deleteOldBooking(String preId);

	/**
	 * This Method is used to update the availability after cancel of booking
	 * 
	 * @param oldBooking
	 *            pass the old Booking details
	 * 
	 */
	boolean increaseAvailability(BookingRequestDTO oldBooking);

	/**
	 * This method is used to audit all the booking events
	 * 
	 * @param eventId
	 * @param eventName
	 * @param eventType
	 * @param description
	 * @param idType
	 */
	void setAuditValues(String eventId, String eventName, String eventType, String description, String idType,
			String userId, String userName, String ref_id);

	/**
	 * This Method is used to retrieve booked PreIds by date and regCenterId**
	 * 
	 * @param fromDate
	 *            pass fromDate*
	 * @param toDate
	 *            pass toDate*
	 * @param regCenterId        
	 * @return MainResponseDTO<PreRegIdsByRegCenterIdResponseDTO>    
	 ***/

	MainResponseDTO<PreRegIdsByRegCenterIdResponseDTO> getBookedPreRegistrationByDate(String fromDateStr,
			String toDateStr, String regCenterId);

}