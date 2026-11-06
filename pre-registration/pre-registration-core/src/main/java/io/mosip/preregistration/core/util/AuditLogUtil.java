package io.mosip.preregistration.core.util;

import static io.mosip.preregistration.core.constant.PreRegCoreConstant.LOGGER_ID;
import static io.mosip.preregistration.core.constant.PreRegCoreConstant.LOGGER_IDTYPE;
import static io.mosip.preregistration.core.constant.PreRegCoreConstant.LOGGER_SESSIONID;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;

import jakarta.annotation.PostConstruct;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.core.code.AuditLogVariables;
import io.mosip.preregistration.core.common.dto.AuditRequestDto;
import io.mosip.preregistration.core.common.dto.AuditResponseDto;
import io.mosip.preregistration.core.common.dto.RequestWrapper;
import io.mosip.preregistration.core.common.dto.ResponseWrapper;
import io.mosip.preregistration.core.common.service.UserDetailsService;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.errorcodes.ErrorCodes;
import io.mosip.preregistration.core.errorcodes.ErrorMessages;
import io.mosip.preregistration.core.exception.UserLookupException;
import io.mosip.preregistration.core.exception.util.ParseResponseException;

/**
 * This class is used to connect to the kernel's audit manager & to provide the
 * generic methods to all the pre-registration services.
 * 
 * @author Jagadishwari S
 * @since 1.0.0
 *
 */
@Service
public class AuditLogUtil {
	/**
	 * Performs log config for MOSIP Pre-Registration.
	 *
	 * @return log config result
	 */
	private Logger log = LoggerConfiguration.logConfig(AuditLogUtil.class);

	/**
	 * Host IP Address
	 */
	String hostIP = "";
	/**
	 * Host Name
	 */
	String hostName = "";

	/**
	 * Autowired reference for {@link #RestTemplate}
	 */
	@Qualifier("selfTokenRestTemplate")
	@Autowired
	RestTemplate restTemplate;

	@Autowired
	/**
	 * object mapper used by this Pre-Registration type.
	 */
	private ObjectMapper objectMapper;

	@Autowired
	/**
	 * Resolves login identifiers to the canonical UUID stored on user_details.
	 */
	private UserDetailsService userDetailsService;

	@Value("${audit.url}")
	/**
	 * audit url used by this Pre-Registration type.
	 */
	private String auditUrl;

	/**
	 * Optional local JDBC for mosip_audit. Empty in cluster; WireMock does not persist rows.
	 */
	@Value("${preregistration.audit.jdbc.url:}")
	private String auditJdbcUrl;

	@Value("${preregistration.audit.jdbc.user:}")
	private String auditJdbcUser;

	@Value("${preregistration.audit.jdbc.password:}")
	private String auditJdbcPassword;

	/**
	 * To Set the Host Ip & Host Name
	 */
	@PostConstruct
	public void getHostDetails() {
		hostIP = getServerIp();
		hostName = getServerName();
	}

	/**
	 * This method return ServerIp.
	 *
	 * @return The ServerIp
	 *
	 */
	public String getServerIp() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			return "UNKNOWN-HOST";
		}
	}

	/**
	 * This method return Server Host Name.
	 *
	 * @return The ServerName
	 *
	 */
	public String getServerName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			return "UNKNOWN-HOST";
		}
	}

	/**
	 * Performs save audit details for MOSIP Pre-Registration.
	 *
	 * @param auditRequestDto audit request dto used by save audit details
	 */
	public void saveAuditDetails(AuditRequestDto auditRequestDto) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"In saveAuditDetails method of AugitLogUtil service - " + auditRequestDto);

		auditRequestDto.setActionTimeStamp(LocalDateTime.now(ZoneId.of("UTC")));
		auditRequestDto.setApplicationId(AuditLogVariables.MOSIP_1.toString());
		auditRequestDto.setApplicationName(AuditLogVariables.PREREGISTRATION.toString());
		auditRequestDto.setHostIp(hostIP);
		auditRequestDto.setHostName(hostName);
		auditRequestDto.setCreatedBy(AuditLogVariables.SYSTEM.toString());
		if (auditRequestDto.getId() == null || auditRequestDto.getId().toString().isEmpty()) {
			auditRequestDto.setId(AuditLogVariables.NO_ID.toString());
		}
		if (auditRequestDto.getSessionUserId() == null || auditRequestDto.getSessionUserId().isEmpty()) {
			auditRequestDto.setSessionUserId(AuditLogVariables.SYSTEM.toString());
		}
		if (auditRequestDto.getSessionUserName() == null || auditRequestDto.getSessionUserName().isEmpty()) {
			auditRequestDto.setSessionUserName(AuditLogVariables.SYSTEM.toString());
		}
		auditRequestDto.setSessionUserId(resolveSessionUser(auditRequestDto.getSessionUserId()));
		auditRequestDto.setSessionUserName(resolveSessionUser(auditRequestDto.getSessionUserName()));
		auditRequestDto.setIdType(AuditLogVariables.PRE_REGISTRATION_ID.toString());
		persistLocalAudit(auditRequestDto);
		callAuditManager(auditRequestDto);
	}

	/**
	 * Performs call audit manager for MOSIP Pre-Registration.
	 *
	 * @param auditRequestDto audit request dto used by call audit manager
	 * @return call audit manager result
	 */
	public boolean callAuditManager(AuditRequestDto auditRequestDto) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"In callAuditManager method of AugitLogUtil service - " + auditRequestDto);

		boolean auditFlag = false;
		try {
			UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(auditUrl);
			RequestWrapper<AuditRequestDto> requestKernel = new RequestWrapper<>();
			requestKernel.setRequest(auditRequestDto);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<RequestWrapper<AuditRequestDto>> requestEntity = new HttpEntity<>(requestKernel, headers);
			String uriBuilder = builder.build().encode(StandardCharsets.UTF_8).toUriString();

			log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"In callAuditManager method of AugitLogUtil service auditUrl: " + uriBuilder);
			ResponseEntity<String> responseEntity2 = restTemplate.exchange(uriBuilder, HttpMethod.POST, requestEntity,
					new ParameterizedTypeReference<String>() {
					});
			ResponseWrapper<AuditResponseDto> response = requestBodyExchange(responseEntity2.getBody());
			AuditResponseDto responseDTO = (AuditResponseDto) requestBodyExchangeObject(
					responseToString(response.getResponse()), AuditResponseDto.class);
			auditFlag = responseDTO.isStatus();
		} catch (Exception ex) {
			log.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, ExceptionUtils.getStackTrace(ex));
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"In callAuditManager method of AugitLogUtil Util - " + ex.getMessage());
		}
		return auditFlag;
	}

	/**
	 * Performs request body exchange for MOSIP Pre-Registration.
	 *
	 * @param serviceResponseBody service response body used by request body exchange
	 * @return request body exchange result
	 * @throws ParseResponseException when the operation cannot complete
	 */
	private ResponseWrapper<AuditResponseDto> requestBodyExchange(String serviceResponseBody)
			throws ParseResponseException {
		try {
			return objectMapper.readValue(serviceResponseBody, ResponseWrapper.class);
		} catch (IOException e) {
			throw new ParseResponseException(ErrorCodes.PRG_CORE_REQ_021.getCode(),
					ErrorMessages.ERROR_WHILE_PARSING.getMessage(), null);

		}
	}

	/**
	 * Performs response to string for MOSIP Pre-Registration.
	 *
	 * @param response response used by response to string
	 * @return response to string result
	 */
	private String responseToString(Object response) {
		try {
			return objectMapper.writeValueAsString(response);
		} catch (JsonProcessingException e) {

			throw new ParseResponseException("", "", null);
		}
	}

	/**
	 * Performs request body exchange object for MOSIP Pre-Registration.
	 *
	 * @param serviceResponseBody service response body used by request body exchange object
	 * @param responseClass response class used by request body exchange object
	 * @return request body exchange object result
	 * @throws ParseResponseException when the operation cannot complete
	 */
	private Object requestBodyExchangeObject(String serviceResponseBody, Class<?> responseClass)
			throws ParseResponseException {
		try {
			objectMapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			return objectMapper.readValue(serviceResponseBody, responseClass);
		} catch (IOException e) {
			log.debug(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, ExceptionUtils.getStackTrace(e));
			throw new ParseResponseException(ErrorCodes.PRG_CORE_REQ_021.getCode(),
					ErrorMessages.ERROR_WHILE_PARSING.getMessage(), null);

		}
	}

	/**
	 * For auditing Login Services
	 * 
	 * @param auditRequestDto
	 * @param token
	 * @return
	 */
	public void saveAuditDetails(AuditRequestDto auditRequestDto, HttpHeaders header) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"In saveAuditDetails method of AugitLogUtil service - " + auditRequestDto);

		auditRequestDto.setActionTimeStamp(LocalDateTime.now(ZoneId.of("UTC")));
		auditRequestDto.setApplicationId(AuditLogVariables.MOSIP_1.toString());
		auditRequestDto.setApplicationName(AuditLogVariables.PREREGISTRATION.toString());
		auditRequestDto.setHostIp(hostIP);
		auditRequestDto.setHostName(hostName);
		auditRequestDto.setCreatedBy(AuditLogVariables.SYSTEM.toString());
		if (auditRequestDto.getId() == null || auditRequestDto.getId().toString().isEmpty()) {
			auditRequestDto.setId(AuditLogVariables.NO_ID.toString());
		}
		if (auditRequestDto.getSessionUserId() == null || auditRequestDto.getSessionUserId().isEmpty()) {
			auditRequestDto.setSessionUserId(AuditLogVariables.SYSTEM.toString());
		}
		if (auditRequestDto.getSessionUserName() == null || auditRequestDto.getSessionUserName().isEmpty()) {
			auditRequestDto.setSessionUserName(AuditLogVariables.SYSTEM.toString());
		}
		auditRequestDto.setSessionUserId(resolveSessionUser(auditRequestDto.getSessionUserId()));
		auditRequestDto.setSessionUserName(resolveSessionUser(auditRequestDto.getSessionUserName()));
		auditRequestDto.setIdType(AuditLogVariables.PRE_REGISTRATION_ID.toString());
		persistLocalAudit(auditRequestDto);
		callAuditManager(auditRequestDto, header);
	}

	/**
	 * Replaces a login identifier with the canonical user UUID so audit rows do not
	 * store email or phone. {@code SYSTEM} and UUID values are left unchanged.
	 *
	 * @param sessionUser session user id or name from the caller
	 * @return UUID, {@code SYSTEM}, or a masked placeholder when lookup fails
	 */
	private String resolveSessionUser(String sessionUser) {
		if (sessionUser == null || sessionUser.isBlank()
				|| AuditLogVariables.SYSTEM.toString().equals(sessionUser)
				|| GenericUtil.isUuid(sessionUser)) {
			return sessionUser;
		}
		try {
			String canonicalUserId = userDetailsService.getOrCreateInternalUserId(sessionUser);
			if (canonicalUserId != null && !canonicalUserId.isBlank()) {
				return canonicalUserId;
			}
		} catch (UserLookupException ex) {
			log.warn(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"Failed to resolve UUID for audit user " + GenericUtil.maskIdentifier(sessionUser));
		}
		return GenericUtil.maskIdentifier(sessionUser);
	}

	/**
	 * Writes the audit row into mosip_audit when local JDBC is configured. Cluster leaves
	 * {@code preregistration.audit.jdbc.url} blank so this is a no-op.
	 */
	private void persistLocalAudit(AuditRequestDto auditRequestDto) {
		if (auditJdbcUrl == null || auditJdbcUrl.isBlank()) {
			return;
		}
		LocalDateTime actionTime = auditRequestDto.getActionTimeStamp() == null
				? LocalDateTime.now(ZoneId.of("UTC"))
				: auditRequestDto.getActionTimeStamp();
		try (Connection connection = DriverManager.getConnection(auditJdbcUrl, auditJdbcUser, auditJdbcPassword);
				PreparedStatement statement = connection.prepareStatement(
						"INSERT INTO audit.app_audit_log (log_id, log_dtimes, log_desc, event_id, event_type, event_name, "
								+ "action_dtimes, host_name, host_ip, session_user_id, session_user_name, app_id, app_name, "
								+ "module_id, module_name, ref_id, ref_id_type, cr_by) "
								+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")) {
			int i = 1;
			statement.setString(i++, UUID.randomUUID().toString());
			statement.setTimestamp(i++, Timestamp.from(Instant.now()));
			statement.setString(i++, clip(auditRequestDto.getDescription(), 2048));
			statement.setString(i++, clip(nvl(auditRequestDto.getEventId(), "UNKNOWN"), 64));
			statement.setString(i++, clip(nvl(auditRequestDto.getEventType(), "USER"), 64));
			statement.setString(i++, clip(nvl(auditRequestDto.getEventName(), "UNKNOWN"), 128));
			statement.setTimestamp(i++, Timestamp.from(actionTime.toInstant(ZoneOffset.UTC)));
			statement.setString(i++, clip(nvl(auditRequestDto.getHostName(), "UNKNOWN-HOST"), 128));
			statement.setString(i++, clip(nvl(auditRequestDto.getHostIp(), "UNKNOWN-HOST"), 256));
			statement.setString(i++, clip(nvl(auditRequestDto.getSessionUserId(), "SYSTEM"), 256));
			statement.setString(i++, clip(auditRequestDto.getSessionUserName(), 128));
			statement.setString(i++, clip(nvl(auditRequestDto.getApplicationId(), "MOSIP_1"), 64));
			statement.setString(i++, clip(nvl(auditRequestDto.getApplicationName(), "PREREGISTRATION"), 128));
			statement.setString(i++, clip(auditRequestDto.getModuleId(), 64));
			statement.setString(i++, clip(auditRequestDto.getModuleName(), 128));
			statement.setString(i++, clip(nvl(auditRequestDto.getId(), "NO_ID"), 64));
			statement.setString(i++, clip(nvl(auditRequestDto.getIdType(), "PRE_REGISTRATION_ID"), 64));
			statement.setString(i++, clip(nvl(auditRequestDto.getCreatedBy(), "SYSTEM"), 256));
			statement.executeUpdate();
		} catch (Exception ex) {
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"Local audit JDBC persist failed: " + ex.getMessage());
		}
	}

	private static String nvl(String value, String fallback) {
		return value == null || value.isBlank() ? fallback : value;
	}

	private static String clip(String value, int max) {
		if (value == null) {
			return null;
		}
		return value.length() > max ? value.substring(0, max) : value;
	}

	/**
	 * For Auditing Login Services
	 * 
	 * @param auditRequestDto
	 * @param token
	 * @return
	 */
	public boolean callAuditManager(AuditRequestDto auditRequestDto, HttpHeaders headers) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"In callAuditManager method of AugitLogUtil service - " + auditRequestDto);

		boolean auditFlag = false;
		try {
			UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(auditUrl);
			RequestWrapper<AuditRequestDto> requestKernel = new RequestWrapper<>();
			requestKernel.setRequest(auditRequestDto);
			HttpEntity<RequestWrapper<AuditRequestDto>> requestEntity = new HttpEntity<>(requestKernel, headers);
			String uriBuilder = builder.build().encode(StandardCharsets.UTF_8).toUriString();

			log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"In callAuditManager method of AugitLogUtil service auditUrl: " + uriBuilder);
			ResponseEntity<ResponseWrapper<AuditResponseDto>> responseEntity2 = restTemplate.exchange(uriBuilder,
					HttpMethod.POST, requestEntity,
					new ParameterizedTypeReference<ResponseWrapper<AuditResponseDto>>() {
					});
			ResponseWrapper<AuditResponseDto> body = responseEntity2.getBody();
			if (body != null) {
				auditFlag = body.getResponse().isStatus();
			}
		} catch (Exception ex) {
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"In callAuditManager method of AugitLogUtil Util - " + ex.getMessage());
		}
		return auditFlag;
	}
}
