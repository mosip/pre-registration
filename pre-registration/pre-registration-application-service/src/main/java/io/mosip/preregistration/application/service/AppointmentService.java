package io.mosip.preregistration.application.service;

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

public interface AppointmentService {

	MainResponseDTO<AvailabilityDto> getSlotAvailablity(String registrationCenterId);

	MainResponseDTO<BookingRegistrationDTO> getAppointmentDetails(String preRegistrationId);

	MainResponseDTO<BookingStatusDTO> makeAppointment(MainRequestDTO<BookingRequestDTO> bookingDTO,
			String preRegistrationId, String userAgent);

	MainResponseDTO<DeleteBookingDTO> deleteBooking(String preId);
	
	MainResponseDTO<DeleteBookingDTO> deleteBookingAndUpdateApplicationStatus(String preId);

	MainResponseDTO<CancelBookingResponseDTO> cancelAppointment(String preRegistrationId);

	MainResponseDTO<BookingStatus> makeMultiAppointment(MainRequestDTO<MultiBookingRequest> bookingRequest, String userAgent);

}
