package io.mosip.preregistration.batchjob.helper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.batchjob.code.PreRegBatchContants;
import io.mosip.preregistration.batchjob.model.ExceptionalHolidayResponseDto;
import io.mosip.preregistration.batchjob.model.RegistrationCenterDto;
import io.mosip.preregistration.batchjob.model.RegistrationCenterHolidayDto;
import io.mosip.preregistration.batchjob.model.RegistrationCenterResponseDto;
import io.mosip.preregistration.batchjob.model.WorkingDaysResponseDto;
import io.mosip.preregistration.core.common.dto.CancelBookingResponseDTO;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.NotificationDTO;
import io.mosip.preregistration.core.common.dto.NotificationResponseDTO;
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

    @Autowired
	private ObjectMapper objectMapper;
    
    @Qualifier("selfTokenWebClient")
    @Autowired
    private WebClient webClient; 

    public List<RegistrationCenterDto> getRegistrationCenterDetails() {

        LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
                    "Fetching the Registration Center Details from Master Data Service. Configured URL: " + regCenterDetailsURL);
        try {
            ObjectNode responseNode = sendWebClientRequest(regCenterDetailsURL);
            if (Objects.isNull(responseNode)) {
                LOGGER.error("Not Received the Registration Center details from Master Data Service.");
                return new ArrayList<RegistrationCenterDto>();
            }
            List<RegistrationCenterDto> regCenterDetails = objectMapper.convertValue(responseNode.get(PreRegBatchContants.RESPONSE), 
                    RegistrationCenterResponseDto.class).getRegistrationCenters();
            LOGGER.info("Received the Registration Center details from Master Data Service.");
            //return regCenterDetails;
            List<RegistrationCenterDto> tempTestList = //new ArrayList<>();
            regCenterDetails.stream().filter(regCenter -> regCenter.getId().equals("10001")).collect(Collectors.toList());
            //tempTestList.add(regCenterDetails.get(0));
            return tempTestList;
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
        
        addGeneralHolidaysList(regCenterId, regCenterLangCode, holidaysList);
        
        addExceptionalHolidaysList(regCenterId, regCenterLangCode, holidaysList);

        addWeekOffHolidays(regCenterId, regCenterLangCode, noOfDaysToSync, holidaysList);
        return holidaysList;
    }

    private void addGeneralHolidaysList(String regCenterId, String regCenterLangCode, List<String> holidaysLst) {
        
        try {
            String generalHolidayListUrl = new StringBuilder(holidayListUrl).append(regCenterLangCode).append("/")
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
            LOGGER.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
                    "Unknown Error in fetching registration center holiday List." + exp.getMessage(), exp);
        }
    }

    private void addExceptionalHolidaysList(String regCenterId, String regCenterLangCode, List<String> holidaysLst) {
        
        try {
            String exceptionalHolidayListEndpoint = new StringBuilder(exceptionalHolidayListUrl).append(regCenterId).append("/")
                                                               .append(regCenterLangCode).toString();
            LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
                                            "Registration Center Exceptional Holidays list for URL: " + exceptionalHolidayListEndpoint);
            ObjectNode responseNode = sendWebClientRequest(exceptionalHolidayListEndpoint);
            if (Objects.isNull(responseNode)) {
                LOGGER.error("Not Received the Exceptional Holiday List from Master Data Service.");
                return;
            }
            
            ExceptionalHolidayResponseDto exceptionalHolidayObj = objectMapper.convertValue(
                            responseNode.get(PreRegBatchContants.RESPONSE), ExceptionalHolidayResponseDto.class);
            String currentYear = Integer.toString(LocalDate.now().getYear());
            exceptionalHolidayObj.getExceptionalHolidayList().stream()
                                 .filter(holiday -> holiday.getHolidayYear().equals(currentYear))
                                 .forEach(holiday -> holidaysLst.add(holiday.getHolidayDate().toString()));
            LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
                    "Added Holiday List for URL: " + exceptionalHolidayListEndpoint);
        } catch (Exception exp) {
            LOGGER.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
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
            workingDaysResponseDto.getWeekdays().stream().filter(weekDay -> weekDay.isWorking())
                    .forEach(weekDay -> workingDaysList.add(weekDay.getName()));
            LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
                "Working Days: " + workingDaysList);

            List<String> weekOffDaysList = new ArrayList<>();
            workingDaysResponseDto.getWeekdays().stream().filter(weekDay -> !weekDay.isWorking())
                .forEach(weekDay -> weekOffDaysList.add(weekDay.getName().toUpperCase()));
            LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
                "Weekoff Days: " + weekOffDaysList);

            LocalDate.now().datesUntil(LocalDate.now().plusDays(noOfDaysToSync))
                                                      .forEach(weekDay -> {
                                                        if (weekOffDaysList.contains(weekDay.getDayOfWeek()
                                                                        .getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase())){
                                                            holidaysLst.add(weekDay.toString());
                                                        }});
            LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
                "Added Holiday List for URL: " + workingDaysListEndpoint);

        } catch (Exception exp) {
            LOGGER.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
                    "Unknown Error in fetching registration center holiday List." + exp.getMessage(), exp);
        }
    }

    private ObjectNode sendWebClientRequest(String anyEndPoint) {
        try {
            ClientResponse response =  webClient.method(HttpMethod.GET)
                                                .uri(UriComponentsBuilder.fromUriString(anyEndPoint).toUriString())
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .exchange().block();
            ObjectNode responseObjNode = response.bodyToMono(ObjectNode.class).block();
            if (response.statusCode() == HttpStatus.OK) {
                if (responseObjNode.has(PreRegBatchContants.ERRORS) && !responseObjNode.get(PreRegBatchContants.ERRORS).isNull()) {
                    LOGGER.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY,
                        "Error in response for URL: " + anyEndPoint + ", Errors:" 
                            + responseObjNode.get(PreRegBatchContants.ERRORS).toString());
                    return null;
                }
                return responseObjNode; 
            } else {
                LOGGER.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY,
                        "Response Code: " + response.statusCode() +
                        ", Error in response for URL: " + anyEndPoint + ", Errors:" 
                            + responseObjNode.get(PreRegBatchContants.ERRORS).toString());
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

            ClientResponse response =  webClient.method(HttpMethod.PUT)
                                                .uri(uriBuilder)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .exchange().block();
                                                
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
            }
        } catch (Throwable t) {
            LOGGER.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
                    "Unknown Error in fetching data for endpoint: " + uriBuilder + ", Error: "  + t.getMessage(), t);
        }
        return false;
    }

    public boolean sendCancelledNotification(String preRegId, String regDate, String regTime, String langCode, String logIdentifier) {
        LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
                "Sending Cancelling Notification for Booked Application Pre Reg Id: " + preRegId);
        try {

            LOGGER.info(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
                "Cancelling Booked Application endpoint: " + sendNotificationURL);

            MultiValueMap<String, String> requestValueMap = buildNotificationRequest(preRegId, regDate, regTime, langCode, logIdentifier);
            ClientResponse response =  webClient.method(HttpMethod.POST)
                                                .uri(UriComponentsBuilder.fromUriString(sendNotificationURL).toUriString())
                                                .contentType(MediaType.MULTIPART_FORM_DATA)
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
            }
        } catch (Throwable t) {
            LOGGER.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, PreRegBatchContants.EMPTY, 
                    "Unknown Error in fetching data for endpoint: " + sendNotificationURL + ", Error: "  + t.getMessage(), t);
        }
        return false;

    }

    private MultiValueMap<String, String>  buildNotificationRequest(String preRegId, String regDate, String regTime, String langCode, String logIdentifier) {
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
            MultiValueMap<String, String> valueMap = new LinkedMultiValueMap<>();
            valueMap.add(PreRegBatchContants.NOTIFICATION_REQ_DTO, objectMapper.writeValueAsString(request));
            valueMap.add(PreRegBatchContants.LANG_CODE, langCode);
            return valueMap;
        } catch(JsonProcessingException exp) {
            LOGGER.error(PreRegBatchContants.SESSIONID, PreRegBatchContants.PRE_REG_BATCH, logIdentifier, 
                    "Unknown Error in fetching data for endpoint: " + sendNotificationURL + ", Error: "  + exp.getMessage(), exp);
        }
        return null;
    }
}
