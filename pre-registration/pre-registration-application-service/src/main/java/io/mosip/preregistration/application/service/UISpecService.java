package io.mosip.preregistration.application.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.application.dto.PageDTO;
import io.mosip.preregistration.application.dto.UISpecDTO;
import io.mosip.preregistration.application.dto.UISpecResponseDTO;
import io.mosip.preregistration.application.dto.UISpecficationRequestDTO;
import io.mosip.preregistration.application.exception.UISpecException;
import io.mosip.preregistration.application.service.util.UISpecServiceUtil;
import io.mosip.preregistration.core.common.dto.ExceptionJSONInfoDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.config.LoggerConfiguration;

@Service
public class UISpecService {

	@Value("${version}")
	private String version;

	@Autowired
	UISpecServiceUtil serviceUtil;

	/**
	 * Logger instance
	 */
	private Logger log = LoggerConfiguration.logConfig(UISpecService.class);

	private final String domain = "pre-registration";

	public MainResponseDTO<UISpecResponseDTO> saveUIspec(UISpecDTO request) {
		log.info("In UISpec service saveUIspec method");
		MainResponseDTO<UISpecResponseDTO> response = new MainResponseDTO<UISpecResponseDTO>();
		response.setVersion(version);
		response.setResponsetime(LocalDateTime.now().toString());
		UISpecResponseDTO uispecReq = new UISpecResponseDTO();
		try {
			log.info("Saving the UiSpec request {}", request);
			uispecReq = serviceUtil.saveUISchema(getMasterDataUISpecRequest(request));
			response.setResponse(uispecReq);
		} catch (UISpecException ex) {
			log.error("Exception occured while saving the UiSpec request {}", request);
			List<ExceptionJSONInfoDTO> explist = new ArrayList<ExceptionJSONInfoDTO>();
			ExceptionJSONInfoDTO exception = new ExceptionJSONInfoDTO();
			exception.setErrorCode(ex.getErrorCode());
			exception.setMessage(ex.getMessage());
			log.error("Exception {}", exception);
			explist.add(exception);
			response.setErrors(explist);
		}
		return response;
	}

	public MainResponseDTO<UISpecResponseDTO> updateUISpec(UISpecDTO updateRequest, String id) {
		log.info("In UISpec service updateUIspec method");
		MainResponseDTO<UISpecResponseDTO> response = new MainResponseDTO<UISpecResponseDTO>();
		response.setVersion(version);
		response.setResponsetime(LocalDateTime.now().toString());
		UISpecResponseDTO uispecResponse = new UISpecResponseDTO();
		try {
			log.info("updating the UiSpec request {}", updateRequest);
			uispecResponse = serviceUtil.updateUISchema(getMasterDataUISpecRequest(updateRequest), id);
			response.setResponse(uispecResponse);
		} catch (UISpecException ex) {
			log.error("Exception occured while updating the UiSpec request {}", updateRequest);
			List<ExceptionJSONInfoDTO> explist = new ArrayList<ExceptionJSONInfoDTO>();
			ExceptionJSONInfoDTO exception = new ExceptionJSONInfoDTO();
			exception.setErrorCode(ex.getErrorCode());
			exception.setMessage(ex.getMessage());
			log.error("Exception {}", exception);
			explist.add(exception);
			response.setErrors(explist);
		}
		return response;
	}

	public MainResponseDTO<List<UISpecResponseDTO>> getUISpec(double version, double identitySchemaVersion) {
		log.info("In UISpec service getUIspec method");
		MainResponseDTO<List<UISpecResponseDTO>> response = new MainResponseDTO<List<UISpecResponseDTO>>();
		response.setVersion(this.version);
		response.setResponsetime(LocalDateTime.now().toString());
		try {
			log.info("fetching the UiSpec version {} and identitySchemaVersion {}", version, identitySchemaVersion);
			response.setResponse(serviceUtil.getUISchema(version, identitySchemaVersion));
		} catch (UISpecException ex) {
			log.error("Exception occured while fetching the UiSpec");
			List<ExceptionJSONInfoDTO> explist = new ArrayList<ExceptionJSONInfoDTO>();
			ExceptionJSONInfoDTO exception = new ExceptionJSONInfoDTO();
			exception.setErrorCode(ex.getErrorCode());
			exception.setMessage(ex.getMessage());
			log.error("Exception {}", exception);
			explist.add(exception);
			response.setErrors(explist);
		}
		return response;
	}

	private UISpecficationRequestDTO getMasterDataUISpecRequest(UISpecDTO request) {
		UISpecficationRequestDTO masterDataRequest = new UISpecficationRequestDTO();
		masterDataRequest.setDomain(domain);
		masterDataRequest.setDescription(request.getDescription());
		masterDataRequest.setIdentitySchemaId(request.getIdentitySchemaId());
		masterDataRequest.setTitle(request.getTitle());
		masterDataRequest.setType(request.getType());
		masterDataRequest.setJsonspec(request.getJsonspec());

		return masterDataRequest;
	}

	public MainResponseDTO<String> deleteUISpec(String id) {
		log.info("In UISpec service deleteUISpec method");
		MainResponseDTO<String> response = new MainResponseDTO<String>();
		response.setVersion(this.version);
		response.setResponsetime(LocalDateTime.now().toString());
		try {
			log.info("deleting the UiSpec id {}", id);
			response.setResponse(serviceUtil.deleteUISchema(id));
		} catch (UISpecException ex) {
			log.error("Exception occured while fetching the UiSpec");
			List<ExceptionJSONInfoDTO> explist = new ArrayList<ExceptionJSONInfoDTO>();
			ExceptionJSONInfoDTO exception = new ExceptionJSONInfoDTO();
			exception.setErrorCode(ex.getErrorCode());
			exception.setMessage(ex.getMessage());
			log.error("Exception {}", exception);
			explist.add(exception);
			response.setErrors(explist);
		}
		return response;
	}

	public MainResponseDTO<String> publishUISpec(String id) {
		log.info("In UISpec service publishUIspec method");
		MainResponseDTO<String> response = new MainResponseDTO<String>();
		response.setVersion(version);
		response.setResponsetime(LocalDateTime.now().toString());
		try {
			log.info("publish the UiSpec request id {}", id);
			response.setResponse(serviceUtil.publishUISchema(id));
		} catch (UISpecException ex) {
			log.error("Exception occured while publishing the UiSpec id {}", id);
			List<ExceptionJSONInfoDTO> explist = new ArrayList<ExceptionJSONInfoDTO>();
			ExceptionJSONInfoDTO exception = new ExceptionJSONInfoDTO();
			exception.setErrorCode(ex.getErrorCode());
			exception.setMessage(ex.getMessage());
			log.error("Exception {}", exception);
			explist.add(exception);
			response.setErrors(explist);
		}
		return response;
	}

	public MainResponseDTO<PageDTO<UISpecResponseDTO>> getAllUISpec(int pageNumber,int pageSize) {
		log.info("In UISpec service getAllUISpec method");
		MainResponseDTO<PageDTO<UISpecResponseDTO>> response = new MainResponseDTO<PageDTO<UISpecResponseDTO>>();
		response.setVersion(this.version);
		response.setResponsetime(LocalDateTime.now().toString());
		try {
			log.info("fetching the All published UiSpec");
			PageDTO<UISpecResponseDTO> res = serviceUtil.getAllUISchema(pageNumber,pageSize);
			res.setData(filterPreRegSpec(res.getData()));
			res.setTotalItems(res.getData().size());
			response.setResponse(res);
		} catch (UISpecException ex) {
			log.error("Exception occured while fetching all the UiSpec");
			List<ExceptionJSONInfoDTO> explist = new ArrayList<ExceptionJSONInfoDTO>();
			ExceptionJSONInfoDTO exception = new ExceptionJSONInfoDTO();
			exception.setErrorCode(ex.getErrorCode());
			exception.setMessage(ex.getMessage());
			log.error("Exception {}", exception);
			explist.add(exception);
			response.setErrors(explist);
		}
		return response;
	}

	private List<UISpecResponseDTO> filterPreRegSpec(List<UISpecResponseDTO> data) {
		List<UISpecResponseDTO> filteredData = new ArrayList<UISpecResponseDTO>();
		data.forEach(spec -> {
			if (spec.getDomain().equals(domain)) {
				filteredData.add(spec);
			}
		});
		return filteredData;
	}

}
