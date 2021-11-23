package io.mosip.preregistration.batchjob.impl;

import static java.time.temporal.ChronoUnit.MINUTES;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.batchjob.code.PreRegBatchContants;
import io.mosip.preregistration.batchjob.entity.AvailibityEntity;
import io.mosip.preregistration.batchjob.helper.PreRegBatchDBHelper;
import io.mosip.preregistration.batchjob.helper.RestHelper;
import io.mosip.preregistration.batchjob.model.RegistrationCenterDto;
import io.mosip.preregistration.batchjob.repository.utils.BatchJpaRepositoryImpl;
import io.mosip.preregistration.core.common.entity.ApplicationEntity;
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

    public void generateRegistrationAvailabilitySlots() {

		LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
		 			"No of days configured to generate slots availability: " + noOfDaysToSync);

		List<RegistrationCenterDto> regCentersList = restHelper.getRegistrationCenterDetails();
		LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
		 				"Total Number of registration Found available in Master Data: <" + regCentersList.size() + ">");

		List<String> processingRegCentersList = new ArrayList<>();
		Map<String, Boolean> cancelledTracker = new HashMap<>();
		Map<String, Boolean> notifierTracker = new HashMap<>();
		regCentersList.stream().forEach(regCenter -> {
			long startTime = System.currentTimeMillis();
			// identifier for debugging
			String logIdentifier = regCenter.getId() + "_" + System.currentTimeMillis();
			try {
				List<String> regCenterholidaysList = restHelper.getRegistrationHolidayList(regCenter.getId(), regCenter.getLangCode(), 
						noOfDaysToSync);
				
				LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
						"Processing Generation of Slots for Reg Center Id: " + regCenter.getId() + 
						", Reg Center Holiday List: " + regCenterholidaysList);
				processingRegCentersList.add(regCenter.getId());
				
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
			} 
			long endTime = System.currentTimeMillis();
			LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
						"Time took to complete slot generation for registration center: " + (endTime - startTime) + " in ms");
		});
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
			cancelAndNotifyApplicant(bookedSlot, logIdentifier, cancelledTracker, notifierTracker);
		});
		batchServiceDAO.deleteSlots(regCenterDetails.getId(), slotGenCurrentDay);
		batchDBHelper.saveAvailability(regCenterDetails.getId(), regCenterDetails.getContactPerson(),
				PreRegBatchContants.ZERO_KIOSK, slotGenCurrentDay, midnightTime, midnightTime);
		LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
				"Deleted & Inserted Empty slot for the date (Existing All Slots available): " + slotGenCurrentDay);
	}

	private void cancelAndNotifyApplicant(RegistrationBookingEntity bookedSlot, String logIdentifier, Map<String, Boolean> cancelledTracker,
						Map<String, Boolean> notifierTracker) {

		String preRegId = bookedSlot.getPreregistrationId();
		ApplicationEntity bookedApplication  = batchServiceDAO.getBookedApplicantEntityDetails(preRegId);
		if (Objects.nonNull(bookedApplication)) {
			boolean cancelled = restHelper.cancelBookedApplication(preRegId, logIdentifier);
			if (cancelled) {
				boolean notified = restHelper.sendCancelledNotification(bookedSlot.getPreregistrationId(), bookedSlot.getRegDate().toString(), 
							bookedSlot.getSlotFromTime().toString(), bookedSlot.getLangCode(), logIdentifier);
				notifierTracker.put(bookedSlot.getPreregistrationId(), notified);
			}
			cancelledTracker.put(bookedSlot.getPreregistrationId(), cancelled);
		}
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
			slotsCnt++;
			batchDBHelper.saveAvailability(regCenterDetails.getId(), regCenterDetails.getContactPerson(),
					regCenterDetails.getNumberOfKiosks(), slotGenCurrentDay, slotStartTime, slotEndTime);
			slotStartTime = slotEndTime;
			slotEndTime = slotEndTime.plusHours(perKioskProcessTime.getHour()).plusMinutes(perKioskProcessTime.getMinute());
		} while(slotEndTime.isBefore(endTime) || slotEndTime.equals(endTime));

		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss");
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
		LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
				"Checking existing slots for change in start, lunch & end time for date: " + slotGenCurrentDay);
		
		LocalTime centerStartTime = regCenterDetails.getCenterStartTime();
		LocalTime centerEndTime = regCenterDetails.getCenterEndTime();
		LocalTime centerLunchStartTime = regCenterDetails.getLunchStartTime();
		LocalTime centerLunchEndTime = regCenterDetails.getLunchEndTime();
		
		LocalTime firstSlotStartTime = slotsAvailableList.get(0).getFromTime();
		LocalTime lastSlotEndTime = slotsAvailableList.get(slotsAvailableList.size() - 1).getToTime();

		AvailibityEntity lunchSlotTiming = slotsAvailableList.stream().filter(slot -> slot.getAvailableKiosks() == 0).findFirst().get();
		LocalTime lunchSlotStartTime = lunchSlotTiming.getFromTime();
		LocalTime lunchSlotEndTime = lunchSlotTiming.getToTime();

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

		// Lunch Start Time.
		if (!centerLunchStartTime.equals(lunchSlotStartTime)) {
			// certerConfiguredTime  = 13:30 (centerLunchStartTime)
			// slotCalculatedTime  = 13:00 (lunchSlotStartTime)
			// add new slots from 13:00 to 13:30 -> 30 mins.

			// certerConfiguredTime  = 13:00 (centerLunchStartTime)
			// slotCalculatedTime  = 13:30 (lunchSlotStartTime)
			// cancel/notify the slots from 13:00 to 13:30 -> 30 mins.
			recalculateSlots(lunchSlotStartTime, centerLunchStartTime, regCenterDetails, slotGenCurrentDay, logIdentifier, 
							cancelledTracker, notifierTracker);
		}

		// Lunch End Time.
		if (!centerLunchEndTime.equals(lunchSlotEndTime)) {
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
				cancelAndNotifyApplicant(bookedSlot, logIdentifier, cancelledTracker, notifierTracker);
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


}
