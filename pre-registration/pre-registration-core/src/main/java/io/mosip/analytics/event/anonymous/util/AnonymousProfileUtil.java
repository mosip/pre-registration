package io.mosip.analytics.event.anonymous.util;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import io.mosip.analytics.event.anonymous.dto.AnonymousProfileRequestDTO;
import io.mosip.analytics.event.anonymous.dto.RegistrationProfileDTO;
import io.mosip.analytics.event.anonymous.dto.RegistrationProfileDeviceDTO;
import io.mosip.analytics.event.anonymous.errorcodes.AnonymousProfileErrorCodes;
import io.mosip.analytics.event.anonymous.errorcodes.AnonymousProfileErrorMessages;
import io.mosip.analytics.event.anonymous.exception.AnonymousProfileException;
import io.mosip.analytics.event.anonymous.service.AnonymousProfileService;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.core.common.dto.BookingRegistrationDTO;
import io.mosip.preregistration.core.common.dto.BrowserInfoDTO;
import io.mosip.preregistration.core.common.dto.DemographicResponseDTO;
import io.mosip.preregistration.core.common.dto.DocumentMultipartResponseDTO;
import io.mosip.preregistration.core.common.dto.DocumentsMetaData;
import io.mosip.preregistration.core.common.dto.identity.DemographicIdentityRequestDTO;
import io.mosip.preregistration.core.common.dto.identity.Identity;
import io.mosip.preregistration.core.config.LoggerConfiguration;

/**
 * This class provides generic methods required to create the anonymous profile
 * in Pre-Registration application.
 * 
 * @author Mayura D
 *
 */
@Service
public class AnonymousProfileUtil {

	private static final String BLANK_STRING = "";

	private static final String VALUE_ATTRIBUTE = "value";

	private static final String PHONE = "phone";

	private static final String EMAIL = "email";

	private static final String DATE_FORMAT_DEMOGRAPHIC_DATA = "yyyy/MM/dd";

	private static final String PREREGISTRATION_APP_NAME = "preregistration";

	private Logger log = LoggerConfiguration.logConfig(AnonymousProfileUtil.class);

	@Value("${mosip.utc-datetime-pattern}")
	private String utcDateTimePattern;

	/**
	 * Reference for ${demographic.resource.url} from property file
	 */
	@Value("${demographic.resource.url}")
	private String applicationServiceUrl;

	/**
	 * Environment instance
	 */
	@Autowired
	private Environment env;

	/**
	 * Rest template used for Rest Exchange
	 */
	@Qualifier("plainRestTemplate")
	@Autowired
	private RestTemplate restTemplate;

	/**
	 * Autowired reference for {@link #AnonymousProfileService}
	 */
	@Autowired
	private AnonymousProfileService anonymourProfileService;

	/**
	 * Name of the identity mapping JSON file
	 */
	@Value("${preregistration.config.identityjson}")
	private String identityMappingJsonFileName;

	/**
	 * Identity mapping JSON string
	 */
	private String identityMappingJsonString = BLANK_STRING;

	/**
	 * Root attribute name under for which identity mapping fields are available
	 */
	@Value("${preregistration.identity}")
	private String identityKey;

	/**
	 * Object mapper to parse JSON
	 */
	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * This method acts as a post constructor to initialize the required request
	 * parameters. Fetch the identity mapping JSON, and save the JSON string
	 */
	@PostConstruct
	public void setup() {
		log.info("Fetching file: " + identityMappingJsonFileName);
		identityMappingJsonString = getJsonFile(identityMappingJsonFileName);
		log.info("Fetched the identity JSON from config server" + identityMappingJsonString);
	}

	/**
	 * This method is used for fetching the identity mapping JSON
	 * 
	 * @param filname
	 * @return
	 */
	public String getJsonFile(String filename) {
		try {
			String configServerUri = env.getProperty("spring.cloud.config.uri");
			String configLabel = env.getProperty("spring.cloud.config.label");
			String configProfile = env.getProperty("spring.profiles.active");
			String configAppName = env.getProperty("spring.cloud.config.name");
			StringBuilder uriBuilder = new StringBuilder();
			uriBuilder.append(configServerUri + "/").append(configAppName + "/").append(configProfile + "/")
					.append(configLabel + "/").append(filename);
			log.info("sessionId", "idType", "id", " URL in getJsonFile() method of AnonymousProfileUtil " + uriBuilder);
			return restTemplate.getForObject(uriBuilder.toString(), String.class);
		} catch (Exception ex) {
			log.error("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id",
					"In getIdentityMapping() method of AnonymousProfileUtil - " + ex.getMessage());
		}
		return null;
	}

	/**
	 * Saves the anonymous profile created using the demographic data, documents
	 * data and booking data of the application.
	 * 
	 * @param demographicData
	 * @param documentsData
	 * @param bookingData
	 * @throws AnonymousProfileException
	 */
	public void saveAnonymousProfile(DemographicResponseDTO demographicData, DocumentsMetaData documentsData,
			BookingRegistrationDTO bookingData, BrowserInfoDTO browserData) throws AnonymousProfileException {
		log.info("sessionId", "idType", "id",
				"The demographicData in saveAnonymousProfile() method of AnonymousProfileUtil service - "
						+ demographicData);
		log.info("sessionId", "idType", "id",
				"The documentsData in saveAnonymousProfile() method of AnonymousProfileUtil service - "
						+ documentsData);
		log.info("sessionId", "idType", "id",
				"The bookingData in saveAnonymousProfile() method of AnonymousProfileUtil service - " + bookingData);
		try {
			DemographicIdentityRequestDTO identityDto = populateIdentityMappingDto(); 
			if (!isNull(identityDto) && !isNull(identityDto.getIdentity()) && !isNull(demographicData)) {
				Identity identityMapping = identityDto.getIdentity();
				JsonNode identityData = objectMapper.readTree(demographicData.getDemographicDetails().toJSONString());
				identityData = identityData.get(identityKey);
				RegistrationProfileDTO registrationProfile = new RegistrationProfileDTO();
				registrationProfile.setProcessName(PREREGISTRATION_APP_NAME);
				registrationProfile.setProcessStage(PREREGISTRATION_APP_NAME);
				registrationProfile.setDate(LocalDateTime.now(ZoneId.of("UTC")));
				registrationProfile.setYearOfBirth(extractYear(
						getValueFromDemographicData(identityMapping.getDob().getValue(), identityData)));
				registrationProfile
						.setGender(getValueFromDemographicData(identityMapping.getGender().getValue(), identityData));
				registrationProfile.setPreferredLanguage(demographicData.getLangCode());
				if (!isNull(bookingData)) {
					registrationProfile.setEnrollmentCenterId(bookingData.getRegistrationCenterId());
				} else {
					registrationProfile.setEnrollmentCenterId(BLANK_STRING);
				}
				registrationProfile.setLocation(getLocations(identityMapping, identityData));
				registrationProfile.setChannel(getChannels(identityMapping, identityData));
				registrationProfile.setDocuments(getDocumentTypesList(documentsData));
				registrationProfile.setStatus(demographicData.getStatusCode());
				RegistrationProfileDeviceDTO device = new RegistrationProfileDeviceDTO();
				if (!isNull(browserData)) {
					device.setBrowser(browserData.getBrowserName());
					device.setBrowserVersion(browserData.getBrowserVersion());
				}
				registrationProfile.setDevice(device);
				AnonymousProfileRequestDTO requestDto = new AnonymousProfileRequestDTO();
				String profileJsonString = objectMapper.writeValueAsString(registrationProfile);
				requestDto.setProfileDetails(profileJsonString);
				anonymourProfileService.saveAnonymousProfile(requestDto);
			} else {
				throw new AnonymousProfileException(AnonymousProfileErrorCodes.PRG_ANO_001.getCode(),
						AnonymousProfileErrorMessages.UNABLE_TO_SAVE_ANONYMOUS_PROFILE.getMessage());
			}

		} catch (Exception ex) {
			log.error("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id",
					"Exception in saveAnonymousProfile() method of AnonymousProfileUtil - " + ex.getMessage());
			throw new AnonymousProfileException(AnonymousProfileErrorCodes.PRG_ANO_001.getCode(),
					AnonymousProfileErrorMessages.UNABLE_TO_SAVE_ANONYMOUS_PROFILE.getMessage());
		}

	}

	/**
	 * Populates the identity mapping JSON string into DTO
	 * 
	 * @return
	 */
	private DemographicIdentityRequestDTO populateIdentityMappingDto() {
		try {
			return objectMapper.readValue(identityMappingJsonString, DemographicIdentityRequestDTO.class);
		} catch (IOException ex) {
			log.error("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id",
					"In getIdentityMapping() method of AnonymousProfileUtil - " + ex.getMessage());
		}
		return null;
	}

	/**
	 * Reads value for the given fieldName key from demographic data.
	 * 
	 * @param key
	 * @param identityData
	 * @return
	 */
	private String getValueFromDemographicData(String key, JsonNode identityData) {
		JsonNode arrayNode = identityData.get(key);
		int index = 0;
		String value = BLANK_STRING;
		if (arrayNode != null) {
			if (arrayNode.isArray()) {
				for (JsonNode jsonNode : arrayNode) {
					if (index == 0) {
						value = jsonNode.get(VALUE_ATTRIBUTE).asText().trim();
					}
					index++;
				}
			} else {
				value = arrayNode.asText().trim();
			}
		}
		return value;
	}

	/**
	 * Returns list of values for the given address hierarchy.
	 * 
	 * @param identityMapping
	 * @param identityData
	 * @return
	 */
	private List<String> getLocations(Identity identityMapping, JsonNode identityData) {
		List<String> locations = new ArrayList<String>();
		String address = identityMapping.getLocationHierarchyForProfiling().getValue();
		String[] addressKeys = address.split(",");
		for (int i = 0; i < addressKeys.length; i++) {
			locations.add(getValueFromDemographicData(addressKeys[i], identityData));
		}
		return locations;
	}

	/**
	 * Returns list with channels used in application.
	 * 
	 * @param identityMapping
	 * @param identityData
	 * @return
	 */
	private List<String> getChannels(Identity identityMapping, JsonNode identityData) {
		String emailVal = getValueFromDemographicData(identityMapping.getEmail().getValue(), identityData);
		String phoneVal = getValueFromDemographicData(identityMapping.getPhone().getValue(), identityData);
		List<String> channels = new ArrayList<String>();
		if (!isNull(emailVal)) {
			channels.add(EMAIL);
		}
		if (!isNull(phoneVal)) {
			channels.add(PHONE);
		}
		return channels;
	}

	/**
	 * Returns list with document type codes based on documents uploaded for the
	 * application.
	 * 
	 * @param documentsMetaData
	 * @return
	 */
	private List<String> getDocumentTypesList(DocumentsMetaData documentsMetaData) {
		List<String> documentsList = new ArrayList<String>();
		if (!isNull(documentsMetaData) && !isNull(documentsMetaData.getDocumentsMetaData())) {
			for (DocumentMultipartResponseDTO documentMultipartResponseDTO : documentsMetaData.getDocumentsMetaData()) {
				documentsList.add(documentMultipartResponseDTO.getDocTypCode());
			}
		}
		return documentsList;
	}

	/**
	 * This method is used as Null checker for different input keys.
	 *
	 * @param key
	 * @return true if key not null and return false if key is null.
	 */
	private boolean isNull(Object key) {
		log.info("sessionId", "idType", "id", "In isNull method of datasync service util");
		if (key instanceof String) {
			if (key.equals(BLANK_STRING))
				return true;
		} else if (key instanceof List<?>) {
			if (((List<?>) key).isEmpty())
				return true;
		} else {
			if (key == null)
				return true;
		}
		return false;
	}

	/**
	 * Returns Year from dateofBirth field in the demographic data.
	 * 
	 * @param dateOfBirthStr
	 * @return
	 */
	private String extractYear(String dateOfBirthStr) {
		String yearStr = BLANK_STRING;
		if (!dateOfBirthStr.equals(BLANK_STRING)) {
			try {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT_DEMOGRAPHIC_DATA);
				LocalDate date = LocalDate.parse(dateOfBirthStr, formatter);
				int year = date.getYear();
				yearStr = BLANK_STRING + year;
			} catch (DateTimeParseException pe) {
				// ignore
			}
		}
		return yearStr;
	}

}
