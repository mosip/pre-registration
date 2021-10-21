package io.mosip.preregistration.core.util;

import java.io.IOException;
import java.util.Date;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.preregistration.core.code.RequestCodes;
import io.mosip.preregistration.core.common.dto.BookingRegistrationDTO;
import io.mosip.preregistration.core.common.dto.KeyValuePairDto;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.dto.NotificationDTO;
import io.mosip.preregistration.core.common.dto.NotificationResponseDTO;
import io.mosip.preregistration.core.common.dto.RequestWrapper;
import io.mosip.preregistration.core.common.dto.ResponseWrapper;
import io.mosip.preregistration.core.common.dto.SMSRequestDTO;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.exception.RestCallException;

/**
 * @author Sanober Noor
 * @since 1.0.0
 */
@Component
public class NotificationUtil {

	private Logger log = LoggerConfiguration.logConfig(NotificationUtil.class);

	@Value("${emailResourse.url}")
	private String emailResourseUrl;

	@Value("${smsResourse.url}")
	private String smsResourseUrl;

	@Value("${email.acknowledgement.template}")
	private String emailAcknowledgement;

	@Value("${email.acknowledgement.subject.template}")
	private String emailAcknowledgementSubject;

	@Value("${sms.acknowledgement.template}")
	private String smsAcknowledgement;

	@Value("${cancel.appoinment.template}")
	private String cancelAppoinment;

	@Value("${booking.resource.url}")
	private String getAppointmentResourseUrl;

	@Autowired
	private TemplateUtil templateUtil;

	@Autowired
	RestTemplate restTemplate;

	@Value("${mosip.utc-datetime-pattern}")
	private String dateTimeFormat;

	public MainResponseDTO<NotificationResponseDTO> notify(String notificationType, NotificationDTO acknowledgementDTO,
			MultipartFile file) throws IOException {

		log.info("sessionId", "idType", "id", "In notify method of NotificationUtil service:" + notificationType);

		MainResponseDTO<NotificationResponseDTO> response = new MainResponseDTO<>();
		if (notificationType.equals(RequestCodes.SMS)) {
			response = smsNotification(acknowledgementDTO);
		}
		if (notificationType.equals(RequestCodes.EMAIL)) {
			response = emailNotification(acknowledgementDTO, null);
		}

		return response;
	}

	/**
	 * This method will send the email notification to the user
	 * 
	 * @param acknowledgementDTO
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public MainResponseDTO<NotificationResponseDTO> emailNotification(NotificationDTO acknowledgementDTO,
			MultipartFile file) throws IOException {
		log.info("sessionId", "idType", "id", "In emailNotification method of NotificationUtil service");
		HttpEntity<byte[]> doc = null;
		String fileText = null;
		if (file != null) {
			LinkedMultiValueMap<String, String> pdfHeaderMap = new LinkedMultiValueMap<>();
			pdfHeaderMap.add("Content-disposition",
					"form-data; name=attachments; filename=" + file.getOriginalFilename());
			pdfHeaderMap.add("Content-type", "text/plain");
			doc = new HttpEntity<>(file.getBytes(), pdfHeaderMap);
		}

		ResponseEntity<ResponseWrapper<NotificationResponseDTO>> resp = null;
		MainResponseDTO<NotificationResponseDTO> response = new MainResponseDTO<>();
		String mergeTemplate = null;
		for (KeyValuePairDto keyValuePair : acknowledgementDTO.getFullName()) {
			if (acknowledgementDTO.getIsBatch()) {
				fileText = templateUtil.getTemplate(keyValuePair.getKey(), cancelAppoinment);
			} else {
				fileText = templateUtil.getTemplate(keyValuePair.getKey(), emailAcknowledgement);
			}

			String languageWiseTemplate = templateUtil.templateMerge(fileText, acknowledgementDTO);
			if (mergeTemplate == null) {
				mergeTemplate = languageWiseTemplate;
			} else {
				mergeTemplate += System.lineSeparator() + languageWiseTemplate;
			}
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		MultiValueMap<Object, Object> emailMap = new LinkedMultiValueMap<>();
		emailMap.add("attachments", doc);
		emailMap.add("mailContent", mergeTemplate);
		emailMap.add("mailSubject", getEmailSubject(acknowledgementDTO));
		emailMap.add("mailTo", acknowledgementDTO.getEmailID());
		HttpEntity<MultiValueMap<Object, Object>> httpEntity = new HttpEntity<>(emailMap, headers);
		log.info("sessionId", "idType", "id",
				"In emailNotification method of NotificationUtil service emailResourseUrl: " + emailResourseUrl);
		try {
			resp = restTemplate.exchange(emailResourseUrl, HttpMethod.POST, httpEntity,
					new ParameterizedTypeReference<ResponseWrapper<NotificationResponseDTO>>() {
					});
		} catch (RestClientException e) {
			throw new RestCallException(e.getMessage(), e.getCause());
		}

		NotificationResponseDTO notifierResponse = new NotificationResponseDTO();
		notifierResponse.setMessage(resp.getBody().getResponse().getMessage());
		notifierResponse.setStatus(resp.getBody().getResponse().getStatus());
		response.setResponse(notifierResponse);
		response.setResponsetime(getCurrentResponseTime());

		return response;
	}

	/**
	 * This method will give the email subject
	 * 
	 * @param acknowledgementDTO
	 * @return
	 * @throws IOException
	 */
	public String getEmailSubject(NotificationDTO acknowledgementDTO) throws IOException {
		log.info("sessionId", "idType", "id", "In getEmailSubject method of NotificationUtil service");
		return templateUtil.templateMerge(
				templateUtil.getTemplate(acknowledgementDTO.getFullName().get(0).getKey(), emailAcknowledgementSubject),
				acknowledgementDTO);
	}

	/**
	 * This method will send the sms notification to the user
	 * 
	 * @param acknowledgementDTO
	 * @return
	 * @throws IOException
	 */
	public MainResponseDTO<NotificationResponseDTO> smsNotification(NotificationDTO acknowledgementDTO)
			throws IOException {
		log.info("sessionId", "idType", "id", "In smsNotification method of NotificationUtil service");
		MainResponseDTO<NotificationResponseDTO> response = new MainResponseDTO<>();
		ResponseEntity<ResponseWrapper<NotificationResponseDTO>> resp = null;
		String mergeTemplate = null;
		for (KeyValuePairDto keyValuePair : acknowledgementDTO.getFullName()) {
			String languageWiseTemplate = null;
			if (acknowledgementDTO.getIsBatch()) {
				languageWiseTemplate = templateUtil.templateMerge(
						templateUtil.getTemplate(keyValuePair.getKey(), cancelAppoinment), acknowledgementDTO);
			} else {
				languageWiseTemplate = templateUtil.templateMerge(
						templateUtil.getTemplate(keyValuePair.getKey(), smsAcknowledgement), acknowledgementDTO);
			}
			if (mergeTemplate == null) {
				mergeTemplate = languageWiseTemplate;
			} else {
				mergeTemplate += System.lineSeparator() + languageWiseTemplate;
			}
		}

		SMSRequestDTO smsRequestDTO = new SMSRequestDTO();
		smsRequestDTO.setMessage(mergeTemplate);
		smsRequestDTO.setNumber(acknowledgementDTO.getMobNum());
		RequestWrapper<SMSRequestDTO> req = new RequestWrapper<>();
		req.setRequest(smsRequestDTO);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<RequestWrapper<SMSRequestDTO>> httpEntity = new HttpEntity<>(req, headers);
		log.info("sessionId", "idType", "id",
				"In smsNotification method of NotificationUtil service smsResourseUrl: " + smsResourseUrl);
		resp = restTemplate.exchange(smsResourseUrl, HttpMethod.POST, httpEntity,
				new ParameterizedTypeReference<ResponseWrapper<NotificationResponseDTO>>() {
				});

		NotificationResponseDTO notifierResponse = new NotificationResponseDTO();
		notifierResponse.setMessage(resp.getBody().getResponse().getMessage());
		notifierResponse.setStatus(resp.getBody().getResponse().getStatus());
		response.setResponse(notifierResponse);
		response.setResponsetime(getCurrentResponseTime());
		return response;
	}

	public MainResponseDTO<BookingRegistrationDTO> getAppointmentDetails(String preRegId) {
		MainResponseDTO<BookingRegistrationDTO> response = new MainResponseDTO<>();
		ResponseEntity<MainResponseDTO<BookingRegistrationDTO>> responseEntity = null;
		String url = getAppointmentResourseUrl + "/appointment/" + preRegId;
		try {
			log.info("sessionId", "idType", "id", "In callBookingService method of DemographicServiceUtil" + url);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<?> entity = new HttpEntity<>(headers);
			log.debug("sessionId", "idType", "id", entity.toString());
			responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity,
					new ParameterizedTypeReference<MainResponseDTO<BookingRegistrationDTO>>() {
					});
			log.debug("sessionId", "idType", "id", responseEntity.toString());
			if (responseEntity.getBody().getErrors() != null && !responseEntity.getBody().getErrors().isEmpty()) {
				log.error("sessionId", "idType", "id", responseEntity.getBody().getErrors().toString());
				response.setErrors(responseEntity.getBody().getErrors());
			} else {
				response.setResponse(responseEntity.getBody().getResponse());
			}
			log.info("sessionId", "idType", "id", "In call to booking rest service :" + url);
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", "Booking rest call exception " + ExceptionUtils.getStackTrace(ex));
			throw new RestClientException("Rest call failed");
		}
		return response;
	}

	public String getCurrentResponseTime() {
		log.info("sessionId", "idType", "id", "In getCurrentResponseTime method of NotificationUtil service");
		return DateUtils.formatDate(new Date(System.currentTimeMillis()), dateTimeFormat);
	}
}
