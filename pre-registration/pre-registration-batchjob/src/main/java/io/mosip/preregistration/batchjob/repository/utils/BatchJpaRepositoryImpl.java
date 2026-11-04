package io.mosip.preregistration.batchjob.repository.utils;

import static io.mosip.preregistration.core.constant.PreRegCoreConstant.LOGGER_SESSIONID;
import static io.mosip.preregistration.core.constant.PreRegCoreConstant.LOGGER_IDTYPE;
import static io.mosip.preregistration.core.constant.PreRegCoreConstant.LOGGER_ID;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.batchjob.code.ErrorCodes;
import io.mosip.preregistration.batchjob.code.ErrorMessages;
import io.mosip.preregistration.batchjob.entity.AvailibityEntity;
import io.mosip.preregistration.batchjob.entity.DemographicEntityConsumed;
import io.mosip.preregistration.batchjob.entity.DocumentEntityConsumed;
import io.mosip.preregistration.batchjob.entity.ProcessedPreRegEntity;
import io.mosip.preregistration.batchjob.entity.RegistrationBookingEntityConsumed;
import io.mosip.preregistration.batchjob.exception.NoPreIdAvailableException;
import io.mosip.preregistration.batchjob.repository.ApplicationRepository;
import io.mosip.preregistration.batchjob.repository.AvailabilityRepository;
import io.mosip.preregistration.batchjob.repository.DemographicConsumedRepository;
import io.mosip.preregistration.batchjob.repository.DemographicRepository;
import io.mosip.preregistration.batchjob.repository.DocumentConsumedRepository;
import io.mosip.preregistration.batchjob.repository.DocumentRespository;
import io.mosip.preregistration.batchjob.repository.ProcessedPreIdRepository;
import io.mosip.preregistration.batchjob.repository.RegAppointmentConsumedRepository;
import io.mosip.preregistration.batchjob.repository.RegAppointmentRepository;
import io.mosip.preregistration.core.code.StatusCodes;
import io.mosip.preregistration.core.common.entity.ApplicationEntity;
import io.mosip.preregistration.core.common.entity.DemographicEntity;
import io.mosip.preregistration.core.common.entity.DocumentEntity;
import io.mosip.preregistration.core.common.entity.RegistrationBookingEntity;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.exception.TableNotAccessibleException;

@Component
/**
 * Class supporting batch jpa repository impl in MOSIP Pre-Registration (io.mosip.preregistration.batchjob.repository.utils).
 */
public class BatchJpaRepositoryImpl {

	/** The Constant LOGGER. */
	private Logger log = LoggerConfiguration.logConfig(BatchJpaRepositoryImpl.class);

	/**
	 * Autowired reference for {@link #demographicRepository}
	 */
	@Autowired
	@Qualifier("demographicRepository")
	private DemographicRepository demographicRepository;

	@Autowired
	@Qualifier("applicationRepository")
	/**
	 * application repository used by this Pre-Registration type.
	 */
	private ApplicationRepository applicationRepository;

	/** Autowired reference for {@link #bookingRepository}. */
	@Autowired
	@Qualifier("availabilityRepository")
	private AvailabilityRepository availabilityRepository;

	/**
	 * Autowired reference for {@link #demographicConsumedRepository}
	 */
	@Autowired
	@Qualifier("demographicConsumedRepository")
	private DemographicConsumedRepository demographicConsumedRepository;

	/**
	 * Autowired reference for {@link #regAppointmentRepository}
	 */
	@Autowired
	@Qualifier("regAppointmentRepository")
	private RegAppointmentRepository regAppointmentRepository;

	/**
	 * Autowired reference for {@link #processedPreIdRepository}
	 */
	@Autowired
	@Qualifier("processedPreIdRepository")
	private ProcessedPreIdRepository processedPreIdRepository;

	/**
	 * Autowired reference for {@link #appointmentConsumedRepository}
	 */
	@Autowired
	@Qualifier("regAppointmentConsumedRepository")
	private RegAppointmentConsumedRepository appointmentConsumedRepository;

	/**
	 * Autowired reference for {@link #documentRespository}
	 */
	@Autowired
	@Qualifier("documentRespository")
	private DocumentRespository documentRespository;

	/**
	 * Autowired reference for {@link #documentConsumedRepository}
	 */
	@Autowired
	@Qualifier("documentConsumedRepository")
	private DocumentConsumedRepository documentConsumedRepository;

	/**
	 * @param preRegId
	 * @return Demographic details for preregId
	 */
	public DemographicEntity getApplicantDemographicDetails(String preRegId) {

		DemographicEntity entity = null;
		try {
			entity = demographicRepository.findBypreRegistrationId(preRegId);
			if (entity == null) {
				processedPreIdRepository.deleteBypreRegistrationId(preRegId);
				log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "Deleted Invalid Pre-Registration ID");
			}
			demographicRepository.flush();
		} catch (DataAccessLayerException e) {
			throw new TableNotAccessibleException(ErrorCodes.PRG_PAM_BAT_004.getCode(),
					ErrorMessages.DEMOGRAPHIC_TABLE_NOT_ACCESSIBLE.getMessage());
		}
		return entity;
	}

	/**
	 * Returns applicant demographic object.
	 *
	 * @param preRegId pre reg id used by get applicant demographic object
	 * @return get applicant demographic object result
	 */
	public DemographicEntity getApplicantDemographicObject(String preRegId) {
		try {
			DemographicEntity entity = demographicRepository.findBypreRegistrationId(preRegId);
			return entity;
		} catch (DataAccessLayerException e) {
			throw new TableNotAccessibleException(ErrorCodes.PRG_PAM_BAT_004.getCode(),
					ErrorMessages.DEMOGRAPHIC_TABLE_NOT_ACCESSIBLE.getMessage());
		}
	}

	/**
	 * Performs delete invalid processed pre reg for MOSIP Pre-Registration.
	 *
	 * @param preRegId pre reg id used by delete invalid processed pre reg
	 * @return delete invalid processed pre reg result
	 */
	public int deleteInvalidProcessedPreReg(String preRegId) {
		try {
			return processedPreIdRepository.deleteBypreRegistrationId(preRegId);
		} catch (DataAccessLayerException e) {
			throw new TableNotAccessibleException(ErrorCodes.PRG_PAM_BAT_004.getCode(),
					ErrorMessages.DEMOGRAPHIC_TABLE_NOT_ACCESSIBLE.getMessage());
		}
	}

	/**
	 * Returns applicant entity details.
	 *
	 * @param applicationID application id used by get applicant entity details
	 * @return get applicant entity details result
	 */
	public ApplicationEntity getApplicantEntityDetails(String applicationID) {

		ApplicationEntity entity = null;
		try {
			entity = applicationRepository.findByApplicationId(applicationID);
			if (entity == null) {
				throw new NoPreIdAvailableException(ErrorCodes.PRG_PAM_BAT_001.getCode(),
						ErrorMessages.NO_PRE_REGISTRATION_ID_FOUND_TO_UPDATE_STATUS.getMessage());

			}

		} catch (DataAccessLayerException e) {
			throw new NoPreIdAvailableException(ErrorCodes.PRG_PAM_BAT_001.getCode(),
					ErrorMessages.NO_PRE_REGISTRATION_ID_FOUND_TO_UPDATE_STATUS.getMessage());
		}
		return entity;
	}

	/**
	 * Returns booked applicant entity details.
	 *
	 * @param applicationID application id used by get booked applicant entity details
	 * @return get booked applicant entity details result
	 */
	public ApplicationEntity getBookedApplicantEntityDetails(String applicationID) {

		ApplicationEntity entity = null;
		try {
			entity = applicationRepository.findByApplicationIdAndBookingStatusCode(applicationID,
					StatusCodes.BOOKED.getCode());
		} catch (DataAccessLayerException e) {
			throw new NoPreIdAvailableException(ErrorCodes.PRG_PAM_BAT_001.getCode(),
					ErrorMessages.NO_PRE_REGISTRATION_ID_FOUND_TO_UPDATE_STATUS.getMessage());
		}
		return entity;
	}

	/**
	 * Returns registration appointment details.
	 *
	 * @param applicationID application id used by get registration appointment details
	 * @return get registration appointment details result
	 */
	public RegistrationBookingEntity getRegistrationAppointmentDetails(String applicationID) {

		RegistrationBookingEntity entity = null;
		try {
			entity = regAppointmentRepository.getRegistrationAppointmentByPreRegistrationId(applicationID);
			if (entity == null) {
				throw new NoPreIdAvailableException(ErrorCodes.PRG_PAM_BAT_001.getCode(),
						ErrorMessages.BOOKING_DATA_NOT_FOUND.getMessage());

			}

		} catch (DataAccessLayerException e) {
			throw new NoPreIdAvailableException(ErrorCodes.PRG_PAM_BAT_001.getCode(),
					ErrorMessages.BOOKING_DATA_NOT_FOUND.getMessage());
		}
		return entity;
	}

	/**
	 * Returns all registration appointment details.
	 *
	 * @param bookingDate booking date used by get all registration appointment details
	 * @return get all registration appointment details result
	 */
	public List<RegistrationBookingEntity> getAllRegistrationAppointmentDetails(LocalDate bookingDate) {
		try {
			List<RegistrationBookingEntity> entity = regAppointmentRepository.findByBookingPKBookingDate(bookingDate);
			return entity;
		} catch (DataAccessLayerException e) {
			throw new NoPreIdAvailableException(ErrorCodes.PRG_PAM_BAT_001.getCode(),
					ErrorMessages.BOOKING_DATA_NOT_FOUND.getMessage());
		}
	}

	/**
	 * @param statusComment
	 * @return List of ProcessedPreRegEntity for given statusComment
	 */
	public List<ProcessedPreRegEntity> getAllConsumedPreIds(String statusComment) {
		List<ProcessedPreRegEntity> entityList = null;
		try {
			entityList = processedPreIdRepository.findBystatusComments(statusComment);
			if (entityList == null || entityList.isEmpty()) {
				log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
						"There are currently no Pre-Registration-Ids to update status to consumed");
				throw new NoPreIdAvailableException(ErrorCodes.PRG_PAM_BAT_001.getCode(),
						ErrorMessages.NO_PRE_REGISTRATION_ID_FOUND_TO_UPDATE_STATUS.getMessage());
			}

		} catch (DataAccessLayerException e) {
			throw new TableNotAccessibleException(ErrorCodes.PRG_PAM_BAT_006.getCode(),
					ErrorMessages.PROCESSED_PREREG_LIST_TABLE_NOT_ACCESSIBLE.getMessage());
		}
		return entityList;
	}

	/**
	 * @param currentdate
	 * @return List of RegistrationBookingEntity based date less then currentDate
	 */
	public List<RegistrationBookingEntity> getAllOldDateBooking() {
		try {
			List<RegistrationBookingEntity> entityList = regAppointmentRepository
					.findByRegDateBetween(StatusCodes.BOOKED.getCode(), LocalDate.now());
			return entityList;
		} catch (DataAccessLayerException e) {
			throw new TableNotAccessibleException(ErrorCodes.PRG_PAM_BAT_005.getCode(),
					ErrorMessages.REG_APPOINTMENT_TABLE_NOT_ACCESSIBLE.getMessage());
		}
	}

	/**
	 * @param applicantDemographic
	 * @return
	 * @return updated demographic details.
	 */
	public DemographicEntity updateApplicantDemographic(DemographicEntity demographicEntity) {
		return demographicRepository.save(demographicEntity);

	}

	/**
	 * Performs update applicant entity for MOSIP Pre-Registration.
	 *
	 * @param applicantEntity applicant entity used by update applicant entity
	 * @return update applicant entity result
	 */
	public ApplicationEntity updateApplicantEntity(ApplicationEntity applicantEntity) {
		return applicationRepository.save(applicantEntity);
	}

	/**
	 * @param entity
	 * @return updated ProcessedPreRegEntity.
	 */
	public ProcessedPreRegEntity updateProcessedList(ProcessedPreRegEntity entity) {
		return processedPreIdRepository.save(entity);
	}

	/** Deleting demographic the consumed demographic data. */
	public void deleteDemographic(DemographicEntity demographicEntity) {
		try {
			demographicRepository.delete(demographicEntity);
			log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"In deleteDemographic to delete consumed demographic details");

		} catch (DataAccessLayerException e) {
			throw new TableNotAccessibleException(ErrorCodes.PRG_PAM_BAT_004.getCode(),
					ErrorMessages.DEMOGRAPHIC_TABLE_NOT_ACCESSIBLE.getMessage());
		}
	}

	/** Deleting document details the consumed demographic data. */
	public void deleteDocument(List<DocumentEntity> documentEntity) {
		try {
			documentEntity.forEach(iterate -> {
				documentRespository.delete(iterate);
				log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
						"In deleteDocument to delete consumed demographic details");
			});

		} catch (Exception e) {
			throw new TableNotAccessibleException(ErrorCodes.PRG_PAM_BAT_007.getCode(),
					ErrorMessages.DOCUMENT_TABLE_NOT_ACCESSIBLE.getMessage());
		}

	}

	/**
	 * Performs delete applicant document for MOSIP Pre-Registration.
	 *
	 * @param documentEntity document entity used by delete applicant document
	 */
	public void deleteApplicantDocument(DocumentEntity documentEntity) {
		try {
			documentRespository.delete(documentEntity);
		} catch (Exception e) {
			throw new TableNotAccessibleException(ErrorCodes.PRG_PAM_BAT_007.getCode(),
					ErrorMessages.DOCUMENT_TABLE_NOT_ACCESSIBLE.getMessage());
		}
	}

	/** Deleting Booking details the consumed demographic data. */
	public void deleteBooking(RegistrationBookingEntity bookingEntity) {
		try {
			regAppointmentRepository.delete(bookingEntity);
			log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"In deleteBooking to delete consumed demographic details");
		} catch (DataAccessLayerException e) {
			throw new TableNotAccessibleException(ErrorCodes.PRG_PAM_BAT_005.getCode(),
					ErrorMessages.REG_APPOINTMENT_TABLE_NOT_ACCESSIBLE.getMessage());
		}
	}

	/** Deleting Booking details the consumed demographic data. */
	public void deleteApplications(ApplicationEntity applicationEntity) {
		try {
			applicationRepository.delete(applicationEntity);
			log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"In deleteapplications to delete consumed demographic details");
		} catch (DataAccessLayerException e) {
			throw new TableNotAccessibleException(ErrorCodes.PRG_PAM_BAT_019.getCode(),
					ErrorMessages.APPLICATIONS_TABLE_NOT_ACCESSIBLE.getMessage());
		}
	}

	/**
	 * @param bookingEntityConsumed
	 * @return true if consumed table of booking updated.
	 */
	public boolean updateConsumedBooking(RegistrationBookingEntityConsumed bookingEntityConsumed) {
		try {
			appointmentConsumedRepository.save(bookingEntityConsumed);
			log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"In updateConsumedBooking to update reg_appointment_consumed");
			return true;
		} catch (DataAccessLayerException e) {
			throw new TableNotAccessibleException(ErrorCodes.PRG_PAM_BAT_008.getCode(),
					ErrorMessages.REG_APPOINTMENT_CONSUMED_TABLE_NOT_ACCESSIBLE.getMessage());
		}
	}

	/**
	 * @param entityConsumed
	 * @return true if consumed table of demographic updated.
	 */
	public boolean updateConsumedDemographic(DemographicEntityConsumed entityConsumed) {
		try {
			demographicConsumedRepository.save(entityConsumed);
			log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"In updateConsumedDemographic to update applicant_demographic_consumed");
			return true;
		} catch (DataAccessLayerException e) {
			throw new TableNotAccessibleException(ErrorCodes.PRG_PAM_BAT_009.getCode(),
					ErrorMessages.DEMOGRAPHIC_CONSUMED_TABLE_NOT_ACCESSIBLE.getMessage());
		}
	}

	/**
	 * @param entityConsumed
	 * @return true if consumed of booking updated.
	 */
	public boolean updateConsumedDocument(DocumentEntityConsumed entityConsumed) {
		try {
			documentConsumedRepository.save(entityConsumed);
			log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"In updateConsumedDemographic to update applicant_document_consumed");
			return true;
		} catch (DataAccessLayerException e) {
			throw new TableNotAccessibleException(ErrorCodes.PRG_PAM_BAT_010.getCode(),
					ErrorMessages.DOCUMENT_CONSUMED_TABLE_NOT_ACCESSIBLE.getMessage());
		}
	}

	/**
	 * 
	 * @param regDate
	 * @return list of regCenter
	 */
	public List<String> findRegCenter(LocalDate regDate) {
		List<String> regCenterList = null;
		try {
			regCenterList = availabilityRepository.findAvaialableRegCenter(regDate);
		} catch (DataAccessLayerException e) {
			throw new TableNotAccessibleException(ErrorCodes.PRG_PAM_BAT_013.getCode(),
					ErrorMessages.AVAILABILITY_TABLE_NOT_ACCESSABLE.getMessage());
		}
		return regCenterList;
	}

	/**
	 * 
	 * @param regDate
	 * @param regID
	 * @return list of date
	 */
	public List<LocalDate> findDistinctDate(LocalDate regDate, String regID) {
		List<LocalDate> localDatList = null;
		try {
			localDatList = availabilityRepository.findAvaialableDate(regDate, regID);
		} catch (DataAccessLayerException e) {
			throw new TableNotAccessibleException(ErrorCodes.PRG_PAM_BAT_013.getCode(),
					ErrorMessages.AVAILABILITY_TABLE_NOT_ACCESSABLE.getMessage());
		}
		return localDatList;
	}

	/**
	 * 
	 * @param regId
	 * @param regDate
	 * @return number of deleted items
	 */
	public int deleteSlots(String regId, LocalDate regDate) {
		int deletedSlots = 0;
		try {
			deletedSlots = availabilityRepository.deleteByRegcntrIdAndRegDate(regId, regDate);
		} catch (DataAccessLayerException e) {
			throw new TableNotAccessibleException(ErrorCodes.PRG_PAM_BAT_013.getCode(),
					ErrorMessages.AVAILABILITY_TABLE_NOT_ACCESSABLE.getMessage());
		}
		return deletedSlots;
	}

	/**
	 * 
	 * @param regId
	 * @param regDate
	 * @return number of deleted items
	 */
	public int deleteSlotsBetweenHours(String regId, LocalDate regDate, LocalTime fromTime, LocalTime toTime) {
		int deletedSlots = 0;
		try {
			deletedSlots = availabilityRepository.deleteByRegcntrIdAndRegDateAndFromTimeBetween(regId, regDate,
					fromTime, toTime);
		} catch (DataAccessLayerException e) {
			throw new TableNotAccessibleException(ErrorCodes.PRG_PAM_BAT_013.getCode(),
					ErrorMessages.AVAILABILITY_TABLE_NOT_ACCESSABLE.getMessage());
		}
		return deletedSlots;
	}

	/**
	 * 
	 * @param regId
	 * @param regDate
	 * @return number of deleted items
	 */
	public int deleteSlotForStartTimeEndTime(String regId, LocalDate regDate, LocalTime fromTime, LocalTime toTime) {
		int deletedSlots = 0;
		try {
			deletedSlots = availabilityRepository.deleteByRegcntrIdAndRegDateAndFromTimeAndToTime(regId, regDate,
					fromTime, toTime);
		} catch (DataAccessLayerException e) {
			throw new TableNotAccessibleException(ErrorCodes.PRG_PAM_BAT_013.getCode(),
					ErrorMessages.AVAILABILITY_TABLE_NOT_ACCESSABLE.getMessage());
		}
		return deletedSlots;
	}

	/**
	 * 
	 * @param regDate
	 * @param regID
	 * @return list of AvailibityEntity
	 */
	public List<AvailibityEntity> findSlots(LocalDate regDate, String regID) {
		List<AvailibityEntity> localDatList = null;
		try {
			localDatList = availabilityRepository.findAvaialableSlots(regDate, regID);
		} catch (DataAccessLayerException e) {
			throw new TableNotAccessibleException(ErrorCodes.PRG_PAM_BAT_013.getCode(),
					ErrorMessages.AVAILABILITY_TABLE_NOT_ACCESSABLE.getMessage());
		}
		return localDatList;
	}

	/**
	 * 
	 * @param regId
	 * @param regDate
	 * @return list of RegistrationBookingEntity
	 */
	public List<RegistrationBookingEntity> findAllPreIds(String regId, LocalDate regDate) {
		List<RegistrationBookingEntity> registrationBookingEntityList = null;
		try {
			registrationBookingEntityList = regAppointmentRepository.findByRegistrationCenterIdAndRegDate(regId,
					regDate);
		} catch (DataAccessLayerException e) {
			throw new TableNotAccessibleException(ErrorCodes.PRG_PAM_BAT_013.getCode(),
					ErrorMessages.AVAILABILITY_TABLE_NOT_ACCESSABLE.getMessage());
		}
		return registrationBookingEntityList;
	}

	/**
	 * 
	 * @param regId
	 * @param date
	 * @return list of RegistrationBookingEntity
	 */
	public List<RegistrationBookingEntity> findAllPreIdsByregID(String regId, LocalDate date) {
		List<RegistrationBookingEntity> registrationBookingEntityList = null;
		try {
			registrationBookingEntityList = regAppointmentRepository.findByRegId(regId, date);
		} catch (DataAccessLayerException e) {
			throw new TableNotAccessibleException(ErrorCodes.PRG_PAM_BAT_008.getCode(),
					ErrorMessages.REG_APPOINTMENT_CONSUMED_TABLE_NOT_ACCESSIBLE.getMessage());
		}
		return registrationBookingEntityList;
	}

	/**
	 * Performs find all pre ids bydate and between hours for MOSIP Pre-Registration.
	 *
	 * @param regCenterId reg center id used by find all pre ids bydate and between hours
	 * @param date date used by find all pre ids bydate and between hours
	 * @param fromTime from time used by find all pre ids bydate and between hours
	 * @param toTime to time used by find all pre ids bydate and between hours
	 * @return find all pre ids bydate and between hours result
	 */
	public List<RegistrationBookingEntity> findAllPreIdsBydateAndBetweenHours(String regCenterId, LocalDate date,
			LocalTime fromTime, LocalTime toTime) {
		List<RegistrationBookingEntity> entityList = null;
		try {
			entityList = regAppointmentRepository
					.findByRegistrationCenterIdAndRegDateAndSlotFromTimeBetween(regCenterId, date, fromTime, toTime);
		} catch (DataAccessLayerException e) {
			throw new TableNotAccessibleException(ErrorCodes.PRG_PAM_BAT_008.getCode(),
					ErrorMessages.REG_APPOINTMENT_CONSUMED_TABLE_NOT_ACCESSIBLE.getMessage());
		}
		return entityList;
	}

	/**
	 * 
	 * Aparam regId
	 * 
	 * @param regDate
	 * @return number of deleted items
	 */
	public int deleteAllSlotsByRegId(String regId, LocalDate regDate) {
		int deletedSlots = 0;
		try {
			deletedSlots = availabilityRepository.deleteByRegcntrIdAndRegDateGreaterThanEqual(regId, regDate);
		} catch (DataAccessLayerException e) {
			throw new TableNotAccessibleException(ErrorCodes.PRG_PAM_BAT_013.getCode(),
					ErrorMessages.AVAILABILITY_TABLE_NOT_ACCESSABLE.getMessage());
		}
		return deletedSlots;
	}

	/**
	 * @param entity
	 * @return boolean
	 */
	public AvailibityEntity saveAvailability(AvailibityEntity entity) {
		return availabilityRepository.save(entity);
	}

	/**
	 * Performs flush availability for MOSIP Pre-Registration.
	 */
	public void flushAvailability() {
		log.info("Flushing Availability...");
		availabilityRepository.flush();
	}

	/**
	 * @param batchSize maximum number of ids to return
	 * @return a bounded page of pre-registration ids that still hold a raw (non-UUID) identifier in any
	 *         of the columns the reconciliation converts, across applications, applicant_demographic,
	 *         applicant_document and reg_appointment — so partially migrated records are also picked
	 *         up. Bounded so a large legacy backlog is not loaded all at once.
	 */
	public List<String> getPreRegIdsWithRawIdentifier(int batchSize) {
		try {
			return applicationRepository.findPreRegIdsWithRawIdentifier(batchSize);
		} catch (DataAccessLayerException e) {
			throw new TableNotAccessibleException(ErrorCodes.PRG_PAM_BAT_019.getCode(),
					ErrorMessages.APPLICATIONS_TABLE_NOT_ACCESSIBLE.getMessage());
		}
	}

	/**
	 * Null-safe application lookup (unlike {@link #getApplicantEntityDetails}, returns {@code null}
	 * instead of throwing when the application no longer exists).
	 *
	 * @param applicationID
	 * @return the application, or {@code null} if not found.
	 */
	public ApplicationEntity getApplicationObject(String applicationID) {
		try {
			return applicationRepository.findByApplicationId(applicationID);
		} catch (DataAccessLayerException e) {
			throw new TableNotAccessibleException(ErrorCodes.PRG_PAM_BAT_019.getCode(),
					ErrorMessages.APPLICATIONS_TABLE_NOT_ACCESSIBLE.getMessage());
		}
	}

	/**
	 * @param preRegId
	 * @return documents linked to the given pre-registration id (may be empty/null).
	 */
	public List<DocumentEntity> getApplicantDocuments(String preRegId) {
		try {
			return documentRespository.findByDemographicEntityPreRegistrationId(preRegId);
		} catch (DataAccessLayerException e) {
			throw new TableNotAccessibleException(ErrorCodes.PRG_PAM_BAT_007.getCode(),
					ErrorMessages.DOCUMENT_TABLE_NOT_ACCESSIBLE.getMessage());
		}
	}

	/**
	 * Null-safe booking lookup (unlike {@link #getRegistrationAppointmentDetails}, does not throw when
	 * a pre-registration has no booking).
	 *
	 * @param preRegId
	 * @return booking for the pre-registration id, or {@code null} if none.
	 */
	public RegistrationBookingEntity getRegistrationAppointmentObject(String preRegId) {
		try {
			return regAppointmentRepository.getRegistrationAppointmentByPreRegistrationId(preRegId);
		} catch (DataAccessLayerException e) {
			throw new TableNotAccessibleException(ErrorCodes.PRG_PAM_BAT_005.getCode(),
					ErrorMessages.REG_APPOINTMENT_TABLE_NOT_ACCESSIBLE.getMessage());
		}
	}

	/**
	 * Performs update applicant document for MOSIP Pre-Registration.
	 *
	 * @param documentEntity document entity used by update applicant document
	 * @return update applicant document result
	 */
	public DocumentEntity updateApplicantDocument(DocumentEntity documentEntity) {
		try {
			return documentRespository.save(documentEntity);
		} catch (DataAccessLayerException e) {
			throw new TableNotAccessibleException(ErrorCodes.PRG_PAM_BAT_007.getCode(),
					ErrorMessages.DOCUMENT_TABLE_NOT_ACCESSIBLE.getMessage());
		}
	}

	/**
	 * Performs update booking for MOSIP Pre-Registration.
	 *
	 * @param bookingEntity booking entity used by update booking
	 * @return update booking result
	 */
	public RegistrationBookingEntity updateBooking(RegistrationBookingEntity bookingEntity) {
		try {
			return regAppointmentRepository.save(bookingEntity);
		} catch (DataAccessLayerException e) {
			throw new TableNotAccessibleException(ErrorCodes.PRG_PAM_BAT_005.getCode(),
					ErrorMessages.REG_APPOINTMENT_TABLE_NOT_ACCESSIBLE.getMessage());
		}
	}
}
