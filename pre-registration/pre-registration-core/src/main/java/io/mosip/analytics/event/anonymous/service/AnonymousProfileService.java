package io.mosip.analytics.event.anonymous.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import io.mosip.analytics.event.anonymous.dto.AnonymousProfileRequestDTO;
import io.mosip.analytics.event.anonymous.dto.AnonymousProfileResponseDTO;
import io.mosip.analytics.event.anonymous.entity.AnonymousProfileEntity;
import io.mosip.analytics.event.anonymous.repository.AnonymousProfileRepostiory;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.util.UUIDGeneratorUtil;

@Service
public class AnonymousProfileService implements AnonymousProfileServiceIntf {

	/**
	 * logger instance
	 */
	private Logger log = LoggerConfiguration.logConfig(AnonymousProfileService.class);

	@Value("${mosip.utc-datetime-pattern}")
	private String utcDateTimePattern;

	@Value("${mosip.preregistration.anonymous-profile-username}")
    private String anonymousProfileUsername;

	/**
	 * Autowired reference for {@link #AnonymousProfileRepostiory}
	 */
	@Autowired
	private AnonymousProfileRepostiory anonymousProfileRepostiory;

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.analytics.event.anonymous.service.AnonymousProfileServiceIntf#
	 * authUserDetails()
	 */
	@Override
	public AuthUserDetails authUserDetails() {
		return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	/*
	 * This method is used to save anonymous profile of the user.
	 * 
	 * 
	 * @param request AnonymousProfileRequestDTO
	 * 
	 * @return AnonymousProfileResponseDTO
	 */
	@Override
	public AnonymousProfileResponseDTO saveAnonymousProfile(AnonymousProfileRequestDTO requestDto) {
		log.info("sessionId", "idType", "id", "In saveAnonymousProfile() method of AnonymousProfileService");
		log.info("sessionId", "idType", "id",
				"Pre Registration start time : " + DateUtils.getUTCCurrentDateTimeString());
		AnonymousProfileResponseDTO responseDto = new AnonymousProfileResponseDTO();
		try {
			LocalDateTime currentDateTime = LocalDateTime.now(ZoneId.of("UTC"));
			AnonymousProfileEntity requestEntity = new AnonymousProfileEntity();
			requestEntity.setId(UUIDGeneratorUtil.generateId());
			requestEntity.setProfile(requestDto.getProfileDetails());
			requestEntity.setCreatedBy(anonymousProfileUsername);
			requestEntity.setCreateDateTime(currentDateTime);
			requestEntity.setUpdatedBy(anonymousProfileUsername);
			requestEntity.setUpdateDateTime(currentDateTime);
			requestEntity.setIsDeleted(false);
			
			AnonymousProfileEntity responseEntity = anonymousProfileRepostiory.save(requestEntity);
			responseDto.setProfile(responseEntity.getProfile());
			responseDto.setCreatedBy(responseEntity.getCreatedBy());
			responseDto.setCreatedDateTime(getLocalDateString(responseEntity.getCreateDateTime()));
			responseDto.setUpdatedBy(responseEntity.getUpdatedBy());
			responseDto.setUpdatedDateTime(getLocalDateString(responseEntity.getUpdateDateTime()));
			log.info("sessionId", "idType", "id",
					"Pre Registration end time : " + DateUtils.getUTCCurrentDateTimeString());
		} catch (Exception exception) {
			log.error("sessionId", "idType", "id", ExceptionUtils.getStackTrace(exception));
			log.error("sessionId", "idType", "id",
					"In saveAnonymousProfile() method of AnonymousProfileService - " + exception.getMessage());
		}
		return responseDto;

	}

	public String getLocalDateString(LocalDateTime date) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(utcDateTimePattern);
		return date.format(dateTimeFormatter);
	}

}
