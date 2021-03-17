package io.mosip.preregistration.application.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.application.dto.NotificationResponseDTO;
import io.mosip.preregistration.application.service.NotificationService;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.swagger.annotations.ApiOperation;

/**
 * Controller class for notification triggering.
 * 
 * @author Sanober Noor
 * @author Tapaswini Behera
 * @since 1.0.0
 */
@RestController
@RequestMapping("/notification")
@CrossOrigin("*")
public class NotificationController {

	/**
	 * Reference to {@link NotificationService}.
	 */
	@Autowired
	private NotificationService notificationService;

	private Logger log = LoggerConfiguration.logConfig(NotificationController.class);

	/**
	 * Api to Trigger notification service.
	 * 
	 * @param jsonbObject the json string.
	 * @param langCode    the language code.
	 * @param file        the file to send.
	 * @return the response entity.
	 */
	@PreAuthorize("hasAnyRole('INDIVIDUAL','PRE_REGISTRATION_ADMIN')")
	@PostMapping(path = "/notify", consumes = { "multipart/form-data" })
	@ApiOperation(value = "Trigger notification")
	public ResponseEntity<MainResponseDTO<NotificationResponseDTO>> sendNotification(
			@RequestPart(value = "NotificationRequestDTO", required = true) String jsonbObject,
			@RequestPart(value = "langCode", required = true) String langCode,
			@RequestPart(value = "attachment", required = false) MultipartFile file, HttpServletRequest res) {
		log.info("sessionId", "idType", "id",
				"In notification controller for send notification with request notification dto  " + jsonbObject);
		log.debug("sessionId", "idType", "id", res.getHeader("Cookie"));
		return new ResponseEntity<>(notificationService.sendNotification(jsonbObject, langCode, file, false),
				HttpStatus.OK);
	}

	/**
	 * Api to Trigger notification service.
	 * 
	 * @param jsonbObject the json string.
	 * @param langCode    the language code.
	 * @param file        the file to send.
	 * @return the response entity.
	 */
	@PreAuthorize("hasAnyRole('INDIVIDUAL','PRE_REGISTRATION_ADMIN')")
	@PostMapping(consumes = { "multipart/form-data" })
	@ApiOperation(value = "Trigger notification")
	public ResponseEntity<MainResponseDTO<NotificationResponseDTO>> sendNotifications(
			@RequestPart(value = "NotificationRequestDTO", required = true) String jsonbObject,
			@RequestPart(value = "langCode", required = false) String langCode,
			@RequestPart(value = "attachment", required = false) MultipartFile file, HttpServletRequest res) {
		log.info("sessionId", "idType", "id",
				"In notification controller for send notification with request notification dto  " + jsonbObject);
		log.debug("sessionId", "idType", "id", res.getHeader("Cookie"));
		return new ResponseEntity<>(notificationService.sendNotification(jsonbObject, langCode, file, true),
				HttpStatus.OK);
	}
}