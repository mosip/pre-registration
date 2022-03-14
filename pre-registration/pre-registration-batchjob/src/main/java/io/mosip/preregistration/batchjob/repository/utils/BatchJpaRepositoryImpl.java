package io.mosip.preregistration.batchjob.repository.utils;

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
				log.info("sessionId", "idType", "id", "Deleted Invalid Pre-Registration ID");
			}
			demographicRepository.flush();
		} catch (DataAccessLayerException e) {
			throw new TableNotAccessibleException(ErrorCodes.PRG_PAM_BAT_004.getCode(),
					ErrorMessages.DEMOGRAPHIC_TABLE_NOT_ACCESSIBLE.getMessage());
		}
		return entity;
	}

	public DemographicEntity getApplicantDemographicObject(String preRegId) {
		try {
			DemographicEntity entity = demographicRepository.findBypreRegistrationId(preRegId);
			return entity;
		} catch (DataAccessLayerException e) {
			throw new TableNotAccessibleException(ErrorCodes.PRG_PAM_BAT_004.getCode(),
					ErrorMessages.DEMOGRAPHIC_TABLE_NOT_ACCESSIBLE.getMessage());
		}
	}

	public int deleteInvalidProcessedPreReg(String preRegId) {
		try {
			return processedPreIdRepository.deleteBypreRegistrationId(preRegId);
		} catch (DataAccessLayerException e) {
			throw new TableNotAccessibleException(ErrorCodes.PRG_PAM_BAT_004.getCode(),
					ErrorMessages.DEMOGRAPHIC_TABLE_NOT_ACCESSIBLE.getMessage());
		}
	}

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

	public ApplicationEntity getBookedApplicantEntityDetails(String applicationID) {

		ApplicationEntity entity = null;
		try {
			entity = applicationRepository.findByApplicationIdAndBookingStatusCode(applicationID, StatusCodes.BOOKED.getCode());
		} catch (DataAccessLayerException e) {
			throw new NoPreIdAvailableException(ErrorCodes.PRG_PAM_BAT_001.getCode(),
					ErrorMessages.NO_PRE_REGISTRATION_ID_FOUND_TO_UPDATE_STATUS.getMessage());
		}
		return entity;
	}

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
				log.info("sessionId", "idType", "id",
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
			List<RegistrationBookingEntity> entityList = regAppointmentRepository.findByRegDateBetween(StatusCodes.BOOKED.getCode(), 
									LocalDate.now());
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
			log.info("sessionId", "idType", "id", "In deleteDemographic to delete consumed demographic details");

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
				log.info("sessionId", "idType", "id", "In deleteDocument to delete consumed demographic details");
			});

		} catch (Exception e) {
			throw new TableNotAccessibleException(ErrorCodes.PRG_PAM_BAT_007.getCode(),
					ErrorMessages.DOCUMENT_TABLE_NOT_ACCESSIBLE.getMessage());
		}

	}

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
			log.info("sessionId", "idType", "id", "In deleteBooking to delete consumed demographic details");
		} catch (DataAccessLayerException e) {
			throw new TableNotAccessibleException(ErrorCodes.PRG_PAM_BAT_005.getCode(),
					ErrorMessages.REG_APPOINTMENT_TABLE_NOT_ACCESSIBLE.getMessage());
		}
	}
	
	/** Deleting Booking details the consumed demographic data. */
	public void deleteApplications(ApplicationEntity applicationEntity) {
		try {
			applicationRepository.delete(applicationEntity);
			log.info("sessionId", "idType", "id", "In deleteapplications to delete consumed demographic details");
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
			log.info("sessionId", "idType", "id", "In updateConsumedBooking to update reg_appointment_consumed");
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
			log.info("sessionId", "idType", "id",
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
			log.info("sessionId", "idType", "id", "In updateConsumedDemographic to update applicant_document_consumed");
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

	public void flushAvailability() {
		log.info("Flushing Availability...");
		availabilityRepository.flush();
	}

}
