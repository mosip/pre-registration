package io.mosip.preregistration.core.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.templatemanager.spi.TemplateManager;
import io.mosip.preregistration.core.common.dto.NotificationDTO;
import io.mosip.preregistration.core.common.dto.RequestWrapper;
import io.mosip.preregistration.core.common.dto.ResponseWrapper;
import io.mosip.preregistration.core.common.dto.TemplateResponseDTO;
import io.mosip.preregistration.core.common.dto.TemplateResponseListDTO;
import io.mosip.preregistration.core.config.LoggerConfiguration;

/**
 * @author Sanober Noor
 * @since 1.0.0
 */
@Component
public class TemplateUtil {

	private Logger log = LoggerConfiguration.logConfig(TemplateUtil.class);
	/**
	 * Reference for ${resource.template.url} from property file
	 */

	@Value("${resource.template.url}")
	private String resourceUrl;

	@Value("${mosip.notification.timezone}")
	private String timeZone;
	
	/**
	 * Autowired reference for {@link #restTemplateBuilder}
	 */
	@Qualifier("selfTokenRestTemplate")
	@Autowired
	RestTemplate restTemplate;

	@Autowired
	private TemplateManager templateManager;

	/**
	 * This method is used for getting template
	 * 
	 * @param object
	 * @param templatetypecode
	 * @return
	 */

	public String getTemplate(Object langCode, String templatetypecode) {
		List<TemplateResponseDTO> response = null;
		String url = resourceUrl + "/" + (String) langCode + "/" + templatetypecode;
		HttpHeaders headers = new HttpHeaders();
		HttpEntity<RequestWrapper<TemplateResponseListDTO>> httpEntity = new HttpEntity<>(headers);
		log.info("sessionId", "idType", "id", "In getTemplate method of TemplateUtil service url: " + url);
		ResponseEntity<ResponseWrapper<TemplateResponseListDTO>> respEntity = restTemplate.exchange(url, HttpMethod.GET,
				httpEntity, new ParameterizedTypeReference<ResponseWrapper<TemplateResponseListDTO>>() {
				});

		ResponseWrapper<TemplateResponseListDTO> body = respEntity.getBody();
		if (body != null) {
			response = body.getResponse().getTemplates();
		}
		return response.get(0).getFileText().replaceAll("^\"|\"$", "");

	}

	/**
	 * This method merging the template
	 * 
	 * @param fileText
	 * @param acknowledgementDTO
	 * @return
	 * @throws IOException
	 */
	public String templateMerge(String fileText, NotificationDTO acknowledgementDTO, String langCode)
			throws IOException {
		log.info("sessionId", "idType", "id", "In templateMerge method of TemplateUtil service ");
		String mergeTemplate = null;
		Map<String, Object> map = mapSetting(langCode, acknowledgementDTO);
		InputStream templateInputStream = new ByteArrayInputStream(fileText.getBytes(Charset.forName("UTF-8")));

		InputStream resultedTemplate = templateManager.merge(templateInputStream, map);

		mergeTemplate = IOUtils.toString(resultedTemplate, StandardCharsets.UTF_8.name());

		return mergeTemplate;
	}

	/**
	 * This method will set the user detail for the template merger
	 * 
	 * @param acknowledgementDTO
	 * @return
	 */
	public Map<String, Object> mapSetting(String langCode, NotificationDTO acknowledgementDTO) {
		Map<String, Object> responseMap = new HashMap<>();
		log.info("sessionId", "idType", "id", "In mapSetting method of TemplateUtil service {}", acknowledgementDTO);
		DateTimeFormatter dateFormate = DateTimeFormatter.ofPattern("dd MMM yyyy");
		DateTimeFormatter timeFormate = DateTimeFormatter.ofPattern("h:mma");

		LocalDateTime now = LocalDateTime.now();
		Instant nowUtc = Instant.now();
		ZoneId countryZoneId = ZoneId.of(timeZone);
		ZonedDateTime nowCountryTime = ZonedDateTime.ofInstant(nowUtc, countryZoneId);

		responseMap.put("name", acknowledgementDTO.getFullName().stream().filter(name -> name.getKey().equals(langCode))
				.map(name -> name.getValue()).collect(Collectors.toList()).get(0));
		responseMap.put("PRID", acknowledgementDTO.getPreRegistrationId());
		responseMap.put("Date", dateFormate.format(now));
		responseMap.put("Time", timeFormate.format(nowCountryTime));
		responseMap.put("Appointmentdate", acknowledgementDTO.getAppointmentDate());
		responseMap.put("Appointmenttime", acknowledgementDTO.getAppointmentTime());
		if (acknowledgementDTO.getRegistrationCenterName() != null) {
			responseMap.put("RegistrationCenterName",
					acknowledgementDTO.getRegistrationCenterName().stream()
							.filter(RegistrationCenterName -> RegistrationCenterName.getKey().equals(langCode))
							.map(RegistrationCenterName -> RegistrationCenterName.getValue())
							.collect(Collectors.toList()).get(0));
			responseMap.put("RegistrationCenterAddress",
					acknowledgementDTO.getAddress().stream()
							.filter(RegistrationCenterAddress -> RegistrationCenterAddress.getKey().equals(langCode))
							.map(RegistrationCenterAddress -> RegistrationCenterAddress.getValue())
							.collect(Collectors.toList()).get(0));
		}
		return responseMap;
	}

}
