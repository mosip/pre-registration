package io.mosip.preregistration.application.controller;

import javax.servlet.http.HttpServletRequest;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.application.dto.NotificationResponseDTO;
import io.mosip.preregistration.application.service.NotificationService;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller class for notification triggering.
 * 
 * @author Sanober Noor
 * @author Tapaswini Behera
 * @since 1.0.0
 */
@RestController
@Tag(name = "notification-controller", description = "Notification Controller")
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
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostnotificationnotify())")
	//@PreAuthorize("hasAnyRole('INDIVIDUAL','PRE_REGISTRATION_ADMIN')")
	@PostMapping(path = "/notification/notify", consumes = { "multipart/form-data" })
	@Operation(summary  = "sendNotification", description = "Trigger notification", tags = "notification-controller")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
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
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostnotification())")
	//@PreAuthorize("hasAnyRole('INDIVIDUAL','PRE_REGISTRATION_ADMIN')")
	@PostMapping(path = "/notification", consumes = { "multipart/form-data" })
	@Operation(summary  = "sendNotifications", description = "Trigger notification", tags = "notification-controller")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
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
	
	/**
	 * Api to Trigger notification service.
	 * 
	 * @param jsonbObject the json string.
	 * @param langCode    the language code.
	 * @param file        the file to send.
	 * @return the response entity.
	 */
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostnotification())")
	//@PreAuthorize("hasAnyRole('INDIVIDUAL','PRE_REGISTRATION_ADMIN')")
	@PostMapping(path = "/internal/notification", consumes = { "multipart/form-data" })
	@Operation(summary = "sendNotificationsInternal", description  = "Trigger notification by internal batch job", tags = "notification-controller")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseEntity<MainResponseDTO<NotificationResponseDTO>> sendNotificationsInternal(
			@RequestPart(value = "NotificationRequestDTO", required = true) String jsonbObject,
			@RequestPart(value = "langCode", required = false) String langCode,
			@RequestPart(value = "attachment", required = false) MultipartFile file, HttpServletRequest res) {
		log.info("sessionId", "idType", "id",
				"In notification controller for sendNotificationsInternal() with request notification dto  " + jsonbObject);
		log.debug("sessionId", "idType", "id", res.getHeader("Cookie"));
		return new ResponseEntity<>(notificationService.sendNotification(jsonbObject, langCode, file, false),
				HttpStatus.OK);
	}
}