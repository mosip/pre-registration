package io.mosip.preregistration.application.service;

import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_ID;
import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_IDTYPE;
import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_SESSIONID;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import io.mosip.analytics.event.anonymous.exception.AnonymousProfileException;
import io.mosip.analytics.event.anonymous.util.AnonymousProfileUtil;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.application.errorcodes.ApplicationErrorCodes;
import io.mosip.preregistration.application.errorcodes.ApplicationErrorMessages;
import io.mosip.preregistration.application.errorcodes.AppointmentErrorCodes;
import io.mosip.preregistration.application.exception.AppointmentExecption;
import io.mosip.preregistration.application.exception.RecordNotFoundException;
import io.mosip.preregistration.application.repository.ApplicationRepostiory;
import io.mosip.preregistration.application.repository.DocumentDAO;
import io.mosip.preregistration.application.service.util.AppointmentUtil;
import io.mosip.preregistration.booking.dto.AvailabilityDto;
import io.mosip.preregistration.booking.dto.BookingRequestDTO;
import io.mosip.preregistration.booking.dto.BookingStatus;
import io.mosip.preregistration.booking.dto.BookingStatusDTO;
import io.mosip.preregistration.booking.dto.MultiBookingRequest;
import io.mosip.preregistration.core.code.BookingTypeCodes;
import io.mosip.preregistration.core.code.StatusCodes;
import io.mosip.preregistration.core.common.dto.BookingRegistrationDTO;
import io.mosip.preregistration.core.common.dto.BrowserInfoDTO;
import io.mosip.preregistration.core.common.dto.CancelBookingResponseDTO;
import io.mosip.preregistration.core.common.dto.DeleteBookingDTO;
import io.mosip.preregistration.core.common.dto.DemographicResponseDTO;
import io.mosip.preregistration.core.common.dto.DocumentsMetaData;
import io.mosip.preregistration.core.common.dto.ExceptionJSONInfoDTO;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.entity.ApplicationEntity;
import io.mosip.preregistration.core.config.LoggerConfiguration;

@Service
@RefreshScope
public class AppointmentServiceImpl implements AppointmentService {

	@Autowired
	private AppointmentUtil appointmentUtils;

	@Autowired
	private DemographicService demographicService;

	@Autowired
	private DocumentService documentService;

	/**
	 * Autowired reference for {@link #AnonymousProfileUtil}
	 */
	@Autowired
	AnonymousProfileUtil anonymousProfileUtil;

	@Value("${version}")
	private String version;

	@Value("${mosip.utc-datetime-pattern:yyyy-MM-dd'T'hh:mm:ss.SSS'Z'}")
	private String mosipDateTimeFormat;

	@Value("${mosip.preregistration.booking.fetch.availability.id}")
	private String availablityFetchId;

	@Value("${mosip.preregistration.booking.fetch.booking.id}")
	private String appointmentDetailsFetchId;

	@Value("${mosip.preregistration.booking.book.id}")
	private String appointmentBookId;

	@Value("${mosip.preregistration.booking.cancel.id}")
	private String appointmentCancelId;

	@Value("${mosip.preregistration.booking.delete.id}")
	private String appointmentDeletelId;

	@Autowired
	private ApplicationRepostiory applicationRepostiory;

	@Autowired
	private DocumentDAO documentDAO;

	public AuthUserDetails authUserDetails() {
		return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	private Logger log = LoggerConfiguration.logConfig(AppointmentServiceImpl.class);

	public MainResponseDTO<AvailabilityDto> getSlotAvailablity(String registrationCenterId) {
		MainResponseDTO<AvailabilityDto> availablityResponse = new MainResponseDTO<AvailabilityDto>();
		availablityResponse.setId(availablityFetchId);
		availablityResponse
				.setResponsetime(DateTimeFormatter.ofPattern(mosipDateTimeFormat).format(LocalDateTime.now()));
		availablityResponse.setVersion(version);
		try {
			log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "In appointment service to get slots availablity");
			availablityResponse.setResponse(appointmentUtils.getSlotAvailablityByRegCenterId(registrationCenterId));
		} catch (AppointmentExecption ex) {
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"Exception has occured while fetching the slots availablity for a regCenterId {"
							+ registrationCenterId + "} ex: " + ExceptionUtils.getStackTrace(ex));
			availablityResponse.setErrors(setErrors(ex));
		}

		return availablityResponse;
	}

	@Override
	public MainResponseDTO<BookingRegistrationDTO> getAppointmentDetails(String preRegistrationId) {
		MainResponseDTO<BookingRegistrationDTO> appointmentDetailsResponse = new MainResponseDTO<BookingRegistrationDTO>();
		appointmentDetailsResponse.setId(appointmentDetailsFetchId);
		appointmentDetailsResponse.setVersion(version);
		appointmentDetailsResponse
				.setResponsetime(DateTimeFormatter.ofPattern(mosipDateTimeFormat).format(LocalDateTime.now()));
		try {
			log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "In appointment service to get appointment details");
			// first check if the applicationId/preRegistrationId belongs to the logged in
			// user or not
			userValidation(preRegistrationId);
			BookingRegistrationDTO bookingrespose = appointmentUtils.fetchAppointmentDetails(preRegistrationId);
			appointmentDetailsResponse.setResponse(bookingrespose);

		} catch (AppointmentExecption ex) {
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"Exception has occurred while fetching appointment details:" + ExceptionUtils.getStackTrace(ex));
			appointmentDetailsResponse.setErrors(setErrors(ex));
		}
		return appointmentDetailsResponse;
	}

	private void userValidation(String applicationId) {
		this.applicationIdValidation(applicationId);
		String authUserId = authUserDetails().getUserId();
		List<String> list = listAuth(authUserDetails().getAuthorities());
		if (list.contains("ROLE_INDIVIDUAL")) {
			log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"In userValidation method of AppointmentService with applicationId " + applicationId
							+ " and userID " + authUserId);
			ApplicationEntity applicationEntity = null;
			try {
				applicationEntity = applicationRepostiory.findById(applicationId).orElseThrow();
			} catch (Exception ex) {
				log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
						"Invaid applicationId/Not Record Found for the ID : " + applicationId);
				throw new AppointmentExecption(ApplicationErrorCodes.PRG_APP_013.getCode(),
						ApplicationErrorMessages.NO_RECORD_FOUND.getMessage());
			}
			if (applicationEntity != null && !authUserId.trim().equals(applicationEntity.getCrBy().trim())) {
				throw new AppointmentExecption(AppointmentErrorCodes.INVALID_APP_ID_FOR_USER.getCode(),
						AppointmentErrorCodes.INVALID_APP_ID_FOR_USER.getMessage());
			}
		}
	}

	private void applicationIdValidation(String applicationId) {
		if (applicationId == null || applicationId.trim().isEmpty()) {
			throw new AppointmentExecption(
					ApplicationErrorCodes.PRG_APP_013.getCode(),
					"preRegistrationId cannot be empty."
			);
		}
	}

	/**
	 * This method is used to get the list of authorization role
	 * 
	 * @param collection
	 * @return list of auth role
	 */
	private List<String> listAuth(Collection<? extends GrantedAuthority> collection) {
		List<String> listWORole = new ArrayList<>();
		for (GrantedAuthority authority : collection) {
			String s = authority.getAuthority();
			listWORole.add(s);
		}
		return listWORole;
	}

	@Override
	public MainResponseDTO<BookingStatusDTO> makeAppointment(MainRequestDTO<BookingRequestDTO> bookingDTO,
			String preRegistrationId, String userAgent) {
		MainResponseDTO<BookingStatusDTO> bookAppointmentResponse = new MainResponseDTO<>();
		bookAppointmentResponse.setId(appointmentBookId);
		bookAppointmentResponse.setVersion(version);
		bookAppointmentResponse
				.setResponsetime(DateTimeFormatter.ofPattern(mosipDateTimeFormat).format(LocalDateTime.now()));
		try {
			log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"In appointment service to make an appointment for ID : " + preRegistrationId);
			// first check if the applicationId/preRegistrationId belongs to the logged in
			// user or not
			userValidation(preRegistrationId);
			BookingStatusDTO bookingResponse = appointmentUtils.makeAppointment(bookingDTO, preRegistrationId);
			if (bookingResponse.getBookingMessage() != null || !bookingResponse.getBookingMessage().isBlank()) {
				log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
						"In appointment booked successfully , updating the applications and demographic tables for ID: "
								+ preRegistrationId);
				ApplicationEntity applicationEntity = this.updateApplicationEntity(preRegistrationId,
						bookingDTO.getRequest(), StatusCodes.BOOKED.getCode());
				if (applicationEntity.getBookingType().equals(BookingTypeCodes.NEW_PREREGISTRATION.toString())) {
					createAnonymousProfile(userAgent, preRegistrationId, bookingDTO.getRequest());
					this.demographicService.updatePreRegistrationStatus(preRegistrationId, StatusCodes.BOOKED.getCode(),
							authUserDetails().getUserId());
				}
				bookAppointmentResponse.setResponse(bookingResponse);
			}
		} catch (AppointmentExecption ex) {
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"Exception has occurred while booking appointment : " + ExceptionUtils.getStackTrace(ex));
			bookAppointmentResponse.setErrors(setErrors(ex));
		}
		return bookAppointmentResponse;
	}

	@Override
	public MainResponseDTO<DeleteBookingDTO> deleteBooking(String preRegistrationId) {
		MainResponseDTO<DeleteBookingDTO> deleteResponse = new MainResponseDTO<DeleteBookingDTO>();
		deleteResponse.setId(appointmentDeletelId);
		deleteResponse.setVersion(version);
		deleteResponse.setResponsetime(DateTimeFormatter.ofPattern(mosipDateTimeFormat).format(LocalDateTime.now()));
		try {
			log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "Deleting appointment for ID: " + preRegistrationId);
			// first check if the applicationId/preRegistrationId belongs to the logged in
			// user or not
			userValidation(preRegistrationId);
			DeleteBookingDTO res = appointmentUtils.deleteBooking(preRegistrationId);
			if (res != null && (res.getDeletedBy() != null && res.getDeletedDateTime() != null
					&& res.getPreRegistrationId() != null)) {
				log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "In appointment deleted successfully for ID:{"
						+ preRegistrationId + "}, updating the applications and demographic tables");
				this.updateApplicationEntity(preRegistrationId, null, null);
				deleteResponse.setResponse(res);
			}

		} catch (AppointmentExecption ex) {
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"Exception has occured while deleting an appointment : " + ExceptionUtils.getStackTrace(ex));
			deleteResponse.setErrors(setErrors(ex));
		}
		return deleteResponse;
	}

	@Override
	public MainResponseDTO<DeleteBookingDTO> deleteBookingAndUpdateApplicationStatus(String preRegistrationId) {
		MainResponseDTO<DeleteBookingDTO> deleteResponse = new MainResponseDTO<>();
		deleteResponse.setId(appointmentDeletelId);
		deleteResponse.setVersion(version);
		deleteResponse.setResponsetime(DateTimeFormatter.ofPattern(mosipDateTimeFormat).format(LocalDateTime.now()));
		try {
			log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "Deleting appointment for ID: " + preRegistrationId);
			// first check if the applicationId/preRegistrationId belongs to the logged in
			// user or not
			userValidation(preRegistrationId);
			DeleteBookingDTO res = appointmentUtils.deleteBooking(preRegistrationId);
			if (res != null && (res.getDeletedBy() != null && res.getDeletedDateTime() != null
					&& res.getPreRegistrationId() != null)) {
				log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "In appointment deleted successfully for ID:{ "
						+ preRegistrationId + " }, updating the applications and demographic tables");
				ApplicationEntity applicationEntity = this.updateApplicationEntity(preRegistrationId, null,
						StatusCodes.CANCELLED.getCode());
				if (applicationEntity.getBookingType().equals(BookingTypeCodes.NEW_PREREGISTRATION.toString())) {
					this.demographicService.updatePreRegistrationStatus(preRegistrationId,
							StatusCodes.CANCELLED.getCode(), authUserDetails().getUserId());
				}
				deleteResponse.setResponse(res);
			}

		} catch (AppointmentExecption ex) {
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"Exception has occured while deleting an appointment : " + ExceptionUtils.getStackTrace(ex));
			deleteResponse.setErrors(setErrors(ex));
		}
		return deleteResponse;
	}

	@Override
	public MainResponseDTO<CancelBookingResponseDTO> cancelAppointment(String preRegistrationId) {
		MainResponseDTO<CancelBookingResponseDTO> cancelResponse = new MainResponseDTO<>();
		cancelResponse.setId(appointmentCancelId);
		cancelResponse.setVersion(version);
		cancelResponse.setResponsetime(DateTimeFormatter.ofPattern(mosipDateTimeFormat).format(LocalDateTime.now()));
		try {
			log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "Cancelling appointment for ID: " + preRegistrationId);
			// first check if the applicationId/preRegistrationId belongs to the logged in
			// user or not
			userValidation(preRegistrationId);
			CancelBookingResponseDTO response = appointmentUtils.cancelAppointment(preRegistrationId);
			if (response != null && (response.getMessage() != null && response.getTransactionId() != null)) {
				log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
						"In appointment cancelled successfully , updating the applications and demographic tables",
						preRegistrationId);
				ApplicationEntity applicationEntity = this.updateApplicationEntity(preRegistrationId, null,
						StatusCodes.CANCELLED.getCode());
				if (applicationEntity.getBookingType().equals(BookingTypeCodes.NEW_PREREGISTRATION.toString())) {
					this.demographicService.updatePreRegistrationStatus(preRegistrationId,
							StatusCodes.CANCELLED.getCode(), authUserDetails().getUserId());
				}
				cancelResponse.setResponse(response);
			}

		} catch (AppointmentExecption ex) {
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"Exception has occured while cancelling an appointment : " + ExceptionUtils.getStackTrace(ex));
			cancelResponse.setErrors(setErrors(ex));
		}

		return cancelResponse;
	}

	@Override
	public MainResponseDTO<BookingStatus> makeMultiAppointment(MainRequestDTO<MultiBookingRequest> bookingRequest,
			String userAgent) {
		MainResponseDTO<BookingStatus> multiBookingResponse = new MainResponseDTO<BookingStatus>();
		multiBookingResponse.setId(appointmentBookId);
		multiBookingResponse
				.setResponsetime(DateTimeFormatter.ofPattern(mosipDateTimeFormat).format(LocalDateTime.now()));
		multiBookingResponse.setVersion(version);
		try {
			// first check if the applicationId/preRegistrationId belongs to the logged in
			// user or not
			bookingRequest.getRequest().getBookingRequest().stream().forEach(action -> {
				String preRegistrationId = action.getPreRegistrationId();
				userValidation(preRegistrationId);
			});
			BookingStatus bookingStatus = appointmentUtils.multiAppointmentBooking(bookingRequest);
			if (bookingStatus != null && !bookingStatus.getBookingStatusResponse().isEmpty()) {

				bookingRequest.getRequest().getBookingRequest().stream().forEach(action -> {
					String preRegistrationId = action.getPreRegistrationId();
					BookingRequestDTO bookRequest = new BookingRequestDTO();
					bookRequest.setRegDate(action.getRegDate());
					bookRequest.setRegistrationCenterId(action.getRegistrationCenterId());
					bookRequest.setSlotToTime(action.getSlotToTime());
					bookRequest.setSlotFromTime(action.getSlotFromTime());
					ApplicationEntity applicationEntity = this.updateApplicationEntity(preRegistrationId, bookRequest,
							StatusCodes.BOOKED.getCode());
					if (applicationEntity.getBookingType().equals(BookingTypeCodes.NEW_PREREGISTRATION.toString())) {
						createAnonymousProfile(userAgent, preRegistrationId, bookRequest);
						this.demographicService.updatePreRegistrationStatus(preRegistrationId,
								StatusCodes.BOOKED.getCode(), authUserDetails().getUserId());
					}
				});
			}
			multiBookingResponse.setResponse(bookingStatus);
		} catch (AppointmentExecption ex) {
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"Exception has occured while booking an appointment : " + ExceptionUtils.getStackTrace(ex));
			multiBookingResponse.setErrors(setErrors(ex));
		}
		return multiBookingResponse;
	}

	private void createAnonymousProfile(String userAgent, String preRegistrationId, BookingRequestDTO bookRequest) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "In createAnonymousProfile()");
		try {
			// get the demographic data, documents data, booking data to create anonymous
			// profile
			BrowserInfoDTO browserInfo = new BrowserInfoDTO();
			browserInfo.setBrowserName(userAgent);
			DemographicResponseDTO demographicData = demographicService.getDemographicData(preRegistrationId)
					.getResponse();
			DocumentsMetaData documentsData = null;
			boolean documentExists = documentDAO.existsByPreregId(preRegistrationId);
			if (documentExists) {
				documentsData = documentService.getAllDocumentForPreId(preRegistrationId).getResponse();
			}
			BookingRegistrationDTO bookingData = new BookingRegistrationDTO();
			bookingData.setRegistrationCenterId(bookRequest.getRegistrationCenterId());
			bookingData.setRegDate(bookRequest.getRegDate());
			bookingData.setSlotFromTime(bookRequest.getSlotFromTime());
			bookingData.setSlotToTime(bookRequest.getSlotToTime());
			log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"In createAnonymousProfile() Status of application: " + demographicData.getStatusCode());
			// insert the anonymous profile only if the appointment is being booked for the
			// only for the first time
			if (demographicData != null
					&& demographicData.getStatusCode().equals(StatusCodes.PENDING_APPOINTMENT.getCode())) {
				// set the status as Booked to be saved in the Anonymous Profile
				demographicData.setStatusCode(StatusCodes.BOOKED.getCode());
				anonymousProfileUtil.saveAnonymousProfile(demographicData, documentsData, bookingData, browserInfo);
			}
		} catch (AnonymousProfileException apex) {
			log.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, ExceptionUtils.getStackTrace(apex));
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"Unable to save AnonymousProfile in getPreRegistrationData method of datasync service -"
							+ apex.getMessage());
		}
	}

	private ApplicationEntity updateApplicationEntity(String preRegistrationId, BookingRequestDTO bookingInfo,
			String newStatus) {
		ApplicationEntity applicationEntity = null;
		try {
			applicationEntity = applicationRepostiory.getOne(preRegistrationId);
		} catch (Exception ex) {
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"Invaid applicationId/Not Record Found for the ID " + preRegistrationId);
			throw new RecordNotFoundException(ApplicationErrorCodes.PRG_APP_014.getCode(),
					ApplicationErrorMessages.INVALID_REQUEST_APPLICATION_ID.getMessage());
		}

		if (bookingInfo == null) {
			applicationEntity.setAppointmentDate(null);
			applicationEntity.setBookingDate(null);
			applicationEntity.setSlotFromTime(null);
			applicationEntity.setSlotToTime(null);
			applicationEntity.setRegistrationCenterId(null);
			if ((applicationEntity.getBookingType().equals(BookingTypeCodes.LOST_FORGOTTEN_UIN.toString())
					|| applicationEntity.getBookingType().equals(BookingTypeCodes.UPDATE_REGISTRATION.toString()))
					&& newStatus != null) {
				applicationEntity.setBookingStatusCode(newStatus);
			}
		} else {
			applicationEntity.setAppointmentDate(LocalDate.parse(bookingInfo.getRegDate()));
			applicationEntity.setBookingDate(LocalDate.now());
			applicationEntity.setSlotFromTime(
					LocalTime.parse(bookingInfo.getSlotFromTime(), DateTimeFormatter.ofPattern("H:mm:ss")));
			applicationEntity.setSlotToTime(
					LocalTime.parse(bookingInfo.getSlotToTime(), DateTimeFormatter.ofPattern("H:mm:ss")));
			applicationEntity.setRegistrationCenterId(bookingInfo.getRegistrationCenterId());
			if ((applicationEntity.getBookingType().equals(BookingTypeCodes.LOST_FORGOTTEN_UIN.toString())
					|| applicationEntity.getBookingType().equals(BookingTypeCodes.UPDATE_REGISTRATION.toString()))
					&& newStatus != null) {
				applicationEntity.setBookingStatusCode(newStatus);
			}
		}
		applicationEntity.setUpdBy(authUserDetails().getUserId());
		applicationEntity.setCrDtime(LocalDateTime.now(ZoneId.of("UTC")));
		try {
			return applicationRepostiory.save(applicationEntity);
		} catch (Exception ex) {
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"Failed to update application for the preregistrationId: " + preRegistrationId);
			throw new AppointmentExecption(AppointmentErrorCodes.FAILED_TO_UPDATE_APPLICATIONS.getCode(),
					String.format(AppointmentErrorCodes.FAILED_TO_UPDATE_APPLICATIONS.getMessage(), preRegistrationId));
		}
	}

	private List<ExceptionJSONInfoDTO> setErrors(AppointmentExecption ex) {
		List<ExceptionJSONInfoDTO> explist = new ArrayList<>();
		ExceptionJSONInfoDTO exception = new ExceptionJSONInfoDTO();
		exception.setErrorCode(ex.getErrorCode());
		exception.setMessage(ex.getErrorMessage());
		explist.add(exception);
		return explist;
	}
}