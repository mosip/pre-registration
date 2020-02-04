package io.mosip.preregistration.notification.service.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.JsonUtils;
import io.mosip.kernel.core.util.exception.JsonMappingException;
import io.mosip.kernel.core.util.exception.JsonParseException;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.NotificationDTO;
import io.mosip.preregistration.core.config.LoggerConfiguration;

/**
 * The util class.
 * 
 * @author Sanober Noor
 * @since 1.0.0
 *
 */
@Component
public class NotificationServiceUtil {

	@Value("${mosip.utc-datetime-pattern}")
	private String utcDateTimePattern;

	private Logger log = LoggerConfiguration.logConfig(NotificationServiceUtil.class);

	/**
	 * Autowired reference for {@link #RestTemplateBuilder}
	 */

	/**
	 * 
	 * @param jsonString
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws io.mosip.kernel.core.exception.IOException
	 * @throws JSONException
	 * @throws ParseException
	 */

	public MainRequestDTO<NotificationDTO> createNotificationDetails(String jsonString) throws JsonParseException,
			JsonMappingException, io.mosip.kernel.core.exception.IOException, JSONException, ParseException {

		log.info("sessionId", "idType", "id",
				"In createUploadDto method of notification service util with body " + jsonString);
		MainRequestDTO<NotificationDTO> notificationReqDto = new MainRequestDTO<>();
		JSONObject notificationData = new JSONObject(jsonString);
		JSONObject notificationDtoData = (JSONObject) notificationData.get("request");
		NotificationDTO notificationDTO = (NotificationDTO) JsonUtils.jsonStringToJavaObject(NotificationDTO.class,
				notificationDtoData.toString());
		notificationReqDto.setId(notificationData.get("id").toString());
		notificationReqDto.setVersion(notificationData.get("version").toString());
		if (!(notificationData.get("requesttime") == null
				|| notificationData.get("requesttime").toString().isEmpty())) {
			notificationReqDto.setRequesttime(
					new SimpleDateFormat(utcDateTimePattern).parse(notificationData.get("requesttime").toString()));
		} else {
			notificationReqDto.setRequesttime(null);
		}
		notificationReqDto.setRequest(notificationDTO);
		return notificationReqDto;

	}
}
