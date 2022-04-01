package io.mosip.preregistration.batchjob.impl;

import static java.time.temporal.ChronoUnit.MINUTES;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.batchjob.code.PreRegBatchContants;
import io.mosip.preregistration.batchjob.entity.AvailibityEntity;
import io.mosip.preregistration.batchjob.helper.CancelAndNotifyHelper;
import io.mosip.preregistration.batchjob.helper.PreRegBatchDBHelper;
import io.mosip.preregistration.batchjob.helper.RegCenterIdsHolder;
import io.mosip.preregistration.batchjob.helper.RestHelper;
import io.mosip.preregistration.batchjob.model.RegistrationCenterDto;
import io.mosip.preregistration.batchjob.repository.utils.BatchJpaRepositoryImpl;
import io.mosip.preregistration.core.code.AuditLogVariables;
import io.mosip.preregistration.core.code.EventId;
import io.mosip.preregistration.core.code.EventName;
import io.mosip.preregistration.core.code.EventType;
import io.mosip.preregistration.core.common.entity.RegistrationBookingEntity;
import io.mosip.preregistration.core.config.LoggerConfiguration;

/**
 * @author Mahammed Taheer
 * @since 1.2.0
 *
 */
@Component
public class SlotAvailabilityGenerator {

	private Logger LOGGER = LoggerConfiguration.logConfig(SlotAvailabilityGenerator.class);

	@Value("${preregistration.availability.sync}")
	int noOfDaysToSync;

	@Value("${mosip.mandatory-languages}")
	private String mandatoryLangCodes;

	@Value("${mosip.optional-languages}")
	private String optionalLangCodes;

	@Value("${notification.url}")
	private String notificationURL;

	@Value("${mosip.batch.token.authmanager.userName}")
	private String auditUsername;

	@Value("${mosip.batch.token.authmanager.appId}")
	private String auditUserId;

	@Autowired
	private RestHelper restHelper;

	/**
	 * Autowired reference for {@link #batchServiceDAO}
	 */
	@Autowired
	private BatchJpaRepositoryImpl batchServiceDAO;

	@Autowired
	private PreRegBatchDBHelper batchDBHelper;

	@Autowired
	private CancelAndNotifyHelper cancelAndNotifyHelper;


    public void generateRegistrationAvailabilitySlots(String partName, List<String> regCenterIdsPartList) {

		LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
		 			"No of days configured to generate slots availability: " + noOfDaysToSync);

		RegCenterIdsHolder idsHolder = RegCenterIdsHolder.getInstance();
		List<RegistrationCenterDto> regCentersList = restHelper.getRegistrationCenterDetails(regCenterIdsPartList, idsHolder);
		LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
		 				"Total Number of registration Found available in Master Data: <" + regCentersList.size() + 
						 ">, on partition Name: " + partName + ", regCenterIdsPartList (Page Nos): " + regCenterIdsPartList);

		long partStartTime = System.currentTimeMillis();
		Map<String, Boolean> cancelledTracker = new HashMap<>();
		Map<String, Boolean> notifierTracker = new HashMap<>();
		List<String> errorredRegCenters = new ArrayList<>();
		final AtomicInteger procCounter = new AtomicInteger(1);
		regCentersList.stream().forEach(regCenter -> {
			long startTime = System.currentTimeMillis();
			// identifier for debugging
			String logIdentifier = partName + "_" + regCenter.getId() + "_" + System.currentTimeMillis();
			try {
				
				List<String> regCenterholidaysList = restHelper.getRegistrationHolidayList(regCenter.getId(), regCenter.getLangCode(), 
						noOfDaysToSync);
				
				LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
						"Processing Generation of Slots for Reg Center Id: " + regCenter.getId() + 
						", Reg Center Holiday List: " + regCenterholidaysList);
				
				LocalDate slotGenStartDate = LocalDate.now();
				LocalDate slotGenEndDate = slotGenStartDate.plusDays(noOfDaysToSync);
				LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
							"Slot Generation/Updation Start Date: " + slotGenStartDate + " and End Date: " + slotGenEndDate);
				
				slotGenStartDate.datesUntil(slotGenEndDate, Period.ofDays(1)).forEach(slotGenDate -> {

					List<AvailibityEntity> slotsAvailableList = batchServiceDAO.findSlots(slotGenDate, regCenter.getId());
					LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
							"For date: " + slotGenDate + ", Slots available: " + slotsAvailableList.size());

					// First, check date is in holiday list.
					if (regCenterholidaysList.contains(slotGenDate.toString())) {
						checkAndSaveEmptySlot(regCenter, slotsAvailableList, slotGenDate, logIdentifier, cancelledTracker, notifierTracker);
					} else {
						// Second, calculate and save the availability slots.
						// Scenario 1 - slots are not available for the day, means not calculated yet.
						if (slotsAvailableList.size() == 0) {
							calculateFullDaySlotsAndSave(regCenter, slotGenDate, logIdentifier);
						} else if(slotsAvailableList.size() == 1) { 
							// Scenario 2 - only one slot available, may be got added thought holiday now removed from holiday list.
							purgeAndCalculateFullDaySlotsThenSave(regCenter, slotGenDate, logIdentifier, slotsAvailableList);
						} else {
							// Scenario 3 - many slots available, check for any change in start, lunch & end time and take action accordingly.
							checkAndReCalculateFullDaySlotsThenSave(regCenter, slotGenDate, logIdentifier, slotsAvailableList, 
										cancelledTracker, notifierTracker);
						}
					}
					
				});
			} catch(Throwable t) {
				LOGGER.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, "Unknown Error: " + t.getMessage(), t);
				errorredRegCenters.add(regCenter.getId());
			}
			batchServiceDAO.flushAvailability();
			long endTime = System.currentTimeMillis();
			LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
						"Time took to complete slot generation for registration center: " + (endTime - startTime) + " in ms," +
						" procCounter: " + procCounter.getAndIncrement());
		});
		long partEndTime = System.currentTimeMillis();
		LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
						"Total Time Took to process partition: " + partName + ", Time(In millis): " + (partEndTime - partStartTime));
		// Deleting all the added slots for the expired registration centers. 
		/* List<String> slotsAddedRegCenters = batchServiceDAO.findRegCenter(LocalDate.now());
		slotsAddedRegCenters.stream().filter(regCenterId ->  !processingRegCentersList.contains(regCenterId))
									 .forEach(regCenterId -> purgeExpiredRegCenterSlots(regCenterId, cancelledTracker, notifierTracker)); */
		
		// Printing the cancelled & notification status
		printCancelNotifyStatus(cancelledTracker, "CANCEL-TRACKER");
		printCancelNotifyStatus(notifierTracker, "NOTIFY-TRACKER");
		LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
						"Total Unique Registration Centers Found...");
		idsHolder.printAllRegCenterIds();
		if (errorredRegCenters.size() > 0) {
			String regCenterIds = String.join(",", errorredRegCenters);
			restHelper.sendAuditDetails(EventId.PRE_405.toString(), EventName.EXCEPTION.toString(), EventType.SYSTEM.toString(),
						"Add Availability Slots Failed, List of Reg Centers.", AuditLogVariables.NO_ID.toString(), 
						auditUserId, auditUsername, regCenterIds, AuditLogVariables.BOOK.toString(), AuditLogVariables.BOOKING_SERVICE.toString());
			return;
		}
		// No Reg Center has resulted in Error.
		restHelper.sendAuditDetails(EventId.PRE_407.toString(), EventName.PERSIST.toString(), EventType.SYSTEM.toString(),
						"Add Availability Slots Successfull.", AuditLogVariables.MULTIPLE_ID.toString(), 
						auditUserId, auditUsername, PreRegBatchContants.EMPTY, AuditLogVariables.BOOK.toString(), 
						AuditLogVariables.BOOKING_SERVICE.toString());
    }

	private void checkAndSaveEmptySlot(RegistrationCenterDto regCenterDetails, List<AvailibityEntity> slotsAvailableList, 
				LocalDate slotGenCurrentDay, String logIdentifier, Map<String, Boolean> cancelledTracker,
				Map<String, Boolean> notifierTracker) {
		
		LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
					"Processing For holiday date: " + slotGenCurrentDay);
		LocalTime midnightTime = LocalTime.MIDNIGHT;
		// First, simply insert the empty slot if not already slots available
		if (slotsAvailableList.size() == 0) {
			
			batchDBHelper.saveAvailability(regCenterDetails.getId(), regCenterDetails.getContactPerson(),
					PreRegBatchContants.ZERO_KIOSK, slotGenCurrentDay, midnightTime, midnightTime);
			LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
					"Inserted Empty slot for the date: " + slotGenCurrentDay);
			return;
		}

		// Second, already slots available, check size and decide.
		if (slotsAvailableList.size() == 1) {
			AvailibityEntity slotAvailibityEntity = slotsAvailableList.get(0);
			if (slotAvailibityEntity.getFromTime().equals(midnightTime) && 
					slotAvailibityEntity.getToTime().equals(midnightTime)) {
				LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
						"Existing One Slot available & its empty slot: " + slotGenCurrentDay);
				return;
			}
			// TODO - Check with Team what needs to be done if case time did not match.
			// Should we implement cancellation & notification logic here.
			// For now just deleting the existing record and inserting empty slot.
			batchServiceDAO.deleteSlots(regCenterDetails.getId(), slotGenCurrentDay);
			batchDBHelper.saveAvailability(regCenterDetails.getId(), regCenterDetails.getContactPerson(),
					PreRegBatchContants.ZERO_KIOSK, slotGenCurrentDay, midnightTime, midnightTime);
			LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
					"Deleted & Inserted Empty slot for the date (Existing One Slot available): " + slotGenCurrentDay);
			return;
		}

		// Third, more than one slot already create for an holiday list.
		// May be some exceptional holiday. 
		List<RegistrationBookingEntity> regBookingEntityList = batchServiceDAO.findAllPreIds(regCenterDetails.getId(), 
																			slotGenCurrentDay);
		LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
						"Total Number of bookings available on the day: " + regBookingEntityList.size());
		regBookingEntityList.stream().forEach(bookedSlot -> {
			cancelAndNotifyHelper.cancelAndNotifyApplicant(bookedSlot, logIdentifier, cancelledTracker, notifierTracker);
		});
		batchServiceDAO.deleteSlots(regCenterDetails.getId(), slotGenCurrentDay);
		batchDBHelper.saveAvailability(regCenterDetails.getId(), regCenterDetails.getContactPerson(),
				PreRegBatchContants.ZERO_KIOSK, slotGenCurrentDay, midnightTime, midnightTime);
		LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
				"Deleted & Inserted Empty slot for the date (Existing All Slots available): " + slotGenCurrentDay);
	}

	

	private void calculateFullDaySlotsAndSave(RegistrationCenterDto regCenterDetails, LocalDate slotGenCurrentDay, 
					String logIdentifier) {
		
		LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
						"Processing For Full Day Slots for date: " + slotGenCurrentDay);

		LocalTime centerStartTime = regCenterDetails.getCenterStartTime();
		LocalTime centerEndTime = regCenterDetails.getCenterEndTime();
		LocalTime centerLunchStartTime = regCenterDetails.getLunchStartTime();
		LocalTime centerLunchEndTime = regCenterDetails.getLunchEndTime();
		LocalTime perKioskProcessTime = regCenterDetails.getPerKioskProcessTime();
		LocalTime midnight = LocalTime.MIDNIGHT;
		int totalSlotAdded = 0;
		if (centerStartTime.equals(midnight) || centerEndTime.equals(midnight)) {
			LOGGER.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
						"Not Processing For Full Day because either start/end time not configured");
			return;
		}

		if (centerLunchStartTime.equals(midnight) || centerLunchEndTime.equals(midnight)) {
			LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
						"Processing For Full Day without considering lunch hour(s).");
			totalSlotAdded += calculateAndSaveSlot(centerStartTime, centerEndTime, perKioskProcessTime, 
											regCenterDetails, slotGenCurrentDay, logIdentifier);
			LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
					"Total Number of slots added(without lunch): " + totalSlotAdded + ", Processing Day: " + slotGenCurrentDay);
			return;
		}

		// save slots before lunch.
		totalSlotAdded += calculateAndSaveSlot(centerStartTime, centerLunchStartTime, perKioskProcessTime, 
											regCenterDetails, slotGenCurrentDay, logIdentifier);
		// save lunch slot
		batchDBHelper.saveAvailability(regCenterDetails.getId(), regCenterDetails.getContactPerson(),
				PreRegBatchContants.ZERO_KIOSK, slotGenCurrentDay, centerLunchStartTime, centerLunchEndTime);
		totalSlotAdded += 1; // adding lunch slot also. just to find total how many slots saved.
		
		// save slots after lunch
		totalSlotAdded += calculateAndSaveSlot(centerLunchEndTime, centerEndTime, perKioskProcessTime, 
											regCenterDetails, slotGenCurrentDay, logIdentifier);
		LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
				"Total Number of slots added (full day including lunch): " + totalSlotAdded + ", Processing Day: " + slotGenCurrentDay);
	}

	private int calculateAndSaveSlot(LocalTime startTime, LocalTime endTime, LocalTime perKioskProcessTime, 
				RegistrationCenterDto regCenterDetails, LocalDate slotGenCurrentDay, String logIdentifier) {
		LocalTime slotStartTime = startTime;
		LocalTime slotEndTime = startTime.plusHours(perKioskProcessTime.getHour()).plusMinutes(perKioskProcessTime.getMinute());
		int slotsCnt = 0;
		do {
			if (slotEndTime.isAfter(endTime)) {
				break;
			}
			slotsCnt++;
			batchDBHelper.saveAvailability(regCenterDetails.getId(), regCenterDetails.getContactPerson(),
					regCenterDetails.getNumberOfKiosks(), slotGenCurrentDay, slotStartTime, slotEndTime);
			slotStartTime = slotEndTime;
			slotEndTime = slotEndTime.plusHours(perKioskProcessTime.getHour()).plusMinutes(perKioskProcessTime.getMinute());
		} while(slotEndTime.isBefore(endTime) || slotEndTime.equals(endTime));

		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
		LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
						"Total Slots Saved : " + slotsCnt + 
						", startTime: " + startTime.format(timeFormatter) + 
						", endTime: " + endTime.format(timeFormatter) +
						", perKioskProcessTime: " + perKioskProcessTime.format(timeFormatter));
		return slotsCnt;
	}

	private void purgeAndCalculateFullDaySlotsThenSave(RegistrationCenterDto regCenterDetails, LocalDate slotGenCurrentDay, 
				String logIdentifier, List<AvailibityEntity> slotsAvailableList) {
		LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
					"Purge existing one slot & Processing For Full Day Slots for date: " + slotGenCurrentDay);
		LocalTime midnightTime = LocalTime.MIDNIGHT;
		AvailibityEntity slotAvailibityEntity = slotsAvailableList.get(0);
		if (slotAvailibityEntity.getFromTime().equals(midnightTime) && 
				slotAvailibityEntity.getToTime().equals(midnightTime)) {
			
			batchServiceDAO.deleteSlots(regCenterDetails.getId(), slotGenCurrentDay);
			calculateFullDaySlotsAndSave(regCenterDetails, slotGenCurrentDay, logIdentifier);
			LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
					"Purged existing one holiday slot & Processed For Full Day Slots for date: " + slotGenCurrentDay);
			return;
		}

		// TODO - Not sure why only one slot with different time got added in DB. Need to check the scenario.
		LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
					"Not able to Purged existing one slot because slot start and end time not" +
						" matching with MIDNIGHT time for date: " + slotGenCurrentDay);

	}

	private void checkAndReCalculateFullDaySlotsThenSave(RegistrationCenterDto regCenterDetails, LocalDate slotGenCurrentDay, 
					String logIdentifier, List<AvailibityEntity> slotsAvailableList, Map<String, Boolean> cancelledTracker,
					Map<String, Boolean> notifierTracker) {

		// First, Check for change in Kiosk time, if there is any change 
		// 1. Cancel the appointment & Notify the resident.
		// 2. Delete all the existing slots & add fresh slots for the full day.
		LocalTime regCenterKioskTime  = regCenterDetails.getPerKioskProcessTime();
		LocalTime prevRegCenterKioskTime = LocalTime.of(0, (int)MINUTES.between(slotsAvailableList.get(0).getFromTime(), 
														slotsAvailableList.get(0).getToTime()), 0);
		if (!regCenterKioskTime.equals(prevRegCenterKioskTime)) {
			LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
				"Found change in Kiosk Process Time, Previous Time: " + prevRegCenterKioskTime + ", Current Configured Time: " 
				+ regCenterKioskTime + ", for date: " + slotGenCurrentDay);
			cancelAndNotifySlots(regCenterDetails, slotGenCurrentDay, logIdentifier, cancelledTracker, notifierTracker);
			batchServiceDAO.deleteSlots(regCenterDetails.getId(), slotGenCurrentDay);
			calculateFullDaySlotsAndSave(regCenterDetails, slotGenCurrentDay, logIdentifier);
			// No need to execute further code because full day slots has been recalculated with the latest start, lunch & end time.
			return;
		}

		// Second, check for change in number of kiosks, if there is any change.
		// 1. if number of kiosks has increased, simply add update the kiosk number to the increased one.
		// 2. if number of kiosks has decreased, Cancel, Notify, and update the kiosk number.
		int noOfKiosks = regCenterDetails.getNumberOfKiosks();
		int previousNoOfKiosks = slotsAvailableList.get(0).getAvailableKiosks();
		if (noOfKiosks != previousNoOfKiosks) {
			LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
				"Found change in Number of Kiosks, Previous Number: " + previousNoOfKiosks + ", Current Number: " 
				+ noOfKiosks + ", for date: " + slotGenCurrentDay);
			if (noOfKiosks < previousNoOfKiosks) {
				cancelAndNotifySlots(regCenterDetails, slotGenCurrentDay, logIdentifier, cancelledTracker, notifierTracker);
			} 
			slotsAvailableList.forEach(availibityEntity -> {
				if (availibityEntity.getAvailableKiosks() != 0) {
					availibityEntity.setAvailableKiosks(noOfKiosks);
					batchServiceDAO.saveAvailability(availibityEntity);
				}
			});
		}


		LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
				"Checking existing slots for change in start, lunch & end time for date: " + slotGenCurrentDay);
		
		LocalTime centerStartTime = regCenterDetails.getCenterStartTime();
		LocalTime centerEndTime = regCenterDetails.getCenterEndTime();
		LocalTime centerLunchStartTime = regCenterDetails.getLunchStartTime();
		LocalTime centerLunchEndTime = regCenterDetails.getLunchEndTime();
		
		LocalTime firstSlotStartTime = slotsAvailableList.get(0).getFromTime();
		LocalTime lastSlotEndTime = slotsAvailableList.get(slotsAvailableList.size() - 1).getToTime();
		
		// To Handle already generated slots. Existing slots generation did not have the lunch slot. 
		LocalTime lunchSlotStartTime = LocalTime.MIDNIGHT;
		LocalTime lunchSlotEndTime = LocalTime.MIDNIGHT;
		LocalTime midnightTime = LocalTime.MIDNIGHT;

		boolean foundLunchSlots = false;
		Optional<AvailibityEntity> lunchSlotTiming = slotsAvailableList.stream().filter(slot -> slot.getAvailableKiosks() == 0).findFirst();
		if (lunchSlotTiming.isPresent()) {
			lunchSlotStartTime = lunchSlotTiming.get().getFromTime();
			lunchSlotEndTime = lunchSlotTiming.get().getToTime();
			foundLunchSlots = true;
		} else {
			Object[] lunchTime = findLunchTimes(slotsAvailableList);
			if (Objects.nonNull(lunchTime[0])) {
				lunchSlotStartTime = (LocalTime) lunchTime[0];
				long mins = MINUTES.between(lunchSlotStartTime, centerLunchStartTime);
				// Scenario -- slot end time = 12:20 and lunch start time = 12:30 and perkiosk time = 20 mins.
				// as 10 mins slot cannot be added so updating the lunch start time if there is difference.
				if (mins != 0) {
					LocalTime perKioskTime = regCenterDetails.getPerKioskProcessTime();
					int perKioskTimeInMins =  perKioskTime.getHour() * 60 + perKioskTime.getMinute();
					if (mins < perKioskTimeInMins) {
						lunchSlotStartTime = centerLunchStartTime;
					}
				}
			}
			if (Objects.nonNull(lunchTime[1]))
				lunchSlotEndTime = (LocalTime) lunchTime[1];
			if (Objects.nonNull(lunchTime[0]) && Objects.nonNull(lunchTime[1])){
				// save lunch slot, will be useful in next calculation.
				batchDBHelper.saveAvailability(regCenterDetails.getId(), regCenterDetails.getContactPerson(),
					PreRegBatchContants.ZERO_KIOSK, slotGenCurrentDay, lunchSlotStartTime, lunchSlotEndTime);
				foundLunchSlots = true;
			}
		}

		if (centerStartTime.equals(firstSlotStartTime) && centerEndTime.equals(lastSlotEndTime) && 
					centerLunchStartTime.equals(lunchSlotStartTime) && centerLunchEndTime.equals(lunchSlotEndTime)) {
			LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
						"no recalculation required as no change found in start, lunch & end time for date: " + slotGenCurrentDay);
			return;
		}

		// Start Time.
		if (!centerStartTime.equals(firstSlotStartTime)) {
			// centerConfiguredTime = 09:30
			// slotCalculatedTime   = 09:00
			// cancel/notify the slots from 09:00 to 09:30 -> 30 mins
			
			// centerConfiguredTime = 09:00
			// slotCalculatedTime   = 09:30
			// add new slots from 09:00 to 09:30 -> 30 mins
			recalculateSlots(centerStartTime, firstSlotStartTime, regCenterDetails, slotGenCurrentDay, logIdentifier, 
							cancelledTracker, notifierTracker);
		}
		// End Time.
		if (!centerEndTime.equals(lastSlotEndTime)) {
			// certerConfiguredTime  = 17:30 (centerEndTime)
			// slotCalculatedTime  = 17:00 (lastSlotEndTime)
			// add new slots from 17:00 to 17:30 -> 30 mins.

			// certerConfiguredTime  = 17:00 (centerEndTime)
			// slotCalculatedTime  = 17:30 (lastSlotEndTime)
			// cancel/notify the slots from 17:00 to 17:30 -> 30 mins.
			// just goes reverse here.... but need to validate the scenario. 
			recalculateSlots(lastSlotEndTime, centerEndTime, regCenterDetails, slotGenCurrentDay, logIdentifier, 
							cancelledTracker, notifierTracker);
		}

		// Scenario - Previously no lunch hours configured, now configured lunch hours.
		// Need to cancel all the appointment and send notification to residents. 
		// Added lunch hour entry in the DB with Zero Kiosk.
		if (!foundLunchSlots && lunchSlotStartTime.equals(midnightTime) && lunchSlotStartTime.equals(lunchSlotEndTime)) {
			long diffMins = MINUTES.between(centerLunchStartTime, centerLunchEndTime);
			if (diffMins > 0) {
				List<RegistrationBookingEntity> regBookingEntityList = batchServiceDAO.findAllPreIdsBydateAndBetweenHours(regCenterDetails.getId(), 
													slotGenCurrentDay, centerLunchStartTime, centerLunchEndTime);
				LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
									"Total Number of bookings available between hours(lunch hours): " + regBookingEntityList.size());
				final AtomicInteger counter = new AtomicInteger();
				regBookingEntityList.stream().forEach(bookedSlot -> {
					LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
									"Cancelling Application for PreReg Id(lunch hours): " + bookedSlot.getPreregistrationId());
					counter.incrementAndGet();
					cancelAndNotifyHelper.cancelAndNotifyApplicant(bookedSlot, logIdentifier, cancelledTracker, notifierTracker);
				});
				LocalTime newCenterLunchEndTime = centerLunchEndTime.minusMinutes(1);
				int deleted = batchServiceDAO.deleteSlotsBetweenHours(regCenterDetails.getId(), slotGenCurrentDay, 
										centerLunchStartTime, newCenterLunchEndTime);
				LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
									"Total Number of bookings cancel & notified between hours(lunch hours): " + counter.get());
				LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
									"Total Number of slots deleted(lunch hours): " + deleted);
				batchDBHelper.saveAvailability(regCenterDetails.getId(), regCenterDetails.getContactPerson(),
						PreRegBatchContants.ZERO_KIOSK, slotGenCurrentDay, centerLunchStartTime, centerLunchEndTime);
				return;
			}
		}

		// Lunch Start Time.
		if (!centerLunchStartTime.equals(lunchSlotStartTime)) {
			// previously lunch hours configured, updated now as no lunch hours (removed lunch hours)
			// add new slots for the lunch hour.
			if (centerLunchStartTime.equals(midnightTime) && centerLunchStartTime.equals(centerLunchEndTime)) {
				batchServiceDAO.deleteSlotForStartTimeEndTime(regCenterDetails.getId(), slotGenCurrentDay, lunchSlotStartTime, lunchSlotEndTime);
				int totalSlotAdded = calculateAndSaveSlot(lunchSlotStartTime, lunchSlotEndTime, regCenterDetails.getPerKioskProcessTime(), 
											regCenterDetails, slotGenCurrentDay, logIdentifier);
				LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
											"Total Number of new bookings slots added between hours(removed lunch hours): " + totalSlotAdded);
			} else {
				// certerConfiguredTime  = 13:30 (centerLunchStartTime)
				// slotCalculatedTime  = 13:00 (lunchSlotStartTime)
				// add new slots from 13:00 to 13:30 -> 30 mins.

				// certerConfiguredTime  = 13:00 (centerLunchStartTime)
				// slotCalculatedTime  = 13:30 (lunchSlotStartTime)
				// cancel/notify the slots from 13:00 to 13:30 -> 30 mins.
				recalculateSlots(lunchSlotStartTime, centerLunchStartTime, regCenterDetails, slotGenCurrentDay, logIdentifier, 
								cancelledTracker, notifierTracker);
			}
		}

		// Lunch End Time.
		if (!centerLunchEndTime.equals(lunchSlotEndTime)) {
			if (!(centerLunchStartTime.equals(midnightTime) && centerLunchStartTime.equals(centerLunchEndTime))) {
				// certerConfiguredTime  = 14:30 (centerLunchStartTime)
				// slotCalculatedTime  = 14:00 (lunchSlotStartTime)
				// cancel/notify the slots from 14:00 to 14:30 -> 30 mins.

				// certerConfiguredTime  = 13:30 (centerLunchStartTime)
				// slotCalculatedTime  = 14:00 (lunchSlotStartTime)
				// add new slots from 13:30 to 14:00 -> 30 mins.
				recalculateSlots(centerLunchEndTime, lunchSlotEndTime, regCenterDetails, slotGenCurrentDay, logIdentifier, 
								cancelledTracker, notifierTracker);
			}
		}
	}

	private void cancelAndNotifySlots(RegistrationCenterDto regCenterDetails, LocalDate slotGenCurrentDay, String logIdentifier, 
									 Map<String, Boolean> cancelledTracker, Map<String, Boolean> notifierTracker) {

		List<RegistrationBookingEntity> regBookingEntityList = batchServiceDAO.findAllPreIds(regCenterDetails.getId(), 
																			slotGenCurrentDay);
		LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
						"For Cancel & Notify, Total Number of bookings available on the day: " + regBookingEntityList.size());
		regBookingEntityList.stream().forEach(bookedSlot -> {
			cancelAndNotifyHelper.cancelAndNotifyApplicant(bookedSlot, logIdentifier, cancelledTracker, notifierTracker);
		});
	}

	private void recalculateSlots(LocalTime centerConfiguredTime, LocalTime slotCalculatedTime, RegistrationCenterDto regCenterDetails, 
					LocalDate slotGenCurrentDay, String logIdentifier, Map<String, Boolean> cancelledTracker,
					Map<String, Boolean> notifierTracker) {
		
		long diffMins = MINUTES.between(centerConfiguredTime, slotCalculatedTime);
		LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
						"recalculating slots, centerConfiguredTime: " + centerConfiguredTime + ", slotCalculatedTime: " + slotCalculatedTime +
						", diffMins: " + diffMins);
		if (diffMins < 0){
			List<RegistrationBookingEntity> regBookingEntityList = batchServiceDAO.findAllPreIdsBydateAndBetweenHours(regCenterDetails.getId(), 
												slotGenCurrentDay, slotCalculatedTime, centerConfiguredTime);
			LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
								"Total Number of bookings available between hours: " + regBookingEntityList.size());
			final AtomicInteger counter = new AtomicInteger();
			regBookingEntityList.stream().forEach(bookedSlot -> {
				LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
								"Cancelling Application for PreReg Id: " + bookedSlot.getPreregistrationId());
				counter.incrementAndGet();
				cancelAndNotifyHelper.cancelAndNotifyApplicant(bookedSlot, logIdentifier, cancelledTracker, notifierTracker);
			});
			batchServiceDAO.deleteSlotsBetweenHours(regCenterDetails.getId(), slotGenCurrentDay, slotCalculatedTime, centerConfiguredTime);
			LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
								"Total Number of bookings cancel & notified between hours: " + counter.get());
			return;
		} 
		int totalSlotAdded = calculateAndSaveSlot(centerConfiguredTime, slotCalculatedTime, regCenterDetails.getPerKioskProcessTime(), 
											regCenterDetails, slotGenCurrentDay, logIdentifier);
		LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
											"Total Number of new bookings slots added between hours: " + totalSlotAdded);
	}

	private Object[] findLunchTimes(List<AvailibityEntity> slotsAvailableList) {
		int listSize = slotsAvailableList.size();
		LocalTime lunchSlotStartTime = null;
		LocalTime lunchSlotEndTime = null;
		for (int i = 0; i < listSize; i++) {
			if ((i + 1) == listSize) {
				break;
			}
			long diffMins = MINUTES.between(slotsAvailableList.get(i + 1).getFromTime(), slotsAvailableList.get(i).getToTime());
			if (diffMins != 0) {
				lunchSlotStartTime = slotsAvailableList.get(i).getToTime();
				lunchSlotEndTime = slotsAvailableList.get(i + 1).getFromTime();
				break;
			}
		}
		return new Object[] {lunchSlotStartTime, lunchSlotEndTime};
	}

	private void printCancelNotifyStatus(Map<String, Boolean> tracker, String trackerLog) {

		tracker.entrySet().stream().forEach(trackerKey -> {
			String mesg = trackerKey.getValue() == Boolean.TRUE ? "Success" : "Failed";
			LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, trackerLog, 
						"For Pre Reg Id: " + trackerKey.getKey() + ", Status: " + mesg);
		});
	}

}
