package io.mosip.preregistration.application.service;

import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_ID;
import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_IDTYPE;
import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_SESSIONID;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.application.dto.PageDTO;
import io.mosip.preregistration.application.dto.UISpecMetaDataDTO;
import io.mosip.preregistration.application.dto.UISpecResponseDTO;
import io.mosip.preregistration.application.exception.UISpecException;
import io.mosip.preregistration.application.service.util.UISpecServiceUtil;
import io.mosip.preregistration.core.common.dto.ExceptionJSONInfoDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.config.LoggerConfiguration;

@Service
public class UISpecService {
	@Value("${version}")
	private String version;

	@Value("${mosip.utc-datetime-pattern}")
	private String mosipDateTimeFormat;

	@Value("${mosip.preregistration.uispec.latest}")
	private String uiSpecLatestId;

	@Value("${mosip.preregistration.uispec.all}")
	private String uiSpecAllId;

	@Autowired
	UISpecServiceUtil serviceUtil;

	/**
	 * Logger instance
	 */
	private Logger log = LoggerConfiguration.logConfig(UISpecService.class);

	private final String domain = "pre-registration";

	public MainResponseDTO<UISpecMetaDataDTO> getLatestUISpec(double version, double identitySchemaVersion) {
		log.info("In UISpec service getUIspec method");
		MainResponseDTO<UISpecMetaDataDTO> response = new MainResponseDTO<>();
		response.setVersion(this.version);
		response.setResponsetime(DateTimeFormatter.ofPattern(mosipDateTimeFormat).format(LocalDateTime.now()));
		try {
			log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"fetching the UiSpec version {} and identitySchemaVersion {}", version, identitySchemaVersion);
			List<UISpecResponseDTO> uiSchema = serviceUtil.getUISchema(version, identitySchemaVersion);
			List<UISpecMetaDataDTO> fetchedSchema = prepareResponse(uiSchema);
			response.setResponse(getLatestPublishedSchema(fetchedSchema));
		} catch (UISpecException ex) {
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "Exception occured while fetching the UiSpec");
			List<ExceptionJSONInfoDTO> explist = new ArrayList<>();
			ExceptionJSONInfoDTO exception = new ExceptionJSONInfoDTO();
			exception.setErrorCode(ex.getErrorCode());
			exception.setMessage(ex.getMessage());
			log.error("Exception {}", exception);
			explist.add(exception);
			response.setErrors(explist);
		}
		return response;
	}

	public MainResponseDTO<PageDTO<UISpecMetaDataDTO>> getAllUISpec(int pageNumber, int pageSize) {
		log.info("In UISpec service getAllUISpec method");
		MainResponseDTO<PageDTO<UISpecMetaDataDTO>> response = new MainResponseDTO<PageDTO<UISpecMetaDataDTO>>();
		response.setVersion(this.version);
		response.setResponsetime(DateTimeFormatter.ofPattern(mosipDateTimeFormat).format(LocalDateTime.now()));
		try {
			log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "fetching the All published UiSpec");
			PageDTO<UISpecResponseDTO> res = serviceUtil.getAllUISchema(pageNumber, pageSize);
			PageDTO<UISpecMetaDataDTO> fetchedSchema = new PageDTO<>();
			fetchedSchema.setData(filterPreRegSpec(res.getData()));
			fetchedSchema.setPageNo(res.getPageNo());
			fetchedSchema.setPageSize(res.getPageSize());
			fetchedSchema.setTotalItems(fetchedSchema.getData().size());
			fetchedSchema.setTotalPages(res.getTotalPages());
			fetchedSchema.setSort(res.getSort());
			response.setResponse(fetchedSchema);
		} catch (UISpecException ex) {
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "Exception occured while fetching all the UiSpec");
			List<ExceptionJSONInfoDTO> explist = new ArrayList<>();
			ExceptionJSONInfoDTO exception = new ExceptionJSONInfoDTO();
			exception.setErrorCode(ex.getErrorCode());
			exception.setMessage(ex.getMessage());
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "Exception " + ExceptionUtils.getFullStackTrace(ex));
			explist.add(exception);
			response.setErrors(explist);
		}
		return response;
	}

	private List<UISpecMetaDataDTO> prepareResponse(List<UISpecResponseDTO> uiSchema) {
		List<UISpecMetaDataDTO> res = new ArrayList<>();
		uiSchema.forEach(spec -> {
			UISpecMetaDataDTO specData = new UISpecMetaDataDTO();
			specData.setId(spec.getId());
			specData.setDescription(spec.getDescription());
			specData.setVersion(spec.getVersion());
			specData.setIdentitySchemaId(spec.getIdentitySchemaId());
			specData.setIdSchemaVersion(spec.getIdSchemaVersion());
			specData.setTitle(spec.getTitle());
			specData.setEffectiveFrom(spec.getEffectiveFrom());
			specData.setStatus(spec.getStatus());
			specData.setCreatedOn(spec.getCreatedOn());
			specData.setUpdatedOn(spec.getUpdatedOn());
			specData.setJsonSpec(spec.getJsonSpec().get(0).getSpec());
			res.add(specData);
		});
		return res;
	}

	private UISpecMetaDataDTO getLatestPublishedSchema(List<UISpecMetaDataDTO> fetchedSchema) {
		List<UISpecMetaDataDTO> sorted = fetchedSchema.stream().filter(spec -> spec.getStatus().equals("PUBLISHED"))
				.sorted(Comparator.comparing(UISpecMetaDataDTO::getEffectiveFrom).reversed())
				.collect(Collectors.toList());
		return sorted.get(0);
	}

	private List<UISpecMetaDataDTO> filterPreRegSpec(List<UISpecResponseDTO> data) {
		List<UISpecResponseDTO> filteredData = new ArrayList<>();
		data.forEach(spec -> {
			if (spec.getDomain().equals(domain)) {
				filteredData.add(spec);
			}
		});
		return prepareResponse(filteredData);
	}
}