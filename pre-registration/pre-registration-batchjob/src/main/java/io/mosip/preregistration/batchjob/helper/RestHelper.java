package io.mosip.preregistration.batchjob.helper;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.batchjob.code.PreRegBatchContants;
import io.mosip.preregistration.batchjob.model.ExceptionalHolidayResponseDto;
import io.mosip.preregistration.batchjob.model.RegistrationCenterDto;
import io.mosip.preregistration.batchjob.model.RegistrationCenterHolidayDto;
import io.mosip.preregistration.batchjob.model.WorkingDaysResponseDto;
import io.mosip.preregistration.core.code.AuditLogVariables;
import io.mosip.preregistration.core.common.dto.AuditRequestDto;
import io.mosip.preregistration.core.common.dto.AuditResponseDto;
import io.mosip.preregistration.core.common.dto.CancelBookingResponseDTO;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.NotificationDTO;
import io.mosip.preregistration.core.common.dto.NotificationResponseDTO;
import io.mosip.preregistration.core.common.dto.RequestWrapper;
import io.mosip.preregistration.core.config.LoggerConfiguration;

/**
 * @author Mahammed Taheer
 * @since 1.2.0
 *
 */
@Component
public class RestHelper {
    
    private Logger LOGGER = LoggerConfiguration.logConfig(RestHelper.class);

    /**
	 * Reference for ${regCenter.url} from property file
	*/
	@Value("${regCenter.url}")
	String regCenterDetailsURL;

    /**
	 * Reference for ${holiday.url} from property file
	*/
    @Value("${holiday.url}")
	String holidayListUrl;

    /**
	 * Reference for ${holiday.exceptional.url} from property file
	 */
	@Value("${holiday.exceptional.url}")
	String exceptionalHolidayListUrl;

    /**
	 * Reference for ${working.day.url} from property file
	 */
	@Value("${working.day.url}")
	String workingDayListUrl;

    @Value("${batch.appointment.cancel}")
	String cancelApplicationURL;

    @Value("${notification.url}")
	private String sendNotificationURL;

    @Value("${audit.url}")
	private String auditEntryURL;

    @Value("#{${mosip.kernel.masterdata.day.codes.map}}")
	private Map<String, String> dayCodesMap;

    @Autowired
	private ObjectMapper objectMapper;
    
    @Qualifier("selfTokenWebClient")
    @Autowired
    private WebClient webClient; 

    @Qualifier("selfTokenRestTemplate")
    @Autowired
    private RestTemplate restTemplate;

    private String hostIP;

	private String hostName;

    @PostConstruct
	public void init() {
		hostIP = getServerIp();
		hostName = getServerName();
        if(Objects.isNull(dayCodesMap)){
            dayCodesMap = new HashMap<>();
        }
	}

    public int getRegistrationCenterTotalPages() {

        String regCentersDetailsPageNo = new StringBuilder(regCenterDetailsURL)
                                                    .append("/")
                                                    .append(PreRegBatchContants.ALL)
                                                    .append(PreRegBatchContants.PAGE_NO + "0")
                                                    .append(PreRegBatchContants.PAGE_SIZE)
                                                    .append(PreRegBatchContants.SORT_BY)
                                                    .append(PreRegBatchContants.ORDER_BY).toString();

        LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
                    "Fetching the Registration Center Details from Master Data Service. Configured URL: " + regCentersDetailsPageNo);
        try {
            
            ObjectNode responseNode = sendWebClientRequest(regCentersDetailsPageNo);
            if (Objects.isNull(responseNode)) {
                LOGGER.error("Not Received the Registration Center details from Master Data Service.");
                return 0;
            }
            ObjectNode objectNode = objectMapper.convertValue(responseNode.get(PreRegBatchContants.RESPONSE), ObjectNode.class);
            LOGGER.info("Received the Registration Center details from Master Data Service.");
            int totalPages = objectNode.get("totalPages").asInt();
            LOGGER.info("Total Number of Pages received: " + totalPages);
            return totalPages;
        } catch (Exception exp) {
            LOGGER.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
                    "Unknown Error in fetching registration center details." + exp.getMessage(), exp);
        } 
        LOGGER.warn("Unknown error, Registration Center details not received.");
        return 0;
    }

    public List<RegistrationCenterDto> getRegistrationCenterDetails(List<String> pageNos, RegCenterIdsHolder idsHolder) {

        try {
            List<RegistrationCenterDto> filteredRegCentersList = new ArrayList<>();
            for (String pageNo : pageNos) {
                String regCentersDetailsPageNo = new StringBuilder(regCenterDetailsURL)
                                                    .append("/")
                                                    .append(PreRegBatchContants.ALL)
                                                    .append(PreRegBatchContants.PAGE_NO + pageNo)
                                                    .append(PreRegBatchContants.PAGE_SIZE)
                                                    .append(PreRegBatchContants.SORT_BY)
                                                    .append(PreRegBatchContants.ORDER_BY).toString();
                LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
                        "Fetching the Registration Center Details from Master Data Service. Configured URL: " + regCentersDetailsPageNo);
                ObjectNode responseNode = sendWebClientRequest(regCentersDetailsPageNo);
                if (Objects.isNull(responseNode)) {
                    LOGGER.error("Not Received the Registration Center details from Master Data Service.");
                    return new ArrayList<RegistrationCenterDto>();
                }
                ObjectNode objectNode = objectMapper.convertValue(responseNode.get(PreRegBatchContants.RESPONSE), ObjectNode.class);
                List<RegistrationCenterDto> regCenterDetails = objectMapper.readValue(objectNode.get(PreRegBatchContants.DATA).toString(), 
                        new TypeReference<List<RegistrationCenterDto>>(){});
                LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY,
                            "Received the Registration Center details from Master Data Service.");
                
                for (RegistrationCenterDto regCenterDetail : regCenterDetails) {
                    String regCenterId = regCenterDetail.getId();
                    if (Objects.nonNull(idsHolder) && !idsHolder.containsRegCenterId(regCenterId)){
                        idsHolder.addRegCenterId(regCenterId);
                        filteredRegCentersList.add(regCenterDetail);
                    } else {
                        filteredRegCentersList.add(regCenterDetail);
                    }
                }
            }
            return filteredRegCentersList;
        } catch (Exception exp) {
            LOGGER.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
                    "Unknown Error in fetching registration center details." + exp.getMessage(), exp);
        } 
        LOGGER.warn("Unknown error, Registration Center details not received.");
        return new ArrayList<RegistrationCenterDto>();
    }

    public List<String> getRegistrationHolidayList(String regCenterId, String regCenterLangCode, int noOfDaysToSync) {

        LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
                    "Fetching the Registration Center Holidays list from Master Data Service.");
        List<String> holidaysList = new ArrayList<>();
        
        addGeneralHolidaysList(regCenterId, holidaysList);
        
        addExceptionalHolidaysList(regCenterId, holidaysList);

        addWeekOffHolidays(regCenterId, regCenterLangCode, noOfDaysToSync, holidaysList);
        return holidaysList;
    }

    private void addGeneralHolidaysList(String regCenterId, List<String> holidaysLst) {
        
        try {
            String generalHolidayListUrl = new StringBuilder(holidayListUrl).append(PreRegBatchContants.ALL).append("/")
                                                               .append(regCenterId).append("/")
                                                               .append(LocalDate.now().getYear()).toString();
            LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
                    "Registration Center General Holidays list for URL: " + generalHolidayListUrl);
            ObjectNode responseNode = sendWebClientRequest(generalHolidayListUrl);
            if (Objects.isNull(responseNode)) {
                LOGGER.error("Not Received the General Holiday List from Master Data Service.");
                return;
            }
            
            RegistrationCenterHolidayDto generalHolidayObject = objectMapper.convertValue(
                        responseNode.get(PreRegBatchContants.RESPONSE), RegistrationCenterHolidayDto.class);
            generalHolidayObject.getHolidays().stream().forEach(holiday -> holidaysLst.add(holiday.getHolidayDate()));
            LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
                    "Added Holiday List for URL: " + generalHolidayListUrl);
        } catch (Exception exp) {
            LOGGER.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, regCenterId, 
                    "Unknown Error in fetching registration center holiday List." + exp.getMessage(), exp);
        }
    }

    private void addExceptionalHolidaysList(String regCenterId, List<String> holidaysLst) {
        
        try {
            String exceptionalHolidayListEndpoint = new StringBuilder(exceptionalHolidayListUrl).append(regCenterId).toString();
            LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
                                            "Registration Center Exceptional Holidays list for URL: " + exceptionalHolidayListEndpoint);
            ObjectNode responseNode = sendWebClientRequest(exceptionalHolidayListEndpoint);
            if (Objects.isNull(responseNode)) {
                LOGGER.error("Not Received the Exceptional Holiday List from Master Data Service.");
                return;
            }
            
            ExceptionalHolidayResponseDto exceptionalHolidayObj = objectMapper.convertValue(
                            responseNode.get(PreRegBatchContants.RESPONSE), ExceptionalHolidayResponseDto.class);
            exceptionalHolidayObj.getExceptionalHolidayList().stream()
                                 .forEach(holiday -> holidaysLst.add(holiday.getHolidayDate().toString()));
            LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
                    "Added Holiday List for URL: " + exceptionalHolidayListEndpoint);
        } catch (Exception exp) {
            LOGGER.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, regCenterId, 
                    "Unknown Error in fetching registration center holiday List." + exp.getMessage(), exp);
        }
    }

    private void addWeekOffHolidays(String regCenterId, String regCenterLangCode, int noOfDaysToSync, List<String> holidaysLst) {

        try {
            String workingDaysListEndpoint = new StringBuilder(workingDayListUrl).append(regCenterId).append("/")
                                                               .append(regCenterLangCode).toString();
            LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
                                            "Registration Center Working days list for URL: " + workingDaysListEndpoint);
            ObjectNode responseNode = sendWebClientRequest(workingDaysListEndpoint);
            if (Objects.isNull(responseNode)) {
                LOGGER.error("Not Received the Working Day List from Master Data Service.");
                return;
            }
            
            WorkingDaysResponseDto workingDaysResponseDto = objectMapper.convertValue(
                            responseNode.get(PreRegBatchContants.RESPONSE), WorkingDaysResponseDto.class);

            List<String> workingDaysList = new ArrayList<>();
            workingDaysResponseDto.getWorkingdays().stream().forEach(weekDay -> workingDaysList.add(weekDay.getCode()));
            LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
                "Working Days: " + workingDaysList);

            LocalDate.now().datesUntil(LocalDate.now().plusDays(noOfDaysToSync))
                                                      .forEach(weekDay -> {
                                                        if (!workingDaysList.contains(dayCodesMap.get(weekDay.getDayOfWeek().toString()))){
                                                            holidaysLst.add(weekDay.toString());
                                                        }});
            LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
                "Added Holiday List for URL: " + workingDaysListEndpoint);

        } catch (Exception exp) {
            LOGGER.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, regCenterId, 
                    "Unknown Error in fetching registration center holiday List." + exp.getMessage(), exp);
        }
    }

    private ObjectNode sendWebClientRequest(String anyEndPoint) {
        try {
            ClientResponse response =  webClient.method(HttpMethod.GET)
                                                .uri(UriComponentsBuilder.fromUriString(anyEndPoint).toUriString())
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .exchange().block();
            ObjectNode responseObjNode = null;
            if (response != null) {
            	responseObjNode = response.bodyToMono(ObjectNode.class).block();	
            }
            if (response != null && response.statusCode() == HttpStatus.OK) {
                if (responseObjNode != null && responseObjNode.has(PreRegBatchContants.ERRORS) && !responseObjNode.get(PreRegBatchContants.ERRORS).isNull()) {
                    LOGGER.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY,
                        "Error in response for URL: " + anyEndPoint + ", Errors:" 
                            + responseObjNode.get(PreRegBatchContants.ERRORS).toString());
                    return null;
                }
                return responseObjNode; 
            } else {
            	if (responseObjNode != null) {
            		LOGGER.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY,
                            "Response Code: " + response.statusCode() +
                            ", Error in response for URL: " + anyEndPoint + ", Errors:" 
                                + responseObjNode.get(PreRegBatchContants.ERRORS).toString());	
            	} else {
            		LOGGER.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY,
                            "Error in response for URL: " + anyEndPoint + ", Errors:" 
                                + response);
            	}
                
            }
        } catch (Throwable t) {
            LOGGER.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
                    "Unknown Error in fetching data for endpoint: " + anyEndPoint + ", Error: "  + t.getMessage(), t);
        }
        return null;
    }

    public boolean cancelBookedApplication(String preRegId, String logIdentifier) {

        LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
                "Cancelling Booked Application Pre Reg Id: " + preRegId);
        String uriBuilder = "";
        try {
            Map<String, Object> params = new HashMap<>();
			params.put("preRegistrationId", preRegId);
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(cancelApplicationURL);
			uriBuilder = builder.buildAndExpand(params).encode().toUriString();
            LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
                "Cancelling Booked Application endpoint: " + uriBuilder);

            ResponseEntity<ObjectNode> response =  restTemplate.exchange(uriBuilder, HttpMethod.PUT, null, ObjectNode.class);
            ObjectNode responseObjNode = response.getBody();
            
            if (responseObjNode != null && response.getStatusCode().is2xxSuccessful()) {
            	if (responseObjNode.has(PreRegBatchContants.ERRORS) && !responseObjNode.get(PreRegBatchContants.ERRORS).isNull()) {
                    LOGGER.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier,
                        "Error in response for URL: " + uriBuilder + ", Errors:" 
                            +  responseObjNode.get(PreRegBatchContants.ERRORS).toString());
                    return false;
                }
                CancelBookingResponseDTO cancelResponse = objectMapper.convertValue(
                                responseObjNode.get(PreRegBatchContants.RESPONSE), CancelBookingResponseDTO.class);
                LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
                            "Booked Application cancelled for pre reg Id: " + preRegId + 
                            ", Server Tranaction Id: " + cancelResponse.getTransactionId() +
                            ", Server Response Message: " + cancelResponse.getMessage());
                return true;	
                 
            } else {
            	if (responseObjNode != null) {
            		LOGGER.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY,
                            "Response Code: " + response.getStatusCode() +
                            ", Error in response for URL: " + uriBuilder + ", Errors:" 
                                + responseObjNode.get(PreRegBatchContants.ERRORS).toString());	
            	} else {
            		LOGGER.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY,
                            "Response Code: " + response.getStatusCode() +
                            ", Error in response for URL: " + uriBuilder + ", Errors:" 
                                + response);
            	}
                
                return false;
            } 

           /*  ClientResponse response =  webClient.method(HttpMethod.PUT)
                                                .uri(uriBuilder)
                                                .contentType(MediaType.APPLICATION_JSON_UTF8)
                                                .exchange()
                                                .block();
                                                
            ObjectNode responseObjNode = response.bodyToMono(ObjectNode.class).block();
            if (response.statusCode() == HttpStatus.OK) {
                if (responseObjNode.has(PreRegBatchContants.ERRORS) && !responseObjNode.get(PreRegBatchContants.ERRORS).isNull()) {
                    LOGGER.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier,
                        "Error in response for URL: " + uriBuilder + ", Errors:" 
                            +  responseObjNode.get(PreRegBatchContants.ERRORS).toString());
                    return false;
                }
                CancelBookingResponseDTO cancelResponse = objectMapper.convertValue(
                                responseObjNode.get(PreRegBatchContants.RESPONSE), CancelBookingResponseDTO.class);
                LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
                            "Booked Application cancelled for pre reg Id: " + preRegId + 
                            ", Server Tranaction Id: " + cancelResponse.getTransactionId() +
                            ", Server Response Message: " + cancelResponse.getMessage());
                return true; 
            } else {
                LOGGER.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY,
                        "Response Code: " + response.statusCode() +
                        ", Error in response for URL: " + uriBuilder + ", Errors:" 
                            + responseObjNode.get(PreRegBatchContants.ERRORS).toString());
                return false;
            } */
        } catch (Throwable t) {
            LOGGER.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
                    "Unknown Error in fetching data for endpoint: " + uriBuilder + ", Error: "  + t.getMessage(), t);
        }
        return false;
    }

    public boolean sendCancelledNotification(String preRegId, String regDate, String regTime, String langCode, String logIdentifier) {
        try {

            LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
                "Sending Cancelling Notification for Booked Application Pre Reg Id: " + sendNotificationURL + ", Pre Reg Id: " + preRegId);
 
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            LinkedMultiValueMap<String, Object> requestValueMap = buildNotificationRequest(preRegId, regDate, regTime, langCode, logIdentifier);
            HttpEntity<LinkedMultiValueMap<String, Object>> httpEntity = new HttpEntity<LinkedMultiValueMap<String, Object>>(requestValueMap, headers);

            String notifyEailResourseUrl = UriComponentsBuilder.fromUriString(sendNotificationURL).toUriString();
            ResponseEntity<ObjectNode> response = restTemplate.exchange(notifyEailResourseUrl, HttpMethod.POST, httpEntity, ObjectNode.class);
            ObjectNode responseObjNode = response.getBody();
            
            if (responseObjNode != null && response.getStatusCode().is2xxSuccessful()) {
                if (responseObjNode.has(PreRegBatchContants.ERRORS) && !responseObjNode.get(PreRegBatchContants.ERRORS).isNull()) {
                    LOGGER.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier,
                        "Error in response for URL: " + sendNotificationURL + ", Errors:" 
                            +  responseObjNode.get(PreRegBatchContants.ERRORS).toString());
                    return false;
                }
                NotificationResponseDTO notifyResponse = objectMapper.convertValue(
                                responseObjNode.get(PreRegBatchContants.RESPONSE), NotificationResponseDTO.class);
                LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
                            "Nofitication Sent for cancelled Application for pre reg Id: " + preRegId + 
                            ", Notification Status: " + notifyResponse.getStatus() +
                            ", Notification Response Message: " + notifyResponse.getMessage());
                return true; 
			} else {
				if (responseObjNode != null) {
					LOGGER.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH,
							PreRegBatchContants.EMPTY,
							"Response Code: " + response.getStatusCode() + ", Error in response for URL: "
									+ sendNotificationURL + ", Errors:"
									+ responseObjNode.get(PreRegBatchContants.ERRORS).toString());
				} else {
					LOGGER.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH,
							PreRegBatchContants.EMPTY,
							"Response Code: " + response.getStatusCode() + ", Error in response for URL: "
									+ sendNotificationURL + ", Errors:" + response);
				}
				return false;
			}

            /* MultiValueMap<String, String> requestValueMap = buildNotificationRequest(preRegId, regDate, regTime, langCode, logIdentifier);
            ClientResponse response =  webClient.method(HttpMethod.POST)
                                                .uri(UriComponentsBuilder.fromUriString(sendNotificationURL).toUriString())
                                                .headers(httpHeaders -> {
                                                    httpHeaders.addAll(headers);
                                                 })
                                                .body(BodyInserters.fromFormData(requestValueMap))
                                                .exchange().block();
            ObjectNode responseObjNode = response.bodyToMono(ObjectNode.class).block();
            if (response.statusCode() == HttpStatus.OK) {
                if (responseObjNode.has(PreRegBatchContants.ERRORS) && !responseObjNode.get(PreRegBatchContants.ERRORS).isNull()) {
                    LOGGER.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier,
                        "Error in response for URL: " + sendNotificationURL + ", Errors:" 
                            + responseObjNode.get(PreRegBatchContants.ERRORS).toString());
                    return false;
                }
                NotificationResponseDTO notifyResponse = objectMapper.convertValue(
                                responseObjNode.get(PreRegBatchContants.RESPONSE), NotificationResponseDTO.class);
                LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
                            "Booked Application cancelled for pre reg Id: " + preRegId + 
                            ", Notification Status: " + notifyResponse.getStatus() +
                            ", Notification Response Message: " + notifyResponse.getMessage());
                return true; 
            } else {
                LOGGER.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY,
                        "Response Code: " + response.statusCode() +
                        ", Error in response for URL: " + sendNotificationURL + ", Errors:" 
                            + responseObjNode.get(PreRegBatchContants.ERRORS).toString());
                return false;
            } */
        } catch (Throwable t) {
            LOGGER.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
                    "Unknown Error in fetching data for endpoint: " + sendNotificationURL + ", Error: "  + t.getMessage(), t);
        }
        return false;

    }

    private LinkedMultiValueMap<String, Object>  buildNotificationRequest(String preRegId, String regDate, String regTime, String langCode, String logIdentifier) {
        NotificationDTO notificationDetails = new NotificationDTO();
        notificationDetails.setAppointmentDate(regDate);
        notificationDetails.setPreRegistrationId(preRegId);
        String time = LocalTime.parse(regTime, DateTimeFormatter.ofPattern("HH:mm")).format(DateTimeFormatter.ofPattern("hh:mm a"));
        notificationDetails.setAppointmentTime(time);
        notificationDetails.setAdditionalRecipient(false);
        notificationDetails.setIsBatch(true);
        try {
            MainRequestDTO<NotificationDTO> request = new MainRequestDTO<>();
            objectMapper.setTimeZone(TimeZone.getDefault());
            request.setRequest(notificationDetails);
            request.setId(PreRegBatchContants.NOTIFICATION_PRE_REG_ID);
            request.setVersion(PreRegBatchContants.NOTIFICATION_PRE_REG_VER);
            request.setRequesttime(new Date());

            LinkedMultiValueMap<String, Object> valueMap = new LinkedMultiValueMap<>();
            valueMap.add(PreRegBatchContants.NOTIFICATION_REQ_DTO, objectMapper.writeValueAsString(request));
            valueMap.add(PreRegBatchContants.LANG_CODE, langCode);
            return valueMap;
        } catch(JsonProcessingException exp) {
            LOGGER.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
                    "Unknown Error in fetching data for endpoint: " + sendNotificationURL + ", Error: "  + exp.getMessage(), exp);
        }
        return null;
    }

    public boolean sendAuditDetails(String eventId, String eventName, String eventType, String description, String idType,
                            String userId, String userName, String regCenterIds, String moduleId, String moduleName) {
        try {

            LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
                "Sending Audit details for : " + moduleName + ", For Job: " +  auditEntryURL);
            
            AuditRequestDto requestDto = buildAuditRequestDto(eventId, eventName, eventType, description, idType,
			                                        userId, userName, regCenterIds, moduleId, moduleName);
            RequestWrapper<AuditRequestDto> requestAudit = new RequestWrapper<>();
			requestAudit.setRequest(requestDto);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
            HttpEntity<RequestWrapper<AuditRequestDto>> requestEntity = new HttpEntity<>(requestAudit, headers);

            String notifyEailResourseUrl = UriComponentsBuilder.fromUriString(auditEntryURL).toUriString();
            ResponseEntity<ObjectNode> response = restTemplate.exchange(notifyEailResourseUrl, HttpMethod.POST, requestEntity, ObjectNode.class);
            ObjectNode responseObjNode = response.getBody();
            if (responseObjNode != null && response.getStatusCode().is2xxSuccessful()) {
                if (responseObjNode.has(PreRegBatchContants.ERRORS) && !responseObjNode.get(PreRegBatchContants.ERRORS).isNull()) {
                    LOGGER.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY,
                        "Error in response for URL: " + sendNotificationURL + ", Errors:" 
                            +  responseObjNode.get(PreRegBatchContants.ERRORS).toString());
                    return false;
                }
                AuditResponseDto auditResponse = objectMapper.convertValue(
                                responseObjNode.get(PreRegBatchContants.RESPONSE), AuditResponseDto.class);
                LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
                            "Audit Sent for Job Completion, Job Name: " + moduleName + 
                            ", Notification Status: " + auditResponse.isStatus());
                return true; 
            } else {
				if (responseObjNode != null) {
					LOGGER.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH,
							PreRegBatchContants.EMPTY,
							"Response Code: " + response.getStatusCode() + ", Error in response for URL: "
									+ sendNotificationURL + ", Errors:"
									+ responseObjNode.get(PreRegBatchContants.ERRORS).toString());
				} else {
					LOGGER.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH,
							PreRegBatchContants.EMPTY,
							"Response Code: " + response.getStatusCode() + ", Error in response for URL: "
									+ sendNotificationURL + ", Errors:" + response);
				}
                return false;
            } 
        } catch (Throwable t) {
            LOGGER.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
                    "Unknown Error in fetching data for endpoint: " + sendNotificationURL + ", Error: "  + t.getMessage(), t);
        }
        return false;
    }

    private AuditRequestDto buildAuditRequestDto(String eventId, String eventName, String eventType, String description, String idType,
			String userId, String userName, String regCenterIds, String moduleId, String moduleName) {
		AuditRequestDto auditRequestDto = new AuditRequestDto();
		auditRequestDto.setEventId(eventId);
		auditRequestDto.setEventName(eventName);
		auditRequestDto.setEventType(eventType);
		auditRequestDto.setDescription(description + " " + regCenterIds);
		auditRequestDto.setId(idType);
		auditRequestDto.setSessionUserId(userId);
		auditRequestDto.setSessionUserName(userName);
		auditRequestDto.setModuleId(moduleId);
		auditRequestDto.setModuleName(moduleName);
        auditRequestDto.setActionTimeStamp(LocalDateTime.now(ZoneId.of("UTC")));
		auditRequestDto.setApplicationId(AuditLogVariables.MOSIP_1.toString());
		auditRequestDto.setApplicationName(AuditLogVariables.PREREGISTRATION.toString());
		auditRequestDto.setHostIp(hostIP);
		auditRequestDto.setHostName(hostName);
		auditRequestDto.setCreatedBy(AuditLogVariables.SYSTEM.toString());
	    auditRequestDto.setIdType(AuditLogVariables.PRE_REGISTRATION_ID.toString());
        return auditRequestDto;
	}

    private String getServerIp() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			return "UNKNOWN-IP";
		}
	}
	
	private String getServerName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			return "UNKNOWN-HOST";
		}
	}

}
